package hbase.coprocessor.secondindex;

import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.update.UpdateRequestBuilder;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Administrator on 2018/4/18.
 */
public class ESBulkOperator {
    private static final int MAX_BULK_COUNT = 10000;
    private static BulkRequestBuilder bulkRequestBuilder;
    private static final Lock commitLock = new ReentrantLock();
    private static ScheduledExecutorService executorService;
    static {
        bulkRequestBuilder = ESClient.client.prepareBulk();
        bulkRequestBuilder.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);

        executorService = Executors.newScheduledThreadPool(1);
        final Runnable beeper = new Runnable(){
            @Override
            public void run() {
                commitLock.lock();
                try{
                    bulkRequest(0);
                }catch (Exception e){
                    System.out.println("Bulk error" + ESClient.indexName + e.getMessage());
                }finally {
                    commitLock.unlock();
                }
            }
        };
        executorService.scheduleAtFixedRate(beeper,10,30, TimeUnit.SECONDS);
    }

    public static void shutdownExcutorservice(){
        if(executorService != null && !executorService.isShutdown())
            executorService.shutdown();
    }
    /**
     * bulk request when number of builders is grate then threshold
     * @param threshold
     */
    private static void bulkRequest(int threshold){
        if(bulkRequestBuilder.numberOfActions() > threshold){
            BulkResponse bulkItemResponses = bulkRequestBuilder.execute().actionGet();
            if(!bulkItemResponses.hasFailures()){
                List<DocWriteRequest> tmpRequests = bulkRequestBuilder.request().requests();
                ESClient.closeEsClient();
                ESClient.initEsClient();
                bulkRequestBuilder = ESClient.client.prepareBulk();
                bulkRequestBuilder.request().add(tmpRequests);
            }
        }
    }

    public static void addUpdateBuilderToBulk(UpdateRequestBuilder builder){
        commitLock.lock();
        try{
            bulkRequestBuilder.add(builder);
            bulkRequest(MAX_BULK_COUNT);
        }catch (Exception e){
            System.out.println("Bulk update error" + ESClient.indexName + e.getMessage());
        }finally {
            commitLock.unlock();
        }
    }

    public static void addDeleteBuilderToBulk(DeleteRequestBuilder builder){
        commitLock.lock();
        try{
            bulkRequestBuilder.add(builder);
            bulkRequest(MAX_BULK_COUNT);
        }catch (Exception e){
            System.out.println("Bulk delete error" + ESClient.indexName + e.getMessage());
        }finally {
            commitLock.unlock();
        }
    }
}

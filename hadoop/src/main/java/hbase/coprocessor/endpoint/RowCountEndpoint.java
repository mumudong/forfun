package hbase.coprocessor.endpoint;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.google.protobuf.Service;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.Coprocessor;
import org.apache.hadoop.hbase.CoprocessorEnvironment;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.coprocessor.CoprocessorException;
import org.apache.hadoop.hbase.coprocessor.CoprocessorService;
import org.apache.hadoop.hbase.coprocessor.RegionCoprocessorEnvironment;
import org.apache.hadoop.hbase.filter.FirstKeyOnlyFilter;
import org.apache.hadoop.hbase.protobuf.ResponseConverter;
import org.apache.hadoop.hbase.regionserver.RegionScanner;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/4/3.
 */
public class RowCountEndpoint extends EndpointTestProtos.CountService implements
        Coprocessor,CoprocessorService{
    public RowCountEndpoint(){}
    private RegionCoprocessorEnvironment env;
    /**
     * 获取hbase表总行数
     * @param controller
     * @param request
     * @param done
     */
    @Override
    public void count(RpcController controller, EndpointTestProtos.CountRequest request, RpcCallback<EndpointTestProtos.CountResponse> done) {
        Scan scan = new Scan();
        scan.setFilter(new FirstKeyOnlyFilter());
        EndpointTestProtos.CountResponse response = null;
        RegionScanner scanner = null;
        try{
            scanner = env.getRegion().getScanner(scan);
            List<Cell> results = new ArrayList<Cell>();
            boolean hasMore = false;
            byte[] lastRow = null;
            long count = 0;
            do{
                hasMore = scanner.next(results);
                for(Cell kv:results){
                    byte[] currentRow = CellUtil.cloneRow(kv);
                    if(lastRow == null||!Bytes.equals(lastRow,currentRow)){
                        lastRow = currentRow;
                        count++;
                    }
                }
                results.clear();
            }while (hasMore);
            response = EndpointTestProtos.CountResponse.newBuilder()
                            .setCount(count).build();
        }catch (IOException e){
            ResponseConverter.setControllerException(controller,e);
        }finally {
            if(scanner != null){
                try {
                    scanner.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        done.run(response);
    }

    @Override
    public void start(CoprocessorEnvironment coprocessorEnvironment) throws IOException {
        if(coprocessorEnvironment instanceof RegionCoprocessorEnvironment){
            this.env = (RegionCoprocessorEnvironment)coprocessorEnvironment;
        }else{
            throw new CoprocessorException("Must be loaded on a table region");
        }
    }

    @Override
    public void stop(CoprocessorEnvironment coprocessorEnvironment) throws IOException {

    }

    @Override
    public Service getService() {
        return this;
    }
}

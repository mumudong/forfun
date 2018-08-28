package flume;

import org.apache.flume.*;
import org.apache.flume.conf.Configurable;
import org.apache.flume.sink.AbstractSink;
import org.apache.log4j.Logger;

/**
 * Created by Administrator on 2018/6/1.
 * 将工程打成jar包，放在flume解压后的lib文件中，依赖包不需要放进去，因为flume中的lib目录已经存在了
 *
 */
public class CustomSink extends AbstractSink implements Configurable{
    private static final Logger logger = Logger.getLogger(CustomSink.class);
    private static final String ConfKey = "key";
    private String key;
    @Override
    public Status process() throws EventDeliveryException {
        Channel ch = getChannel();
        //get the transaction
        Transaction txn = ch.getTransaction();
        Event event =null;
        //begin the transaction
        txn.begin();
        while(true){
            event = ch.take();
            if (event!=null) {
                break;
            }
        }
        try {
            logger.debug("Get event.");
            String body = new String(event.getBody());
            System.out.println("event.getBody()-----" + body);

            return Status.READY;
        } catch (Throwable th) {
            txn.rollback();

            if (th instanceof Error) {
                throw (Error) th;
            } else {
                throw new EventDeliveryException(th);
            }
        } finally {
            txn.close();
        }
    }

    @Override
    public void configure(Context context) {
        this.key = context.getString(ConfKey);
    }
}

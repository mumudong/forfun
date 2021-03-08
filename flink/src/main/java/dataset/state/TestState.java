package dataset.state;

import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.runtime.state.memory.MemoryStateBackend;
import org.apache.flink.state.api.BootstrapTransformation;
import org.apache.flink.state.api.ExistingSavepoint;
import org.apache.flink.state.api.OperatorTransformation;
import org.apache.flink.state.api.Savepoint;
import org.apache.flink.state.api.functions.StateBootstrapFunction;

import java.io.IOException;

public class TestState {
    public static void main(String[] args) {
//        writeState();
    }
    static String path = "/xxxx/xxxx";
    static String uid = "ididid";
    public static void writeState(){
        ExecutionEnvironment executionEnvironment = ExecutionEnvironment.getExecutionEnvironment();
        DataSet<Integer> dataSet = executionEnvironment.fromElements(1,2,3);
        BootstrapTransformation<Integer> transform = OperatorTransformation
                .bootstrapWith(dataSet)
                .transform(new SimpleBootstrapFunction());
        Savepoint.create(new MemoryStateBackend(),2)
                .withOperator(uid,transform)
                .write(path);
        try {
            executionEnvironment.execute("write state");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void readState(){
        ExecutionEnvironment exe = ExecutionEnvironment.getExecutionEnvironment();
        try {
            ExistingSavepoint load = Savepoint.load(exe, path, new MemoryStateBackend());
            DataSet<Integer> state = load.readListState(uid, "state", Types.INT);
            state.map((x)->{
                System.out.println(x);
                return x;
            }).print();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
class SimpleBootstrapFunction extends StateBootstrapFunction<Integer> {

    private ListState<Integer> state;

    @Override
    public void processElement(Integer value, Context ctx) throws Exception {
        System.out.println(value);
        state.add(value);
    }

    @Override
    public void snapshotState(FunctionSnapshotContext context) throws Exception {
    }

    @Override
    public void initializeState(FunctionInitializationContext context) throws Exception {
        //注意这里的写入state 描述
        state = context.getOperatorStateStore().getListState(new ListStateDescriptor<>("state", Types.INT));
    }
}
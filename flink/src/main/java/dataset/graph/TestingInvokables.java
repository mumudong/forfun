package dataset.graph;

import org.apache.flink.runtime.execution.Environment;
import org.apache.flink.runtime.io.network.api.reader.RecordReader;
import org.apache.flink.runtime.io.network.api.writer.RecordWriter;
import org.apache.flink.runtime.io.network.api.writer.RecordWriterBuilder;
import org.apache.flink.runtime.jobgraph.tasks.AbstractInvokable;
import org.apache.flink.types.IntValue;

public class TestingInvokables {

    private TestingInvokables() {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " should not be instantiated.");
    }



    public static class Sender extends AbstractInvokable {

        public Sender(Environment environment) {
            super(environment);
        }

        @Override
        public void invoke() throws Exception {
            //写数据
            final RecordWriter<IntValue> writer = new RecordWriterBuilder().build(getEnvironment().getWriter(0));
            try {
                writer.emit(new IntValue(42));
                writer.emit(new IntValue(1337));
                writer.flushAll();
            } finally {
                writer.clearBuffers();
            }
        }
    }


    public static class Receiver extends AbstractInvokable {

        public Receiver(Environment environment) {
            super(environment);
        }

        @Override
        public void invoke() throws Exception {
            //读数据
            final RecordReader<IntValue> reader = new RecordReader<>(
                    getEnvironment().getInputGate(0),
                    IntValue.class,
                    getEnvironment().getTaskManagerInfo().getTmpDirectories());
            final IntValue i1 = reader.next();
            System.out.println("输出" + i1);
            final IntValue i2 = reader.next();
            System.out.println("输出" + i2);
            final IntValue i3 = reader.next();
            System.out.println("输出" + i3);
            if (i1.getValue() != 42 || i2.getValue() != 1337 || i3 != null) {
                throw new Exception("Wrong data received.");
            }
        }
    }
}

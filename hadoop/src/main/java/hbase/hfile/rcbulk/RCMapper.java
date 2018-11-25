package hbase.hfile.rcbulk;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hive.serde2.columnar.BytesRefArrayWritable;
import org.apache.hadoop.hive.serde2.columnar.BytesRefWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RCMapper extends Mapper<LongWritable,BytesRefArrayWritable,ImmutableBytesWritable,KeyValue> {
        private String[] fieldName = null;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        super.setup(context);
        Configuration conf = context.getConfiguration();
        String schema = conf.get("schema");
        fieldName = schema.split(":");
    }

    @Override
    protected void map(LongWritable key, BytesRefArrayWritable values, Context context) throws IOException, InterruptedException {
//        super.map(key, values, context);
        List<String> fields = new ArrayList<>();
        int size = values.size();
        for(int i = 0; i < size;i++){
            Text line = new Text();
            BytesRefWritable value = values.get(i);
            line.set(value.getData(),value.getStart(),value.getLength());
            fields.add(line.toString());
        }
        String rowKey = fields.get(0);
        String columnFamily = "cf";
        int length = fieldName.length;
        ImmutableBytesWritable hkey = new ImmutableBytesWritable();
        hkey.set(rowKey.getBytes());
        KeyValue kv = null;
        for(int i = 1;i < length; i++){
            kv = new KeyValue(hkey.get(),columnFamily.getBytes(),fieldName[i].getBytes(),fields.get(i).getBytes());
            context.write(hkey,kv);
        }

    }
}

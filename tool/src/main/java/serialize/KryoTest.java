package serialize;

import com.alibaba.fastjson.JSON;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CollectionSerializer;
import com.esotericsoftware.kryo.serializers.JavaSerializer;
import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class KryoTest {
    public static void main(String[] args) throws Exception {
        List<String> list = new ArrayList<>();
        list.add("field1");
        list.add("第二个字段");
        list.add("field3");
        list.add("第四个字段");
        list.add("field5");
        list.add("field6");
        list.add("field7");
        list.add(null);
        String s = serializationList(list, String.class);
        long start = System.currentTimeMillis();
//        for(int i = 0;i < 1000000;i++) {
//            List<String> strings = deserializationList(s, String.class);
//        }
//        System.out.println("耗时:" + (System.currentTimeMillis() - start) / 1000);


        start = System.currentTimeMillis();
        String json = JSON.toJSONString(list);
        for(int i = 0;i < 1000000;i++) {
            Object parse = JSON.parse(json);
        }
        System.out.println("耗时:" + (System.currentTimeMillis() - start) / 1000);

        byte[] serialize = KryoSerialiseUtil.serialize(list);
        start = System.currentTimeMillis();
        List<String> parse = null;
        for(int i = 0;i < 1000000;i++) {
            parse = (List<String>)KryoSerialiseUtil.deserialize(serialize);
        }
        System.out.println(parse.get(0));
        System.out.println("耗时:" + (System.currentTimeMillis() - start) / 1000);
    }

    private static <T extends Serializable> String serializationList(List<T> obj, Class<T> clazz) {
        Kryo kryo = new Kryo();
        kryo.setReferences(false);
        kryo.setRegistrationRequired(true);
        CollectionSerializer serializer = new CollectionSerializer();
        serializer.setElementClass(clazz, new JavaSerializer());
        serializer.setElementsCanBeNull(true);
        kryo.register(clazz, new JavaSerializer());
        kryo.register(ArrayList.class, serializer);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Output output = new Output(baos);
        kryo.writeObject(output, obj);
        output.flush();
        output.close();
        byte[] b = baos.toByteArray();
        try {
            baos.flush();
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(new Base64().encode(b));
    }

    @SuppressWarnings("unchecked")
    private static  <T extends Serializable> List<T> deserializationList(String obj,
                                                                         Class<T> clazz) {
        Kryo kryo = new Kryo();
        kryo.setReferences(false);
        kryo.setRegistrationRequired(true);
        CollectionSerializer serializer = new CollectionSerializer();
        serializer.setElementClass(clazz, new JavaSerializer());
        serializer.setElementsCanBeNull(true);
        kryo.register(clazz, new JavaSerializer());
        kryo.register(ArrayList.class, serializer);
        ByteArrayInputStream bais = new ByteArrayInputStream(
                new Base64().decode(obj));
        Input input = new Input(bais);
        return (List<T>) kryo.readObject(input, ArrayList.class, serializer);
    }
}

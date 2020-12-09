package protobuf.serialize;

import com.github.os72.protobuf.dynamic.DynamicSchema;
import com.github.os72.protobuf.dynamic.MessageDefinition;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import sun.reflect.FieldInfo;

import java.util.*;

public class DynamicSchemaTest {
    public static void main(String[] args) {

        List<FiledInfo> fieldInfos = new ArrayList<>();
        fieldInfos.add(new FiledInfo("required","int32","id"));
        fieldInfos.add(new FiledInfo("required","string","name"));
        fieldInfos.add(new FiledInfo("optional","string","email"));
        DynamicSchema schema1 = buildSchema("test",fieldInfos);

        Map<String,Object> data = new HashMap<>();
        data.put("id",1);
        data.put("name","zhangsan");
        data.put("email","xxx@test.com");
        byte[] bytesData = buildDynamicMessage(schema1,"test", data);

        DynamicMessage messageData1 = dynamicMessage(schema1, "test", bytesData);
        System.out.println("messageData1===================");
        System.out.println(messageData1.toString());

        fieldInfos.add(new FiledInfo("optional","string","addr"));
        DynamicSchema schema2 = buildSchema("test2",fieldInfos);
        data.put("addr","xx省xx市");
        byte[] bytesDataNew = buildDynamicMessage(schema2,"test2",data);
        DynamicMessage messageData2 = dynamicMessage(schema2,"test2",bytesDataNew);
        System.out.println("messageData2====================");
        System.out.println(messageData2.toString());

        DynamicMessage msgNewSchemaOldData = dynamicMessage(schema2,"test2",bytesData);
        System.out.println("msgNewSchemaOldData==================");
        System.out.println(msgNewSchemaOldData.toString());

        DynamicMessage msgOldSchemaNewData = dynamicMessage(schema1,"test",bytesDataNew);
        System.out.println("msgOldSchemaNewData=============================");
        System.out.println(msgOldSchemaNewData.toString());

        System.out.println("=========================");

        Map<Descriptors.FieldDescriptor, Object> allFields = msgOldSchemaNewData.getAllFields();
        Iterator<Map.Entry<Descriptors.FieldDescriptor, Object>> iterator = allFields.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<Descriptors.FieldDescriptor, Object> next = iterator.next();














        }

    }

    static class FiledInfo{
        String lable;
        String type;
        String name;

        public FiledInfo(String lable, String type, String name) {
            this.lable = lable;
            this.type = type;
            this.name = name;
        }

        public String getLable() {
            return lable;
        }

        public String getType() {
            return type;
        }

        public String getName() {
            return name;
        }
    }

    private static DynamicSchema buildSchema(String typeName, List<FiledInfo> filedInfos){
        DynamicSchema.Builder schemaBuilder = DynamicSchema.newBuilder();
        schemaBuilder.setName("testDynamicOne.proto");
        MessageDefinition.Builder defBuilder = MessageDefinition.newBuilder(typeName);

        for(int i = 0;i < filedInfos.size();i++){
            defBuilder.addField(filedInfos.get(i).getLable(),filedInfos.get(i).getType(),filedInfos.get(i).getName(),i+1);
        }
        MessageDefinition definition = defBuilder.build();

        schemaBuilder.addMessageDefinition(definition);
        DynamicSchema schema = null;
        try {
            schema = schemaBuilder.build();
        } catch (Descriptors.DescriptorValidationException e) {
            e.printStackTrace();
        }
        return schema;
    }

    private static byte[] buildDynamicMessage(DynamicSchema schema,String msgTypeName, Map<String,Object> data){
        DynamicMessage.Builder msgBuilder = schema.newMessageBuilder(msgTypeName);
        Descriptors.Descriptor msgDesc = msgBuilder.getDescriptorForType();
        Iterator<Map.Entry<String, Object>> iterator = data.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String, Object> next = iterator.next();
            msgBuilder.setField(msgDesc.findFieldByName(next.getKey()),next.getValue());
        }
        DynamicMessage msg = msgBuilder.build();
        return msg.toByteArray();
    }

    private static DynamicMessage dynamicMessage(DynamicSchema schema,String msgTypename,byte[] msg){
        try {
            return DynamicMessage.parseFrom(schema.getMessageDescriptor(msgTypename),msg);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return null;
    }
}

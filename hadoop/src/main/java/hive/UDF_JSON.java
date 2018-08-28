package hive;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;

/**
 *   create temporary function row_json as 'com.tx.demo_hive.UDF_JSON';
 *   create temporary function col_json as 'com.tx.demo_hive.UDAF_COL_JSON';
 *   row_json('abc:123','def:456','{'aaa':'111'}')
 */
import com.alibaba.fastjson.JSON; 
public class UDF_JSON extends GenericUDF{
	//定义输入输出变量
	ObjectInspector[]  objIn;
	ObjectInspector   objOut; 
	Map<String,Object> map=new HashMap<String,Object>();
	@Override
	public Object evaluate(DeferredObject[] args) throws HiveException {
        // TODO Auto-generated method stub
        if(args.length<1) return null;
        map.clear(); ;
        String str;
        String [] strs=new String[2];
        for(int i=0;i<args.length;i++){
            str=args[i].get().toString();
            strs[0]=str.substring(0, str.indexOf(":"));

            strs[1]=str.substring(str.indexOf(":")+1);
            Object obj=strs[1];
            if(strs[1].startsWith("[{")&&strs[1].endsWith("}]")){
                obj=JSON.parseArray(strs[1]);
            }else if(strs[1].startsWith("[{")&&strs[1].endsWith("ArrayList")){
                try {
                    obj=JSON.parseArray(strs[1].substring(0, strs[1].lastIndexOf("/")));
                } catch (Exception e) {
                    if(e.getMessage()=="unclosed jsonArray"){
                        obj="[".concat(strs[1].replace('[', ' '));
                    }
                }

            }else if(strs[1].startsWith("{")){
                obj=JSON.parse(strs[1]);
            }
            map.put(strs[0], obj);
        }
        String result=JSON.toJSONString(map);

        return result;
	}

	@Override
	public String getDisplayString(String[] arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ObjectInspector initialize(ObjectInspector[] args) throws UDFArgumentException {
		// 初始化，输入变量为参数对应列
		objIn=args;
		return  PrimitiveObjectInspectorFactory
		.getPrimitiveJavaObjectInspector(PrimitiveObjectInspector.PrimitiveCategory.STRING);
		

	}

}

package hive;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;

import com.alibaba.fastjson.JSON; 
public class Convert_dict extends GenericUDF{
	//定义map存放字典值
	Map<String,String> dict=new HashMap<String,String>();

	static final Log LOG = LogFactory.getLog(Convert_dict.class.getName());
	//定义输入输出变量
	ObjectInspector[]  objIn;
	ObjectInspector   objOut;  
	
	private static String driverName = "org.apache.hive.jdbc.HiveDriver";
    private static String url = "jdbc:hive2://xxxxxxxx:10000/ods";
    private static String user = "hdfs";
    private static String password = "hdfs";
    private   String sql = "";
    private   ResultSet res;
    private   String tableName="ods.t_gl_dict";
    private   String type="";
	private  int i=0; 
	@Override
	public Object evaluate(DeferredObject[] args) throws HiveException {
		 Connection conn =null;
		 Statement stmt=null;
		 
		if(args.length<1) return null; 
		if (i==0){
			i++;
			tableName=args[1].get().toString();
			type=args[2].get().toString();
				 try {
		             Class.forName(driverName);
		              conn = DriverManager.getConnection(url, user, password);
		              stmt = conn.createStatement();
		             sql = "select value,label from " + tableName+" a where a.type= '"+type+"'";
		             res = stmt.executeQuery(sql);
		             while (res.next()) { 
		             	 dict.put(res.getString(1), res.getString(2));
		             }
		     } catch (ClassNotFoundException e) {
		             e.printStackTrace();  
		     } catch (SQLException e) {
		             e.printStackTrace();  
		     }finally{ 
	             try {
	            	 if(stmt!=null)
	            	 stmt.close();
				} catch (SQLException e) { 
					e.printStackTrace();
				}
	             try {
	            	 if(conn!=null)
	            	 stmt.close();
				} catch (SQLException e) { 
					e.printStackTrace();
				}
		     }
		}

		StringBuffer sb=new StringBuffer("");
		String str;
		if(args[0].get()==null)
			return "";
		str=args[0].get().toString();
		if(str.length()>0){
		String [] strs=str.split(",");
			for(String index:strs){

				sb.append(dict.get(index)).append(",");
			}
			return sb.toString().substring(0, sb.length()-1);
		}else{
			return "";
		}
	}

	@Override
	public String getDisplayString(String[] arg0) {
		// TODO Auto-generated method stub
		return "字段转换函数";
	}

	@Override
	public ObjectInspector initialize(ObjectInspector[] args) throws UDFArgumentException {
		// TODO Auto-generated method stub
		objIn=args; 

		return  PrimitiveObjectInspectorFactory.javaStringObjectInspector;
		
	}

}

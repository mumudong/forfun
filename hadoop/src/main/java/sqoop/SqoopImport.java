package sqoop;

import org.junit.Test;

/**
 * sqoop1.x client
 *
 * <dependency>
 <groupId>org.apache.sqoop</groupId>
 <artifactId>sqoop</artifactId>
 <version>1.4.6.2.6.0.3-8</version>
 </dependency>
 <dependency>
 <groupId>commons-fileupload</groupId>
 <artifactId>commons-fileupload</artifactId>
 <version>1.3.1</version>
 </dependency>
 <dependency>
 <groupId>mysql</groupId>
 <artifactId>mysql-connector-java</artifactId>
 <version>5.1.44</version>
 </dependency>
 <dependency>
 <groupId>org.apache.commons</groupId>
 <artifactId>commons-lang3</artifactId>
 <version>3.0</version>
 </dependency>
 <!--hadoop-->
 <dependency>
 <groupId>org.apache.hadoop</groupId>
 <artifactId>hadoop-common</artifactId>
 <version>2.7.3.2.6.0.3-8</version>
 </dependency>
 <dependency>
 <groupId>org.apache.hadoop</groupId>
 <artifactId>hadoop-hdfs</artifactId>
 <version>2.7.3.2.6.0.3-8</version>
 </dependency>
 <dependency>
 <groupId>org.apache.hadoop</groupId>
 <artifactId>hadoop-mapreduce-client-core</artifactId>
 <version>2.7.3.2.6.0.3-8</version>
 </dependency>
 <dependency>
 <groupId>org.apache.hadoop</groupId>
 <artifactId>hadoop-mapreduce-client-common</artifactId>
 <version>2.7.3.2.6.0.3-8</version>
 </dependency>
 <dependency>
 <groupId>org.apache.hadoop</groupId>
 <artifactId>hadoop-mapreduce-client-jobclient</artifactId>
 <version>2.7.3.2.6.0.3-8</version>
 </dependency>
 <dependency>
 <groupId>org.apache.avro</groupId>
 <artifactId>avro-mapred</artifactId>
 <version>1.8.1</version>
 </dependency>
 <dependency>
 <groupId>org.apache.hive</groupId>
 <artifactId>hive-common</artifactId>
 <version>1.2.1</version>
 </dependency>
 <dependency>
 <groupId>org.apache.avro</groupId>
 <artifactId>avro</artifactId>
 <version>1.8.1</version>
 </dependency>
 <dependency>
 <groupId>commons-io</groupId>
 <artifactId>commons-io</artifactId>
 <version>2.5</version>
 </dependency>
 <dependency>
 <groupId>org.apache.hadoop</groupId>
 <artifactId>hadoop-yarn-common</artifactId>
 <version>2.7.3.2.6.0.3-8</version>
 </dependency>
 <dependency>
 <groupId>commons-configuration</groupId>
 <artifactId>commons-configuration</artifactId>
 <version>1.10</version>
 </dependency>
 <dependency>
 <groupId>org.apache.hadoop</groupId>
 <artifactId>hadoop-auth</artifactId>
 <version>2.7.3.2.6.0.3-8</version>
 </dependency>
 <dependency>
 <groupId>org.apache.htrace</groupId>
 <artifactId>htrace-core</artifactId>
 <version>3.2.0-incubating</version>
 </dependency>
 <dependency>
 <groupId>org.apache.hadoop</groupId>
 <artifactId>hadoop-yarn-client</artifactId>
 <version>2.7.3.2.6.0.3-8</version>
 </dependency>
 <dependency>
 <groupId>org.apache.hadoop</groupId>
 <artifactId>hadoop-yarn-api</artifactId>
 <version>2.7.3.2.6.0.3-8</version>
 </dependency>
 *
 */
public class SqoopImport {
    @Test
    public void importMysql() throws Exception{
        System.setProperty("HADOOP_USER_NAME","hdfs");
        String[] args = new String[]{
                "--connect",String.format("jdbc:mysql://%s:%s/%s?tinyIntlistBit=false","hadoop-7","3306","test2"),
                "--username","root",
                "--password","123456",
                "--table","wan2",
                "--columns","id,sc",
                "--target-dir","/test/sqoop/table",
                "-m","1",
                "--delete-target-dir"
        };
//        com.cloudera.sqoop.tool.SqoopTool sqoopTool=(com.cloudera.sqoop.tool.SqoopTool)SqoopTool.getTool("import");
//        Configuration conf= new Configuration();
//        conf.set("mapreduce.app-submission.cross-platform","true");
////        conf.set("fs.default.name","hdfs://localhost:9000");
//        Configuration hive=HiveConfig.getHiveConf(conf);
//        Sqoop sqoop = new Sqoop(sqoopTool,SqoopTool.loadPlugins(conf) );
//        int res = Sqoop.runSqoop(sqoop,args);
//        System.out.println(res);
//        System.out.println("执行sqoop结束");

    }
}

package driver.test;

import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.schema.SchemaPlus;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * 在这里，我们定义jdbc url字符串，并在创建连接的时候，对url进行分析，并将json的名字，注册到root schema 。
 * 当然这里是最小化实现
 */
public class Driver extends org.apache.calcite.jdbc.Driver {
    public static final String CONNECT_STRING_PREFIX = "jdbc:json:";
    static {
        new Driver().register();
    }

    @Override protected String getConnectStringPrefix() {
        return CONNECT_STRING_PREFIX;
    }


    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        Connection c = super.connect(url, info);
        CalciteConnection optiqConnection = (CalciteConnection) c.unwrap(CalciteConnection.class);
        SchemaPlus rootSchema = optiqConnection.getRootSchema();
        String[] pars = url.split("_123_");
        Path f = Paths.get(pars[1]);
        try {
            JsonSchema js = new JsonSchema();
            Files.list(f).filter(path -> {
                return path.toString().endsWith("json");
            }).forEach(it->{
                System.out.println("it --> " + it);
                for(int i = 0;i < it.getNameCount();i++){
                    System.out.println("path.getname-" + i + " : " + it.getName(i));
                }
                File file = it.getName(it.getNameCount()-1).toFile();
                String filename = file.getName();
                filename = filename.substring(0,filename.lastIndexOf("."));
                String json = "";
                try {
                    json = Files.readAllLines(it.toAbsolutePath()).stream().collect(Collectors.joining());//.forEach(line->{ sb.append(line);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                js.put(filename,json);
            });

            rootSchema.add("resources", js);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return c;
    }

}

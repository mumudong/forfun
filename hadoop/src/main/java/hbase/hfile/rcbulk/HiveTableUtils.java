package hbase.hfile.rcbulk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class HiveTableUtils {
    public static List<String> getFieldName(String filePath) {
        File file = new File(filePath);
        BufferedReader reader = null;
        List<String> fieldName = new ArrayList<String>();

        try {
            if (file.exists()) {
                reader = new BufferedReader(new FileReader(file));
                String tmp = null;
                while ((tmp = reader.readLine()) != null) {
                    if (tmp.contains("`") && tmp.contains("COMMENT")) {
                        int start = tmp.indexOf("`");
                        int end = tmp.lastIndexOf("`");
                        fieldName.add(tmp.substring(start + 1, end));
                    }
                }
            } else {
                System.err.println("The file doesn't exist!");
                System.exit(1);
            }

            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fieldName;
    }
}

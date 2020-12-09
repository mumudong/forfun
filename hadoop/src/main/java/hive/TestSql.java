package hive;

import com.alibaba.fastjson.JSON;
import org.antlr.runtime.tree.Tree;
import org.apache.hadoop.hive.ql.lib.Node;
import org.apache.hadoop.hive.ql.parse.*;
import org.apache.hadoop.hive.serde.serdeConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.hadoop.hive.ql.parse.BaseSemanticAnalyzer.*;

public class TestSql {

    public static void main(String[] args)throws Exception {
        String sql = "CREATE TABLE IF NOT EXISTS `idm`.`idm_zyh_004_ddddd_i_d` ( `salary` decimal(12,2) comment \"实时\" ,`fox` array<decimal(12,2)> comment \"搜索\" ,`hh` map<string, string> comment \"能\") COMMENT \"ddd\" PARTITIONED BY ( `dt` string comment \"日期\", `dt2` string comment \"分区2\")  STORED AS ORC";
        System.out.println(sql);
        Map<String, List<FieldInfo>> stringListMap = parseDdl(sql);
        System.out.println(JSON.toJSONString(stringListMap));
    }

    public static Map<String,List<FieldInfo>> parseDdl(String sql){
        sql = sql.trim();
        while(sql.endsWith(";")){
            sql = sql.substring(0,sql.length()-1);
        }
        Map<String,List<FieldInfo>> result = new HashMap<>();
        try {
            ParseDriver pd = new ParseDriver();
            ASTNode tree = pd.parse(sql);
            tree = findRootNonNullToken(tree);
            if(tree.getType() != HiveParser.TOK_CREATETABLE){
                throw new RuntimeException("不是建表语句,请您检查sql语句后重新填写");
            }
            ArrayList<Node> children = tree.getChildren();
            for(Node childNode:children){
                ASTNode childASTNode = (ASTNode) childNode;
                switch (childASTNode.getType()){
                    case HiveParser.TOK_TABCOLLIST:
                        List<FieldInfo> cols = getColumns(childASTNode,true);
                        result.put("columns",cols);
                        break;
                    case HiveParser.TOK_TABLEPARTCOLS:
                        List<FieldInfo> partCols = getColumns( childASTNode, false);
                        result.put("partitions",partCols);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Get the list of FieldSchema out of the ASTNode.
     */
    public static List<FieldInfo> getColumns(ASTNode ast, boolean lowerCase) throws SemanticException {
        List<FieldInfo> colList = new ArrayList<FieldInfo>();
        int numCh = ast.getChildCount();
        for (int i = 0; i < numCh; i++) {
            FieldInfo col = new FieldInfo();
            ASTNode child = (ASTNode) ast.getChild(i);
            Tree grandChild = child.getChild(0);
            if(grandChild != null) {
                String name = grandChild.getText();
                if(lowerCase) {
                    name = name.toLowerCase();
                }
                // child 0 is the name of the column
                col.setFieldName(unescapeIdentifier(name));
                // child 1 is the type of the column
                ASTNode typeChild = (ASTNode) (child.getChild(1));
                col.setFieldType(getTypeStringFromAST(typeChild));

                // child 2 is the optional comment of the column
                if (child.getChildCount() == 3) {
                    col.setFieldNameCh(unescapeSQLString(child.getChild(2).getText()));
                }
            }
            colList.add(col);
        }
        return colList;
    }

    protected static String getTypeStringFromAST(ASTNode typeNode)
            throws SemanticException {
        switch (typeNode.getType()) {
            case HiveParser.TOK_LIST:
                return serdeConstants.LIST_TYPE_NAME + "<"
                        + getTypeStringFromAST((ASTNode) typeNode.getChild(0)) + ">";
            case HiveParser.TOK_MAP:
                return serdeConstants.MAP_TYPE_NAME + "<"
                        + getTypeStringFromAST((ASTNode) typeNode.getChild(0)) + ","
                        + getTypeStringFromAST((ASTNode) typeNode.getChild(1)) + ">";
            case HiveParser.TOK_STRUCT:
                return getStructTypeStringFromAST(typeNode);
            case HiveParser.TOK_UNIONTYPE:
                return getUnionTypeStringFromAST(typeNode);
            default:
                return DDLSemanticAnalyzer.getTypeName(typeNode);
        }
    }

    private static String getStructTypeStringFromAST(ASTNode typeNode)
            throws SemanticException {
        String typeStr = serdeConstants.STRUCT_TYPE_NAME + "<";
        typeNode = (ASTNode) typeNode.getChild(0);
        int children = typeNode.getChildCount();
        if (children <= 0) {
            throw new SemanticException("empty struct not allowed.");
        }
        StringBuilder buffer = new StringBuilder(typeStr);
        for (int i = 0; i < children; i++) {
            ASTNode child = (ASTNode) typeNode.getChild(i);
            buffer.append(unescapeIdentifier(child.getChild(0).getText())).append(":");
            buffer.append(getTypeStringFromAST((ASTNode) child.getChild(1)));
            if (i < children - 1) {
                buffer.append(",");
            }
        }

        buffer.append(">");
        return buffer.toString();
    }

    private static String getUnionTypeStringFromAST(ASTNode typeNode)
            throws SemanticException {
        String typeStr = serdeConstants.UNION_TYPE_NAME + "<";
        typeNode = (ASTNode) typeNode.getChild(0);
        int children = typeNode.getChildCount();
        if (children <= 0) {
            throw new SemanticException("empty union not allowed.");
        }
        StringBuilder buffer = new StringBuilder(typeStr);
        for (int i = 0; i < children; i++) {
            buffer.append(getTypeStringFromAST((ASTNode) typeNode.getChild(i)));
            if (i < children - 1) {
                buffer.append(",");
            }
        }
        buffer.append(">");
        typeStr = buffer.toString();
        return typeStr;
    }

    private static ASTNode findRootNonNullToken(ASTNode tree) {
        while ((tree.getToken() == null) && (tree.getChildCount() > 0)) {
            tree = (ASTNode) tree.getChild(0);
        }
        return tree;
    }

    public void test(){
        String sql = "insert overwrite table avt_dev.avtdev_zyh1022_baitiao_py2_a_d PARTITION (dt = '{TX_DATE}') select id from avt_dev.avtdev_zyh1022_baitiao_pyinsert_i_d";
        try {
            ParseUtils.parse(sql);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    static class FieldInfo{
        private String id = "";
        private String fieldName;
        private String fieldNameCh;
        private String fieldType;
        private String fieldLen = "";
        private String srt = "";
        private String isPk = "n";

        public FieldInfo() {
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getFieldName() {
            return fieldName;
        }

        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }

        public String getFieldNameCh() {
            return fieldNameCh;
        }

        public void setFieldNameCh(String fieldNameCh) {
            this.fieldNameCh = fieldNameCh;
        }

        public String getFieldType() {
            return fieldType;
        }

        public void setFieldType(String fieldType) {
            this.fieldType = fieldType;
        }

        public String getFieldLen() {
            return fieldLen;
        }

        public void setFieldLen(String fieldLen) {
            this.fieldLen = fieldLen;
        }

        public String getSrt() {
            return srt;
        }

        public void setSrt(String srt) {
            this.srt = srt;
        }

        public String getIsPk() {
            return isPk;
        }

        public void setIsPk(String isPk) {
            this.isPk = isPk;
        }
    }
}

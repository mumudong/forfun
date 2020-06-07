//package antlr.math.hivelineage;
//
//import antlr.math.rule.HplsqlBaseVisitor;
//import antlr.math.rule.HplsqlLexer;
//import antlr.math.rule.HplsqlParser;
//import org.antlr.v4.runtime.CharStream;
//import org.antlr.v4.runtime.CharStreams;
//import org.antlr.v4.runtime.CommonTokenStream;
//import org.antlr.v4.runtime.tree.ParseTree;
//
//import java.util.List;
//
//public class TestHive {
//    public static void main(String[] args) {
//        String sql = "insert into table t_table (id,name,age)  select s_id,s_name,s_age from (select id1,name1,age1 from s_table) t";
//        CharStream input = CharStreams.fromString(sql);
//        HplsqlLexer lexer = new HplsqlLexer(input);
//        CommonTokenStream tokens = new CommonTokenStream(lexer);
//        HplsqlParser parser = new HplsqlParser(tokens);
//        HplsqlParser.Insert_stmtContext tree = parser.insert_stmt(); // parse
//
//        System.out.println(tree.start);
//        System.out.println(tree.stop);
//        for(ParseTree tr:tree.children){
//            System.out.println(tr);
//        }
//
//        HplsqlBaseVisitor visitor = new MyVisitor();
//        visitor.visit(tree);
//    }
//}
//
//class MyVisitor extends HplsqlBaseVisitor{
//    @Override
//    public Object visitInsert_stmt_cols(HplsqlParser.Insert_stmt_colsContext ctx) {
//        System.out.println("insert_stmt_cols " + ctx.getText());
//        return super.visitInsert_stmt_cols(ctx);
//    }
//
//    @Override
//    public Object visitSubselect_stmt(HplsqlParser.Subselect_stmtContext ctx) {
//        System.out.println("subSelect " + ctx.getText());
//        return super.visitSubselect_stmt(ctx);
//    }
//
//    @Override
//    public Object visitSelect_list(HplsqlParser.Select_listContext ctx) {
//        List<HplsqlParser.Select_list_itemContext> select_list_itemContexts = ctx.select_list_item();
//        for(int i = 0;i < select_list_itemContexts.size();i++){
//            HplsqlParser.Select_list_itemContext select_list_itemContext = select_list_itemContexts.get(i);
//            System.out.println("select ident " + select_list_itemContext.getText());
//        }
//        return super.visitSelect_list(ctx);
//    }
//}

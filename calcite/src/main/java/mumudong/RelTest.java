package mumudong;

import org.apache.calcite.plan.RelOptUtil;
import org.apache.calcite.plan.RelTraitDef;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.Programs;
import org.apache.calcite.tools.RelBuilder;

import java.util.List;

/**
 * 关系代数测试
 */
public class RelTest {
    public static void main(String[] args) {
//        final SchemaPlus rootSchema = Frameworks.createRootSchema(true);
//        return Frameworks.newConfigBuilder()
//                .parserConfig(SqlParser.Config.DEFAULT)
//                .defaultSchema(
//                        CalciteAssert.addSchema(rootSchema, CalciteAssert.SchemaSpec.SCOTT_WITH_TEMPORAL))
//                .traitDefs((List<RelTraitDef>) null)
//                .programs(Programs.heuristicJoinOrder(Programs.RULE_SET, true, 2));
//
//
//        RelBuilder builder = RelBuilder.create(config);
//        RelNode node = builder.scan("EMP")
//                              .build();
//        System.out.println(RelOptUtil.toString(node));
    }
}

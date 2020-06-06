package study.yee;

import com.google.common.collect.ImmutableList;
import org.apache.calcite.plan.RelOptQuery;
import org.apache.calcite.plan.RelOptRule;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.plan.RelOptUtil;
import org.apache.calcite.plan.hep.HepPlanner;
import org.apache.calcite.plan.hep.HepProgram;
import org.apache.calcite.plan.hep.HepProgramBuilder;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.metadata.RelMetadataQuery;
import org.apache.calcite.rel.rules.CalcSplitRule;
import org.apache.calcite.rel.rules.FilterToCalcRule;
import org.apache.calcite.rel.rules.ProjectToCalcRule;
import org.apache.calcite.sql.type.SqlTypeName;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import study.yee.plan.QueryPlanner;
import study.yee.schema.TestSchema;

import java.util.Arrays;
import java.util.List;

public class QueryPlannerTest {

  private static TestSchema schema;

  public static void setUp() throws Exception {
    schema = new TestSchema();
    List<String> names = ImmutableList.of(
        "orderid",
        "productid");
    List<SqlTypeName> types = ImmutableList.of(
        SqlTypeName.INTEGER,
        SqlTypeName.INTEGER);
    schema.registerTable("ORDERS", names, types);
  }


  public static void getPlan() throws Exception {
    Assert.assertNotNull(schema.getSchema());

    QueryPlanner planner = new QueryPlanner(schema.getSchema());
    String sql = "select stream orderid from orders where productid > 3";
    RelNode rel = planner.getPlan(sql);

//    RelMetadataQuery metadataQuery = rel.getCluster().getMetadataQuery();
//    RelOptTable table = rel.getTable();

    System.out.println(RelOptUtil.toString(rel));
    hepTest(rel);
  }

  //计划优化
  public static void hepTest(RelNode temp){
    List<RelOptRule> relOptRules = Arrays.asList(FilterToCalcRule.INSTANCE, ProjectToCalcRule.INSTANCE, CalcSplitRule.INSTANCE);
    for (RelOptRule rule : relOptRules) {
      HepProgramBuilder builder = HepProgram.builder();
      builder.addRuleInstance(rule);
      HepProgram program = builder.build();
      HepPlanner hepPlanner = new HepPlanner(program);
      hepPlanner.setRoot(temp);
      temp = hepPlanner.findBestExp();
      System.out.println("规则:" + rule.toString());
      System.out.println(RelOptUtil.toString(temp));
      System.out.println();
    }
  }

  public static void main(String[] args) throws Exception{
    setUp();
    getPlan();
  }

}
package study.yee;

import com.google.common.collect.ImmutableList;
import org.apache.calcite.plan.RelOptUtil;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.sql.type.SqlTypeName;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import study.yee.plan.QueryPlanner;
import study.yee.schema.TestSchema;

import java.util.List;

public class QueryPlannerTest {

  private TestSchema schema;
  @Before
  public void setUp() throws Exception {
    schema = new TestSchema();
    List<String> names = ImmutableList.of(
        "orderid",
        "productid");
    List<SqlTypeName> types = ImmutableList.of(
        SqlTypeName.INTEGER,
        SqlTypeName.INTEGER);
    schema.registerTable("ORDERS", names, types);
  }

  @Test
  public void getPlan() throws Exception {
    Assert.assertNotNull(schema.getSchema());

    QueryPlanner planner = new QueryPlanner(schema.getSchema());
    String sql = "select stream orderid from orders where productid > 3";
    RelNode rel = planner.getPlan(sql);
    System.out.println(RelOptUtil.toString(rel));
  }

}
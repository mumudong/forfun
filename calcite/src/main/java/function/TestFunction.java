package function;

import com.google.common.collect.Lists;
import org.apache.calcite.config.CalciteConnectionConfigImpl;
import org.apache.calcite.config.CalciteConnectionProperty;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.volcano.VolcanoPlanner;
import org.apache.calcite.prepare.CalciteCatalogReader;
import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rel.type.RelDataTypeSystem;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.impl.ScalarFunctionImpl;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.type.*;
import org.apache.calcite.sql.util.ChainedSqlOperatorTable;
import org.apache.calcite.sql.util.ListSqlOperatorTable;
import org.apache.calcite.sql.validate.SqlConformance;
import org.apache.calcite.sql.validate.SqlConformanceEnum;
import org.apache.calcite.sql.validate.SqlValidatorCatalogReader;
import org.apache.calcite.sql.validate.SqlValidatorImpl;
import org.apache.calcite.sql2rel.RelDecorrelator;
import org.apache.calcite.sql2rel.SqlToRelConverter;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.Planner;
import org.apache.calcite.tools.RelBuilder;

import java.util.Properties;

import static function.FunctionUtil.TYPE_FACTORY;
import static org.apache.calcite.sql.type.InferTypes.VARCHAR_1024;
import static org.apache.calcite.sql.type.SqlTypeTransforms.FORCE_NULLABLE;

public class TestFunction {
    public static RelRoot sqlToRelNode(String sql) throws Exception{
        CalciteSchema rootSchema = CalciteSchema.createRootSchema(false, false);

        SchemaPlus plus = rootSchema.plus();
        plus.add("FUNC1", ScalarFunctionImpl.create(
                FunctionUtil.class, "func1"));

        plus.add("FUNC2", ScalarFunctionImpl.create(
                FunctionUtil.class, "func2"));

        CalciteCatalogReader calciteCatalogReader = new CalciteCatalogReader(rootSchema, Lists.newArrayList(),
                new SqlTypeFactoryImpl(RelDataTypeSystem.DEFAULT), new CalciteConnectionConfigImpl(new Properties()));

        ListSqlOperatorTable listSqlOperatorTable = new ListSqlOperatorTable();
        listSqlOperatorTable.add(new SqlFunction(
                        "my_function".toUpperCase(),
                        SqlKind.OTHER_FUNCTION,
                        ReturnTypes.cascade(ReturnTypes.explicit(SqlTypeName.VARCHAR), FORCE_NULLABLE),//return type is varchar with length of 1024
                        VARCHAR_1024, //parameter varchar with length of 1024
                        OperandTypes.family(SqlTypeFamily.CHARACTER),
                        SqlFunctionCategory.STRING
                )
        );

        FrameworkConfig frameworkConfig =
                Frameworks.newConfigBuilder()
                        .defaultSchema(plus)
                        .operatorTable(ChainedSqlOperatorTable.of(SqlStdOperatorTable.instance(),
                                calciteCatalogReader, listSqlOperatorTable))
                        .build();

        SqlParser.ConfigBuilder parserConfig =
                SqlParser.configBuilder(frameworkConfig.getParserConfig());

        //SQL 大小写不敏感
        parserConfig.setCaseSensitive(false).setConfig(parserConfig.build());

        Planner planner = Frameworks.getPlanner(frameworkConfig);

        SqlParser parser = SqlParser.create(sql, parserConfig.build());
        SqlNode sqlNode = parser.parseStmt();

        //这里需要注意大小写问题，否则表会无法找到

        //to supported user' define function
        SqlOperatorTable sqlOperatorTable = ChainedSqlOperatorTable
                .of(SqlStdOperatorTable.instance(), calciteCatalogReader);

        TestSqlValidatorImpl validator = new TestSqlValidatorImpl(
                sqlOperatorTable,
                calciteCatalogReader,
                TYPE_FACTORY,
                SqlConformanceEnum.DEFAULT);

        //try to union trait set
        //addRelTraitDef for is HepPlanner has not effect in fact
        VolcanoPlanner volcanoPlanner = new VolcanoPlanner();
        SqlNode validateSqlNode = validator.validate(sqlNode);
        final RexBuilder rexBuilder = new RexBuilder(TYPE_FACTORY);
        RelOptCluster cluster = RelOptCluster.create(volcanoPlanner, rexBuilder);

        final SqlToRelConverter.Config sqlToRelConverterConfig
                = SqlToRelConverter.configBuilder()
                .withConfig(frameworkConfig.getSqlToRelConverterConfig())
                .withTrimUnusedFields(false)
                .withConvertTableAccess(false)
                .build();

        final SqlToRelConverter sqlToRelConverter =
                new SqlToRelConverter(null, validator,
                        calciteCatalogReader, cluster, frameworkConfig.getConvertletTable(),
                        sqlToRelConverterConfig);

        RelRoot root =
                sqlToRelConverter.convertQuery(validateSqlNode, false, true);
        root = root.withRel(sqlToRelConverter.flattenTypes(root.rel, true));
        final RelBuilder relBuilder = sqlToRelConverterConfig
                .getRelBuilderFactory().create(cluster, null);

        //change trait set of TableScan
        return root.withRel(
                RelDecorrelator.decorrelateQuery(root.rel, relBuilder));
    }
    static class TestSqlValidatorImpl  extends SqlValidatorImpl {
        public TestSqlValidatorImpl(SqlOperatorTable opTab,
                                    SqlValidatorCatalogReader catalogReader,
                                    RelDataTypeFactory typeFactory,
                                    SqlConformance conformance) {
            super(opTab, catalogReader, typeFactory, conformance);
        }
    }
}



package function;

import org.apache.calcite.config.CalciteConnectionConfigImpl;
import org.apache.calcite.config.CalciteConnectionProperty;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.prepare.CalciteCatalogReader;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.tools.Frameworks;

import java.util.Properties;

public class CsvCalciteCatalogReader {

    public static CalciteCatalogReader createMockCatalogReader(SqlParser.Config parserConfig,SchemaPlus schema) {
        SchemaPlus rootSchema = Frameworks.createRootSchema(true);
        rootSchema.add("hr",schema);
        return createCatalogReader(parserConfig, rootSchema);
    }

    public static CalciteCatalogReader createCatalogReader(SqlParser.Config parserConfig, SchemaPlus rootSchema) {

        Properties prop = new Properties();
        prop.setProperty(CalciteConnectionProperty.CASE_SENSITIVE.camelName(),
                String.valueOf(parserConfig.caseSensitive()));
        // 设置时区
        prop.setProperty(CalciteConnectionProperty.TIME_ZONE.camelName(), "GMT+:08:00");
        CalciteConnectionConfigImpl calciteConnectionConfig = new CalciteConnectionConfigImpl(prop);
        return new CalciteCatalogReader(
                CalciteSchema.from(rootSchema),
                CalciteSchema.from(rootSchema).path(null),
                new JavaTypeFactoryImpl(),
                calciteConnectionConfig
        );
    }
}

package function;

import com.google.common.collect.Lists;
import org.apache.calcite.rel.type.RelDataTypeSystem;
import org.apache.calcite.sql.SqlFunction;
import org.apache.calcite.sql.SqlFunctionCategory;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.calcite.sql.type.*;

public class FunctionUtil {
    public static final SqlTypeFactoryImpl TYPE_FACTORY = new SqlTypeFactoryImpl(
            RelDataTypeSystem.DEFAULT);
    public static final RelDataTypeSystem TYPE_SYSTEM = RelDataTypeSystem.DEFAULT;

    public static final SqlFunction FUNC1 = new SqlFunction(
            new SqlIdentifier("FUNC1", SqlParserPos.ZERO),
            ReturnTypes.cascade(ReturnTypes.explicit(SqlTypeName.INTEGER), SqlTypeTransforms.TO_NULLABLE),
            InferTypes.VARCHAR_1024,
            OperandTypes.family(SqlTypeFamily.STRING),
            Lists.newArrayList(TYPE_FACTORY.createSqlType(SqlTypeName.VARCHAR)),
            SqlFunctionCategory.USER_DEFINED_FUNCTION);

    public static final SqlFunction FUNC2 = new SqlFunction(
            new SqlIdentifier("FUNC2", SqlParserPos.ZERO),
            ReturnTypes.cascade(ReturnTypes.explicit(SqlTypeName.VARCHAR), SqlTypeTransforms.TO_NULLABLE),
            InferTypes.FIRST_KNOWN,
            OperandTypes.family(SqlTypeFamily.INTEGER),
            Lists.newArrayList(TYPE_FACTORY.createSqlType(SqlTypeName.INTEGER)),
            SqlFunctionCategory.USER_DEFINED_FUNCTION);
}

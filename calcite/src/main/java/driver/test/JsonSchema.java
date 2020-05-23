package driver.test;

import com.google.common.collect.Maps;
import org.apache.calcite.DataContext;
import org.apache.calcite.linq4j.AbstractEnumerable;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.linq4j.Linq4j;
import org.apache.calcite.linq4j.tree.Expression;
import org.apache.calcite.linq4j.tree.Expressions;
import org.apache.calcite.linq4j.tree.Types;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.ScannableTable;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.Schemas;
import org.apache.calcite.schema.Statistic;
import org.apache.calcite.schema.Statistics;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractSchema;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.calcite.util.BuiltInMethod;
import org.apache.calcite.util.Pair;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 来定义json的schema和table，主要是为了遍历获取元数据，以及迭代数据的时候，使用的方法。
 */
public class JsonSchema extends AbstractSchema {
    private String target;
    private String topic;
    static Map<String, Table> table = Maps.newHashMap();
    public JsonSchema(){
        super();
    }

    public void put(String topic, String target) {
        this.topic = topic;
        if (!target.startsWith("[")) {
            this.target = '[' + target + ']';
        } else {
            this.target = target;
        }
        final Table table = fieldRelation();
        if (table != null) {
            this.table.put(topic,table);
        }

    }
    public JsonSchema(String topic, String target) {
        super();
        this.put(topic,target);
    }

    @Override
    public String toString() {
        return "driver.test.JsonSchema(topic=" + topic + ":target=" + target + ")"+ this.table;
    }

    public String getTarget() {
        return target;
    }

    @Override
    protected Map<String, Table> getTableMap() {
        return table;
    }


    Expression getTargetExpression(SchemaPlus parentSchema, String name) {
        return Types.castIfNecessary(target.getClass(),
                Expressions.call(Schemas.unwrap(getExpression(parentSchema, name), JsonSchema.class),
                        BuiltInMethod.REFLECTIVE_SCHEMA_GET_TARGET.method));
    }

    private <T> Table fieldRelation() {
        JSONArray jsonarr = JSON.parseArray(target);
        // final Enumerator<Object> enumerator = Linq4j.enumerator(list);
        return new JsonTable(jsonarr);
    }

    private static class JsonTable extends AbstractTable implements ScannableTable {
        private final JSONArray jsonarr;
        // private final Enumerable<Object> enumerable;
        public JsonTable(JSONArray obj) {
            this.jsonarr = obj;
        }

        public RelDataType getRowType(RelDataTypeFactory typeFactory) {
            final List<RelDataType> types = new ArrayList<RelDataType>();
            final List<String> names = new ArrayList<String>();
            JSONObject jsonobj = jsonarr.getJSONObject(0);
            for (String field : jsonobj.keySet()) {
                final RelDataType type;
                type = typeFactory.createJavaType(jsonobj.get(field).getClass());
                names.add(field);
                types.add(type);
            }
            if (names.isEmpty()) {
                names.add("line");
                types.add(typeFactory.createJavaType(String.class));
            }
            return typeFactory.createStructType(Pair.zip(names, types));
        }

        public Statistic getStatistic() {
            return Statistics.UNKNOWN;
        }

        public Enumerable<Object[]> scan(DataContext root) {
            return new AbstractEnumerable<Object[]>() {
                public Enumerator<Object[]> enumerator() {
                    return new JsonEnumerator(jsonarr);
                }
            };
        }
    }

    public static class JsonEnumerator implements Enumerator<Object[]> {

        private Enumerator<Object[]> enumerator;
        public JsonEnumerator(JSONArray jsonarr) {
            List<Object[]> objs = new ArrayList<Object[]>();
            for (Object obj : jsonarr) {
                objs.add(((JSONObject) obj).values().toArray());
            }
            enumerator = Linq4j.enumerator(objs);
        }

        public Object[] current() {
            return (Object[]) enumerator.current();
        }

        public boolean moveNext() {
            return enumerator.moveNext();
        }

        public void reset() {
            enumerator.reset();
        }

        public void close() {
            enumerator.close();
        }

    }
}
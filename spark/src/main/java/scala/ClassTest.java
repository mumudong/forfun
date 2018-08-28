package scala;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/6/20.
 */
class C<T>{}
public class ClassTest extends C<Integer>{
    private static List<String> list;
    public static void main(String[] args) throws Exception {
        list = new ArrayList<String>();
        list.add("1");
        System.out.println(list.getClass());
        System.out.println(list.getClass().getGenericSuperclass());
        System.out.println(ClassTest.class.getGenericSuperclass());//获取继承中的泛型
        System.out.println(list );
        ParameterizedType parameterizedType = (ParameterizedType) list.getClass().getGenericInterfaces()[0];


        Type genType = list.getClass().getGenericSuperclass();
        if(ParameterizedType.class.isInstance(genType)) {
            ParameterizedType pType = (ParameterizedType) genType;
//            templatClazz = (Class) pType.getActualTypeArguments()[0];
            System.out.println("-->" + pType.getTypeName() + "  " + pType.getRawType() + "  " + pType.getOwnerType());
            testList();
        }

    }

    public static void testList() throws NoSuchFieldException, SecurityException {
        Type t = ClassTest.class.getDeclaredField("list").getGenericType();
        if (ParameterizedType.class.isAssignableFrom(t.getClass())) {
            for (Type t1 : ((ParameterizedType) t).getActualTypeArguments()) {
                System.out.print(t1 + ",");
            }
            System.out.println();
        }
        List<String> listUser = new ArrayList<String>();
        System.out.println(listUser);
        ParameterizedType genType = (ParameterizedType)listUser.getClass().getGenericSuperclass();
        Class templatClazz = null;
        System.out.println("--");
        if(ParameterizedType.class.isInstance(genType)){
            //无法获取到User类，或者可能获取到错误的类型，如果有同名且不带包名的泛型存在
//            ParameterizedType parameterizedType = (ParameterizedType) genType;
//            templatClazz = (Class) genType.getActualTypeArguments()[0];
            System.out.println(genType.getActualTypeArguments()[0]);
        }
    }
}

package javalearn.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

public class FuncTest {
    public static void main(String[] args) {
        Set<Class<?>> annotationedClasses = AnnotationScanner.getAnnotationClassByReflection("javalearn/annotation", FuncAnnotation.class);
        for(Class clazz:annotationedClasses){
            System.out.println(clazz.getName());
            Annotation[] annotations = clazz.getAnnotations();
            for(Annotation annotation:annotations){
                if(annotation instanceof FuncAnnotation){
                    System.out.println(((FuncAnnotation) annotation).order());
                    System.out.println(((FuncAnnotation) annotation).msg());
                }
            }

            System.out.println(clazz.getCanonicalName());
            System.out.println("---------------------------------");
        }

        Set<Method> annotationMethods = AnnotationScanner.getAnnotationMethodByReflection("javalearn/annotation", FuncAnnotation.class);
        for(Method method:annotationMethods){
            Class<?> declaringClass = method.getDeclaringClass();
            try {
                Object o = declaringClass.newInstance();
                method.setAccessible(true);
                method.invoke(o);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
}

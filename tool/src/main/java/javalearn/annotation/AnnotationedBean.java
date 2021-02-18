package javalearn.annotation;

@FuncAnnotation(order = 2)
public class AnnotationedBean {
    public void func(){
        System.out.println("this is func");
    }
}

@FuncAnnotation(order = 1)
class AnnotationBeanTwo{
    public void func2(){
        System.out.println("this is func2");
    }
}



class AnnotationBeanThree{
    @FuncAnnotation(order = 3)
    public void func3(){
        System.out.println("this is func3");
    }
}
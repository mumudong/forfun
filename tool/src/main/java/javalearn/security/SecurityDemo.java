package javalearn.security;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

public class SecurityDemo {
    public static void main(String[] args) {
        System.setProperty("java.security.auth.login.config", "E:\\mydata\\forfun\\tool\\src\\main\\resources\\securityDemo.config");
        System.setProperty("java.security.policy", "E:\\mydata\\forfun\\tool\\src\\main\\resources\\securityDemo.policy");
        System.out.println(System.getenv("java.security.policy"));
        System.setSecurityManager(new SecurityManager());
        /** 系统属性也是一种资源
         *  指定策略的两种方式：
         *    -Djava.security.policy=securityDemo.policy
         *    System.setProperty(“java.security.policy”, “securityDemo.policy”)
         *
         *
         */
        //以上为授权,不区分用户,所以需要结合认证
        //以下为认证
        //1.LoginContext:认证核心类，也是入口类，用于触发登录认证，具体的登录模块由构造方法name参数指定
        //2.LoginModule:登录模块，封装具体的登录认证逻辑，如果认证失败则抛出异常，成为则向Subject中添加一个Principal
        //3.CallbackHandler:回调处理器，用于搜集认证信息
        //4.Principal:代表程序用户的某一身份，与其密切相关的为Subject，用于代表程序用户，而一个用户可以多种身份，授权时可以针对某用户的多个身份分别授权

        try {
            //创建登录上下文
            LoginContext context = new LoginContext("demo", new CallbackHandlerDemo());
            //进行登录，登录不成功则系统退出
            context.login();
        } catch (LoginException le) {
            System.err.println("Cannot create LoginContext. " + le.getMessage());
            System.exit(-1);
        } catch (SecurityException se) {
            System.err.println("Cannot create LoginContext. " + se.getMessage());
            System.exit(-1);
        }

        String property = System.getProperty("java.home");
        System.out.println(property);
    }
}

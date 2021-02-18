package javalearn.security;

import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.io.IOException;
import java.security.Principal;
import java.util.Iterator;
import java.util.Map;

public class LoginModuleDemo implements LoginModule {
    //subject代表用户、principal代表身份，一个用户可以有多个身份
    private Subject subject;
    private CallbackHandler callbackHandler;
    private boolean success = false;
    private String user;
    private String password;
    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
    }

    @Override
    public boolean login() throws LoginException {
        NameCallback nameCallback = new NameCallback("请输入用户名");
        PasswordCallback passwordCallback = new PasswordCallback("请输入密码", false);
        Callback[] callbacks = new Callback[]{nameCallback, passwordCallback};
        try {
            //执行回调，回调过程中获取用户名与密码
            callbackHandler.handle(callbacks);
            //TODO 得到用户名与密码
            user = nameCallback.getName();
            password = new String(passwordCallback.getPassword());
        } catch (IOException | UnsupportedCallbackException e) {
            success = false;
            throw new FailedLoginException("用户名或密码获取失败");
        }
        System.out.println("user = " + user);
        System.out.println("password = " + password);
        //为简单起见认证条件写死
        if(user.length()>3 && password.length()>3) {
            success = true;//认证成功
        }
        System.out.println("登录" + (success?"成功":"失败"));
        return true;
    }

    @Override
    public boolean commit() throws LoginException {
        if(!success) {
            return false;
        } else {
            //如果认证成功则得subject中添加一个Principal对象
            //这样某身份用户就认证通过并登录了该应用，即表明了谁在执行该程序
            this.subject.getPrincipals().add(new PrincipalDemo(user));
            return true;
        }
    }

    @Override
    public boolean abort() throws LoginException {
        logout();
        return true;
    }

    @Override
    public boolean logout() throws LoginException {
        //退出时将相应的Principal对象从subject中移除
        Iterator<Principal> iter = subject.getPrincipals().iterator();
        while(iter.hasNext()) {
            Principal principal = iter.next();
            if(principal instanceof PrincipalDemo) {
                if(principal.getName().equals(user)) {
                    iter.remove();
                    break;
                }
            }
        }
        return true;
    }
}

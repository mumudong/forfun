package javalearn.security;

import javax.security.auth.callback.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class CallbackHandlerDemo implements CallbackHandler {
    private Scanner scanner = new Scanner(System.in);
    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        NameCallback nameCallback = (NameCallback) callbacks[0];
        PasswordCallback passwordCallback = (PasswordCallback) callbacks[1];
        //设置用户名与密码
        nameCallback.setName(getUserFromSomeWhere(nameCallback.getPrompt()));
        passwordCallback.setPassword(getPasswordFromSomeWhere(passwordCallback.getPrompt()).toCharArray());
    }
    //为简单起见用户名与密码写死直接返回，真实情况可以由用户输入等具体获取
    public String getUserFromSomeWhere(String promt) {
        System.out.println(promt);
        return scanner.nextLine();
    }
    public String getPasswordFromSomeWhere(String promt) {
        System.out.println(promt);
        return scanner.nextLine();
    }
}

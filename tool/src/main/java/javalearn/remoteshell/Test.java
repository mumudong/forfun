package javalearn.remoteshell;

/**
 * Created by Administrator on 2017/11/7.
 */
public class Test {
    public static void main(String[] args) {
        RemoteExecuteCommand rec=new RemoteExecuteCommand("10.167.202.183", "root","12qwaszx");
        //执行命令
        String cmd = "su root<<EOF\n" +
                "12qwaszx\n" +
                "whoami\n" +
                "EOF";
        System.out.println("cmd执行结果如下:\n " + rec.execute(cmd));

    }
}

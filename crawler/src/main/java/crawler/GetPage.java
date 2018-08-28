package crawler;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.UnexpectedPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.Scanner;

public class GetPage {
    public static void main(String[] args) throws Exception{
        LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");

        String url = "http://221.207.175.178:7989/uaa/personlogin#/personLogin";
        WebClient wc = new WebClient(BrowserVersion.CHROME);
        wc.getOptions().setJavaScriptEnabled(true); //启用JS解释器，默认为true
        wc.setJavaScriptTimeout(100000);//设置JS执行的超时时间
        wc.getOptions().setCssEnabled(false); //禁用css支持
        wc.getOptions().setThrowExceptionOnScriptError(false); //js运行错误时，是否抛出异常
        wc.getOptions().setTimeout(0); //设置连接超时时间 ，这里是10S。如果为0，则无限期等待
        wc.setAjaxController(new NicelyResynchronizingAjaxController());//设置支持AJAX
        // 等待JS驱动dom完成获得还原后的网页
        wc.getCache().setMaxSize(10);
        wc.waitForBackgroundJavaScriptStartingBefore(600000);
        wc.waitForBackgroundJavaScript(600000);
        HtmlPage htmlPage = wc.getPage(url);
        int i=0;
        while (true) {

            if (htmlPage.asText().contains("用户名")) {
                break;
            }
            System.out.println("开始等待第:"+ ++i +"次爬取");
            wc.getCache().clear();
            htmlPage = wc.getPage(url);
            synchronized (htmlPage) {
                htmlPage.wait(20000);
            }
        }

        // 网页内容
        System.out.println("\n\n--------------"+htmlPage.asText()+"\n-------------\n");

        HtmlTextInput textUserName = htmlPage.getFirstByXPath("//input[@id='username']");
        HtmlPasswordInput password = htmlPage.getFirstByXPath("//input[@id='inputPassword']");
        HtmlImage cod = htmlPage.getFirstByXPath("//img[@id='captchaImage']");
        HtmlTextInput code = htmlPage.getFirstByXPath("//input[@id='captchaWord']");
        HtmlElement button = htmlPage.getFirstByXPath("//button[@type='submit']");

        textUserName.setText("231084198511174027");
        password.setText("851117");
        File f = new File("C:\\Users\\Administrator\\Desktop\\img.jpg");
        if(f.exists())
            f.delete();
        cod.saveAs(f);

        System.out.println("请输入验证码:");
        String s= new Scanner(System.in).next();


        code.setText(s);
        HtmlPage page = button.click();
        wc.waitForBackgroundJavaScript(10000);

        System.out.println("\n\n=========\n\n"+page.asXml());

        UnexpectedPage pageJson = wc.getPage("http://221.207.175.178:7989/api/security/user");
        System.out.println("\n=====\n====\n=====\n=====\n=====");
        System.out.println(pageJson.getWebResponse().getContentAsString());


//        HtmlElement ele = page.getFirstByXPath("//div[@class='fastTrack_pic']");
//        HtmlPage pageNext = ele.click();
//        webClient.waitForBackgroundJavaScript(10000);
//        System.out.println("\n\n\n\n\n\n\n\n\n=========="+pageNext.asXml());
    }
}

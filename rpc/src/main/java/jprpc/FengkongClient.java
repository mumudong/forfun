package jprpc;

import com.alibaba.fastjson.JSONObject;
import fengkong.FKPredictGrpc;
import fengkong.Fengkong;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2018/6/26.
 */
public class FengkongClient {
    private final ManagedChannel channel;
    private final FKPredictGrpc.FKPredictBlockingStub blockingStub;


    public FengkongClient(String host,int port){
        channel = ManagedChannelBuilder.forAddress(host,port)
                .usePlaintext(true)
                .build();

        blockingStub = FKPredictGrpc.newBlockingStub(channel);
    }


    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public  void predict(String record){
        Fengkong.DataReq request = Fengkong.DataReq.newBuilder().setText(record)
                                                                .build();
        Fengkong.DataResp response = blockingStub.cardPredict(request);

        System.out.println("预测结果-------------------->\n" + response.toString() + new Date().toLocaleString());

    }
    public static Object getNumber(String str,String type){
        boolean isFloat = Pattern.matches("\\d*\\.\\d*",str.trim());
        if("int".equals(type)){
            if(Pattern.matches("\\d*",str.trim())){
                return Integer.valueOf(str);
            }else if("".equals(str)){
                return -999999;
            }
        }
        if("float".equals(type)){
            if(Pattern.matches("\\d*\\.\\d*",str.trim())){
                return Integer.valueOf(str);
            }else if("".equals(str)){
                return -999999.0;
            }
        }
        return -9999999;
    }
    public static void main(String[] args) throws InterruptedException {
        FengkongClient client = new FengkongClient("127.0.0.1",44444);
        JSONObject obj = new JSONObject();
        //正常用户
        String str = "1,5000, 36 months,Fully Paid,10.65%,10+ years,RENT,24000,Verified,  Borrower added on 12/22/11 > I need to upgrade my business technologies.<br>,credit_card,Computer,860xx,AZ,27.65,0,1,,,3,0,9,0,Dec-11,Jan-85";
        String str1 = "3,2400, 36 months,Fully Paid,15.96%,10+ years,RENT,12252,Not Verified,,small_business,real estate business,606xx,IL,8.72,0,2,,,2,0,10,0,Dec-11,Nov-01";
        String str11 = "4,10000, 36 months,Fully Paid,13.49%,10+ years,RENT,49200,Source Verified,\"  Borrower added on 12/21/11 > to pay for property tax (borrow from friend  need to pay back) & central A/C need to be replace. I'm very sorry to let my loan expired last time.<br>\",other,personel,917xx,CA,20,0,1,35,,10,0,37,0,Dec-11,Feb-96";
        //违约用户
        String str2 = "13,9000, 36 months,Charged Off,13.49%,< 1 year,RENT,30000,Source Verified,  Borrower added on 12/15/11 > Plan to pay off 2 charge accounts. I will close one of them and ask for a credit line decrease from the other. Also borrowed money from a friend and would like to pay that off.......<br><br>  Borrower added on 12/17/11 > The credit card that I am asking to be decreased will be ONLY for emergency purposes.....<br>,debt_consolidation,freedom,245xx,VA,10.08,0,1,,,4,0,9,0,Dec-11,Apr-04";
        String str3 = "15,10000, 36 months,Charged Off,10.65%,3 years,RENT,100000,Source Verified,,other,Other Loan,951xx,CA,7.06,0,2,,,14,0,29,0,Dec-11,May-91";
        String str4 = "22,21000, 36 months,Charged Off,12.42%,10+ years,RENT,105000,Verified,  Borrower added on 12/16/11 > Decided to clean up the debt and get my finances together.  Thank you for your consideration.<br>,debt_consolidation,Debt Cleanup,335xx,FL,13.22,0,0,,,7,0,38,0,Dec-11,Feb-83";
        client.predict(str);
        System.out.println(System.currentTimeMillis());
        client.predict(str1);
        System.out.println(System.currentTimeMillis());
        client.predict(str11);
        System.out.println(System.currentTimeMillis());
        client.predict(str2);
        System.out.println(System.currentTimeMillis());
        client.predict(str3);
        System.out.println(System.currentTimeMillis());
        client.predict(str4);
        System.out.println(System.currentTimeMillis());

//        for(int i=0;i<5;i++){
//            client.predict(str);
//        }


    }
}

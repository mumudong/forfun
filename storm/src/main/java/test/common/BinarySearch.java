package test.common;

/**
 * Created by Administrator on 2018/5/15.
 */
@SuppressWarnings("all")
public class BinarySearch {
    //年纪 年龄大于100分数减10000
    private static int[] age = {30,35,40,45,50,55,60,65,75,100};
    private static int[] ageScore = {30,20,17,11,5,0,-5,-11,-19,-26,-30};
    //appOverdueCount 发生过逾期的贷款个数
    private static int[] appOverdueCount = {0,1,3,5};
    private static int[] appOverdueCountScore = {50,47,34,18,-10};
    //monthAfterMoney	月税后工资
    private static int[] monthAfterMoney = {1000,5000,10000,15000,20000,25000,30000,35000,40000};
    private static int[] monthAfterMoneyScore = {-10,0,4,8,12,16,20,25,30,35};
    //ccOverdueTimesL60S 	简版信用卡信息-信用卡单张5年内累计逾期次数 
    private static int[] ccOverdueTimesL60S = {0,1,3,5,10};
    private static int[] ccOverdueTimesL60SScore = {30,11,0,-5,-10,-33};
    //ccDue90TimesL60S 	简版信用卡信息-信用卡5年内出现90天以上的逾期次数
    private static int[] ccDue90TimesL60S = {0,1,2,3,5};
    private static int[] ccDue90TimesL60SScore = {20,6,-6,-14,-22,-40};
    //plOverdueTimesL60S 	简版贷款信息-贷款单笔5年内累计逾期次数
    private static int[] plOverdueTimesL60S = {0,1,3,5};
    private static int[] plOverdueTimesL60SScore = {27,10,-5,-10,-20};
    //plDue90TimesL60S 	简版贷款信息-贷款5年内出现90天以上的逾期次数
    private static int[] plDue90TimesL60S = {0,1,2,3,5};
    private static int[] plDue90TimesL60SScore = {27,13,-2,-7,-11,-15};
    /* overdue6Timesl6	近6个月逾期6天以上（不含）的次数
       此处规则有问题
     */
    private static int[] overdue6Timesl6 = {0,1,2,3,5};
    private static int[] overdue6Timesl6Score = {8,4,0,-3,-4,-8};

    private static int[] edu = {1000,5000,10000,15000,20000,30000,40000};
    private static int[] eduScore = {-10,0,4,8,12,16,20,25};

    public static int binaryIntSearch(int num[], int number) {
        if(number <= num[0])
            return 0;
        if(number > num[num.length-1])
            return num.length;
        int start, end, mid;
        start = 0;
        end = num.length - 1;
        while (start <= end ) {
            mid = (start + end) / 2;
            if(mid == start)
                return start + 1;
            if (num[mid] == number)
                return mid ;
            else if (num[mid] > number) {
                end = mid ;
            } else {
                start = mid ;
            }
        }
        return -1;
    }
    public static String binarySearchByItem(String item,String value){
        if("age".equals(item))
            return ageScore[binaryIntSearch(age,Integer.valueOf(value))] + "";
        else if("income".equals(item))
            return monthAfterMoneyScore[binaryIntSearch(monthAfterMoney,Integer.valueOf(value))] + "";
        else if("yuqi".equals(item))
            return ccDue90TimesL60SScore[binaryIntSearch(ccDue90TimesL60S,Integer.valueOf(value))] + "";
        else if("sx".equals(item))
            return eduScore[binaryIntSearch(edu,Integer.valueOf(value))] + "";
        else
            return "异常";
    }
    public static void calc(int nianling,int yuqi1,int yuqi2,int yuqi3,int yuqi4,int yuqi5,int yuqi6,int salary){
        //年龄得分
        System.out.println("年龄得分-->" + ageScore[binaryIntSearch(age,nianling)]);
        //发生逾期个数
        System.out.println("逾期个数得分-->" + appOverdueCountScore[binaryIntSearch(appOverdueCount,yuqi1)]);
        //六个月逾期得分 -- 缺少对应规则
        System.out.println("六个月逾期得分-->" + overdue6Timesl6Score[binaryIntSearch(overdue6Timesl6,yuqi2)]);
        //简版信用卡五年累计逾期
        System.out.println("简版信用卡五年累计逾期得分-->" + ccOverdueTimesL60SScore[binaryIntSearch(ccOverdueTimesL60S,yuqi3)]);
        //简版信用卡五年90天以上逾期
        System.out.println("简版信用卡五年90天以上逾期得分-->" + ccDue90TimesL60SScore[binaryIntSearch(ccDue90TimesL60S,yuqi4)]);
        //简版贷款-单笔五年内累计逾期
        System.out.println("简版贷款-单笔五年内累计逾期得分-->" + plOverdueTimesL60SScore[binaryIntSearch(plOverdueTimesL60S,yuqi5)]);
        //简版贷款-五年内90天以上逾期
        System.out.println("简版贷款-五年内90天以上逾期得分-->" + plDue90TimesL60SScore[binaryIntSearch(plDue90TimesL60S,yuqi6)]);
        //税后工资
        System.out.println("税后工资得分-->" + monthAfterMoneyScore[binaryIntSearch(monthAfterMoney,salary)]);
        int sum = 540 +
                ageScore[binaryIntSearch(age,nianling)] +
                appOverdueCountScore[binaryIntSearch(appOverdueCount,yuqi1)] +
                overdue6Timesl6Score[binaryIntSearch(overdue6Timesl6,yuqi2)] +
                ccOverdueTimesL60SScore[binaryIntSearch(ccOverdueTimesL60S,yuqi3)] +
                ccDue90TimesL60SScore[binaryIntSearch(ccDue90TimesL60S,yuqi4)] +
                plOverdueTimesL60SScore[binaryIntSearch(plOverdueTimesL60S,yuqi5)] +
                plDue90TimesL60SScore[binaryIntSearch(plDue90TimesL60S,yuqi6)] +
                monthAfterMoneyScore[binaryIntSearch(monthAfterMoney,salary)];
        System.out.println("总得分-->" + sum + " 对应等级-->" + Stratege.get(sum));
    }
    public static Stratege calcX(int nianling,int salary,int yuqi,int sxed){
//        System.out.println("年龄得分-->" + ageScore[binaryIntSearch(age,nianling)]);
//        System.out.println("税后工资得分-->" + monthAfterMoneyScore[binaryIntSearch(monthAfterMoney,salary)]);
//        System.out.println("简版信用卡五年90天以上逾期得分-->" + ccDue90TimesL60SScore[binaryIntSearch(ccDue90TimesL60S,yuqi)]);
//        System.out.println("授信额度得分-->" + eduScore[binaryIntSearch(edu,sxed)]);
        int sum = 620 +
                ageScore[binaryIntSearch(age,nianling)] +
                ccDue90TimesL60SScore[binaryIntSearch(ccDue90TimesL60S,yuqi)] +
                monthAfterMoneyScore[binaryIntSearch(monthAfterMoney,salary)] +
                eduScore[binaryIntSearch(edu,sxed)];
        System.out.println("总得分-->" + sum + " 对应等级-->" + Stratege.get(sum));
        return Stratege.get(sum);
    }
    public static void main(String[] args) {
//        calc(38,1,2,1,0,0,0,7000);
        calcX(38,19000,5,40000);
    }


    public enum Stratege{
        D("D","[0-633.83)","64.36%" ,"D"),
        C3("C3","(633.83,640.94]","39.46%","D"),
        C2("C2","(640.94,649.91]","29.39%","D"),
        C1("C1","(649.91,658.54]","20.22%","C"),
        B3("B3","(658.54,662.98]","13.03%","C"),
        B2("B2","(662.98,667.87]","10.60%","B"),
        B1("B1","(667.87,72.76]","7.77%","B"),
        A3("A3","(672.76,679.51]","7.13%","B"),
        A2("A2","(679.51,686.21]","3.66%","A"),
        A1("A1","(686.21,700]","2.73%","A");
        //stratege 策略
        private static double[] scores = {633.83,640.94,649.91,658.54,662.98,667.87,672.76,679.51,686.21,700};
        private static Stratege[] scoreStratege = {Stratege.D, Stratege.C3, Stratege.C2,
                Stratege.C1, Stratege.B3,
                Stratege.B2, Stratege.B1, Stratege.A3,
                Stratege.A2, Stratege.A1};
        private String level;
        private String scoreSection;
        private String rateOfBadAccount;
        private String stratege;
        private Stratege(String level,String scoreSection,String rateOfBadAccount,String stratege){
            this.level = level;
            this.scoreSection = scoreSection;
            this.rateOfBadAccount = rateOfBadAccount;
            this.stratege = stratege;
        }
        public String getLeve(){return this.level;}
        public String getScoreSection(){return this.scoreSection;}
        public String getRateOfBadAccount(){return this.rateOfBadAccount;}
        public String getStratege(){return this.stratege;}
        public static Stratege get(double score){
            return scoreStratege[binaryDoubleSearch(scores,score)];
        }
        public static int binaryDoubleSearch(double num[], double number) {
            if(number > num[num.length-1])
                return num.length - 1;
            if(number <= num[0])
                return 0;
            int start, end, mid;
            start = 0;
            end = num.length - 1;
            while (start <= end) {
                mid = (start + end) / 2;
                if(mid == start)
                    return start + 1;
                if (num[mid] == number)
                    return mid ;
                else if (num[mid] > number) {
                    end = mid ;
                } else {
                    start = mid ;
                }
            }
            return -1;
        }
        @Override
        public String toString() {
            return "Stratege{" +
                    "level='" + level + '\'' +
                    ", scoreSection='" + scoreSection + '\'' +
                    ", rateOfBadAccount='" + rateOfBadAccount + '\'' +
                    ", stratege='" + stratege + '\'' +
                    '}';
        }
    }
}

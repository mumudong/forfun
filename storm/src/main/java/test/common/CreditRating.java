package test.common;

import java.sql.Timestamp;

/**
 * Created by Administrator on 2018/5/23.
 */
public class CreditRating {
    private Long Id;
    private String CustomNo;
    private String Age;
    private String AgeScore;
    private String Income;
    private String IncomeScore;
    private String OverdueNo90;
    private String OverdueNo90Score;
    private String AuthorizedAmounts;
    private String AuthorizedAmountsScore;
    private String CreditRatings;
    private String IsFraud;
    private String EnterTime;
    private Timestamp CreateTime;

    public CreditRating() {
    }

    public CreditRating(Long id, String customNo, String age, String ageScore, String income, String incomeScore, String overdueNo90, String overdueNo90Score, String authorizedAmounts, String authorizedAmountsScore, String creditRatings, String isFraud, String enterTime, Timestamp createTime) {
        Id = id;
        CustomNo = customNo;
        Age = age;
        AgeScore = ageScore;
        Income = income;
        IncomeScore = incomeScore;
        OverdueNo90 = overdueNo90;
        OverdueNo90Score = overdueNo90Score;
        AuthorizedAmounts = authorizedAmounts;
        AuthorizedAmountsScore = authorizedAmountsScore;
        CreditRatings = creditRatings;
        IsFraud = isFraud;
        EnterTime = enterTime;
        CreateTime = createTime;
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public String getCustomNo() {
        return CustomNo;
    }

    public void setCustomNo(String customNo) {
        CustomNo = customNo;
    }

    public String getAge() {
        return Age;
    }

    public void setAge(String age) {
        Age = age;
    }

    public String getAgeScore() {
        return AgeScore;
    }

    public void setAgeScore(String ageScore) {
        AgeScore = ageScore;
    }

    public String getIncome() {
        return Income;
    }

    public void setIncome(String income) {
        Income = income;
    }

    public String getIncomeScore() {
        return IncomeScore;
    }

    public void setIncomeScore(String incomeScore) {
        IncomeScore = incomeScore;
    }

    public String getOverdueNo90() {
        return OverdueNo90;
    }

    public void setOverdueNo90(String overdueNo90) {
        OverdueNo90 = overdueNo90;
    }

    public String getOverdueNo90Score() {
        return OverdueNo90Score;
    }

    public void setOverdueNo90Score(String overdueNo90Score) {
        OverdueNo90Score = overdueNo90Score;
    }

    public String getAuthorizedAmounts() {
        return AuthorizedAmounts;
    }

    public void setAuthorizedAmounts(String authorizedAmounts) {
        AuthorizedAmounts = authorizedAmounts;
    }

    public String getAuthorizedAmountsScore() {
        return AuthorizedAmountsScore;
    }

    public void setAuthorizedAmountsScore(String authorizedAmountsScore) {
        AuthorizedAmountsScore = authorizedAmountsScore;
    }

    public String getCreditRatings() {
        return CreditRatings;
    }

    public void setCreditRatings(String creditRatings) {
        CreditRatings = creditRatings;
    }

    public String getIsFraud() {
        return IsFraud;
    }

    public void setIsFraud(String isFraud) {
        IsFraud = isFraud;
    }

    public String getEnterTime() {
        return EnterTime;
    }

    public void setEnterTime(String enterTime) {
        EnterTime = enterTime;
    }

    public Timestamp getCreateTime() {
        return CreateTime;
    }

    public void setCreateTime(Timestamp createTime) {
        CreateTime = createTime;
    }

    @Override
    public String toString() {
        return "CreditRating{" +
                "Id=" + Id +
                ", CustomNo='" + CustomNo + '\'' +
                ", Age='" + Age + '\'' +
                ", AgeScore='" + AgeScore + '\'' +
                ", Income='" + Income + '\'' +
                ", IncomeScore='" + IncomeScore + '\'' +
                ", OverdueNo90='" + OverdueNo90 + '\'' +
                ", OverdueNo90Score='" + OverdueNo90Score + '\'' +
                ", AuthorizedAmounts='" + AuthorizedAmounts + '\'' +
                ", AuthorizedAmountsScore='" + AuthorizedAmountsScore + '\'' +
                ", CreditRatings='" + CreditRatings + '\'' +
                ", IsFraud='" + IsFraud + '\'' +
                ", EnterTime='" + EnterTime + '\'' +
                ", CreateTime=" + CreateTime +
                '}';
    }
}

package cronexpression;

import org.quartz.CronExpression;

import java.util.Date;

public class SchedTest {
    public static void main(String[] args)throws Exception {
        CronExpression cronExpression = new CronExpression("5 2 2 */2 * ?");
        cronExpression.getTimeAfter(new Date());
    }
}

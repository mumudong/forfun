package hdfs.yarn;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.apache.hadoop.yarn.util.Records;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

public class YarnUtil {

//获取任务的applicationId

    public static String getAppId(String jobName) {

        YarnClient client = YarnClient.createYarnClient();

        Configuration conf = new Configuration();

        client.init(conf);

        client.start();

        EnumSet<YarnApplicationState> appStates = EnumSet.noneOf(YarnApplicationState.class);

        if (appStates.isEmpty()) {

            appStates.add(YarnApplicationState.RUNNING);

            appStates.add(YarnApplicationState.ACCEPTED);

            appStates.add(YarnApplicationState.SUBMITTED);

        }

        List<ApplicationReport> appsReport = null;

        try {

//返回EnumSet<YarnApplicationState>中个人任务状态的所有任务

            appsReport = client.getApplications(appStates);

        } catch (YarnException | IOException e) {

            e.printStackTrace();

        }

        assert appsReport != null;

        for (ApplicationReport appReport : appsReport) {

//获取任务名

            String jn = appReport.getName();

            String applicationType = appReport.getApplicationType();

            if (jn.equals(jobName) && "Apache Flink".equals(applicationType)) {

                try {

                    client.close();

                } catch (IOException e) {

                    e.printStackTrace();

                }

                return appReport.getApplicationId().toString();

            }

        }

        try {

            client.close();

        } catch (IOException e) {

            e.printStackTrace();

        }

        return null;

    }

//根据任务的applicationId去获取任务的状态

    public static YarnApplicationState getState(String appId) {

        YarnClient client = YarnClient.createYarnClient();

        Configuration conf = new Configuration();

        client.init(conf);

        client.start();

        YarnApplicationState yarnApplicationState = null;

        try {

            String [] apps = appId.split("_");

            ApplicationReport applicationReport = client.getApplicationReport(ApplicationId.newInstance(Long.valueOf(apps[1]),Integer.valueOf(apps[2])));

            yarnApplicationState = applicationReport.getYarnApplicationState();

        } catch (YarnException | IOException e) {

            e.printStackTrace();

        }

        try {

            client.close();

        } catch (IOException e) {

            e.printStackTrace();

        }

        return yarnApplicationState;

    }

    public static void main(String[] args) throws IOException, InterruptedException {

        YarnApplicationState state = getState("application_id4244262");

        System.out.println(state);

        System.out.println(state == YarnApplicationState.RUNNING);

    }

}

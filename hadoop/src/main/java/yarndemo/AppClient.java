package yarndemo;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.yarn.api.ApplicationConstants;
import org.apache.hadoop.yarn.api.protocolrecords.GetNewApplicationResponse;
import org.apache.hadoop.yarn.api.records.*;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.client.api.YarnClientApplication;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.apache.hadoop.yarn.util.ConverterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class AppClient {
    private static Logger LOG = LoggerFactory.getLogger(AppClient.class);
    private static String appMasterClass = "com.example.yarn.demo01.AppMaster";
    private static final String appName = "yarn application demo";

    public static void main(String[] args) {
        AppClient client = new AppClient();
        try {
            client.run();
        } catch (Exception e) {
            LOG.error("client run exception , please check log file.", e);
        }
    }

    // 开始执行任务
    public void run() throws IOException, YarnException {

        Configuration hadoopConf = new Configuration();

        // 1. 创建YarnClient和ResourceManager进行交互
        YarnClient yarnClient = YarnClient.createYarnClient();
        yarnClient.init(hadoopConf);

        // yarnClient需要启动之后才能用
        yarnClient.start();

        // 这是我们在yarn上创建出来的应用
        YarnClientApplication application = yarnClient.createApplication();
        ApplicationSubmissionContext applicationSubmissionContext = application.getApplicationSubmissionContext();
        GetNewApplicationResponse newApplicationResponse = application.getNewApplicationResponse();
        // 设置application对象运行上下文
        /**
         * 设置上限文对象都需要设置哪些东西呢
         *{{{
         * // 一般情况这个id是不用设置的这个ID会根据集群的时间戳和排序的id自动生成
         * setApplicationId(ApplicationId applicationId);
         * // 这是任务的名称，这个需要设置
         * setApplicationName(String applicationName);
         * // 设置任务指定所在的队列，默认的队列default
         * setQueue(String queue);
         * // 任务优先级设置
         * setPriority(Priority priority);
         * // 设置applicationMaster运行container环境，也就是任务的master，最为关键
         * setAMContainerSpec(ContainerLaunchContext amContainer);
         * // 设置UnmmanageAM，默认值是false，am默认是启动在节点上的container，如果设置成true，再配合其他设置可将这个am启动在指定的环境下方便调试
         * setUnmanagedAM(boolean value);
         * // 完成任务之后是否销毁令牌
         * setCancelTokensWhenComplete(boolean cancel);
         * // 最多重试多少次
         * setMaxAppAttempts(int maxAppAttempts);
         * // 设置资源，这里的资源指的是计算机资源包括cpu和内存等的资源
         * setResource(Resource resource);
         * // 设置任务类型
         * setApplicationType(String applicationType);
         * // 在应用重试的时候这个container容器是否可以正常访问
         * setKeepContainersAcrossApplicationAttempts(Boolean boolean);
         * // 为应用程序设置标签
         * setApplicationTags(Set<String> tags);
         * //
         * setNodeLabelExpression(String nodeLabelExpression);
         * //
         * setAMContainerResourceRequest(ResourceRequest request);
         * setAttemptFailuresValidityInterval()
         * setLogAggregationContext()
         * setReservationID(ReservationId reservationID);
         *}}}
         */
        // 这个如果不设置的话默认是N/A，也就是空的意思
        applicationSubmissionContext.setApplicationName(appName);
        // 设置任务优先级，数字越高优先级越高，默认是-1
        applicationSubmissionContext.setPriority(Priority.newInstance(10));

        // TODO 添加本地资源
        Map<String, LocalResource> localResources = new HashMap<>(1 << 4);
        FileSystem fs = FileSystem.get(hadoopConf);
        String appMasterJarPath = "yarn-application-demo-1.0-SNAPSHOT.jar";
        String appMasterJar = "D:\\Users\\Bigdata\\learning\\source\\yarn-application-demo\\target\\yarn-application-demo-1.0-SNAPSHOT.jar" ;
        ApplicationId appId = applicationSubmissionContext.getApplicationId();
        addToLocalResources(fs,appMasterJar,appMasterJarPath,appId.toString(),localResources,null);

        // TODO 添加运行环境
        Map<String, String> env = new HashMap<>(1 << 4);
        // 任务的运行依赖jar包的准备
        StringBuilder classPathEnv = new StringBuilder(ApplicationConstants.Environment.CLASSPATH.$$())
                .append(ApplicationConstants.CLASS_PATH_SEPARATOR).append("./*");
        for (String c : hadoopConf.getStrings(
                YarnConfiguration.YARN_APPLICATION_CLASSPATH,
                YarnConfiguration.DEFAULT_YARN_CROSS_PLATFORM_APPLICATION_CLASSPATH)) {
            classPathEnv.append(ApplicationConstants.CLASS_PATH_SEPARATOR);
            classPathEnv.append(c.trim());
        }
        classPathEnv.append(ApplicationConstants.CLASS_PATH_SEPARATOR).append(
                "./log4j.properties");

        // add the runtime classpath needed for tests to work
        if (hadoopConf.getBoolean(YarnConfiguration.IS_MINI_YARN_CLUSTER, false)) {
            classPathEnv.append(':');
            classPathEnv.append(System.getProperty("java.class.path"));
        }
        env.put("CLASSPATH", classPathEnv.toString());

        // TODO 添加命令列表
        List<String> commands = new ArrayList<>(1 << 4);

        // 1. 需要将path下面的jar包上传至hdfs，然后其他节点从hdfs上下载下来
        commands.add(ApplicationConstants.Environment.JAVA_HOME.$$() + "/bin/java"+ " -Xmx200m -Xms200m -Xmn20m " + appMasterClass);

        ContainerLaunchContext amContainer = ContainerLaunchContext.newInstance(
                localResources, env, commands, null, null, null);
        // 准备amContainer的运行环境
        applicationSubmissionContext.setAMContainerSpec(amContainer);

        // 设置UnmmanageAM，默认值是false，am默认是启动在节点上的container，如果设置成true，再配合其他设置可将这个am启动在指定的环境下方便调试
        //  applicationSubmissionContext.setUnmanagedAM(false);
        // 任务完成时令牌是否销毁，默认值是true
        applicationSubmissionContext.setCancelTokensWhenComplete(true);
        // 任务失败后最大重试次数，
        //  applicationSubmissionContext.setMaxAppAttempts();

        // 对资源进行设置，正常是从用户输入的参数中解析出来设置进入
        int memory = 1024;
        int vCores = 2;
        applicationSubmissionContext.setResource(Resource.newInstance(memory, vCores));
        // 设置任务类型
        applicationSubmissionContext.setApplicationType("my-yarn-application");
        // 默认是false
        applicationSubmissionContext.setKeepContainersAcrossApplicationAttempts(false);

        // 为应用程序设置标签
        Set<String> tags = new HashSet<>(1 << 2);
        tags.add("tag1");
        tags.add("tag2");
        applicationSubmissionContext.setApplicationTags(tags);

        // 设置节点标签
        //  applicationSubmissionContext.setNodeLabelExpression();

        // 设置applicationMaster的container运行资源请求
//        String hostName = "127.0.0.1";
//        int numContainers = 1;
//        ResourceRequest amRequest = ResourceRequest.newInstance(Priority.newInstance(10), hostName, Resource.newInstance(memory, vCores), numContainers);
//        applicationSubmissionContext.setAMContainerResourceRequest(amRequest);

        // 应用失败重试时间间隔
        applicationSubmissionContext.setAttemptFailuresValidityInterval(30 * 1000L);
        // 日志聚合上下文
        // applicationSubmissionContext.setLogAggregationContext();

        // TODO 检查提交申请的资源上限，避免程序资源过载造成系统宕机

        // 最后提交开始正式运行设置好的任务
        yarnClient.submitApplication(applicationSubmissionContext);
    }

    private void addToLocalResources(FileSystem fs, String fileSrcPath,
                                     String fileDstPath, String appId, Map<String, LocalResource> localResources,
                                     String resources) throws IOException {
        String suffix =
                appName + "/" + appId + "/" + fileDstPath;
        Path dst =
                new Path(fs.getHomeDirectory(), suffix);
        if (fileSrcPath == null) {
            FSDataOutputStream ostream = null;
            try {
                ostream = FileSystem
                        .create(fs, dst, new FsPermission((short) 0710));
                ostream.writeUTF(resources);
            } finally {
                IOUtils.closeQuietly(ostream);
            }
        } else {
            fs.copyFromLocalFile(new Path(fileSrcPath), dst);
        }
        FileStatus scFileStatus = fs.getFileStatus(dst);
        LocalResource scRsrc =
                LocalResource.newInstance(
                        ConverterUtils.getYarnUrlFromURI(dst.toUri()),
                        LocalResourceType.FILE, LocalResourceVisibility.APPLICATION,
                        scFileStatus.getLen(), scFileStatus.getModificationTime());
        localResources.put(fileDstPath, scRsrc);
    }
}

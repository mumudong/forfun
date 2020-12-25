package mumudong;

public class ReadMe {
    /**
     *  查看mysql binlog日志
     *    show binary logs ;
     *
     *  查看最近的一个binlog日志
     *   show binlog events in 'mysql-bin.000047';
     *
     *
     *
     *
     *
     *
     */

    // mysql5.6.x引入gtid,用于优化主从同步机制
    // show global variables like 'gtid%' ;
    // gtid_excuted 当前mysql已执行过的事务,开启gtid模块时每执行一个事务会产生一个全局唯一的事务id
    // gtid_executed_compression_period mysql5.7引入一个系统表,mysql.gtid_executed 执行多少个事务对该表进行压缩
    // gtid_purged 不在binlog中的事务id,mysql不会永久存储binlog日志,保留日期
    /**
     * GTID的生成有自动递增与手动执行模式，自动递增模式可以在单个Server集群中保证有序，
     * 即GTID值越大，说明事务越后执行，但如果进行了人工干预，GTID就不是越大越先执行了
     * 人工干预
     * set gtid_next='1f0eee4c-a66e-11ea-8999-00dbdfe417b8:10';
     * show master status;
     */
    /**
     * binlog的寻找过程可能的场景如下：
     *   instance第一次启动
     *   发生数据库主备切换
     *   canal server HA情况下的切换
     *
     * 主从切换位点不一致问题
     *可以通过在主库上通过"flush logs"命令重新生成信息binlog，然后使用"show master status"查询信息位点，
     * 重新使用“CHANGE MASTER TO MASTER_LOG_FILE='log-bin.00000xx',MASTER_LOG_POS=xxx;”重新同步binlog。
     *
     * 主从同步时主删数据,而从库无此条记录报错,可跳过
     *stop slave;
     * set global sql_slave_skip_counter=1;
     * start slave;
     *
     *
     *
     *
     *
     */
}

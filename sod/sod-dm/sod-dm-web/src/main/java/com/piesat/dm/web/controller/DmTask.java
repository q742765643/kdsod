package com.piesat.dm.web.controller;


import com.piesat.dm.rpc.api.datatable.TableDataStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class DmTask {

    @Autowired
    private TableDataStatisticsService tableDataStatisticsService;

    /**
     * 统计最新数据库表信息
     */
    @Scheduled(cron="0 0 0/3 * * ?")
    private void countNewDatabaseInfo(){
        System.out.print("统计最新数据库信息");
        tableDataStatisticsService.countNewDatabaseInfo();
    }

    /**
     * 统计路径下所有目录及大小
     */
    @Scheduled(cron="0 10,40 * * * ?")
    private void countFilePathInfo(){
        System.out.print("统计路径信息");
        tableDataStatisticsService.countFilePathInfo();
    }
}

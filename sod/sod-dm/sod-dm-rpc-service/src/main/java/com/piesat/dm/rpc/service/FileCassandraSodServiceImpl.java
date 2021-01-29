package com.piesat.dm.rpc.service;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.piesat.dm.mapper.MybatisQueryMapper;
import com.piesat.dm.rpc.api.FileCassandraSodService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName : FileCassandraSodService
 * @Description :
 * @Author : zzj
 * @Date: 2021-01-27 17:33
 */
@Service
public class FileCassandraSodServiceImpl implements FileCassandraSodService {
    @Autowired
    private MybatisQueryMapper mybatisQueryMapper;
    @Override
    public String queryRecordNum(String schema, String tableName) throws Exception {
        String num = "";
        try {
           Map<String,Object> params=new HashMap<>();
           params.put("vtableName",tableName);
           Map<String,Object> result= mybatisQueryMapper.queryRecordNum(params);
           if(null!=result&&null!=result.get("COUNT")){
               num=String.valueOf(result.get("COUNT"));
           }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return num;
    }

    @Override
    public String queryMinTime(String schema, String tableName, String timeColumnName) throws Exception {
        String minTime = "";
        try {
            Map<String,Object> params=new HashMap<>();
            params.put("vtableName",tableName);
            Map<String,Object> result= mybatisQueryMapper.queryMinTime(params);
            if(null!=result&&null!=result.get("TIME")){
                minTime=String.valueOf(result.get("TIME"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return minTime;
    }

    @Override
    public String queryMaxTime(String schema, String tableName, String timeColumnName) throws Exception {
        String maxTime = "";
        try {
            Map<String,Object> params=new HashMap<>();
            params.put("vtableName",tableName);
            Map<String,Object> result= mybatisQueryMapper.queryMaxTime(params);
            if(null!=result&&null!=result.get("TIME")){
                maxTime=String.valueOf(result.get("TIME"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return maxTime;
    }

    @Override
    public String queryIncreCount(String schema, String tableName, String timeColumnName, String beginTime, String endTime) throws Exception {
        String num = "";
        try {
            Map<String,Object> params=new HashMap<>();
            params.put("vtableName",tableName);
            params.put("beginTime",beginTime);
            params.put("endTime",endTime);
            Map<String,Object> result= mybatisQueryMapper.queryIncreCount(params);
            if(null!=result&&null!=result.get("COUNT")){
                num=String.valueOf(result.get("COUNT"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("错误：" + e.getMessage());
        }
        return num;
    }
    @Override
    public String queryElapsedTime(String schema, String tableName, String timeColumnName, String beginTime, String endTime) throws Exception {
        String num = "";
        try {
            Map<String,Object> params=new HashMap<>();
            params.put("vtableName",tableName);
            params.put("beginTime",beginTime);
            params.put("endTime",endTime);
            Map<String,Object> result= mybatisQueryMapper.queryElapsedTime(params);
            if(null!=result&&null!=result.get("TIME")){
                num=String.valueOf(result.get("TIME"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("错误：" + e.getMessage());
        }
        return num;
    }

    public long queryCassandra(String dataSql){
        long num=0;
        try {
            Map<String,Object> result= mybatisQueryMapper.queryCassandra(dataSql);
            if(null!=result&&null!=result.get("NUM")){
                num= (long) result.get("NUM");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return num;
    }
}


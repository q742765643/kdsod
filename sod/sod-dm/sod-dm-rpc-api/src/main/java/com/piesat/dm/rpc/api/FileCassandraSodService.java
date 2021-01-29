package com.piesat.dm.rpc.api;

public interface FileCassandraSodService {
    public String queryRecordNum(String schema, String tableName) throws Exception;

    public String queryMinTime(String schema, String tableName, String timeColumnName) throws Exception;

    public String queryMaxTime(String schema, String tableName, String timeColumnName) throws Exception;

    public String queryIncreCount(String schema, String tableName, String timeColumnName, String beginTime, String endTime) throws Exception;

    public String queryElapsedTime(String schema, String tableName, String timeColumnName, String beginTime, String endTime) throws Exception;

    public long queryCassandra(String dataSql);
}

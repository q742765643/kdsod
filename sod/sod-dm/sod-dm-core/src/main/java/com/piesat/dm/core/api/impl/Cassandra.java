package com.piesat.dm.core.api.impl;

import com.datastax.driver.core.*;
import com.piesat.dm.core.api.DatabaseDcl;
import com.piesat.dm.core.model.Column;
import com.piesat.util.ResultT;
import org.apache.commons.lang.StringUtils;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Cassandra数据库管理
 *
 * @author cwh
 * @date 2020年 02月04日 16:11:43
 */
public class Cassandra implements DatabaseDcl {
    private Session instance = null;
    private Cluster cluster = null;
    private Lock lock = new ReentrantLock();

    public Cassandra() {
    }

    public Cassandra(String ip, int port, String username, String password, String keyspace) throws Exception {
        String[] ips = ip.split(",");
        if (null == instance) {
            lock.lock();
            try {
                if (null == instance) {
                    cluster = Cluster.builder()
                            .addContactPoints(ips).withPort(port)
                            .withCredentials(username, password)
                            .build();
                    if (StringUtils.isNotBlank(keyspace)) {
                        instance = cluster.connect(keyspace);
                    } else {
                        instance = cluster.connect();
                    }
                    // 也可以针对一个特定的keyspace获取一个session
                    // instance = cluster.connect("mycas");
                }
            } catch (Exception e) {
                closeConnect();
                e.printStackTrace();
                throw new Exception("Cassandra数据库连接失败！");
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    public void closeConnect() {
        Optional.ofNullable(this.instance).ifPresent(i -> i.close());
        Optional.ofNullable(this.cluster).ifPresent(c -> c.close());
    }

    @Override
    public void autoCommit(Boolean flag) {

    }

    @Override
    public void commit() {

    }

    @Override
    public void rollback() {

    }

    @Override
    public int getUserNum(String user) throws Exception {
        String cql = "LIST USERS;";
        ResultSet rs = instance.execute(cql);
        Iterator<Row> it = rs.iterator();
        while (it.hasNext()) {
            Row row = it.next();
            if (user.equals(row.getObject(0).toString())) {
                return 1;
            }
        }
        return 0;
    }

    @Override
    public void addUser(String identifier, String password, String[] ips) throws Exception {
//        int userNum = getUserNum(identifier);
//        if (userNum > 0) {
//            throw new Exception("数据库用户已经存在!");
//        }
        String cql = "CREATE USER " + identifier + " WITH PASSWORD '" + password + "' NOSUPERUSER";
        instance.execute(cql);
    }

    @Override
    public void deleteUser(String identifier, String ip) {
        String cql = "DROP USER IF EXISTS " + identifier;
        instance.execute(cql);
    }

    @Override
    public void deleteUser(String identifier) throws Exception {
        String cql = "DROP USER IF EXISTS " + identifier;
        instance.execute(cql);
    }

    @Override
    public void addEnable(String identifier, String resource, List<String> ips, int type) {

    }

    @Override
    public void deleteEnable(String identifier, String resource, List<String> ips, int type) {

    }

    @Override
    public void addPermissions(Boolean select, String resource, String tableName, String identifier, String password, List<String> ips) throws Exception {
        String permission = select ? "SELECT" : "MODIFY,SELECT";
        String[] split = permission.split(",");
        try {
            for (String s : split) {
                String cql = "GRANT " + s + " ON TABLE " + resource + "." + tableName + " To '" + identifier + "'";
                instance.execute(cql);
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }


    }

    @Override
    public void deletePermissions(String[] permissions, String resource, String tableName, String identifier, String password, List<String> ips) throws Exception {
        try {
            for (String s : permissions) {
                String cql = "REVOKE " + s + " ON " + resource + "." + tableName + " FROM '" + identifier + "'";
                instance.execute(cql);
            }
        } catch (Exception e) {
            throw new Exception("撤销Cassandra数据库授权失败！errInfo：" + e.getMessage());
        }
    }

    @Override
    public void createSchemas(String schemaName, String dataBaseUser, String password, boolean dataAuthor, boolean creatAuthor, boolean dropAuthor, List<String> ips) throws Exception {
        try {
            String cql = "CREATE KEYSPACE IF NOT EXISTS " + schemaName + " WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 4}";
            instance.execute(cql);
            if (dataAuthor) {
                cql = "GRANT MODIFY ON KEYSPACE " + schemaName + " TO '" + dataBaseUser + "'";
                instance.execute(cql);
            }
            if (creatAuthor) {
                cql = "GRANT CREATE ON KEYSPACE " + schemaName + " TO '" + dataBaseUser + "'";
                instance.execute(cql);
                cql = "GRANT ALTER ON KEYSPACE " + schemaName + " TO '" + dataBaseUser + "'";
                instance.execute(cql);
            }
            if (dropAuthor) {
                cql = "GRANT DROP ON KEYSPACE " + schemaName + " TO '" + dataBaseUser + "'";
                instance.execute(cql);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("检查Cassandra数据库账户是否存在，errInfo：" + e.getMessage());
        }
    }

    @Override
    public void dropSchemas(String schemaName) {
        String cql = "DROP KEYSPACE " + schemaName;
        instance.execute(cql);
    }

    @Override
    public ResultT createTable(String sql, String tableName, Boolean deleteOld) {
        try {
            String cql = "DROP TABLE IF EXISTS " + tableName;
            if (deleteOld) {
                instance.execute(cql);
            }
            instance.execute(sql);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultT.failed(e.getMessage());
        }
        return ResultT.success();
    }

    @Override
    public ResultT existTable(String schema, String tableName) {
        String cql = "SELECT * FROM " + tableName + " LIMIT 10";
        try {
            ResultSet rs = instance.execute(cql);
            Iterator<Row> it = rs.iterator();
            int no = 0;
            while (it.hasNext()) {
                it.next();
                no++;
            }
            if (no > 0) {
                return ResultT.success(true);
            } else {
                return ResultT.success(false);
            }
        } catch (Exception e) {
            if (e.getMessage().contains("unconfigured table")) {
                return ResultT.success(false);
            } else {
                e.printStackTrace();
                return ResultT.failed(e.getMessage());
            }
        }
    }

    @Override
    public ResultT updateAccount(String dataBaseUser, String newPassword) {
        String cql = "ALTER USER " + dataBaseUser + " WITH PASSWORD '" + newPassword + "'";
        try {
            instance.execute(cql);
        } catch (Exception e) {
            e.printStackTrace();
            if (!e.getMessage().contains("新口令不能与老口令相同")) {
                return ResultT.failed(e.getMessage());
            }
        }
        return ResultT.success();
    }


    @Override
    public ResultT queryData(String schema, String tableName, List<String> column, int pageNum,int pageSize) throws Exception {
        column.remove("data");
        column.remove("DATA");
        String columns = String.join(",", column);
        String sql = "SELECT " + columns + " FROM " + schema + "." + tableName + " limit " +pageSize;
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        try {
            ResultSet rs = instance.execute(sql);
            for (Row r : rs) {
                Map<String, Object> rowData = new HashMap<String, Object>();
                for (int i = 1; i <= column.size(); i++) {
                    rowData.put(column.get(i - 1), r.getObject(i-1));
                }
                list.add(rowData);
            }
        } catch (Exception e) {
            e.printStackTrace();
//            throw new Exception("数据查询异常：请在对应物理库内创建表结构"+tableName);
        }
        return ResultT.success(list);
    }

    @Override
    public ResultT queryAllTableName(String schema) throws Exception {
        List<String> list = new ArrayList<String>();
        String sql = "select table_name from system_schema.tables where keyspace_name='" + schema.toLowerCase() + "'";
        try {

            ResultSet rs = instance.execute(sql);
            for (Row r : rs) {
                list.add(r.getString("table_name").toUpperCase());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResultT.failed(e.getMessage());
        }
        return ResultT.success(list);
    }

    @Override
    public ResultT queryAllColumnInfo(String schema, String tableName) throws Exception {
        return null;
    }

    @Override
    public ResultT queryAllIndexAndShardingInfo(String schema, String tableName) throws Exception {
        return null;
    }

    @Override
    public ResultT updateColumn(String schema, String tableName, Column oldColumn, Column newColumn) {
        return null;
    }

    @Override
    public void bindIp(String identifier, String[] ips) throws Exception {

    }

    @Override
    public String queryRecordNum(String schema, String tableName) throws Exception {
        String num = "";
   /*     String sql = "SELECT count(*) as count from " + schema + "." + tableName;
        try {
            ResultSet rs = instance.execute(sql);
            for (Row r : rs) {
                num=String.valueOf(r.getObject("count"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("错误：" + e.getMessage());
        }*/
        return num;
    }

    @Override
    public String queryMinTime(String schema, String tableName, String timeColumnName) throws Exception {
        String minTime = "";
        String sql = "SELECT MIN(time) as time FROM " + schema + "." + tableName;
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            ResultSet rs = instance.execute(sql);
            for (Row r : rs) {
               long time=r.getLong("time");
                minTime=simpleDateFormat.format(time);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("错误：" + e.getMessage());
        }
        return minTime;
    }

    @Override
    public String queryMaxTime(String schema, String tableName, String timeColumnName) throws Exception {
        String maxTime = "";
        String sql = "SELECT MAX(time) as time FROM " + schema + "." + tableName;
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            ResultSet rs = instance.execute(sql);
            for (Row r : rs) {
                long time=r.getLong("time");
                maxTime=simpleDateFormat.format(time);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("错误：" + e.getMessage());
        }
        return maxTime;
    }

    @Override
    public String queryIncreCount(String schema, String tableName, String timeColumnName, String beginTime, String endTime) throws Exception {
        String num = "";
        try {
            SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            long startTime=simpleDateFormat.parse(beginTime).getTime();
            long endTime1=simpleDateFormat.parse(endTime).getTime();
            String sql = "SELECT COUNT(time) as count FROM " + schema + "." + tableName + " WHERE time>=" + startTime + " AND time<" + endTime1  + " allow filtering";

            ResultSet rs = instance.execute(sql);
            for (Row r : rs) {
                num = String.valueOf(r.getObject("count"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("错误：" + e.getMessage());
        }
        return num;
    }

    @Override
    public List<Map<String,Object>> querySql(List<String> column, String querySql) throws Exception {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        try {
            ResultSet rs = instance.execute(querySql);
            for (Row r : rs) {
                Map<String, Object> rowData = new HashMap<String, Object>();
                for (int i = 1; i <= column.size(); i++) {
                    rowData.put(column.get(i - 1), r.getObject(i-1));
                }
                list.add(rowData);
            }
        } catch (Exception e) {
            e.printStackTrace();
//            throw new Exception("数据查询异常：请在对应物理库内创建表结构"+tableName);
        }
        return list;
    }
    public Map<String,Object> querySqlLimit(List<String> column, String querySql,String pageState,int pageSize) throws Exception {
        Map<String,Object> map=new HashMap<>();

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        try {
            Statement statement = new SimpleStatement(querySql);
            statement.setFetchSize(pageSize);
            if (pageState != null) {
                statement.setPagingState( PagingState.fromString(pageState));
            }
            ResultSet rs = instance.execute(statement);
            int remaining = rs.getAvailableWithoutFetching();
            System.out.println("remaining " + remaining);
            for (Row r : rs) {
                Map<String, Object> rowData = new HashMap<String, Object>();
                for (int i = 1; i <= column.size(); i++) {
                    rowData.put(column.get(i - 1), r.getObject(i-1));
                }
                list.add(rowData);
                if (--remaining == 0) {
                    break;
                }
            }
            map.put("data",list);
            if(null!=rs.getExecutionInfo().getPagingState()){
                map.put("pageState",rs.getExecutionInfo().getPagingState().toString());
            }
         /*   ResultSet rs = instance.execute(querySql);
            for (Row r : rs) {
                Map<String, Object> rowData = new HashMap<String, Object>();
                for (int i = 1; i <= column.size(); i++) {
                    rowData.put(column.get(i - 1), r.getObject(i-1));
                }
                list.add(rowData);
            }*/
        } catch (Exception e) {
            e.printStackTrace();
//            throw new Exception("数据查询异常：请在对应物理库内创建表结构"+tableName);
        }

        return map;
    }

    @Override
    public boolean udpateSql(String updateSql) {
        try {
            instance.execute(updateSql);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean udpateSql(String updateSql, List<Object> list) {
        PreparedStatement prepared = instance.prepare(updateSql);
        //BoundStatement bound=prepared.bind();
        BoundStatement bound = prepared.bind(list.toArray());
        instance.execute(bound);
        return true;
    }

}

package com.piesat.sql;

import com.alibaba.fastjson.JSON;
import com.piesat.util.ResultT;
import io.swagger.annotations.Api;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.SQLExec;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.bouncycastle.cms.RecipientId.password;

/**
 * @program: sod
 * @description:
 * @author: zzj
 * @create: 2020-05-13 17:05
 **/
@RestController
@Api(value="导入数据sql===========",tags = {"导入数据sql==========="})
public class SqlController {
    private static Connection conn = null;
    private static Statement sm = null;
    //private static String schema = "BABJ_SMDB";//模式名
    private static String select = "SELECT * FROM";//查询sql
    private static String insert = "INSERT INTO";//插入sql
    private static String values = "VALUES";//values关键字
    private static String[] table = {""};//table数组
    private static List<String> insertList = new ArrayList<String>();//全局存放insertsql文件的数据
    private static String filePath = "/zzj/data/sql/";//绝对路径导出数据的文件

    @GetMapping("/api/initSql")
    public ResultT<String>  initSql(String filePath1,String url,String user,String pass,String model,String isDel) {
        filePath=filePath1;
        connectSQL("com.mysql.jdbc.Driver", url, user, pass);//连接数据库
        if("1".equals(isDel)){

            //List<Map<String, Object>> map=queryMapList(sql,null);
            try {
                ResultSet ts=conn.getMetaData().getTables(null,model,null,new String[]{"TABLE"});
                List<String> ll=new ArrayList<>();
                while (ts.next()) {
                  String tableName=ts.getString("TABLE_NAME");
                    /*  if(tableName.toUpperCase().indexOf("TAB_OMIN") == -1){

                    }*/
                    sm.executeUpdate("DELETE FROM "+tableName);


                }
            } catch (SQLException e) {
                e.printStackTrace();
            }


        }

        File[] files=new File(filePath).listFiles();

        for(File file:files){
            try {
                System.out.println(file.getPath());
                SQLExec sqlExec = new SQLExec();
                sqlExec.setDriver("com.mysql.jdbc.Driver");
                sqlExec.setUrl(url);
                sqlExec.setUserid(user);
                sqlExec.setPassword(pass);
                sqlExec.setEncoding("UTF8");
                sqlExec.setSrc(file);
                sqlExec.setOnerror((SQLExec.OnError) (EnumeratedAttribute.getInstance(SQLExec.OnError.class, "abort")));
                sqlExec.setProject(new Project()); // 要指定这个属性，不然会出错
                sqlExec.execute();
                //readFileByLines(file.getPath());
            } catch (Exception e) {

                e.printStackTrace();
                try {
                        readFileByLines(file.getPath());

                } catch (Exception e1) {
                    e1.printStackTrace();
                }

            }
        }
        System.out.println("结束");
        try {
            conn.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return new ResultT<>();
    }
    public static void main2(String[] args)  {
        String aa="'aaa'";
        aa=aa.replaceAll("'","''");
        System.out.println(aa);
        ResultT<String> resultT=new ResultT<>();
        resultT.setErrorMessage("源目录错误{}","111");
        System.out.println(resultT.getProcessMsg());
         //initSql();
    }


    /**
     * 导出数据库表*@paramargs *@throwsSQLException
     */
    public static void main(String[] args) throws SQLException {
        List<String> listSQL = new ArrayList<String>();
        connectSQL("com.xugu.cloudjdbc.Driver", "jdbc:xugu://10.40.17.34:5138/BABJ_SMDB?char_set=utf8", "USR_SOD", "Pnmic_qwe123");//连接数据库

        String sql="select A.TABLE_NAME   from   DBA_TABLES A,DBA_SCHEMAS B WHERE A.SCHEMA_ID = B.SCHEMA_ID AND B.SCHEMA_NAME='USR_SOD'";
        //List<Map<String, Object>> map=queryMapList(sql,null);
        ResultSet ts=conn.getMetaData().getTables(null,"USR_SOD",null,new String[]{"TABLE"});
        List<String> ll=new ArrayList<>();
        while (ts.next()) {
            String tableName=ts.getString("TABLE_NAME");
            if(!tableName.endsWith("LOG")){
                ll.add(tableName);
            }
            System.out.println(ts.getString("TABLE_NAME") + "  "
                    + ts.getString("TABLE_TYPE"));
        }
        table=ll.toArray(new String[ll.size()]);
        listSQL = createSQL();//创建查询语句
        executeSQL(conn, sm, listSQL);//执行sql并拼装


    }
    public static void main1(String[] args) throws SQLException {
        connectSQL("com.mysql.jdbc.Driver", "jdbc:postgresql://10.1.6.42:5432/soddb?currentSchema=\"USR_SOD\"", "soder", "soder123");//连接数据库

        String sql="select count(1) from usr_sod.t_sod_data_class where type = '2' and update_time <= '2020-05-14 09:56:41' ";
        List<Map<String, Object>> map=queryMapList(sql,null);
        System.out.println(JSON.toJSONString(map));



    }
    public static List<Map<String, Object>> queryMapList(String sql, List<Object> list) {
        List<Map<String, Object>> lists = new ArrayList<Map<String, Object>>();
        PreparedStatement preStmt = null;
        ResultSet rs = null;
        try {
            preStmt = conn.prepareStatement(sql);
            if (null != list && !list.isEmpty()) {
                for (int i = 0; i < list.size(); i++) {
                    // 下标从1开始
                    preStmt.setObject(i + 1, list.get(i));
                }
            }
            rs = preStmt.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();
            //列的个数
            int colCount = rsmd.getColumnCount();
            //存放列名
            List<String> colNameList = new ArrayList<String>();
            for (int i = 0; i < colCount; i++) {
                colNameList.add(rsmd.getColumnName(i + 1).toLowerCase());
            }
            while (null != rs && rs.next()) {
                Map<String, Object> map = new HashMap<String, Object>(10);
                for (int i = 0; i < colCount; i++) {
                    String key = colNameList.get(i);
                    String value = rs.getString(key);
                    map.put(key, value);
                }
                lists.add(map);
            }
        } catch (SQLException e) {
           e.printStackTrace();
        } finally {
        }
        return lists;
    }

    /**
     * 创建insertsql.txt并导出数据
     */
    private static void createFile(String tableName) {
        if(!new File(filePath).exists()){
            new File(filePath).mkdirs();
        }
        File file = new File(filePath+"/"+tableName+".sql");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.out.println("创建文件名失败！！");
                e.printStackTrace();
            }
        }
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            fw = new FileWriter(file);
            bw = new BufferedWriter(fw);
            if (insertList.size() > 0) {
                for (int i = 0; i < insertList.size(); i++) {
                    bw.append(insertList.get(i));
                    bw.append("\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bw.close();
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 拼装查询语句
     *
     * @return返回 select集合
     */
    private static List<String> createSQL() {
        List<String> listSQL = new ArrayList<String>();
        for (int i = 0; i < table.length; i++) {
            StringBuffer sb = new StringBuffer();
            sb.append(select).append(" ").append(table[i]);
            listSQL.add(sb.toString());
        }
        return listSQL;
    }

    /**
     * 连接数据库创建statement对象
     * *@paramdriver
     * *@paramurl
     * *@paramUserName
     * *@paramPassword
     */
    public static void connectSQL(String driver, String url, String UserName, String Password) {
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(url, UserName, Password);
            sm = conn.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 执行sql并返回插入sql
     *
     * @paramconn
     * @paramsm
     * @paramlistSQL *
     * @throwsSQLException
     */
    public static void executeSQL(Connection conn, Statement sm, List listSQL) throws SQLException {
        List<String> insertSQL = new ArrayList<String>();
        ResultSet rs = null;
        try {
            rs = getColumnNameAndColumeValue(sm, listSQL, rs);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            rs.close();
            sm.close();
            conn.close();
        }
    }

    /**
     * 获取列名和列值
     *
     * @return
     * @paramsm
     * @paramlistSQL
     * @paramrs
     * @throwsSQLException
     */
    private static ResultSet getColumnNameAndColumeValue(Statement sm, List listSQL, ResultSet rs) throws SQLException {
        if (listSQL.size() > 0) {
            for (int j = 0; j < listSQL.size(); j++) {
                String sql = String.valueOf(listSQL.get(j));
                rs = sm.executeQuery(sql);
                ResultSetMetaData rsmd = rs.getMetaData();
                int columnCount = rsmd.getColumnCount();
                while (rs.next()) {
                    StringBuffer ColumnName = new StringBuffer();
                    StringBuffer ColumnValue = new StringBuffer();
                    for (int i = 1; i <= columnCount; i++) {
                        String value = null;
                        try {
                            value = rs.getString(i).trim();
                            if ("".equals(value)) {
                                value = "";
                            }
                        } catch (Exception e) {
                            //e.printStackTrace();
                        }
                        if(value==null){
                            value="hthtnull";
                        }
                        value=value.replaceAll("'","''");
                        if (i == 1 || i == columnCount) {
                            if(i==columnCount){
                                ColumnName.append(",");
                            }
                            ColumnName.append(rsmd.getColumnName(i));
                            if( i== 1){
                                if (Types.CHAR == rsmd.getColumnType(i) || Types.VARCHAR == rsmd.getColumnType(i) || Types.LONGVARCHAR == rsmd.getColumnType(i)) {
                                    ColumnValue.append("'").append(value).append("',");
                                } else if (Types.SMALLINT == rsmd.getColumnType(i) || Types.INTEGER == rsmd.getColumnType(i) || Types.BIGINT == rsmd.getColumnType(i) || Types.FLOAT == rsmd.getColumnType(i) || Types.DOUBLE == rsmd.getColumnType(i) || Types.NUMERIC == rsmd.getColumnType(i) || Types.DECIMAL == rsmd.getColumnType(i)|| Types.TINYINT == rsmd.getColumnType(i)) {
                                    ColumnValue.append(value).append(",");
                                } else if (Types.DATE == rsmd.getColumnType(i) || Types.TIME == rsmd.getColumnType(i) || Types.TIMESTAMP == rsmd.getColumnType(i)) {
                                    ColumnValue.append("'").append(value).append("',");
                                } else {
                                    ColumnValue.append("'").append(value).append("',");
                                }
                            }else{
                                if (Types.CHAR == rsmd.getColumnType(i) || Types.VARCHAR == rsmd.getColumnType(i) || Types.LONGVARCHAR == rsmd.getColumnType(i)) {
                                    ColumnValue.append("'").append(value).append("'");
                                } else if (Types.SMALLINT == rsmd.getColumnType(i) || Types.INTEGER == rsmd.getColumnType(i) || Types.BIGINT == rsmd.getColumnType(i) || Types.FLOAT == rsmd.getColumnType(i) || Types.DOUBLE == rsmd.getColumnType(i) || Types.NUMERIC == rsmd.getColumnType(i) || Types.DECIMAL == rsmd.getColumnType(i)|| Types.TINYINT == rsmd.getColumnType(i)) {
                                    ColumnValue.append(value);
                                } else if (Types.DATE == rsmd.getColumnType(i) || Types.TIME == rsmd.getColumnType(i) || Types.TIMESTAMP == rsmd.getColumnType(i)) {
                                    ColumnValue.append("'").append(value).append("'");
                                } else {
                                    ColumnValue.append("'").append(value).append("'");
                                }
                            }

                        } else {
                            ColumnName.append("," + rsmd.getColumnName(i));
                            if (Types.CHAR == rsmd.getColumnType(i) || Types.VARCHAR == rsmd.getColumnType(i) || Types.LONGVARCHAR == rsmd.getColumnType(i)) {
                                ColumnValue.append("'").append(value).append("'").append(",");
                            } else if (Types.SMALLINT == rsmd.getColumnType(i) || Types.INTEGER == rsmd.getColumnType(i) || Types.BIGINT == rsmd.getColumnType(i) || Types.FLOAT == rsmd.getColumnType(i) || Types.DOUBLE == rsmd.getColumnType(i) || Types.NUMERIC == rsmd.getColumnType(i) || Types.DECIMAL == rsmd.getColumnType(i)|| Types.TINYINT == rsmd.getColumnType(i)) {
                                ColumnValue.append(value).append(",");
                            } else if (Types.DATE == rsmd.getColumnType(i) || Types.TIME == rsmd.getColumnType(i) || Types.TIMESTAMP == rsmd.getColumnType(i)) {
                                ColumnValue.append("'").append(value).append("',");
                            } else {
                                ColumnValue.append("'").append(value).append("',");
                            }
                        }
                    }
                   // System.out.println(ColumnName.toString());
                    System.out.println(ColumnValue.toString());
                    insertSQL(ColumnName, ColumnValue,table[j]);
                }
                createFile(table[j]);//创建文件
                insertList.clear();
            }

        }
        return rs;
    }

    /**
     * 拼装insertsql放到全局list里面
     * @paramColumnName
     * @paramColumnValue
     */
    private static void insertSQL(StringBuffer ColumnName, StringBuffer ColumnValue,String tableName) {
        StringBuffer insertSQL = new StringBuffer();
        insertSQL.append(insert).append(" ")
                .append(tableName).append("(").append(ColumnName.toString()).append(")").append(values).append("(").append(ColumnValue.toString().replaceAll("'hthtnull'","null")).append(")/n#htht#");
        insertList.add(insertSQL.toString().replaceAll("hthtnull","null"));



    }
    /**
     * 以行为单位读取文件，常用于读面向行的格式化文件
     */
    private static String readFileByLines(String filePath) throws Exception {
        StringBuffer str = new StringBuffer();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(filePath), "UTF-8"));
            String tempString = null;
            int line = 1;
            int ii=0;
            conn.setAutoCommit(false);
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                // 显示行号
                // System.out.println("line " + line + ": " + tempString);
                if(line>1&&tempString.toUpperCase().startsWith("INSERT")&&str.length()>0){
                    try {
                        sm.addBatch(str.toString().substring(0,str.toString().length()-1));
                        ii++;
                        if(ii>10000){
                            try {
                                sm.executeBatch();
                                conn.commit();
                            } catch (SQLException throwables) {
                                sm.clearBatch();
                                throwables.printStackTrace();
                            }
                            ii=0;
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    str=new StringBuffer();
                }
                str = str.append(" " + tempString);

                line++;

            }
            if(ii>0){
                try {
                    sm.executeBatch();
                    conn.commit();
                } catch (SQLException throwables) {
                    sm.clearBatch();
                    throwables.printStackTrace();
                }
            }

            if(str.length()>0){
                try {
                    sm.execute(str.toString());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }

        return str.toString();
    }
}


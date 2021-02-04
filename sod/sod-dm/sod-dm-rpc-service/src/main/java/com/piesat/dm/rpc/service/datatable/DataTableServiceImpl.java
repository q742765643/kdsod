package com.piesat.dm.rpc.service.datatable;

import com.alibaba.fastjson.JSONObject;
import com.piesat.common.MapUtil;
import com.piesat.common.config.DatabseType;
import com.piesat.common.jpa.BaseDao;
import com.piesat.common.jpa.BaseService;
import com.piesat.common.utils.StringUtils;
import com.piesat.dm.core.api.DatabaseDcl;
import com.piesat.dm.core.api.impl.Cassandra;
import com.piesat.dm.core.parser.DatabaseInfo;
import com.piesat.dm.dao.database.DatabaseDao;
import com.piesat.dm.dao.dataclass.DataClassDao;
import com.piesat.dm.dao.dataclass.DataLogicDao;
import com.piesat.dm.dao.datatable.*;
import com.piesat.dm.entity.database.DatabaseEntity;
import com.piesat.dm.entity.dataclass.DataClassEntity;
import com.piesat.dm.entity.dataclass.DataLogicEntity;
import com.piesat.dm.entity.datatable.*;
import com.piesat.dm.mapper.MybatisQueryMapper;
import com.piesat.dm.rpc.api.FileCassandraSodService;
import com.piesat.dm.rpc.api.StorageConfigurationService;
import com.piesat.dm.rpc.api.dataapply.NewdataApplyService;
import com.piesat.dm.rpc.api.datatable.DataTableService;
import com.piesat.dm.rpc.dto.StorageConfigurationDto;
import com.piesat.dm.rpc.dto.dataapply.NewdataApplyDto;
import com.piesat.dm.rpc.dto.database.DatabaseDto;
import com.piesat.dm.rpc.dto.datatable.DataTableDto;
import com.piesat.dm.rpc.dto.datatable.SampleData;
import com.piesat.dm.rpc.dto.datatable.TableIndexDto;
import com.piesat.dm.rpc.dto.datatable.TableSqlDto;
import com.piesat.dm.rpc.mapper.datatable.DataTableMapper;
import com.piesat.dm.rpc.mapper.database.DatabaseMapper;
import com.piesat.dm.rpc.mapper.datatable.TableForeignKeyMapper;
import com.piesat.dm.rpc.util.DatabaseUtil;
import com.piesat.ucenter.rpc.dto.system.UserDto;
import com.piesat.util.ResultT;
import com.piesat.util.page.PageBean;
import com.piesat.util.page.PageForm;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 表信息
 *
 * @author cwh
 * @date 2019年 11月22日 16:34:17
 */
@Service
public class DataTableServiceImpl extends BaseService<DataTableEntity> implements DataTableService {
    @Autowired
    private DataTableDao dataTableDao;
    @Autowired
    private DataTableMapper dataTableMapper;
    @Autowired
    private ShardingDao shardingDao;
    @Autowired
    private DatabaseDao databaseDao;
    @Autowired
    private DatabaseMapper databaseMapper;
    @Autowired
    private DatabaseSqlService databaseSqlService;
    @Autowired
    private DatabaseInfo databaseInfo;
    @Autowired
    private MybatisQueryMapper mybatisQueryMapper;
    @Autowired
    private DataLogicDao dataLogicDao;
    @Autowired
    private DataClassDao dataClassDao;
    @Autowired
    private TableColumnDao tableColumnDao;
    @Autowired
    private TableIndexDao tableIndexDao;
    @Autowired
    private TableForeignKeyDao tableForeignKeyDao;
    @Autowired
    private NewdataApplyService newdataApplyService;
    @Autowired
    private StorageConfigurationService storageConfigurationService;
    @Autowired
    private TableForeignKeyMapper tableForeignKeyMapper;
    @Autowired
    private FileCassandraSodService fileCassandraSodService;


    @Override
    public BaseDao<DataTableEntity> getBaseDao() {
        return dataTableDao;
    }

    @Override
    @Transactional
    public DataTableDto saveDto(DataTableDto dataTableDto) {
        if (dataTableDto.getId() != null) {
            DataTableDto dotById = this.getDotById(dataTableDto.getId());
            String dataClassId = dataTableDto.getClassLogic().getDataClassId();
            List<NewdataApplyDto> NewdataApplyDtos = this.newdataApplyService
                    .findByDataClassIdAndUserId(dataClassId, dotById.getUserId());
            if (NewdataApplyDtos.size() > 0) {
                NewdataApplyDto newdataApplyDto = NewdataApplyDtos.get(0);
                newdataApplyDto.setTableName(dataTableDto.getTableName());
                this.newdataApplyService.saveDto(newdataApplyDto);
            }
        }

        DataTableEntity dataTableEntity = this.dataTableMapper.toEntity(dataTableDto);
        UserDto loginUser = (UserDto) SecurityUtils.getSubject().getPrincipal();
        dataTableEntity.setCreator(loginUser.getUserName());
        dataTableEntity = this.saveNotNull(dataTableEntity);
        List<StorageConfigurationDto> sc = this.storageConfigurationService.findByClassLogicId(dataTableEntity.getClassLogic().getId());
        if (sc != null && sc.size() > 0) {
            StorageConfigurationDto storageConfigurationDto = sc.get(0);
            storageConfigurationDto.setStorageDefineIdentifier(1);
            this.storageConfigurationService.updateDataAuthorityConfig(storageConfigurationDto);
        } else {
            StorageConfigurationDto storageConfigurationDto = new StorageConfigurationDto();
            storageConfigurationDto.setClassLogicId(dataTableEntity.getClassLogic().getId());
            storageConfigurationDto.setStorageDefineIdentifier(1);
            storageConfigurationDto.setSyncIdentifier(2);
            storageConfigurationDto.setCleanIdentifier(2);
            storageConfigurationDto.setMoveIdentifier(2);
            storageConfigurationDto.setBackupIdentifier(2);
            storageConfigurationDto.setArchivingIdentifier(2);
            this.storageConfigurationService.saveDto(storageConfigurationDto);
        }
        return this.dataTableMapper.toDto(dataTableEntity);
    }

    @Override
    public List<DataTableDto> all() {
        List<DataTableEntity> all = this.getAll();
        return this.dataTableMapper.toDto(all);
    }

    @Override
    public DataTableDto getDotById(String id) {
        DataTableEntity dataTableEntity = this.getById(id);
        return this.dataTableMapper.toDto(dataTableEntity);
    }

    @Override
    public void deleteByClassLogicId(String classLogicId) {
        this.dataTableDao.deleteByClassLogic_Id(classLogicId);
    }

    @Override
    public List<DataTableDto> getByDatabaseIdAndClassId(String databaseId, String dataClassId) {
        List<DataTableEntity> tableEntities = this.dataTableDao.getByDatabaseIdAndClassId(databaseId, dataClassId);
        return this.dataTableMapper.toDto(tableEntities);
    }

    @Override
    public List<Map<String, Object>> getByDatabaseId(String databaseId) {
        //List<Map<String, Object>> list = new ArrayList<Map<String, Object>>(this.dataTableDao.getByDatabaseId(databaseId));
        String sql = "select A.* ,B.data_class_id,B.storage_type from T_SOD_DATA_TABLE A,T_SOD_DATA_LOGIC B where A.class_logic_id=B.id and B.database_id ='" + databaseId + "'";
        List<Map<String, Object>> list = this.queryByNativeSQL(sql);
        List<Map<String, Object>> maps = MapUtil.transformMapList(list);
        return maps;
    }

    @Override
    public List<Map<String, Object>> findByUserId(String userId) {
        List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
        if ("mysql".equals(DatabseType.type.toLowerCase())) {
            resultList = mybatisQueryMapper.getInfoByUserIdMysql(userId);
        } else {
            resultList = mybatisQueryMapper.getInfoByUserId(userId);
        }
        for (Map<String, Object> map : resultList) {
            map.put("id", map.get("ID"));
            map.put("pId", map.get("PID"));
            map.put("name", map.get("NAME"));
        }
        return resultList;
    }

    @Override
    public List<Map<String, Object>> getByClassId(String dataClassId) {
        return mybatisQueryMapper.getTableInfoByClassId(dataClassId);
    }

    @Override
    public List<Map<String, Object>> getMultiDataInfoByClassId(String dataClassId) {
        //根据存储编码查询表信息
        List<Map<String, Object>> tableInfoLists = mybatisQueryMapper.getTableInfoByClassId(dataClassId);
        if (tableInfoLists != null && tableInfoLists.size() > 0) {
            for (int i = 0; i < tableInfoLists.size(); i++) {
                Map<String, Object> tableInfo = tableInfoLists.get(i);

                //根据表ID查询表实体
                DataTableDto dataTableDto = this.getDotById(String.valueOf(tableInfo.get("ID")));

                //索引涉及到的字段
                List<String> indexField = new ArrayList<String>();
                JSONObject indexFieldJson = new JSONObject();
                LinkedHashSet<TableIndexDto> tableIndexList = dataTableDto.getTableIndexList();
                if (tableIndexList != null && tableIndexList.size() > 0) {
                    for (TableIndexDto tableIndexDto : tableIndexList) {
                        String indexColumn = tableIndexDto.getIndexColumn();
                        for (String column : indexColumn.split(",")) {
                            if (!indexField.contains(column)) {
                                indexField.add(column);
                                indexFieldJson.put(String.format("index_field%d", indexField.size()), column);
                            }
                        }
                    }
                }

                tableInfo.put("table_structure", dataTableDto.getColumns());//表字段信息
                tableInfo.put("table_index1", tableIndexList);//索引信息
                tableInfo.put("table_index2", indexFieldJson);
                tableInfo.put("table_index3", indexField);
            }
        }
        return tableInfoLists;
    }

    @Override
    public List<DataTableDto> getByClassLogicId(String classLogic) {
        List<DataTableEntity> tableEntities = this.dataTableDao.findByClassLogicId(classLogic);
        return this.dataTableMapper.toDto(tableEntities);
    }

    @Override
    public int updateById(DataTableDto dataTableDto) {
        return dataTableDao.updateById(dataTableDto.getTableName(), dataTableDto.getId());
    }


    @Override
    public ResultT getOverview(String databaseId, String dataClassId) {
        List<DataTableEntity> tableEntities = this.dataTableDao.getByDatabaseIdAndClassId(databaseId, dataClassId);
        if (tableEntities == null || tableEntities.size() == 0) {
            return ResultT.failed("没有适应表");
        } else {
            Map<String, Object> map = new HashMap<>();
            DatabaseEntity databaseEntity = this.databaseDao.findById(databaseId).get();
            DataTableEntity keyTable = null;
            DataTableEntity eleTable = null;
            if (tableEntities.size() == 1) {
                keyTable = tableEntities.get(0);
            } else {
                if ("K".equals(tableEntities.get(0).getDbTableType().toUpperCase())) {
                    keyTable = tableEntities.get(0);
                    eleTable = tableEntities.get(1);
                } else {
                    keyTable = tableEntities.get(1);
                    eleTable = tableEntities.get(0);
                }
            }
            map.put("K", keyTable == null ? "" : keyTable.getTableName());
            map.put("E", eleTable == null ? "" : eleTable.getTableName());
            List<TableForeignKeyEntity> foreignKeyEntities = this.tableForeignKeyDao.findByClassLogicId(keyTable.getClassLogic().getId());
            if (foreignKeyEntities.size() > 0) {
                map.put("foreignKey", this.tableForeignKeyMapper.toDto(foreignKeyEntities));
            }
            List<TableColumnEntity> primaryKey = this.tableColumnDao.findByTableIdAndIsPrimaryKeyTrue(keyTable.getId());
            if (primaryKey.size() > 0) {
                map.put("primaryKey", primaryKey.get(0).getDbEleCode());
            }
            map.put("database", this.databaseMapper.toDto(databaseEntity));
            DataClassEntity dataClass = this.dataClassDao.findByDataClassId(keyTable.getClassLogic().getDataClassId());
            map.put("D_DATA_ID", dataClass.getDDataId());
            map.put("CLASSNAME", dataClass.getClassName());
            return ResultT.success(map);
        }
    }

    @Override
    public ResultT getSampleData(SampleData sampleData) throws Exception {
        DatabaseEntity databaseEntity = this.databaseDao.findById(sampleData.getDatabaseId()).get();
        DatabaseDto databaseDto = databaseMapper.toDto(databaseEntity);
        DatabaseDcl database = null;
        ResultT r = new ResultT();
        try {
            database = DatabaseUtil.getDatabase(databaseDto, databaseInfo);
            r = database.queryData(databaseDto.getSchemaName(), sampleData.getTableName(), sampleData.getColumn(), 1,10);
            database.closeConnect();
        } catch (Exception e) {
        } finally {
            if (database != null) {
                database.closeConnect();
            }
        }
        return r;
    }

    @Override
    public List<Map<String, Object>> getByDatabaseIdAndTableName(String databaseId, String tableName) {
        return mybatisQueryMapper.getByDatabaseIdAndTableName(databaseId, tableName);
    }

    @Override
    @Transactional
    public ResultT paste(String copyId, String pasteId) {
        List<DataTableEntity> copys = this.dataTableDao.findByClassLogicId(copyId);
        DataLogicEntity paste = this.dataLogicDao.getOne(pasteId);
        List<DataTableEntity> pDataTableEntitys = this.dataTableDao.findByClassLogicId(pasteId);
        for (DataTableEntity pd : pDataTableEntitys) {
            this.shardingDao.deleteByTableId(pd.getId());
            this.tableColumnDao.deleteByTableId(pd.getId());
            this.tableIndexDao.deleteByTableId(pd.getId());
            this.delete(pd.getId());
        }

        DataClassEntity dataClassEntity = this.dataClassDao.findByDataClassId(paste.getDataClassId());
        for (DataTableEntity copy : copys) {
            DataTableEntity dte = new DataTableEntity();
            BeanUtils.copyProperties(copy, dte);
            List<ShardingEntity> shardingEntities = this.shardingDao.findByTableId(dte.getId());
            dte.setClassLogic(paste);
            dte.setDataServiceId(dataClassEntity.getDataClassId());
            dte.setDataServiceName(dataClassEntity.getClassName());
            dte.setNameCn(dataClassEntity.getClassName());
            dte.setCreateTime(new Date());
            dte.setId(null);
            dte.setCreateTime(new Date());
            dte.setVersion(0);
            Set<TableColumnEntity> columns = dte.getColumns();
            Set<TableColumnEntity> cl = new LinkedHashSet();
            for (TableColumnEntity c : columns) {
                TableColumnEntity cc = new TableColumnEntity();
                BeanUtils.copyProperties(c, cc);
                cc.setVersion(0);
                String id = UUID.randomUUID().toString();
                cc.setId(null);
                cc.setCreateTime(new Date());
                cl.add(cc);
            }
            dte.setUserId(dataClassEntity.getCreateBy());
            dte.setColumns(cl);
            Set<TableIndexEntity> tableIndexList = dte.getTableIndexList();
            Set<TableIndexEntity> til = new LinkedHashSet();
            for (TableIndexEntity index : tableIndexList) {
                TableIndexEntity te = new TableIndexEntity();
                BeanUtils.copyProperties(index, te);
                String id = UUID.randomUUID().toString();
                te.setId(null);
                te.setCreateTime(new Date());
                te.setVersion(0);
                til.add(te);
            }
            dte.setTableIndexList(til);
            DataTableEntity save = this.dataTableDao.saveNotNull(dte);

            List<StorageConfigurationDto> sc = this.storageConfigurationService.findByClassLogicId(save.getClassLogic().getId());
            if (sc != null && sc.size() > 0) {
                StorageConfigurationDto storageConfigurationDto = sc.get(0);
                storageConfigurationDto.setStorageDefineIdentifier(1);
                this.storageConfigurationService.updateDataAuthorityConfig(storageConfigurationDto);
            } else {
                StorageConfigurationDto scd = new StorageConfigurationDto();
                scd.setClassLogicId(save.getClassLogic().getId());
                scd.setStorageDefineIdentifier(1);
                scd.setSyncIdentifier(2);
                scd.setCleanIdentifier(2);
                scd.setMoveIdentifier(2);
                scd.setBackupIdentifier(2);
                scd.setArchivingIdentifier(2);
                this.storageConfigurationService.saveDto(scd);
            }

            for (ShardingEntity se : shardingEntities) {
                ShardingEntity sde = new ShardingEntity();
                BeanUtils.copyProperties(se, sde);
                sde.setTableId(save.getId());
                String id = UUID.randomUUID().toString();
                sde.setId(null);
                sde.setCreateTime(new Date());
                sde.setVersion(0);
                this.shardingDao.saveNotNull(sde);
            }

        }
        List<NewdataApplyDto> NewdataApplyDtos = this.newdataApplyService
                .findByDataClassIdAndUserId(dataClassEntity.getDataClassId(), dataClassEntity.getCreateBy());
        if (NewdataApplyDtos.size() > 0 && copys.size() > 0) {
            NewdataApplyDto newdataApplyDto = NewdataApplyDtos.get(0);
            newdataApplyDto.setTableName(copys.get(0).getTableName());
            this.newdataApplyService.saveDto(newdataApplyDto);
        }
        return ResultT.success();
    }

    @Override
    public ResultT createTable(TableSqlDto tableSqlDto) {
        DatabaseEntity databaseEntity = this.databaseDao.findById(tableSqlDto.getDatabaseId()).get();
        DatabaseDto databaseDto = databaseMapper.toDto(databaseEntity);
        DatabaseDcl database = null;
        try {
            database = DatabaseUtil.getDatabase(databaseDto, databaseInfo);
            String tableName = databaseDto.getSchemaName() + "." + tableSqlDto.getTableName();
            ResultT t = database.createTable(tableSqlDto.getTableSql(), tableName, tableSqlDto.getDelOld());
            database.closeConnect();
            if (t.getCode() != 200) {
                return ResultT.failed(t.getMsg());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResultT.failed(e.getMessage());
        } finally {
            if (database != null) {
                database.closeConnect();
            }
        }
        return ResultT.success();
    }

    @Override
    public ResultT existTable(TableSqlDto tableSqlDto) {
        DatabaseEntity databaseEntity = this.databaseDao.findById(tableSqlDto.getDatabaseId()).get();
        DatabaseDto databaseDto = databaseMapper.toDto(databaseEntity);
        DatabaseDcl database = null;
        try {
            database = DatabaseUtil.getDatabase(databaseDto, databaseInfo);
            ResultT resultT = database.existTable(databaseDto.getSchemaName(), tableSqlDto.getTableName());
            database.closeConnect();
            return resultT;
        } catch (Exception e) {
            database.closeConnect();
            e.printStackTrace();
            return ResultT.failed(e.getMessage());
        } finally {
            if (database != null) {
                database.closeConnect();
            }
        }
    }

    @Override
    public List<DataTableDto> findByTableNameAndDatabaseIdAndDataclassId(String tableName, String databaseId, String dataclassId) {
        List<DataTableEntity> dataTables = this.dataTableDao.findByTableNameAndClassLogic_DatabaseIdAndClassLogic_DataClassId(tableName, databaseId, dataclassId);
        return this.dataTableMapper.toDto(dataTables);
    }

    @Override
    public PageBean dataSearchPage(PageForm<Map<String, Object>> pageForm){
        PageBean page = new PageBean();
        try{
            Map<String,Object> param = pageForm.getT();
            DatabaseEntity databaseEntity = this.databaseDao.findById(param.get("databaseId").toString()).get();
            DatabaseDto databaseDto = databaseMapper.toDto(databaseEntity);
            DatabaseDcl database = null;
            try {
                database = DatabaseUtil.getDatabase(databaseDto, databaseInfo);

                int pageSize = pageForm.getPageSize();
                int pageNum = pageForm.getCurrentPage();
                String schema = databaseDto.getSchemaName();
                if(schema.equals(schema.toUpperCase())){
                    schema="\""+schema+"\"";
                }
                if(param.get("tableName")==null){
                    return null;
                }
                String tableName = param.get("tableName").toString();
                Map<String,String> colType= (Map<String, String>) param.get("colType");
                //过滤条件
                StringBuilder builderWhere = new StringBuilder();
                /*if(!"Cassandra".equals(databaseDto.getDatabaseDefine().getDatabaseType())){
                    builderWhere.append(" where 1=1 ");

                }*/
                List<String> types=new ArrayList<>();
                types.add("numeric");
                types.add("smallint");
                types.add("double");
                types.add("float");
                types.add("int");
                types.add("bigint");
                types.add("decimal");
                types.add("int4");
                List<SampleData.CheckParam> checkParamList = (List<SampleData.CheckParam>)param.get("checkList");
                if(checkParamList!=null&&checkParamList.size()>0){
                    builderWhere.append(" where ");
                    boolean flag=false;
                    for(SampleData.CheckParam checkParam : checkParamList){
                        if(StringUtils.isNotEmpty(checkParam.getValue())){
                            if(checkParam.getSymbol().equalsIgnoreCase("LIKE")){
                                if(!flag){
                                    builderWhere.append(" "+checkParam.getProp()+" "+checkParam.getSymbol()+" '%"+checkParam.getValue()+"%' ");

                                }else {
                                    builderWhere.append(" and "+checkParam.getProp()+" "+checkParam.getSymbol()+" '%"+checkParam.getValue()+"%' ");

                                }
                            }else{
                                if(!flag) {
                                    if(types.contains(colType.get(checkParam.getProp()))){
                                        builderWhere.append(" " + checkParam.getProp() + " " + checkParam.getSymbol() + "" + checkParam.getValue() + "");

                                    }else {
                                        builderWhere.append(" " + checkParam.getProp() + " " + checkParam.getSymbol() + " '" + checkParam.getValue() + "' ");

                                    }
                                }
                                else {
                                    if(types.contains(colType.get(checkParam.getProp()))){
                                        builderWhere.append(" and " + checkParam.getProp() + " " + checkParam.getSymbol() + "" + checkParam.getValue() + "");

                                    }else {
                                        builderWhere.append(" and " + checkParam.getProp() + " " + checkParam.getSymbol() + " '" + checkParam.getValue() + "' ");

                                    }
                                }
                            }
                            flag =true;
                        }
                    }
                    if(!flag){
                        builderWhere=new StringBuilder();
                    }
                }
                System.out.println("表名称："+tableName);
                //查询数据
                List<String> columnList = (List)(param.get("columnList"));
                if("Cassandra".equals(databaseDto.getDatabaseDefine().getDatabaseType())){
                    columnList.remove("data");
                    columnList.remove("DATA");
                }
                String columns = String.join(",", columnList);
                String dataSql = "SELECT " + columns + " FROM "+schema+"."+tableName
                        +builderWhere.toString()+" limit "+pageSize+" offset "+(pageNum - 1)*pageSize;
                if("Cassandra".equals(databaseDto.getDatabaseDefine().getDatabaseType())){
                    dataSql = "SELECT " + columns + " FROM "+schema+"."+tableName
                            +builderWhere.toString()+ " allow filtering";
                }
                System.out.println("查询语句："+dataSql);
                List<Map<String,Object>> dataList=new ArrayList<>();
                if("Cassandra".equals(databaseDto.getDatabaseDefine().getDatabaseType())){
                    Cassandra cassandra= (Cassandra) database;
                    Map<String,Object> map=cassandra.querySqlLimit(columnList,dataSql,pageForm.getOrderBy(),pageSize);
                    dataList= (List<Map<String, Object>>) map.get("data");
                    page.setPageState((String) map.get("pageState"));

                }else {
                    dataList = database.querySql(columnList, dataSql);
                }
                //查询数据条数
                List<String> countColumnList = new ArrayList<>();
                countColumnList.add("num");
                List<Map<String,Object>> countList=new ArrayList<>();
                if("Cassandra".equals(databaseDto.getDatabaseDefine().getDatabaseType())){
                    builderWhere=new StringBuilder();
                    if(checkParamList!=null&&checkParamList.size()>0){
                        builderWhere.append(" where ");
                        boolean flag=false;
                        colType.put("time","varchar");
                        for(SampleData.CheckParam checkParam : checkParamList){
                            if(StringUtils.isNotEmpty(checkParam.getValue())){
                                if("time".equals(checkParam.getProp())){
                                    SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    long value=Long.parseLong(checkParam.getValue().toString());
                                    checkParam.setValue(format.format(value));
                                }
                                if(!flag) {
                                    if(types.contains(colType.get(checkParam.getProp()))){
                                        builderWhere.append(" " + checkParam.getProp() + " " + checkParam.getSymbol() + "" + checkParam.getValue() + "");

                                    }else {
                                        builderWhere.append(" " + checkParam.getProp() + " " + checkParam.getSymbol() + " '" + checkParam.getValue() + "' ");

                                    }
                                }
                                else {
                                    if(types.contains(colType.get(checkParam.getProp()))){
                                        builderWhere.append(" and " + checkParam.getProp() + " " + checkParam.getSymbol() + "" + checkParam.getValue() + "");

                                    }else {
                                        builderWhere.append(" and " + checkParam.getProp() + " " + checkParam.getSymbol() + " '" + checkParam.getValue() + "' ");

                                    }
                                }
                                flag =true;
                            }
                        }
                        if(!flag){
                            builderWhere=new StringBuilder();
                        }
                    }

                    if(builderWhere.length()>0){
                        builderWhere.append(" and v_tablename='"+tableName.toUpperCase()+"'");
                    }else {
                        builderWhere.append(" where v_tablename='"+tableName.toUpperCase()+"'");
                    }
                    String countSql = "SELECT count(*) as num FROM t_sod_log_file_cassandra_sod"+builderWhere.toString();//+ " allow filtering"
                    long totalCount=fileCassandraSodService.queryCassandra(countSql);
                    page.setTotalCount(totalCount);
                    page.setPageData(dataList);
                }else {
                    String countSql = "SELECT count(*) as num FROM "+schema+"."+tableName+builderWhere.toString();
                    countList = database.querySql(countColumnList, countSql);
                    Map<String,Object> countMap = countList.get(0);
                    int totalCount = Integer.parseInt(countMap.get("num").toString());
                    int totalPage = (totalCount  +  pageSize  - 1) / pageSize;
                    //配置分页信息
                    page.setPageData(dataList);
                    page.setTotalCount(totalCount);
                    page.setTotalPage(totalPage);
                }

                database.closeConnect();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (database != null) {
                    database.closeConnect();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return page;
    }

    @Override
    public ResultT deleteData(SampleData sampleData) {
        try{
            if(StringUtils.isEmpty(sampleData.getPrimaryKey())){
                return ResultT.failed("表结构未配置主键");
            }
            DatabaseEntity databaseEntity = this.databaseDao.findById(sampleData.getDatabaseId()).get();
            DatabaseDto databaseDto = databaseMapper.toDto(databaseEntity);
            DatabaseDcl database = null;
            try {
                database = DatabaseUtil.getDatabase(databaseDto, databaseInfo);
                String schema = databaseDto.getSchemaName();
                if(schema.equals(schema.toUpperCase())){
                    schema="\""+schema+"\"";
                }
                String tableName = sampleData.getTableName();

                if("Cassandra".equals(databaseDto.getDatabaseDefine().getDatabaseType())) {
                    List<SampleData.CheckParam> updateColumnList = sampleData.getCheckList();
                    Map<String,String> colType=sampleData.getColType();
                    if(updateColumnList!=null&&updateColumnList.size()>0){
                        StringBuilder wheres=new StringBuilder();
                        List<Object> params=new ArrayList<>();
                        for(SampleData.CheckParam updateColumn : updateColumnList){

                            if("true".equals(updateColumn.getIsPrimaryKey())){
                                wheres.append(updateColumn.getProp()+"=? and ");
                                params.add(this.valueOf(updateColumn.getProp(),updateColumn.getValue(),colType));
                            }
                        }
                        String deleteSql="DELETE FROM "+schema+"."+tableName +
                                " WHERE "+wheres.toString().substring(0,wheres.length()-4);
                        System.out.println(deleteSql);
                        database.udpateSql(deleteSql,params);

                    }


                }else {
                    List<String> idList = sampleData.getIds();
                    String ids = "'"+String.join("','", idList)+"'";
                    if(sampleData.getIfDeleteFile()!=null&&sampleData.getIfDeleteFile()){
                        String filePath = sampleData.getFilePathColumn();
                        List<String> columnList = new ArrayList<>();
                        columnList.add(filePath);
                        String selectFileSql = "select "+filePath+" from "+schema+"."+tableName+" where "
                                +sampleData.getPrimaryKey()+" in("+ids+")";
                        List<Map<String,Object>> dataList = database.querySql(columnList,selectFileSql);
                        if(dataList!=null&&dataList.size()>0){
                            for(Map<String,Object> fileMap : dataList){
                                String path = fileMap.get(filePath)+"";
                                File file = new File(path);
                                file.delete();
                            }
                        }
                    }
                    //删除数据
                    String deleteSql = "DELETE FROM "+schema+"."+tableName +" WHERE "+sampleData.getPrimaryKey()+" in("+ids+")";
                    System.out.print(deleteSql);
                    database.udpateSql(deleteSql);
                }
                //删除文件

                database.closeConnect();
                return ResultT.success();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (database != null) {
                    database.closeConnect();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return ResultT.failed(e.getMessage());
        }
        return ResultT.failed();
    }

    @Override
    public ResultT updateData(SampleData sampleData) {
        try{
            if(StringUtils.isEmpty(sampleData.getPrimaryKey())){
                return ResultT.failed("表结构未配置主键");
            }

            DatabaseEntity databaseEntity = this.databaseDao.findById(sampleData.getDatabaseId()).get();
            DatabaseDto databaseDto = databaseMapper.toDto(databaseEntity);
            DatabaseDcl database = null;
            try {
                database = DatabaseUtil.getDatabase(databaseDto, databaseInfo);
                String schema = databaseDto.getSchemaName();
                if(schema.equals(schema.toUpperCase())){
                    schema="\""+schema+"\"";
                }
                String tableName = sampleData.getTableName();
                List<SampleData.CheckParam> updateColumnList = sampleData.getCheckList();
                Map<String,String> colType=sampleData.getColType();
                if("Cassandra".equals(databaseDto.getDatabaseDefine().getDatabaseType())){
                    if(updateColumnList!=null&&updateColumnList.size()>0){
                        StringBuilder columns=new StringBuilder();
                        StringBuilder wheres=new StringBuilder();
                        List<Object> params=new ArrayList<>();
                        List<Object> whereParams=new ArrayList<>();
                        for(SampleData.CheckParam updateColumn : updateColumnList){

                            if("true".equals(updateColumn.getIsPrimaryKey())){
                                wheres.append(updateColumn.getProp()+"=? and ");
                                whereParams.add(this.valueOf(updateColumn.getProp(),updateColumn.getValue(),colType));
                            }else {
                                columns.append(updateColumn.getProp()+"=?,");
                                params.add(this.valueOf(updateColumn.getProp(),updateColumn.getValue(),colType));
                            }
                        }
                        String updateSql="UPDATE "+schema+"."+tableName +" SET "+columns.toString().substring(0,columns.length()-1)+"" +
                                " WHERE "+wheres.toString().substring(0,wheres.length()-4);
                        System.out.println(updateSql);
                        params.addAll(whereParams);
                        database.udpateSql(updateSql,params);

                    }
                }else{
                    if(updateColumnList!=null&&updateColumnList.size()>0){
                        String primary = null;
                        StringBuilder insertColumn = new StringBuilder();
                        StringBuilder insertValues = new StringBuilder();
                        for(SampleData.CheckParam updateColumn : updateColumnList){
                            insertColumn.append(updateColumn.getProp()+",");
                            insertValues.append("'"+updateColumn.getValue()+"',");
                            if(updateColumn.getProp().equalsIgnoreCase(sampleData.getPrimaryKey())){
                                primary = updateColumn.getValue();
                            }
                        }
                        String insertColumnStr = insertColumn.toString();
                        String insertValuesStr = insertValues.toString();
                        if(insertColumnStr.endsWith(",")){
                            insertColumnStr = insertColumnStr.substring(0,insertColumnStr.length()-1);
                        }
                        if(insertValuesStr.endsWith(",")){
                            insertValuesStr = insertValuesStr.substring(0,insertValuesStr.length()-1);
                        }
                        //分布式数据库不能修改，先删除，再插入
                        //删除数据
                        String deleteSql = "DELETE FROM "+schema+"."+tableName +" WHERE "+sampleData.getPrimaryKey()+"='"+primary+"'";
                        database.udpateSql(deleteSql);
                        String insertSql = "INSERT INTO "+schema+"."+tableName +"("+insertColumnStr+")values("+insertValuesStr+")";
                        System.out.print(insertSql);
                        database.udpateSql(insertSql);
                    }
                }


                database.closeConnect();
                return ResultT.success();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (database != null) {
                    database.closeConnect();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return ResultT.failed(e.getMessage());
        }
        return ResultT.failed();
    }

    @Override
    public List<Map<String, Object>> findDataTableCount() {
        return mybatisQueryMapper.findDataTableCount();
    }

    @Override
    public List<Map<String, Object>> findArrivedDataCount() {
        return null;
    }

    @Override
    public List<Map<String, Object>> findArrivedPro() {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Calendar calendar = Calendar.getInstance();
            String beginTime = format.format(calendar.getTime());
            calendar.add(Calendar.DATE,1);
            String endTime = format.format(calendar.getTime());
            //资料列表
            List<Map<String,Object>> dataList = mybatisQueryMapper.findArrivedDataList();
            //统计列表
            List<Map<String,Object>> countList = mybatisQueryMapper.findArrivedDataCount(beginTime,endTime);
            for(Map<String,Object> dataMap : dataList){
                String tableName = dataMap.get("TABLE_NAME").toString();
                int shouldNum = Integer.parseInt(dataMap.getOrDefault("SHOULD_ARRIVED_NUM",1).toString());
                int arrivedNum = 0;
                for(Map<String,Object> countMap : countList){
                    String table = countMap.get("TABLE_NAME").toString();
                    if(tableName.equals(table)){
                        arrivedNum = Integer.parseInt(countMap.getOrDefault("NUM",0).toString());
                        break;
                    }
                }
                double pro = arrivedNum * 1.0 / shouldNum;
                dataMap.put("PRO",pro);
            }
            System.out.print(dataList);
            return dataList;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public PageBean selectPathStatistics(PageForm<Map<String, Object>> pageForm) {
        PageBean page = new PageBean();
        try{
            Map<String,Object> param = pageForm.getT();
            String dataClassId = param.get("dataClassId").toString();
            int pageSize = pageForm.getPageSize();
            int pageNum = pageForm.getCurrentPage();
            //查询数据
            String dataSql = "SELECT * FROM T_SOD_PATH_STATISTICS " +
                    " where data_class_id='"+dataClassId+"' " +
                    " order by create_time desc " +
                    " limit "+pageSize+" offset "+(pageNum - 1)*pageSize;
            System.out.println("查询语句："+dataSql);
            List<Map<String,Object>> dataList = mybatisQueryMapper.querySql(dataSql);
            //查询数据条数
            String countSql = "SELECT count(*)num FROM T_SOD_PATH_STATISTICS where data_class_id='"+dataClassId+"'";
            List<Map<String,Object>> countList = mybatisQueryMapper.querySql(countSql);
            Map<String,Object> countMap = countList.get(0);
            int totalCount = Integer.parseInt(countMap.get("NUM").toString());
            int totalPage = (totalCount  +  pageSize  - 1) / pageSize;
            //配置分页信息
            page.setPageData(dataList);
            page.setTotalCount(totalCount);
            page.setTotalPage(totalPage);
        }catch (Exception e){
            e.printStackTrace();
        }
        return page;
    }

  public Object valueOf(String key,String value,Map<String,String> colType){
       if("bigint".equals(colType.get(key))){
          return Long.parseLong(value);
       }else if("int".equals(colType.get(key))){
           return Integer.parseInt(value);
       }else if("double".equals(colType.get(key))){
           return Double.parseDouble(value);
       }else if("float".equals(colType.get(key))){
           return Float.parseFloat(value);
       }else if("decimal".equals(colType.get(key))){
           return new BigDecimal(value);
       }else if("timestamp".equals(colType.get(key))){
           SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
           try {
               return simpleDateFormat.parse(value);
           } catch (ParseException e) {
               e.printStackTrace();
           }
       }

       return value;
  }
}

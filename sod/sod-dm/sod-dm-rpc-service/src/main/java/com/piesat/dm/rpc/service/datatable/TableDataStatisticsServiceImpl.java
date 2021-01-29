package com.piesat.dm.rpc.service.datatable;

import com.piesat.common.jpa.BaseDao;
import com.piesat.common.jpa.BaseService;
import com.piesat.common.jpa.specification.SimpleSpecificationBuilder;
import com.piesat.common.jpa.specification.SpecificationOperator;
import com.piesat.dm.dao.datatable.TableDataStatisticsDao;
import com.piesat.dm.entity.datatable.TableDataStatisticsEntity;
import com.piesat.dm.mapper.MybatisModifyMapper;
import com.piesat.dm.mapper.MybatisQueryMapper;
import com.piesat.dm.rpc.api.dataclass.DataOnlineTimeService;
import com.piesat.dm.rpc.api.datatable.TableDataStatisticsService;
import com.piesat.dm.rpc.dto.dataclass.DataOnlineTimeDto;
import com.piesat.dm.rpc.dto.datatable.TableDataStatisticsDto;
import com.piesat.dm.rpc.mapper.datatable.TableDataStatisticsMapper;
import com.piesat.dm.rpc.util.FileLoop;
import com.piesat.util.page.PageBean;
import com.piesat.util.page.PageForm;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 表数据统计
 *
 * @author cwh
 * @date 2020年 02月13日 14:52:57
 */
@Service
public class TableDataStatisticsServiceImpl extends BaseService<TableDataStatisticsEntity> implements TableDataStatisticsService {
    @Autowired
    private TableDataStatisticsDao tableDataStatisticsDao;
    @Autowired
    private TableDataStatisticsMapper tableDataStatisticsMapper;

    @Autowired
    private MybatisQueryMapper mybatisQueryMapper;

    @Autowired
    private DataOnlineTimeService dataOnlineTimeService;

    @Autowired
    private MybatisModifyMapper mybatisModifyMapper;

    @Override
    public BaseDao<TableDataStatisticsEntity> getBaseDao() {
        return this.tableDataStatisticsDao;
    }

    @Override
    public TableDataStatisticsDto saveDto(TableDataStatisticsDto tableDataStatisticsDto) {
        TableDataStatisticsEntity tableDataStatisticsEntity = this.tableDataStatisticsMapper.toEntity(tableDataStatisticsDto);
        tableDataStatisticsEntity = this.saveNotNull(tableDataStatisticsEntity);
        return this.tableDataStatisticsMapper.toDto(tableDataStatisticsEntity);
    }

    @Override
    public TableDataStatisticsDto getDotById(String id) {
        TableDataStatisticsEntity tableDataStatisticsEntity = this.getById(id);
        return this.tableDataStatisticsMapper.toDto(tableDataStatisticsEntity);
    }

    @Override
    public List<TableDataStatisticsDto> all() {
        List<TableDataStatisticsEntity> all = this.getAll();
        return this.tableDataStatisticsMapper.toDto(all);
    }

    @Override
    public PageBean list(PageForm<TableDataStatisticsDto> pageForm) {
        TableDataStatisticsEntity tableDataStatisticsEntity = tableDataStatisticsMapper.toEntity(pageForm.getT());

        SimpleSpecificationBuilder ssb = new SimpleSpecificationBuilder();
        if (StringUtils.isNotBlank(tableDataStatisticsEntity.getTableId())) {
            ssb.add("tableId", SpecificationOperator.Operator.eq.name(), tableDataStatisticsEntity.getTableId());
        }
        if (StringUtils.isNotBlank(tableDataStatisticsEntity.getDatabaseId())) {
            ssb.add("databaseId", SpecificationOperator.Operator.eq.name(), tableDataStatisticsEntity.getDatabaseId());
        }
        Sort sort = Sort.by(Sort.Direction.DESC, "statisticDate");
        PageBean page = this.getPage(ssb.generateSpecification(), pageForm, sort);
        List<TableDataStatisticsEntity> pageData = (List<TableDataStatisticsEntity>)page.getPageData();
        page.setPageData(this.tableDataStatisticsMapper.toDto(pageData));
        return page;
    }

    @Override
    public Map<String, Object> getOnlineTime(String classDataId, String statisticDate) {

        Map<String,Object> map = new HashMap<String,Object>();

        List<Map<String,Object>> onlineList = mybatisQueryMapper.getOnlineTime(classDataId,statisticDate);
        DataOnlineTimeDto dataOnlineTimeDto = dataOnlineTimeService.findByDataClassId(classDataId);

        if (dataOnlineTimeDto!=null && !"0".equals(dataOnlineTimeDto.getUsing())){
            if ("today".equals(dataOnlineTimeDto.getEndTimeFlag())) {
                onlineList.get(0).put("END_TIME",new Date());
            }else if (dataOnlineTimeDto.getEndTime()!=null){
                onlineList.get(0).put("END_TIME", dataOnlineTimeDto.getEndTime());
            }
            if (dataOnlineTimeDto.getBeginTime()!=null){
                onlineList.get(0).put("BEGIN_TIME",dataOnlineTimeDto.getBeginTime());
            }
        }
        if(null != onlineList && onlineList.size()>0) {
            map.put("online", onlineList.get(0));
        }
        return map;
    }

    @Override
    public List<TableDataStatisticsDto> findByParam(TableDataStatisticsDto tableDataStatisticsDto) {

        SimpleSpecificationBuilder specificationBuilder=new SimpleSpecificationBuilder();
        if(com.piesat.common.utils.StringUtils.isNotNullString(tableDataStatisticsDto.getDatabaseId())){
            specificationBuilder.add("databaseId", SpecificationOperator.Operator.eq.name(),tableDataStatisticsDto.getDatabaseId());
        }
        if(com.piesat.common.utils.StringUtils.isNotNullString(tableDataStatisticsDto.getTableId())){
            specificationBuilder.add("tableId", SpecificationOperator.Operator.eq.name(),tableDataStatisticsDto.getTableId());
        }
        if(tableDataStatisticsDto.getStatisticDate() != null){
            String statisticDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(tableDataStatisticsDto.getStatisticDate());
            specificationBuilder.add("statisticDate", SpecificationOperator.Operator.ges.name(),statisticDate);
        }
        List<TableDataStatisticsEntity> tableDataStatisticsEntities = this.getAll(specificationBuilder.generateSpecification());
        List<TableDataStatisticsDto> tableDataStatisticsDtos = this.tableDataStatisticsMapper.toDto(tableDataStatisticsEntities);
        return tableDataStatisticsDtos;
    }

    @Override
    public void countNewDatabaseInfo() {
        mybatisModifyMapper.countNewDatabaseInfo();
    }

    @Override
    public void countFilePathInfo() {
        try{
            //获取存储编码及根路径
            String rootSql = "select distinct id,dir_norm from T_SOD_DATACLASS_NORM";
            List<Map<String,Object>> rootPathList = mybatisQueryMapper.querySql(rootSql);
            //统计路径信息
            if(rootPathList!=null&&rootPathList.size()>0){
                DecimalFormat format = new DecimalFormat("#.00");
                for(Map<String,Object> map : rootPathList){
                    String dataClassId = map.get("ID").toString();
                    String rootPath = map.get("DIR_NORM")+"";
                    FileLoop.fileList.clear();
                    List<File> pathList = FileLoop.fileReadLoop(rootPath);
                    if(pathList!=null&&pathList.size()>0){
                        //先删除之前的目录信息
                        String deleteSql = "delete from T_SOD_PATH_STATISTICS where data_class_id='"+dataClassId+"'";
                        mybatisModifyMapper.updateSql(deleteSql);
                        //存储信息的目录信息
                        List<Map<String,Object>> insertList = new ArrayList<>();
                        for(int i=0; i<pathList.size(); i++){
                            File file = pathList.get(i);
                            String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(file.lastModified()));
                            String path = file.getAbsolutePath();
                            long size = FileUtils.sizeOfDirectory(file);
                            double d_size = size * 1.0 / 1024;
                            String s_size = format.format(d_size);
                            Map<String,Object> pathMap = new HashMap<>();
                            pathMap.put("id",System.currentTimeMillis());
                            pathMap.put("data_class_id",dataClassId);
                            pathMap.put("path",path);
                            pathMap.put("path_size",s_size);
                            pathMap.put("create_time",time);
                            insertList.add(pathMap);
                            //批量入库
                            if(insertList.size()==1000){
                                mybatisModifyMapper.insertIntoPathStatistics(insertList);
                                insertList.clear();
                            }
                        }
                        //剩余数据入库
                        if(insertList.size()>0){
                            mybatisModifyMapper.insertIntoPathStatistics(insertList);
                            insertList.clear();
                        }
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}

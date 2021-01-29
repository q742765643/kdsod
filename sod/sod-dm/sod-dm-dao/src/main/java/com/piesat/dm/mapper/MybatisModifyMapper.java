package com.piesat.dm.mapper;

import com.piesat.dm.entity.StorageConfigurationEntity;
import com.piesat.dm.entity.special.DatabaseSpecialAccessEntity;
import com.piesat.dm.entity.special.DatabaseSpecialReadWriteEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * mybatis修改Mapper
 *
 * @author cwh
 * @date 2020年 02月12日 09:26:56
 */
@Component
public interface MybatisModifyMapper {
    void updateDataAuthorityConfig(StorageConfigurationEntity sce);

    void modifyDatabaseSpecialReadWrite(DatabaseSpecialReadWriteEntity databaseSpecialReadWriteEntity);

    void updateSpecialAccess(DatabaseSpecialAccessEntity databaseSpecialAccessEntity);

    void countNewDatabaseInfo();

    void updateSql(@Param("dataSql") String dataSql);

    void insertIntoPathStatistics(@Param("list") List<Map<String,Object>> pathList);
}

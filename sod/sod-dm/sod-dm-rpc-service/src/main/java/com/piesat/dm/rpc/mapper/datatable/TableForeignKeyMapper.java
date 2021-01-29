package com.piesat.dm.rpc.mapper.datatable;

import com.piesat.common.jpa.BaseMapper;
import com.piesat.dm.entity.datatable.TableForeignKeyEntity;
import com.piesat.dm.rpc.dto.datatable.TableForeignKeyDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.stereotype.Service;

/**
 * 数据库外键关联
 *
 * @author cwh
 * @date 2019年 11月22日 15:53:17
 */
@Service
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TableForeignKeyMapper extends BaseMapper<TableForeignKeyDto, TableForeignKeyEntity> {
}

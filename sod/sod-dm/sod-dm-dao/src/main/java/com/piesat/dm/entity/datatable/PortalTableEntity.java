package com.piesat.dm.entity.datatable;

import com.piesat.common.jpa.entity.BaseEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * @ClassName : PortalTableEntity
 * @Description :
 * @Author : zzj
 * @Date: 2021-04-12 09:43
 */
@Data
@Table(name = "T_SOD_PORTAL_TABLE")
@Entity
public class PortalTableEntity extends BaseEntity {
    @Column(name = "database_id", length = 255)
    private String databaseId;

    @Column(name = "parent_id", length = 255)
    private String parentId;

    @Column(name = "table_id", length = 255)
    private String tableId;

    @Column(name = "table_name", length = 255)
    private String tableName;

    @Column(name = "begin_time", length = 255)
    private Date beginTime;

    @Column(name = "end_time", length = 255)
    private Date endTime;

    @Column(name = "record_count", length = 255)
    private Date recordCount;

    @Column(name = "file_size", length = 255)
    private Date fileSize;

}


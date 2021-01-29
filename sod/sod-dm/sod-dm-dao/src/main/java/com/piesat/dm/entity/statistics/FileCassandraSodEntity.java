package com.piesat.dm.entity.statistics;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @ClassName : FileCassandraSodEntity
 * @Description :
 * @Author : zzj
 * @Date: 2021-01-27 15:31
 */
@Data
@Table(name = "T_SOD_LOG_FILE_CASSANDRA_SOD")
@Entity
public class FileCassandraSodEntity implements Serializable {
    @EmbeddedId
    private FileCassandraSodPk id;
    /**
     * 资料标识
     */
    @Column(name = "D_DATA_ID")
    private String ddataId;

    /**
     * 入库表名
     */
    @Column(name = "V_TABLENAME")
    private String vtableName;

    /**
     * 入库时间
     */
    @Column(name = "D_IYMDHM")
    private Date diymdhm;




}


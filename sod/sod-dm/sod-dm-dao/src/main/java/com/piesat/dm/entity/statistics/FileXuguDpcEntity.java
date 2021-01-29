package com.piesat.dm.entity.statistics;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @ClassName : FileXuguDpc
 * @Description :
 * @Author : zzj
 * @Date: 2021-01-27 14:47
 */
@Data
@Table(name = "T_SOD_LOG_FILE_XUGU_DPC")
@Entity
public class FileXuguDpcEntity implements Serializable {

    /**
     * 文件标识
     */
    @Id
    @Column(name = "D_FILE_ID")
    private String dfileId;

    /**
     * 资料标识
     */
    @Column(name = "D_DATA_ID")
    private String ddataId;

    /**
     * 入库时间
     */
    @Column(name = "D_IYMDHM")
    private Date diymdhm;

    /**
     * 数据来源
     */
    @Column(name = "D_SOURCE_ID")
    private String dsourceId;

    /**
     * 文件名
     */
    @Column(name = "V_FILE_NAME")
    private String vfileName;

    /**
     * 存储路径
     */
    @Column(name = "D_STORAGE_SITE")
    private String dstorageSite;

    /**
     * 文件大小
     */
    @Column(name = "D_FILE_SIZE")
    private BigDecimal dfileSize;

    /**
     * 文件修改时间
     */
    @Column(name = "D_FILETIME")
    private Date dfileTime;

    /**
     * 资料时间
     */
    @Column(name = "D_DATETIME")
    private Date ddateTime;

    /**
     * 解码开始时间
     */
    @Column(name = "D_DPC_STIME")
    private Date ddpcStime;

    /**
     * 解码结束时间
     */
    @Column(name = "D_DPC_ETIME")
    private Date ddpcEtime;

    /**
     * 解码记录数
     */
    @Column(name = "D_DPC_NUM")
    private BigDecimal ddpcNum;

    /**
     * 入库开始时间
     */
    @Column(name = "D_SOD_STIME")
    private Date dsodStime;

    /**
     * 入库结束时间
     */
    @Column(name = "D_SOD_ETIME")
    private Date dsodEtime;

    /**
     * 入库记录数据
     */
    @Column(name = "D_SOD_NUM")
    private BigDecimal dsodNum;

    /**
     * 入库表名
     */
    @Column(name = "V_TABLENAME")
    private String vtableName;

    /**
     * 解码耗时
     */
    @Column(name = "D_DPC_ELAPSED_TIME")
    private BigDecimal ddpcelapsedTime;

    /**
     * 入库耗时
     */
    @Column(name = "D_SOD_ELAPSED_TIME")
    private BigDecimal dsodelapsedTime;
}


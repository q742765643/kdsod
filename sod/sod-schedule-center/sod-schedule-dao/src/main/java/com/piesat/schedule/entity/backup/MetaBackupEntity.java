package com.piesat.schedule.entity.backup;

import com.piesat.common.annotation.Excel;
import com.piesat.schedule.entity.JobInfoEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @program: sod
 * @description:
 * @author: zzj
 * @create: 2020-02-26 14:34
 **/
@Data
@Entity
@Table(name="T_SOD_JOB_METABACKUP_INFO")
@DiscriminatorValue("METABACKUP")
public class MetaBackupEntity extends JobInfoEntity {
    @Excel(name = "物理库ID")
    @Column(name="database_id", length=50)
    private String databaseId;
    @Excel(name = "备份类型",readConverterExp = "0=结构,1=数据,0,1=结构+数据")
    @Column(name="is_structure", length=50)
    private String isStructure;
    @Excel(name = "备份内容")
    @Column(name="back_content",columnDefinition = "TEXT")
    private String backContent;
    @Excel(name = "物理库基础ID")
    @Column(name="parent_id", length=50)
    private String parentId;
    @Excel(name = "物理库名称")
    @Column(name="database_name", length=200)
    private String databaseName;
    @Excel(name = "存储目录")
    @Column(name="storage_directory", length=200)
    private String storageDirectory;
    @Column(name="conditions", length=200)
    private String conditions;
    @Excel(name = "物理库类型")
    @Column(name="database_type", length=50)
    private String databaseType;


}


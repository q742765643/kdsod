package com.piesat.dm.rpc.dto.datatable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 表数据统计
 *
 * @author cwh
 * @date 2020年 02月13日 14:44:37
 */
@Data
public class TableDataStatisticsDto {
    private static final long serialVersionUID = 1L;

    private String id;
    @ApiModelProperty("物理库id")
    private String databaseId;

    private String tableId;

    @ApiModelProperty("统计日期")
    private Date statisticDate;

    @ApiModelProperty("开始时间")
    private Date beginTime;

    @ApiModelProperty("结束时间")
    private Date endTime;

    @ApiModelProperty("总量")
    private long recordCount;

    @ApiModelProperty("日增量")
    private int dayTotal;

    @ApiModelProperty("统计时间")
    private String statisticTime;

    private Date createTime;

    private Date updateTime;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    private String delFlag;

    private Integer version;

    @ApiModelProperty("解码耗时")
    private BigDecimal elapsedTime;

    @ApiModelProperty("文件大小总量")
    private BigDecimal fileSize;

    @ApiModelProperty("数据id 查询条件 可空")
    private String parentId;
    @ApiModelProperty("资料id 查询条件 可空")
    private String dataclassId;
    @ApiModelProperty("数据名称 查询条件 可空")
    private String parentName;
    @ApiModelProperty("资料名称 查询条件 可空")
    private String dataclassName;
}

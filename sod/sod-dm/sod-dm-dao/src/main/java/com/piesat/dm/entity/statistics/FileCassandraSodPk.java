package com.piesat.dm.entity.statistics;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @ClassName : FileCassandraSodPk
 * @Description :
 * @Author : zzj
 * @Date: 2021-01-27 16:01
 */
@Embeddable
public class FileCassandraSodPk implements Serializable {

    /**
     * 资料种类代码
     */
    @Column(name = "PRODCODE")
    private String prodCode;

    /**
     * 资料时间
     */
    @Column(name = "TIME")
    private Date time;

    /**
     * 层次高度序号
     */
    @Column(name = "LEVEL")
    private BigDecimal level;

    /**
     * 预报时效
     */
    @Column(name = "VALIDTIME")
    private BigDecimal validTime;

    /**
     * 纬向块号（列）
     */
    @Column(name = "SLICEX")
    private BigDecimal slicex;

    /**
     * 经向块号（行）
     */
    @Column(name = "SLICEY")
    private BigDecimal slicey;
}


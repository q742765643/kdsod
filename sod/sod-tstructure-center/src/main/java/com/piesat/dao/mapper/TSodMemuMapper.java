package com.piesat.dao.mapper;

import com.piesat.model.mybatis.TSodMemu;

public interface TSodMemuMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table T_SOD_MEMU
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(String id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table T_SOD_MEMU
     *
     * @mbg.generated
     */
    int insert(TSodMemu record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table T_SOD_MEMU
     *
     * @mbg.generated
     */
    int insertSelective(TSodMemu record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table T_SOD_MEMU
     *
     * @mbg.generated
     */
    TSodMemu selectByPrimaryKey(String id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table T_SOD_MEMU
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(TSodMemu record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table T_SOD_MEMU
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(TSodMemu record);
}
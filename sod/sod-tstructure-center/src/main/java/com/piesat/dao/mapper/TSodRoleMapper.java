package com.piesat.dao.mapper;

import com.piesat.model.mybatis.TSodRole;

public interface TSodRoleMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table T_SOD_ROLE
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(String id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table T_SOD_ROLE
     *
     * @mbg.generated
     */
    int insert(TSodRole record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table T_SOD_ROLE
     *
     * @mbg.generated
     */
    int insertSelective(TSodRole record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table T_SOD_ROLE
     *
     * @mbg.generated
     */
    TSodRole selectByPrimaryKey(String id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table T_SOD_ROLE
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(TSodRole record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table T_SOD_ROLE
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(TSodRole record);
}
package com.piesat.dao.mapper;

import com.piesat.model.mybatis.TSodGrpcLog;

public interface TSodGrpcLogMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table T_SOD_GRPC_LOG
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(String id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table T_SOD_GRPC_LOG
     *
     * @mbg.generated
     */
    int insert(TSodGrpcLog record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table T_SOD_GRPC_LOG
     *
     * @mbg.generated
     */
    int insertSelective(TSodGrpcLog record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table T_SOD_GRPC_LOG
     *
     * @mbg.generated
     */
    TSodGrpcLog selectByPrimaryKey(String id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table T_SOD_GRPC_LOG
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(TSodGrpcLog record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table T_SOD_GRPC_LOG
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(TSodGrpcLog record);
}
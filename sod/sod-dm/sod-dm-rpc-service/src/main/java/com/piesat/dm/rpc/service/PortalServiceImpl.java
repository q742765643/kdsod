package com.piesat.dm.rpc.service;

import com.piesat.dm.mapper.MybatisQueryMapper;
import com.piesat.dm.rpc.api.PortalService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @ClassName : PortalServiceImpl
 * @Description :
 * @Author : zzj
 * @Date: 2021-04-12 14:07
 */
@Service
public class PortalServiceImpl implements PortalService {
    @Autowired
    private MybatisQueryMapper mybatisQueryMapper;



    public List<Map<String, Object>> queryParent(String parentName,String parentId){
       return mybatisQueryMapper.queryParent(parentName,parentId);
    }

    public List<Map<String, Object>> queryChild(String parentId){
       return mybatisQueryMapper.queryChild(parentId);
    }



}


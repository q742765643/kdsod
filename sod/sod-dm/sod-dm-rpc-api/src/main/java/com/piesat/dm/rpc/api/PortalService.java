package com.piesat.dm.rpc.api;

import com.piesat.common.grpc.annotation.GrpcHthtService;
import com.piesat.common.grpc.constant.SerializeType;
import com.piesat.util.constant.GrpcConstant;

import java.util.List;
import java.util.Map;

@GrpcHthtService(server = GrpcConstant.DM_SERVER, serialization = SerializeType.PROTOSTUFF)
public interface PortalService {
    List<Map<String, Object>> queryParent(String parentName, String parentId);

    List<Map<String, Object>> queryChild(String parentId);
}

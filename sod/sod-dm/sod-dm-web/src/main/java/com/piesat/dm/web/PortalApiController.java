package com.piesat.dm.web;

import com.piesat.dm.rpc.api.PortalService;
import com.piesat.dm.rpc.api.datatable.TableDataStatisticsService;
import com.piesat.dm.rpc.dto.datatable.TableDataStatisticsDto;
import com.piesat.sod.system.rpc.api.IndexService;
import com.piesat.util.ResultT;
import com.piesat.util.page.PageBean;
import com.piesat.util.page.PageForm;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @ClassName : PortalApiController
 * @Description :
 * @Author : zzj
 * @Date: 2021-04-12 13:32
 */
@Api(tags = "门户网站接口")
@RequestMapping("/api/portal")
@RestController
public class PortalApiController {
    @Autowired
    private PortalService portalService;
    @Autowired
    private IndexService indexService;
    @Autowired
    private TableDataStatisticsService tableDataStatisticsService;

    @ApiOperation(value = "门户查询数据类型")
    @GetMapping(value = "/queryParent")
    @ApiImplicitParams({
            @ApiImplicitParam(name="parentId",value="数据类型id 可空",required=false),
            @ApiImplicitParam(name="parentName",value="数据名称 可空",required=false)
    })
    public ResultT<List<Map<String, Object>>> queryParent(@RequestParam(value = "parentName",required = false)String parentName, @RequestParam(value = "parentId",required = false) String parentId){
        ResultT<List<Map<String, Object>>> resultT=new ResultT<>();
        List<Map<String, Object>> mapList=portalService.queryParent(parentName,parentId);
        resultT.setData(mapList);
        return resultT;
    }
    @ApiOperation(value = "门户查询资料")
    @ApiImplicitParams({
            @ApiImplicitParam(name="parentId",value="数据类型id 可空",required=false)
    })
    @GetMapping(value = "/queryChild")
    public ResultT<List<Map<String, Object>>> queryChild(@RequestParam(value = "parentId",required = false) String parentId){
        ResultT<List<Map<String, Object>>> resultT=new ResultT<>();
        List<Map<String, Object>> mapList=portalService.queryChild(parentId);
        resultT.setData(mapList);
        return resultT;
    }
    @ApiOperation(value="门户获取资料分类统计数量",notes="获取资料分类统计数量")
    @GetMapping(value="/findDataCount")
    public ResultT findDataCount() {
        try {
            List<Map<String,Object>> data = indexService.findDataCount();
            return ResultT.success(data);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultT.failed(e.getMessage());
        }
    }

    @GetMapping("/list")
    @ApiOperation(value = "门户条件分页查询资料", notes = "条件分页查询资料")
    public ResultT<PageBean> list(TableDataStatisticsDto tableDataStatisticsDto,
                                  @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                  @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        ResultT<PageBean> resultT = new ResultT<>();
        PageForm<TableDataStatisticsDto> pageForm = new PageForm<>(pageNum, pageSize, tableDataStatisticsDto);
        PageBean pageBean = tableDataStatisticsService.portalList(pageForm);
        resultT.setData(pageBean);
        return resultT;
    }

}


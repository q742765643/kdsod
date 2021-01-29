package com.piesat.dm.web.controller.datatable;

import com.piesat.common.utils.StringUtils;
import com.piesat.dm.rpc.api.datatable.DataTableService;
import com.piesat.dm.rpc.dto.datatable.SampleData;
import com.piesat.util.ResultT;
import com.piesat.util.page.PageBean;
import com.piesat.util.page.PageForm;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据动态查询
 */
@Api(tags = "数据动态查询")
@RequestMapping("/dm/dataSearch")
@RestController
public class DataSearchController {

    @Autowired
    private DataTableService dataTableService;

    @ApiOperation(value = "查询")
    @PostMapping(value = "/search")
    public ResultT<PageBean> getSampleData(@RequestBody SampleData sampleData) {
        try {
            Map<String, Object> map = new HashMap<>();
            if(StringUtils.isNotEmpty(sampleData.getTableName())){
                map.put("tableName",sampleData.getTableName());
            }
            if(StringUtils.isNotEmpty(sampleData.getDatabaseId())){
                map.put("databaseId",sampleData.getDatabaseId());
            }
            if(StringUtils.isNotEmpty(sampleData.getTableName())){
                map.put("columnList",sampleData.getColumn());
            }
            if(sampleData.getCheckList()!=null){
                map.put("checkList",sampleData.getCheckList());
            }
            map.put("colType",sampleData.getColType());
            ResultT<PageBean> resultT = new ResultT<>();
            int pageNum = 1;
            if(sampleData.getPageNum()!=null){
                pageNum = sampleData.getPageNum();
            }
            int pageSize = 10;
            if(sampleData.getPageSize()!=null){
                pageSize = sampleData.getPageSize();
            }
            PageForm<Map<String, Object>> pageForm = new PageForm<>(pageNum, pageSize, map);
            if(StringUtils.isNotEmpty(sampleData.getPageState())){
                pageForm.setOrderBy(sampleData.getPageState());
            }
            PageBean pageBean = dataTableService.dataSearchPage(pageForm);
            resultT.setData(pageBean);
            return resultT;
        } catch (Exception e) {
            e.printStackTrace();
            return ResultT.failed(e.getMessage());
        }
    }

    @ApiOperation(value = "删除")
    @PostMapping(value = "/deleteData")
    public ResultT deleteData(@RequestBody SampleData sampleData) {
        try{
            ResultT r = dataTableService.deleteData(sampleData);
            return r;
        }catch (Exception e){
            e.printStackTrace();
            return ResultT.failed(e.getMessage());
        }
    }

    @ApiOperation(value = "修改")
    @PostMapping(value = "/updateData")
    public ResultT updateData(@RequestBody SampleData sampleData) {
        try{
            ResultT r = dataTableService.updateData(sampleData);
            return r;
        }catch (Exception e){
            e.printStackTrace();
            return ResultT.failed(e.getMessage());
        }
    }

    /**
     *  获取资料到报情况
     * @description
     * @return
     */
    @ApiOperation(value="数据库表信息统计",notes="数据库表信息统计")
    @GetMapping(value="/findDataTableCount")
    public ResultT findDataTableCount() {
        try {
            List<Map<String,Object>> data = dataTableService.findDataTableCount();
            return ResultT.success(data);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultT.failed(e.getMessage());
        }
    }

    /**
     *  获取资料到报情况
     * @description
     * @return
     */
    @ApiOperation(value="获取资料到报情况",notes="获取资料到报情况")
    @GetMapping(value="/findArrivedPro")
    public ResultT findArrivedPro() {
        try {
            List<Map<String,Object>> data = dataTableService.findArrivedPro();
            return ResultT.success(data);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultT.failed(e.getMessage());
        }
    }

    @ApiOperation(value = "查询路径统计信息")
    @PostMapping(value = "/selectPathStatistics")
    public ResultT<PageBean> selectPathStatistics(String dataClassId,String pageNum,String pageSize) {
        try {
            Map<String, Object> map = new HashMap<>();
            if(StringUtils.isNotEmpty(dataClassId)){
                map.put("dataClassId",dataClassId);
            }
            ResultT<PageBean> resultT = new ResultT<>();
            int _pageNum = 1;
            if(pageNum != null){
                _pageNum = Integer.parseInt(pageNum);
            }
            int _pageSize = 10;
            if(pageSize != null){
                _pageSize = Integer.parseInt(pageSize);
            }
            PageForm<Map<String, Object>> pageForm = new PageForm<>(_pageNum, _pageSize, map);
            PageBean pageBean = dataTableService.selectPathStatistics(pageForm);
            resultT.setData(pageBean);
            return resultT;
        } catch (Exception e) {
            e.printStackTrace();
            return ResultT.failed(e.getMessage());
        }
    }
}

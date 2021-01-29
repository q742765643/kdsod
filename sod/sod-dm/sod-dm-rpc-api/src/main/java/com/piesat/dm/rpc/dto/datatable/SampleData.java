package com.piesat.dm.rpc.dto.datatable;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 样例数据
 *
 * @author cwh
 * @date 2020年 02月13日 16:28:41
 */
@Data
public class SampleData {
    private String tableName;
    private String databaseId;
    private List<String> column;
    private List<String> ids;
    private Integer pageNum;
    private Integer pageSize;
    private List<CheckParam> checkList;
    private String primaryKey;
    private Boolean ifDeleteFile;
    private String filePathColumn;
    private String dataClassId;
    private String pageState;
    private Map<String,String> colType;

    @Data
    public static class CheckParam{
        String label;
        String prop;
        String symbol;
        String value;
        String isPrimaryKey;

        public CheckParam(){

        }
    }
}

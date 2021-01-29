<template>
  <el-main class="PathStatistics">
      <el-form ref="ruleForm" :model="pathTotal" label-width="100px" :inline="true" style="margin-top:10px;">
        <el-form-item label="总目录数量">
          <el-input v-model.trim="pathTotal.totalNum" size="small" readonly></el-input>
        </el-form-item>
        <el-form-item label="总目录大小">
          <el-input v-model.trim="pathTotal.totalSize" size="small" readonly></el-input>
        </el-form-item>
      </el-form>
      <el-table border :data="tableData" stripe style="width: 100%;margin-top:10px" row-key="id">
        <el-table-column type="index" label="序号" :index="table_index" width="50"></el-table-column>
        <el-table-column prop="PATH" label="目录" style="text-align: left"></el-table-column>
        <el-table-column prop="PATH_SIZE" label="容量(K)" width="200"></el-table-column>
        <el-table-column prop="CREATE_TIME" label="创建日期" width="200">
          <template slot-scope="scope">
            <span v-if="scope.row.CREATE_TIME">{{ parseTime(scope.row.CREATE_TIME) }}</span>
          </template>
        </el-table-column>
      </el-table>
      <pagination
        v-show="total>0"
        :total="total"
        :page.sync="queryParams.pageNum"
        :limit.sync="queryParams.pageSize"
        @pagination="searchFun"
      />
  </el-main>
</template>

<script>
import {
  pathstatisticsList,
  updateIsAllLine,
  getArchive
} from "@/api/structureManagement/tableStructureManage/StructureManageTable";
import { parseTime } from "@/utils/ruoyi";
export default {
  name: "DataStatistics",
  props: { rowData: Object, tableInfo: Object },
  components: {},
  data() {
    return {
      loading: false,
      queryParams: {
        databaseId: this.rowData.DATABASE_ID,
        tableId: this.tableInfo.id == undefined ? "NULL" : this.tableInfo.id,
        pageNum: 1,
        pageSize: 10,
        dataClassId:''
      },
      searchParams: {
        isAllLine: 1,
        beginTime: "",
        endTime: ""
      },
      tableData: [],
      total: 0,
      pathTotal:{
        totalNum:0,
        totalSize:0
      }
    };
  },
  /*  mounted() {
    this.searchFun();
  }, */
  methods: {
    handleQuery(value) {
      if (value == 2) {
        getArchive({ ddataid: this.rowData.D_DATA_ID }).then(res => {
          this.searchParams.endTime = parseTime(res.data.endTime);
          this.searchParams.beginTime = parseTime(res.data.beginTime);
        });
        //return;
      }
      let obj = {};
      obj.dataClassId = this.rowData.DATA_CLASS_ID;
      obj.isAllLine = value;
      updateIsAllLine(obj).then(response => {});
    },
    // table自增定义方法
    table_index(index) {
      return (
        (this.queryParams.pageNum - 1) * this.queryParams.pageSize + index + 1
      );
    },
    searchFun() {
      if (this.tableInfo.id) {
        this.queryParams.tableId = this.tableInfo.id;
      }
      this.queryParams.dataClassId = this.tableInfo.dataServiceId;
      pathstatisticsList(this.queryParams).then(response => {
        this.tableData = response.data.pageData;
        this.total = response.data.totalCount;
        this.pathTotal.totalNum = response.data.totalCount;
        this.loading = false;
      });
    }
  },
  watch: {
    tableInfo(val) {
      this.tableInfo = val;
      this.searchFun();
    }
  }
};
</script>

<style lang="scss">
.DataStatistics {
  fieldset {
    border: 1px solid #ebeef5;
    padding: 20px;
    margin-top: 12px;
  }
}
</style>

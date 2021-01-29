<template>
  <el-main style="padding-top:10px;">
    <el-form :model="queryParams" ref="queryForm" :inline="true" class="searchBox">
      <el-form-item>
        <el-dropdown size="mini" @command="handleCommand">
          <el-button >
            选择查询条件<i class="el-icon-arrow-down el-icon--right"></i>
          </el-button>
          <el-dropdown-menu slot="dropdown" >
            <template v-for="item in columnList_p">
              <el-dropdown-item :command="item.label+'|'+item.prop">{{item.label}}</el-dropdown-item>
            </template>
          </el-dropdown-menu>
        </el-dropdown>
      </el-form-item>
      <template v-for="param in selectParamList">
      <el-form-item :label="param.label">
        <el-input placeholder="请输入内容" v-model="param.value" class="input-with-select">
          <el-select v-model="param.symbol" slot="prepend" style="width:80px">
            <el-option label="=" value="="></el-option>
            <el-option label=">" value=">"></el-option>
            <el-option label="≥" value=">="></el-option>
            <el-option label="<" value="<"></el-option>
            <el-option label="≤" value="<="></el-option>
          </el-select>
        </el-input>
      </el-form-item>
      </template>
      <el-form-item>
        <el-button size="small" type="primary" @click="handleQuery" icon="el-icon-search">查询</el-button>
        <el-button size="small" type="warning" @click="handleEdit" icon="el-icon-edit">修改</el-button>
        <el-button size="small" type="danger" @click="handleDelete" icon="el-icon-delete">删除</el-button>
        <el-button size="small" @click="resetQuery" icon="el-icon-refresh-right">重置</el-button>
      </el-form-item>
    </el-form>
    <el-tag>表名称：{{tableInfo.tableName}}</el-tag>
    <el-table border class="tb-edit" highlight-current-row :data="tableData" style="width: 100%"
              @selection-change="handleSelectionChange" ref="multipleTable">
      <af-table-column
        v-for="(item, index) in cols"
        :key="index"
        :prop="item.prop"
        :label="item.label"
        :type="item.type"
        :fixed="item.fixed"
      ></af-table-column>
    </el-table>
    <div align="right">
      共计{{total}}条
      <el-button type="primary" icon="el-icon-arrow-left" @click="handleLast" >上一页</el-button>
      <el-button type="primary" @click="handleNext">下一页<i class="el-icon-arrow-right"></i></el-button>
    </div>

    <el-dialog
      :close-on-click-modal="false"
      title="数据修改"
      :visible.sync="dataUpdate"
      width="500px"
      append-to-body
      top="auto">
      <el-form :model="form">
        <template v-for="columns in columnArray">
          <el-form-item :label="columns.label" v-show="columns.show" :label-width="formLabelWidth">
            <el-input v-model="columns.value" autocomplete="off" v-if="columns.isPrimaryKey==true" disabled></el-input>

            <el-input v-model="columns.value" autocomplete="off" v-if="columns.isPrimaryKey!=true"></el-input>

          </el-form-item>
        </template>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dataUpdate = false">取 消</el-button>
        <el-button type="warning" @click="updateData">修 改</el-button>
      </div>
    </el-dialog>

    <!--非结构化数据删除-->
    <el-dialog
      title="非结构化数据删除"
      :visible.sync="dialogFormVisible"
      width="500px"
      append-to-body>
      <el-form :model="fileForm">
        <el-form-item label="是否删除文件" >
          <el-switch
            v-model="fileForm.ifDeleteFile">
          </el-switch>
        </el-form-item>
        <el-form-item label="文件路径字段" >
          <el-select v-model="fileForm.filePathColumn" placeholder="请选择文件路径字段">
            <template v-for="item in columnList">
              <el-option :label="item.label" :value="item.prop"></el-option>
            </template>
          </el-select>
        </el-form-item>
        <span>提示：数据或文件删除后不可恢复！</span>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogFormVisible = false">取 消</el-button>
        <el-button type="primary" @click="doDeleteFile">确 定</el-button>
      </div>
    </el-dialog>

  </el-main>
</template>

<script>
import { dataSearchList } from "@/api/structureManagement/tableStructureManage/StructureManageTable";
import { deleteData } from "@/api/structureManagement/tableStructureManage/StructureManageTable";
import { updateData } from "@/api/structureManagement/tableStructureManage/StructureManageTable";

export default {
  name: "SampleData",
  props: { rowData: Object, tableInfo: Object },
  data() {
    return {
      cols: [],
      pageStates: [null],
      tableData: [],
      columnData: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
      },
      total: 0,
      selectSymbol:'',
      timeInput:'',
      selectParamList:[],
      multipleTable:[],
      dataUpdate:false,
      form: {
        desc: ''
      },
      formLabelWidth: '200px',
      columnArray:[],
      dialogFormVisible:false,
      fileForm:{


        ifDeleteFile:false,
        filePathColumn:''
      },
      columnList:[],
      columnList_p:[],
      tablePrimaryKey:''
    };
  },
  created() {
    this.getSampleData();
  },
  methods: {
    handleLast(){
      let arr = [];
      let checkbox = {
        fixed:true,
        type: 'selection',
        width: '50'
      };
      this.cols = [];
      this.columnList = [];
      this.cols.push(checkbox);
      this.colType = {};
      for (let i = 0; i < this.columnData.length; i++) {
        arr.push(this.columnData[i].dbEleCode.toLowerCase());
        let obj = {
          label: this.columnData[i].eleName,
          prop: this.columnData[i].dbEleCode.toLowerCase()
        };
        this.cols.push(obj);
        this.columnList.push(obj);
        //获取表结构主键
        if(this.columnData[i].isPrimaryKey){
          this.tablePrimaryKey = this.columnData[i].dbEleCode.toLowerCase();
        }
        this.colType[this.columnData[i].dbEleCode.toLowerCase()]=this.columnData[i].type;
      }
      let params = {};
      console.log(this.pageStates);
      if(this.pageStates.length>=2){
        params.pageState=this.pageStates[this.pageStates.length-2];
        this.pageStates.splice(this.pageStates.length-1,1);
        console.log(this.pageStates);

      }
      params.column = arr;
      params.databaseId = this.rowData.DATABASE_ID;
      params.tableName = this.tableInfo.tableName;
      params.pageNum = this.queryParams.pageNum;
      params.pageSize = this.queryParams.pageSize;
      params.checkList = this.selectParamList;
      params.colType = this.colType;
      console.log(params);
      dataSearchList(params).then(res => {
        if (res.code == 200) {
          this.tableData = res.data.pageData;
          this.total=res.data.totalCount;
        } else {
          this.$message({
            message: res.msg,
            type: "error"
          });
        }
      });
    },
    handleNext(){
      let arr = [];
      let checkbox = {
        fixed:true,
        type: 'selection',
        width: '50'
      };
      this.cols = [];
      this.columnList = [];
      this.cols.push(checkbox);
      this.colType = {};
      for (let i = 0; i < this.columnData.length; i++) {
        arr.push(this.columnData[i].dbEleCode.toLowerCase());
        let obj = {
          label: this.columnData[i].eleName,
          prop: this.columnData[i].dbEleCode.toLowerCase()
        };
        this.cols.push(obj);
        this.columnList.push(obj);
        //获取表结构主键
        if(this.columnData[i].isPrimaryKey){
          this.tablePrimaryKey = this.columnData[i].dbEleCode.toLowerCase();
        }
        this.colType[this.columnData[i].dbEleCode.toLowerCase()]=this.columnData[i].type;
      }
      let params = {};
      if(this.pageStates.length>=1){
        params.pageState=this.pageStates[this.pageStates.length-1];
      }
      params.column = arr;
      params.databaseId = this.rowData.DATABASE_ID;
      params.tableName = this.tableInfo.tableName;
      params.pageNum = this.queryParams.pageNum;
      params.pageSize = this.queryParams.pageSize;
      params.checkList = this.selectParamList;
      params.colType = this.colType;
      console.log(params);
      dataSearchList(params).then(res => {
        if (res.code == 200) {
          if(null!=res.data.pageState){
            this.pageStates.push(res.data.pageState);
          }
          this.tableData = res.data.pageData;
          this.total=res.data.totalCount;
        } else {
          this.$message({
            message: res.msg,
            type: "error"
          });
        }
      });
    },
    getSampleData() {
      let arr = [];
      let checkbox = {
        fixed:true,
        type: 'selection',
        width: '50'
      };
      this.cols = [];
      this.columnList = [];
      this.columnList_p =[];
      this.cols.push(checkbox);
      this.colType = {};
      for (let i = 0; i < this.columnData.length; i++) {
        arr.push(this.columnData[i].dbEleCode.toLowerCase());
        let obj = {
          label: this.columnData[i].eleName,
          prop: this.columnData[i].dbEleCode.toLowerCase()
        };
        this.cols.push(obj);
        this.columnList.push(obj);
        //获取表结构主键
        if(this.columnData[i].isPrimaryKey){
          this.tablePrimaryKey = this.columnData[i].dbEleCode.toLowerCase();
          this.columnList_p.push(obj);
        }
        this.colType[this.columnData[i].dbEleCode.toLowerCase()]=this.columnData[i].type;
      }
      let params = {};
      params.column = arr;
      params.databaseId = this.rowData.DATABASE_ID;
      params.tableName = this.tableInfo.tableName;
      params.pageNum = this.queryParams.pageNum;
      params.pageSize = this.queryParams.pageSize;
      params.checkList = this.selectParamList;
      params.colType = this.colType;
      console.log(params);
      dataSearchList(params).then(res => {
        if (res.code == 200) {
          if(null!=res.data.pageState){
            this.pageStates.push(res.data.pageState);
          }

          console.log(this.pageStates)
          this.tableData = res.data.pageData;
          this.total=res.data.totalCount;
        } else {
          this.$message({
            message: res.msg,
            type: "error"
          });
        }
      });
    },
    // table自增定义方法
    table_index(index) {
      return (
        (this.queryParams.pageNum - 1) * this.queryParams.pageSize + index + 1
      );
    },
    //查询
    handleQuery(){
      this.queryParams.pageNum = 1;
      this.queryParams.pagesize = 10;
      this.getSampleData();
    },
    //修改
    handleEdit(){
      if(this.multipleTable.length==0){
        this.$message({
          type:'warning',
          message: "请选择需要修改的数据！",
        });
        return;
      }
      if(this.multipleTable.length>1){
        this.$message({
          type:'warning',
          message: "每次只能修改一条数据！",
        });
        return;
      }
      //打开弹窗
      this.dataUpdate = true;
      this.columnArray = [];
      //遍历内容
      let columnList = this.tableInfo.columns;
      let checkColumn = this.multipleTable[0];
      for(let i in columnList){
        let columnInfo = columnList[i];
        let columnEN = columnInfo.dbEleCode.toLowerCase();
        let columnName = columnInfo.eleName;
        for(let checked in checkColumn){
          if(checked===columnEN){
            let show = true;
            //记录标志作为记录主键，不修改
            if(columnEN===this.tablePrimaryKey){
              show = false;
            }
            let obj = {
              label:columnName,
              prop:checked,
              value:checkColumn[checked],
              show:show,
              isPrimaryKey:columnList[i].isPrimaryKey
            }
            this.columnArray.push(obj);
          }
        }
      }
    },
    //执行修改操作
    updateData(){
      let params = {};
      params.databaseId = this.rowData.DATABASE_ID;
      params.tableName = this.tableInfo.tableName;
      params.checkList = this.columnArray;
      params.primaryKey = this.tablePrimaryKey;
      params.colType = this.colType;
      updateData(params).then(res => {
        this.dataUpdate = false;
        if (res.code == 200) {
          this.$message({
            type:'success',
            message: "修改成功！",
          });
          //查询
          this.handleQuery();
        } else {
          this.$message({
            message: res.msg,
            type: "error"
          });
        }
      });
    },
    //删除
    handleDelete(){
      console.log(this.rowData);

      if(this.multipleTable.length==0){
        this.$message({
          type:'warning',
          message: "请选择需要删除的数据！",
        });
        return;
      }
      if(this.multipleTable.length>1){
        this.$message({
          type:'warning',
          message: "每次只能修改一条数据！",
        });
        return;
      }
      let type = this.rowData.DATABASE_DEFINE_ID;
      if(type=='FIDB'){
        //删除数据及文件
        this.handleDeleteFile();
      }else{
        //删除数据
        this.handleDeleteData();
      }
    },
    //删除数据
    handleDeleteData(){

      let params = {};
      params.databaseId = this.rowData.DATABASE_ID;
      params.tableName = this.tableInfo.tableName;
      params.primaryKey = this.tablePrimaryKey;
      params.colType = this.colType;
      this.columnArray = [];
      //遍历内容
      let columnList = this.tableInfo.columns;
      let checkColumn = this.multipleTable[0];
      for(let i in columnList){
        let columnInfo = columnList[i];
        let columnEN = columnInfo.dbEleCode.toLowerCase();
        let columnName = columnInfo.eleName;
        for(let checked in checkColumn){
          if(checked===columnEN){
            let show = true;
            //记录标志作为记录主键，不修改
            if(columnEN===this.tablePrimaryKey){
              show = false;
            }
            let obj = {
              label:columnName,
              prop:checked,
              value:checkColumn[checked],
              show:show,
              isPrimaryKey:columnList[i].isPrimaryKey
            }
            this.columnArray.push(obj);
          }
        }
      }
      params.checkList = this.columnArray;
      this.$confirm('数据删除后不可恢复，确认删除？')
        .then(_ => {
          deleteData(params).then(res => {
            if (res.code == 200) {
              this.$message({
                type:'success',
                message: "删除成功！",
              });
              //查询
              this.handleQuery();
            } else {
              this.$message({
                message: res.msg,
                type: "error"
              });
            }
          })
        })
        .catch(_ => {});
    },
    //删除数据及文件
    handleDeleteFile(){
      this.dialogFormVisible = true;
    },
    //删除数据及文件
    doDeleteFile(){
      //获取删除ID
      let ids = [];
      for(let i in this.multipleTable){
        let d_retain_id = this.multipleTable[i][this.tablePrimaryKey];
        ids.push(d_retain_id);
      }
      let params = {};
      params.databaseId = this.rowData.DATABASE_ID;
      params.tableName = this.tableInfo.tableName;
      params.ids = ids;
      params.primaryKey = this.tablePrimaryKey;
      params.ifDeleteFile = this.fileForm.ifDeleteFile;
      params.filePathColumn = this.fileForm.filePathColumn;
      console.log(params);
      deleteData(params).then(res => {
        if (res.code == 200) {
          this.dialogFormVisible = false;
          this.$message({
            type:'success',
            message: "删除成功！",
          });
          //查询
          this.handleQuery();
        } else {
          this.$message({
            message: res.msg,
            type: "error"
          });
        }
      })
    },
    //重置
    resetQuery(){
      this.selectParamList = [];
      this.getSampleData();
    },
    //选择查询条件
    handleCommand(command) {
      let commandArray = command.split("|");
      let selectParam ={
        label:commandArray[0],
        prop:commandArray[1],
        value:'',
        symbol:'='
      }
      this.selectParamList.push(selectParam);
    },
    //复选框操作
    handleSelectionChange(val){
      this.multipleTable = val;
    }
  },
  watch: {
    tableInfo(val) {
      this.columnData = this.tableInfo.columns;
      this.getSampleData();
    }
  }
};
</script>

<style scoped>
  .searchBox {
    margin-bottom: 20px;
  }
  .el-select .el-input {
    width: 130px;
  }
  .input-with-select .el-input-group__prepend {
    background-color: #fff;
  }
</style>

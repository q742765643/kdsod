import request from '@/utils/request'
const baseUrl = process.env.VUE_APP_SYSTEM;

// 获取资料分类统计数量
export function findDataCount(query) {
  return request({
    url: baseUrl + '/index/findDataCount',
    method: 'get',
    params: query
  })
}

// 获取最近12个月的资料数量统计
export function findDataMonthCount(query) {
  return request({
    url: baseUrl + '/index/findDataMonthCount',
    method: 'get',
    params: query
  })
}
// 获取已办待办
export function findUndoCount(query) {
  return request({
    url: baseUrl + '/index/findUndoCount',
    method: 'get',
    params: query
  })
}

//

export function findLogicInfo(query) {
  return request({
    url: baseUrl + '/index/findLogicInfo',
    method: 'get',
    params: query
  })
}

// 获取所有已审核专题库信息
export function findSpecialDbList(query) {
  return request({
    url: baseUrl + '/index/findSpecialDbList',
    method: 'get',
    params: query
  })
}

// 获取文件列表
export function findFileList(query) {
  return request({
    url: baseUrl + '/index/findFileList',
    method: 'get',
    params: query
  })
}

// 按数据库分类
export function findLogicCountData(query) {
  return request({
    url: baseUrl + '/index/findLogicCountData',
    method: 'get',
    params: query
  })
}

// 资料到报统计
export function findArrivedDataCount(query) {
  return request({
    url: baseUrl + '/dm/dataSearch/findArrivedPro',
    method: 'get',
    params: query
  })
}

// 数据库资料统计
export function findDataTableCount(query) {
  return request({
    url: baseUrl + '/dm/dataSearch/findDataTableCount',
    method: 'get',
    params: query
  })
}

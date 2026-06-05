import request from '@/utils/request'

export function submitReport(data) {
  return request({
    url: '/report',
    method: 'post',
    data
  })
}

export function getReportList(params) {
  return request({
    url: '/report/list',
    method: 'get',
    params
  })
}

export function getMyReports(params) {
  return request({
    url: '/report/mine',
    method: 'get',
    params
  })
}

export function getReportDetail(id) {
  return request({
    url: `/report/${id}`,
    method: 'get'
  })
}

export function handleReport(id, params) {
  return request({
    url: `/report/${id}/handle`,
    method: 'put',
    params
  })
}

export function getReportStatistics() {
  return request({
    url: '/report/statistics',
    method: 'get'
  })
}

export function getReportDistribution() {
  return request({
    url: '/report/distribution',
    method: 'get'
  })
}

import request from '@/utils/request'

export function startExam(params) {
  return request({
    url: '/exam/start',
    method: 'post',
    params
  })
}

export function submitExam(userId, data) {
  return request({
    url: '/exam/submit',
    method: 'post',
    params: { userId },
    data
  })
}

export function getExamRecordList(params) {
  return request({
    url: '/exam/records',
    method: 'get',
    params
  })
}

export function getExamRecordDetail(recordId) {
  return request({
    url: `/exam/record/${recordId}`,
    method: 'get'
  })
}

export function getExamRanking(topN = 10) {
  return request({
    url: '/exam/ranking',
    method: 'get',
    params: { topN }
  })
}

export function getQuestionList(params) {
  return request({
    url: '/exam/question/list',
    method: 'get',
    params
  })
}

export function addQuestion(data) {
  return request({
    url: '/exam/question',
    method: 'post',
    data
  })
}

export function updateQuestion(id, data) {
  return request({
    url: `/exam/question/${id}`,
    method: 'put',
    data
  })
}

export function deleteQuestion(id) {
  return request({
    url: `/exam/question/${id}`,
    method: 'delete'
  })
}

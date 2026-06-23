import request from '@/axios';

export const getList = (current, size, params) => {
  return request({
    url: '/blade-sportinviteauditlog/sportInviteAuditLog/list',
    method: 'get',
    params: {
      ...params,
      current,
      size,
    }
  })
}

export const getDetail = (id) => {
  return request({
    url: '/blade-sportinviteauditlog/sportInviteAuditLog/detail',
    method: 'get',
    params: {
      id
    }
  })
}

export const remove = (ids) => {
  return request({
    url: '/blade-sportinviteauditlog/sportInviteAuditLog/remove',
    method: 'post',
    params: {
      ids,
    }
  })
}

export const add = (row) => {
  return request({
    url: '/blade-sportinviteauditlog/sportInviteAuditLog/submit',
    method: 'post',
    data: row
  })
}

export const update = (row) => {
  return request({
    url: '/blade-sportinviteauditlog/sportInviteAuditLog/submit',
    method: 'post',
    data: row
  })
}


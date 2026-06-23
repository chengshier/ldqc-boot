import request from '@/axios';

export const getList = (current, size, params) => {
  return request({
    url: '/blade-sportinvitemedia/sportInviteMedia/list',
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
    url: '/blade-sportinvitemedia/sportInviteMedia/detail',
    method: 'get',
    params: {
      id
    }
  })
}

export const remove = (ids) => {
  return request({
    url: '/blade-sportinvitemedia/sportInviteMedia/remove',
    method: 'post',
    params: {
      ids,
    }
  })
}

export const add = (row) => {
  return request({
    url: '/blade-sportinvitemedia/sportInviteMedia/submit',
    method: 'post',
    data: row
  })
}

export const update = (row) => {
  return request({
    url: '/blade-sportinvitemedia/sportInviteMedia/submit',
    method: 'post',
    data: row
  })
}


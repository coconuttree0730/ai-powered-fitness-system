import request from '@/utils/request'

export function getVideoCourseList(params) {
  return request({
    url: '/admin/video-courses',
    method: 'get',
    params
  })
}

export function getVideoCourseDetail(id) {
  return request({
    url: `/admin/video-courses/${id}`,
    method: 'get'
  })
}

export function createVideoCourse(data) {
  return request({
    url: '/admin/video-courses',
    method: 'post',
    data
  })
}

export function updateVideoCourse(id, data) {
  return request({
    url: `/admin/video-courses/${id}`,
    method: 'put',
    data
  })
}

export function deleteVideoCourse(id) {
  return request({
    url: `/admin/video-courses/${id}`,
    method: 'delete'
  })
}

export function getVideoCourseCategories() {
  return request({
    url: '/admin/video-courses/categories',
    method: 'get'
  })
}

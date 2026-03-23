import request from '@/utils/request'

export function getPublicCourses(params) {
  return request({
    url: '/courses/public/list',
    method: 'get',
    params
  })
}

export function getCourseList(params) {
  return request({
    url: '/courses/list',
    method: 'get',
    params
  })
}

export function getCourseDetail(id) {
  return request({
    url: `/courses/${id}`,
    method: 'get'
  })
}

export function createCourse(data) {
  return request({
    url: '/admin/courses',
    method: 'post',
    data
  })
}

export function updateCourse(id, data) {
  return request({
    url: `/admin/courses/${id}`,
    method: 'put',
    data
  })
}

export function deleteCourse(id) {
  return request({
    url: `/admin/courses/${id}`,
    method: 'delete'
  })
}

export function bookCourse(courseId) {
  return request({
    url: '/bookings',
    method: 'post',
    data: { courseId }
  })
}

export function cancelBooking(bookingId) {
  return request({
    url: `/bookings/${bookingId}/cancel`,
    method: 'put'
  })
}

export function getMyBookings() {
  return request({
    url: '/bookings/my',
    method: 'get'
  })
}

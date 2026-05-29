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

// ========== 预约相关（基于课程实例） ==========

export function cancelBooking(bookingId, data = {}) {
  return request({
    url: `/bookings/${bookingId}/cancel`,
    method: 'put',
    data
  })
}

export function getMyBookings() {
  return request({
    url: '/bookings/my',
    method: 'get'
  })
}

export function getCoachMySessions(params) {
  return request({
    url: '/course-sessions/coach/my',
    method: 'get',
    params
  })
}

export function getCourseCategories() {
  return request({
    url: '/courses/categories',
    method: 'get'
  })
}

/**
 * 获取首页课程体系数据（按分类分组）
 * @returns {Promise} 课程体系分类列表
 */
export function getHomePageCourses() {
  return request({
    url: '/courses/homepage/categories',
    method: 'get'
  })
}

/**
 * 获取首页课程卡片列表
 * @param {number} limit 限制数量（默认6）
 * @returns {Promise} 课程卡片列表
 */
export function getHomePageCourseCards(limit = 6) {
  return request({
    url: '/courses/homepage/cards',
    method: 'get',
    params: { limit }
  })
}

// ========== 课程实例（周期性课程的具体某一次） ==========

/**
 * 获取课程实例列表
 */
export function getCourseSessions(params) {
  return request({
    url: '/course-sessions/list',
    method: 'get',
    params
  })
}

function getTodayKey() {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

/**
 * 获取课程实例详情
 */
export function getSessionDetail(sessionId) {
  return request({
    url: `/course-sessions/${sessionId}`,
    method: 'get'
  })
}

/**
 * 获取即将开始的课程实例
 */
export function getUpcomingSessions(limit = 10) {
  return request({
    url: '/course-sessions/upcoming',
    method: 'get',
    params: { limit }
  })
}

/**
 * 手动生成未来N周的课程实例
 */
export function generateFutureSessions(weeksAhead = 4) {
  return request({
    url: '/course-sessions/generate',
    method: 'post',
    params: { weeksAhead }
  })
}

/**
 * 预约课程实例（基于具体某一天）
 */
export function bookSession(sessionId, courseId) {
  return request({
    url: '/bookings',
    method: 'post',
    data: { sessionId, courseId }
  })
}

export async function bookCourse(courseId) {
  const page = await getCourseSessions({
    courseId,
    pageNum: 1,
    pageSize: 1,
    startDate: getTodayKey()
  })

  const session = page?.records?.[0]
  if (!session) {
    throw new Error('当前课程暂无可预约场次')
  }

  return bookSession(session.id, courseId)
}

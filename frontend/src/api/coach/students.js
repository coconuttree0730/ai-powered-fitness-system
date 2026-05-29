import request from '@/utils/request'

export function getMyStudents() {
  return request.get('/coach/students/my')
}

export function getStudentBinding(memberId) {
  return request.get('/coach/students/binding', { params: { memberId } })
}
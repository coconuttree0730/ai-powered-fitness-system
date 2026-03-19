import request from '@/utils/request'

export function getEquipmentList(params) {
  return request({
    url: '/equipment/list',
    method: 'get',
    params
  })
}

export function getEquipmentDetail(id) {
  return request({
    url: `/equipment/${id}`,
    method: 'get'
  })
}

export function createEquipment(data) {
  return request({
    url: '/admin/equipment',
    method: 'post',
    data
  })
}

export function updateEquipment(id, data) {
  return request({
    url: `/admin/equipment/${id}`,
    method: 'put',
    data
  })
}

export function deleteEquipment(id) {
  return request({
    url: `/admin/equipment/${id}`,
    method: 'delete'
  })
}

export function submitRepair(data) {
  return request({
    url: '/repairs',
    method: 'post',
    data
  })
}

export function getMyRepairs() {
  return request({
    url: '/repairs/my',
    method: 'get'
  })
}

export function cancelRepair(repairId) {
  return request({
    url: `/repairs/${repairId}/cancel`,
    method: 'put'
  })
}

export function getAllRepairs(params) {
  return request({
    url: '/admin/equipment/repairs',
    method: 'get',
    params
  })
}

export function handleRepair(repairId, data) {
  return request({
    url: `/admin/equipment/repairs/${repairId}`,
    method: 'put',
    params: data
  })
}

export function getEquipmentTypes() {
  return request({
    url: '/admin/equipment/types',
    method: 'get'
  })
}

export function getEquipmentRepairs(equipmentId) {
  return request({
    url: `/equipment/${equipmentId}/repairs`,
    method: 'get'
  })
}

import request from '@/utils/request'

/**
 * 上传文件
 * @param {FormData} formData 包含文件的 FormData 对象
 * @param {string} folder 存储文件夹，默认为 files
 * @returns {Promise} 上传结果
 */
export function uploadFile(formData, folder = 'files') {
  return request({
    url: '/files/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    params: { folder }
  })
}

/**
 * 上传图片
 * @param {FormData} formData 包含图片的 FormData 对象
 * @returns {Promise} 上传结果
 */
export function uploadImage(formData) {
  return request({
    url: '/files/upload/image',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 删除文件
 * @param {string} fileUrl 文件URL
 * @returns {Promise} 删除结果
 */
export function deleteFile(fileUrl) {
  return request({
    url: '/files',
    method: 'delete',
    params: { fileUrl }
  })
}

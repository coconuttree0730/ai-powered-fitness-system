import request from '@/utils/request'

/**
 * 获取知识库文档列表
 * @param {Object} params 查询参数
 * @returns {Promise} 文档列表
 */
export function getKnowledgeDocuments(params) {
  return request({
    url: '/knowledge/documents',
    method: 'get',
    params
  })
}

/**
 * 获取文档详情
 * @param {number} id 文档ID
 * @returns {Promise} 文档详情
 */
export function getKnowledgeDocumentById(id) {
  return request({
    url: `/knowledge/documents/${id}`,
    method: 'get'
  })
}

/**
 * 创建文档
 * @param {Object} data 文档信息
 * @returns {Promise} 创建后的文档ID
 */
export function createKnowledgeDocument(data) {
  return request({
    url: '/knowledge/documents',
    method: 'post',
    data
  })
}

/**
 * 上传文档文件
 * @param {FormData} formData 包含文件的表单数据
 * @returns {Promise} 上传后的文档ID
 */
export function uploadKnowledgeDocument(formData) {
  return request({
    url: '/knowledge/documents/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 更新文档
 * @param {number} id 文档ID
 * @param {Object} data 文档信息
 * @returns {Promise} 更新结果
 */
export function updateKnowledgeDocument(id, data) {
  return request({
    url: `/knowledge/documents/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除文档
 * @param {number} id 文档ID
 * @returns {Promise} 删除结果
 */
export function deleteKnowledgeDocument(id) {
  return request({
    url: `/knowledge/documents/${id}`,
    method: 'delete'
  })
}

/**
 * 发布文档
 * @param {number} id 文档ID
 * @returns {Promise} 发布结果
 */
export function publishKnowledgeDocument(id) {
  return request({
    url: `/knowledge/documents/${id}/publish`,
    method: 'patch'
  })
}

/**
 * 归档文档
 * @param {number} id 文档ID
 * @returns {Promise} 归档结果
 */
export function archiveKnowledgeDocument(id) {
  return request({
    url: `/knowledge/documents/${id}/archive`,
    method: 'patch'
  })
}

/**
 * 重新索引文档
 * @param {number} id 文档ID
 * @returns {Promise} 索引结果
 */
export function reindexKnowledgeDocument(id) {
  return request({
    url: `/knowledge/documents/${id}/reindex`,
    method: 'post'
  })
}

/**
 * RAG搜索
 * @param {Object} data 搜索参数
 * @returns {Promise} 搜索结果
 */
export function searchKnowledgeRAG(data) {
  return request({
    url: '/knowledge/rag/search',
    method: 'post',
    data
  })
}

/**
 * RAG对话
 * @param {string} query 查询内容
 * @returns {Promise} 对话结果
 */
export function chatWithKnowledgeRAG(query) {
  return request({
    url: '/knowledge/rag/chat',
    method: 'post',
    params: {
      query
    }
  })
}

// ==================== 知识库分类管理 API ====================

/**
 * 获取所有知识库分类
 * @returns {Promise} 分类列表
 */
export function getKnowledgeCategories() {
  return request({
    url: '/knowledge/categories',
    method: 'get'
  })
}

/**
 * 获取分类详情
 * @param {number} id 分类ID
 * @returns {Promise} 分类详情
 */
export function getKnowledgeCategoryById(id) {
  return request({
    url: `/knowledge/categories/${id}`,
    method: 'get'
  })
}

/**
 * 创建分类
 * @param {Object} data 分类信息
 * @returns {Promise} 创建后的分类ID
 */
export function createKnowledgeCategory(data) {
  return request({
    url: '/knowledge/categories',
    method: 'post',
    data
  })
}

/**
 * 更新分类
 * @param {number} id 分类ID
 * @param {Object} data 分类信息
 * @returns {Promise} 更新结果
 */
export function updateKnowledgeCategory(id, data) {
  return request({
    url: `/knowledge/categories/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除分类
 * @param {number} id 分类ID
 * @returns {Promise} 删除结果
 */
export function deleteKnowledgeCategory(id) {
  return request({
    url: `/knowledge/categories/${id}`,
    method: 'delete'
  })
}

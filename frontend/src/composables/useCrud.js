import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

export function useCrud({ fetchFn, deleteFn, updateStatusFn, nameKey = 'name' } = {}) {
  const loading = ref(false)
  const tableData = ref([])
  const showModal = ref(false)
  const isEdit = ref(false)
  const submitting = ref(false)
  const currentId = ref(null)
  const formRef = ref(null)

  const pagination = reactive({ page: 1, pageSize: 10, total: 0 })
  const itemCount = computed(() => pagination.total)
  const searchForm = reactive({})

  function buildSearchParams() {
    const params = {}
    Object.entries(searchForm).forEach(([k, v]) => {
      if (v !== '' && v !== null && v !== undefined) params[k] = v
    })
    return params
  }

  async function fetchData(params = {}) {
    if (!fetchFn) return
    loading.value = true
    try {
      const res = await fetchFn({
        pageNum: pagination.page,
        pageSize: pagination.pageSize,
        ...buildSearchParams(),
        ...params
      })
      tableData.value = res?.records || []
      pagination.total = res?.total || 0
    } catch {
      ElMessage.error('获取数据失败')
    } finally {
      loading.value = false
    }
  }

  function handleSearch() {
    pagination.page = 1
    fetchData()
  }

  function handleReset() {
    Object.keys(searchForm).forEach(k => delete searchForm[k])
    handleSearch()
  }

  function handlePageChange(p) {
    pagination.page = p
    fetchData()
  }

  function handleSizeChange(s) {
    pagination.pageSize = s
    fetchData()
  }

  function resetForm(form) {
    Object.keys(form).forEach(k => {
      if (Array.isArray(form[k])) form[k] = []
      else if (typeof form[k] === 'number') form[k] = null
      else form[k] = ''
    })
  }

  function handleAdd(resetFn) {
    isEdit.value = false
    currentId.value = null
    showModal.value = true
    resetFn?.()
  }

  function handleEdit(row, fillFn) {
    isEdit.value = true
    currentId.value = row.id
    showModal.value = true
    fillFn?.(row)
    return row
  }

  async function handleDelete(row, customDeleteFn) {
    const delFn = customDeleteFn || deleteFn
    if (!delFn) return
    try {
      await ElMessageBox.confirm(
        `确定删除"${row[nameKey]}"吗？`,
        '确认删除',
        { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
      )
      await delFn(row.id)
      ElMessage.success('删除成功')
      fetchData()
    } catch { /* 用户取消 */ }
  }

  async function handleSubmit(form, apiFn, beforeSubmit) {
    if (!formRef.value || !apiFn) return
    try {
      await formRef.value.validate()
    } catch {
      return
    }

    beforeSubmit?.()

    submitting.value = true
    try {
      if (isEdit.value) {
        await apiFn(currentId.value, form)
        ElMessage.success('更新成功')
      } else {
        await apiFn(form)
        ElMessage.success('创建成功')
      }
      showModal.value = false
      fetchData()
    } catch {
      ElMessage.error('操作失败')
    } finally {
      submitting.value = false
    }
  }

  async function handleStatusChange(row, newStatus) {
    if (!updateStatusFn) return
    try {
      await updateStatusFn(row.id, newStatus)
      ElMessage.success('状态更新成功')
      fetchData()
    } catch {
      ElMessage.error('状态更新失败')
    }
  }

  return {
    loading,
    tableData,
    showModal,
    isEdit,
    submitting,
    currentId,
    formRef,
    pagination,
    itemCount,
    searchForm,
    fetchData,
    handleSearch,
    handleReset,
    handlePageChange,
    handleSizeChange,
    handleAdd,
    handleEdit,
    handleDelete,
    handleSubmit,
    handleStatusChange,
    resetForm,
    buildSearchParams
  }
}
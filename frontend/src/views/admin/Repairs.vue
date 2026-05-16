<template>
  <div class="admin-repairs">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>报修管理</span>
          <el-space>
            <el-input
              v-model="searchKeyword"
              placeholder="搜索用户"
              clearable
              style="width: 200px"
              @keyup.enter="handleSearch"
            />
            <el-select v-model="filterStatus" placeholder="状态筛选" clearable style="width: 120px">
              <el-option label="待处理" :value="0" />
              <el-option label="处理中" :value="1" />
              <el-option label="已完成" :value="2" />
              <el-option label="已关闭" :value="3" />
            </el-select>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-space>
        </div>
      </template>

      <el-table
        :data="displayRepairs"
        v-loading="loading"
        :row-key="row => row.id"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="userName" label="报修人" min-width="100" />
        <el-table-column label="报修器械" width="180">
          <template #default="{ row }">
            <span v-if="row.equipmentName || row.equipmentNo">{{ row.equipmentNo || '-' }} - {{ row.equipmentName || '-' }}</span>
            <span v-else style="color: #c0c4cc">未指定</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="问题描述" show-overflow-tooltip min-width="300" />
        <el-table-column label="图片" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.imageUrls && row.imageUrls.length" type="info" size="small">
              {{ row.imageUrls.length }}张
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <el-space>
              <el-button
                size="small"
                @click="viewDetail(row)"
              >
                查看详情
              </el-button>
              <el-button
                v-if="row.status === 0"
                size="small"
                type="primary"
                @click="handleProcess(row, 1)"
              >
                开始处理
              </el-button>
              <el-button
                v-if="row.status === 1"
                size="small"
                type="success"
                @click="handleProcess(row, 2)"
              >
                完成
              </el-button>
              <el-button
                v-if="row.status === 0 || row.status === 1"
                size="small"
                type="danger"
                @click="handleProcess(row, 3)"
              >
                关闭
              </el-button>
              <el-button
                size="small"
                type="danger"
                plain
                @click="handleDelete(row)"
              >
                删除
              </el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="paginationReactive.page"
          v-model:page-size="paginationReactive.pageSize"
          :total="paginationReactive.itemCount"
          :page-sizes="[5, 10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 报修详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="报修详情"
      width="700px"
      destroy-on-close
    >
      <div v-if="selectedRepair" class="repair-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="报修人">{{ selectedRepair.userName }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(selectedRepair.status)">{{ getStatusText(selectedRepair.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="报修时间" :span="2">{{ formatTime(selectedRepair.createTime) }}</el-descriptions-item>
          <el-descriptions-item v-if="selectedRepair.equipmentName || selectedRepair.equipmentNo" label="报修器械" :span="2">
            {{ selectedRepair.equipmentNo || '-' }} - {{ selectedRepair.equipmentName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="问题描述" :span="2">{{ selectedRepair.description }}</el-descriptions-item>
          <el-descriptions-item v-if="selectedRepair.handleRemark" label="处理备注" :span="2">
            {{ selectedRepair.handleRemark }}
          </el-descriptions-item>
        </el-descriptions>

        <div v-if="selectedRepair.imageUrls && selectedRepair.imageUrls.length" class="detail-images">
          <h4>问题图片</h4>
          <div class="image-list">
            <el-image
              v-for="(img, idx) in selectedRepair.imageUrls"
              :key="idx"
              :src="img"
              :preview-src-list="selectedRepair.imageUrls"
              :initial-index="idx"
              fit="cover"
              class="detail-image"
            />
          </div>
        </div>

        <div class="repair-timeline">
          <h4>处理记录</h4>
          <el-timeline v-if="selectedRepair.records && selectedRepair.records.length">
            <el-timeline-item
              v-for="(item, idx) in selectedRepair.records"
              :key="idx"
              :type="getRecordType(item.recordType)"
              :timestamp="formatTime(item.createTime)"
            >
              <div class="timeline-content">
                <div class="timeline-title">{{ getRecordTitle(item) }}</div>
                <div class="timeline-desc">{{ item.content }}</div>
                <div class="timeline-handler" v-if="item.handlerName">处理人: {{ item.handlerName }}</div>
              </div>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无处理记录" />
        </div>

        <!-- 添加处理记录 -->
        <div class="add-record-section">
          <h4>添加处理记录</h4>
          <el-input
            v-model="newRecordContent"
            type="textarea"
            :rows="3"
            placeholder="请输入处理备注..."
          />
          <div class="record-actions">
            <el-button type="primary" @click="addRecord">添加记录</el-button>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 处理报修弹窗 -->
    <el-dialog
      v-model="processDialogVisible"
      :title="processTitle"
      width="500px"
      destroy-on-close
    >
      <el-form :model="processForm" label-width="80px">
        <el-form-item label="处理备注">
          <el-input
            v-model="processForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入处理备注（可选）..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="processDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmProcess" :loading="processing">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllRepairs, getRepairDetail, handleRepair, addRepairRecord, deleteRepair } from '@/api/equipment'

const loading = ref(false)
const repairs = ref([])
const searchKeyword = ref('')
const filterStatus = ref('')
const detailDialogVisible = ref(false)
const processDialogVisible = ref(false)
const selectedRepair = ref(null)
const processing = ref(false)
const newRecordContent = ref('')

const processForm = reactive({
  status: null,
  remark: ''
})

const currentProcessRow = ref(null)

const processTitle = computed(() => {
  const titles = { 1: '开始处理', 2: '完成报修', 3: '关闭报修' }
  return titles[processForm.status] || '处理报修'
})

const paginationReactive = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0
})

const filteredRepairs = computed(() => {
  let result = repairs.value
  
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(r => 
      (r.userName && r.userName.toLowerCase().includes(keyword)) ||
      (r.description && r.description.toLowerCase().includes(keyword))
    )
  }
  
  if (filterStatus.value !== '' && filterStatus.value !== null) {
    result = result.filter(r => r.status === filterStatus.value)
  }
  
  return result
})

const displayRepairs = computed(() => {
  const start = (paginationReactive.page - 1) * paginationReactive.pageSize
  const end = start + paginationReactive.pageSize
  return filteredRepairs.value.slice(start, end)
})

function getStatusType(status) {
  const types = { 0: 'warning', 1: 'primary', 2: 'success', 3: 'info' }
  return types[status] || 'info'
}

function getStatusText(status) {
  const texts = { 0: '待处理', 1: '处理中', 2: '已完成', 3: '已关闭' }
  return texts[status] || '未知'
}

function getRecordType(recordType) {
  const types = { 1: 'primary', 2: 'warning', 3: 'info', 4: 'danger' }
  return types[recordType] || 'info'
}

function getRecordTitle(item) {
  const titles = { 1: '提交报修', 2: '状态变更', 3: '处理备注', 4: '取消报修' }
  return titles[item.recordType] || '处理记录'
}

function formatTime(time) {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

async function fetchRepairs() {
  loading.value = true
  try {
    const res = await getAllRepairs()
    repairs.value = res || []
    paginationReactive.itemCount = repairs.value.length
  } catch (error) {
    console.error('Fetch repairs error:', error)
    ElMessage.error('获取报修列表失败')
  } finally {
    loading.value = false
  }
}

async function viewDetail(row) {
  try {
    const res = await getRepairDetail(row.id)
    selectedRepair.value = res
    detailDialogVisible.value = true
  } catch (error) {
    console.error('Get repair detail error:', error)
    ElMessage.error('获取报修详情失败')
  }
}

function handleProcess(row, status) {
  currentProcessRow.value = row
  processForm.status = status
  processForm.remark = ''
  processDialogVisible.value = true
}

async function confirmProcess() {
  if (!currentProcessRow.value) return
  
  processing.value = true
  try {
    await handleRepair(currentProcessRow.value.id, {
      status: processForm.status,
      remark: processForm.remark
    })
    ElMessage.success('处理成功')
    processDialogVisible.value = false
    fetchRepairs()
  } catch (error) {
    console.error('Process repair error:', error)
    ElMessage.error('处理失败')
  } finally {
    processing.value = false
  }
}

async function addRecord() {
  if (!selectedRepair.value || !newRecordContent.value.trim()) {
    ElMessage.warning('请输入处理内容')
    return
  }
  
  try {
    await addRepairRecord(selectedRepair.value.id, newRecordContent.value.trim())
    ElMessage.success('添加记录成功')
    newRecordContent.value = ''
    // 刷新详情
    const res = await getRepairDetail(selectedRepair.value.id)
    selectedRepair.value = res
  } catch (error) {
    console.error('Add record error:', error)
    ElMessage.error('添加记录失败')
  }
}

function handleSearch() {
  paginationReactive.page = 1
}

function resetSearch() {
  searchKeyword.value = ''
  filterStatus.value = ''
  paginationReactive.page = 1
}

function handleSizeChange(size) {
  paginationReactive.pageSize = size
  paginationReactive.page = 1
}

function handlePageChange(page) {
  paginationReactive.page = page
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确定要删除这条报修记录吗？\n报修人：${row.userName}\n问题描述：${row.description}`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteRepair(row.id)
    ElMessage.success('删除成功')
    fetchRepairs()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      console.error('Delete repair error:', error)
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  fetchRepairs()
})
</script>

<style scoped>
.admin-repairs {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.repair-detail {
  max-height: 600px;
  overflow-y: auto;
}

.detail-images {
  margin-top: 20px;
}

.detail-images h4 {
  margin-bottom: 12px;
  font-size: 16px;
  color: #303133;
}

.image-list {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.detail-image {
  width: 120px;
  height: 120px;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
}

.repair-timeline {
  margin-top: 24px;
}

.repair-timeline h4 {
  margin-bottom: 16px;
  font-size: 16px;
  color: #303133;
}

.timeline-content {
  padding: 8px 0;
}

.timeline-title {
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.timeline-desc {
  color: #606266;
  font-size: 14px;
  margin-bottom: 4px;
}

.timeline-handler {
  color: #909399;
  font-size: 12px;
}

.add-record-section {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}

.add-record-section h4 {
  margin-bottom: 12px;
  font-size: 16px;
  color: #303133;
}

.record-actions {
  margin-top: 12px;
  text-align: right;
}
</style>

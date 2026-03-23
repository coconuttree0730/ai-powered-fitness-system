<template>
  <div class="admin-repairs">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>报修管理</span>
        </div>
      </template>

      <el-table
        :data="displayRepairs"
        v-loading="loading"
        :row-key="row => row.id"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="equipmentName" label="器材名称" />
        <el-table-column prop="userName" label="报修人" />
        <el-table-column prop="description" label="问题描述" show-overflow-tooltip />
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
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-space>
              <el-button
                v-if="row.status === 0"
                size="small"
                type="primary"
                @click="handleProcessClick(row.id, 1)"
              >
                开始处理
              </el-button>
              <el-button
                v-if="row.status === 0"
                size="small"
                type="danger"
                @click="handleProcessClick(row.id, 3)"
              >
                关闭
              </el-button>
              <el-button
                v-if="row.status === 1"
                size="small"
                type="success"
                @click="handleProcessClick(row.id, 2)"
              >
                完成
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getAllRepairs, handleRepair } from '@/api/equipment'

const message = ElMessage
const loading = ref(false)
const allRepairs = ref([])

const paginationReactive = reactive({
  page: 1,
  pageSize: 5,
  itemCount: 0
})

const displayRepairs = computed(() => {
  const start = (paginationReactive.page - 1) * paginationReactive.pageSize
  const end = start + paginationReactive.pageSize
  return allRepairs.value.slice(start, end)
})

const getStatusType = (status) => {
  const statusMap = {
    0: 'warning',
    1: 'primary',
    2: 'success',
    3: 'info'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    0: '待处理',
    1: '处理中',
    2: '已完成',
    3: '已关闭'
  }
  return statusMap[status] || '未知'
}

function handleSizeChange(size) {
  paginationReactive.pageSize = size
}

function handlePageChange(page) {
  paginationReactive.page = page
}

onMounted(() => {
  fetchRepairs()
})

async function fetchRepairs() {
  loading.value = true
  try {
    const res = await getAllRepairs()
    allRepairs.value = res || []
    paginationReactive.itemCount = allRepairs.value.length
  } catch (error) {
    message.error('获取报修列表失败')
  } finally {
    loading.value = false
  }
}

async function handleProcessClick(id, status) {
  try {
    await handleRepair(id, { status })
    message.success('操作成功')
    fetchRepairs()
  } catch (error) {
    message.error('操作失败')
  }
}

function formatTime(time) {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
.admin-repairs {
  padding: 0;
}

.card-header {
  font-weight: bold;
  font-size: 16px;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

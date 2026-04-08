<template>
  <div class="analysis-page">
    <el-card class="analysis-container">
      <template #header>
        <div class="card-header">
          <span>AI数据分析报告管理</span>
        </div>
      </template>

      <!-- 筛选区域 -->
      <div class="filter-section">
        <el-radio-group v-model="timeFilter" size="default" @change="handleFilterChange">
          <el-radio-button label="today">本日</el-radio-button>
          <el-radio-button label="month">本月</el-radio-button>
          <el-radio-button label="year">本年</el-radio-button>
        </el-radio-group>
        <el-tag type="info" effect="plain" class="count-tag">
          共 {{ filteredReports.length }} 条记录
        </el-tag>
      </div>

      <!-- 报告列表 -->
      <el-table
        :data="filteredReports"
        stripe
        style="width: 100%"
        v-loading="loading"
        class="report-table"
      >
        <el-table-column type="index" label="序号" width="80" align="center" />
        <el-table-column prop="analysisType" label="分析类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getAnalysisTypeTag(row.analysisType)" size="small">
              {{ getAnalysisTypeText(row.analysisType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="generateTime" label="生成日期" width="180" align="center">
          <template #default="{ row }">
            <el-icon class="time-icon"><Clock /></el-icon>
            {{ formatDateTime(row.generateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              size="small"
              @click="handleView(row)"
            >
              <el-icon><View /></el-icon>
              查看
            </el-button>
            <el-button
              type="danger"
              link
              size="small"
              @click="handleDelete(row)"
            >
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="filteredReports.length"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 查看报告详情对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="报告详情"
      width="800px"
      destroy-on-close
      class="report-detail-dialog"
    >
      <div v-if="currentReport" class="report-detail">
        <div class="detail-header">
          <h3>{{ currentReport.reportTitle }}</h3>
          <el-tag :type="getAnalysisTypeTag(currentReport.analysisType)">
            {{ getAnalysisTypeText(currentReport.analysisType) }}
          </el-tag>
        </div>
        <el-divider />
        <div class="detail-content">
          <div class="detail-item">
            <span class="label">生成时间：</span>
            <span class="value">{{ formatDateTime(currentReport.generateTime) }}</span>
          </div>
          <div class="detail-item">
            <span class="label">报告内容：</span>
            <div class="content-text">{{ currentReport.reportContent }}</div>
          </div>
          <div class="detail-item" v-if="currentReport.suggestions?.length">
            <span class="label">优化建议：</span>
            <ul class="suggestions-list">
              <li v-for="(suggestion, index) in currentReport.suggestions" :key="index">
                {{ suggestion }}
              </li>
            </ul>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Clock, View, Delete } from '@element-plus/icons-vue'

// 加载状态
const loading = ref(false)

// 筛选条件
const timeFilter = ref('today')

// 分页
const currentPage = ref(1)
const pageSize = ref(10)

// 对话框显示状态
const viewDialogVisible = ref(false)

// 当前查看的报告
const currentReport = ref(null)

// 获取今天的日期字符串
const today = new Date().toISOString().split('T')[0]

// 模拟报告数据 - 包含今天的数据
const mockReports = ref([
  {
    id: 1,
    reportTitle: '2026年4月8日 - 综合运营分析报告',
    analysisType: 'OVERALL',
    generateTime: `${today} 09:30:00`,
    reportContent: '本报告对健身房整体运营情况进行了全面分析。本月会员活跃度较上月提升15%，课程预约率达到78%，营收同比增长22%。建议加强高峰时段的课程安排，优化器材配置以满足会员需求。',
    suggestions: [
      '增加晚间高峰时段的课程数量',
      '优化器材维护计划，减少故障率',
      '推出会员推荐奖励计划',
      '加强新会员 onboarding 流程'
    ]
  }
])

// 根据筛选条件过滤报告
const filteredReports = computed(() => {
  const now = new Date()
  let filtered = mockReports.value

  switch (timeFilter.value) {
    case 'today':
      // 本日：显示今天生成的报告
      filtered = mockReports.value.filter(report => {
        const reportDate = new Date(report.generateTime)
        return reportDate.toDateString() === now.toDateString()
      })
      break
    case 'month':
      // 本月：显示本月生成的报告
      filtered = mockReports.value.filter(report => {
        const reportDate = new Date(report.generateTime)
        return reportDate.getMonth() === now.getMonth() &&
               reportDate.getFullYear() === now.getFullYear()
      })
      break
    case 'year':
      // 本年：显示今年生成的报告
      filtered = mockReports.value.filter(report => {
        const reportDate = new Date(report.generateTime)
        return reportDate.getFullYear() === now.getFullYear()
      })
      break
  }

  // 按时间倒序排列
  return filtered.sort((a, b) => new Date(b.generateTime) - new Date(a.generateTime))
})

// 获取分析类型标签样式
function getAnalysisTypeTag(type) {
  const tagMap = {
    'OVERALL': 'primary',
    'REVENUE': 'success',
    'MEMBER': 'warning',
    'COURSE': 'info'
  }
  return tagMap[type] || 'info'
}

// 获取分析类型显示文本
function getAnalysisTypeText(type) {
  const textMap = {
    'OVERALL': '综合分析',
    'REVENUE': '营收分析',
    'MEMBER': '会员分析',
    'COURSE': '课程分析'
  }
  return textMap[type] || type
}

// 格式化日期时间
function formatDateTime(dateTime) {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 筛选变化处理
function handleFilterChange() {
  currentPage.value = 1
  ElMessage.success(`已切换到${timeFilter.value === 'today' ? '本日' : timeFilter.value === 'month' ? '本月' : '本年'}数据`)
}

// 查看报告
function handleView(row) {
  currentReport.value = row
  viewDialogVisible.value = true
}

// 删除报告
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确定要删除报告 "${row.reportTitle}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    // 模拟删除
    const index = mockReports.value.findIndex(r => r.id === row.id)
    if (index > -1) {
      mockReports.value.splice(index, 1)
      ElMessage.success('删除成功')
    }
  } catch {
    // 用户取消删除
  }
}

// 分页处理
function handleSizeChange(val) {
  pageSize.value = val
}

function handleCurrentChange(val) {
  currentPage.value = val
}

onMounted(() => {
  loading.value = true
  // 模拟加载数据
  setTimeout(() => {
    loading.value = false
  }, 500)
})
</script>

<style scoped>
.analysis-page {
  padding: 0;
}

.analysis-container {
  min-height: calc(100vh - 120px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 16px;
}

/* 筛选区域 */
.filter-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.count-tag {
  font-size: 14px;
}

/* 表格样式 */
.report-table {
  margin-bottom: 20px;
}

.report-title-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.report-icon {
  flex-shrink: 0;
}

.report-title {
  font-weight: 500;
  color: #303133;
}

.time-icon {
  margin-right: 4px;
  color: #909399;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

/* 报告详情对话框 */
.report-detail-dialog :deep(.el-dialog__body) {
  padding: 20px 30px;
}

.report-detail {
  max-height: 600px;
  overflow-y: auto;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detail-header h3 {
  margin: 0;
  color: #303133;
  font-size: 18px;
}

.detail-content {
  padding: 10px 0;
}

.detail-item {
  margin-bottom: 20px;
}

.detail-item .label {
  font-weight: 600;
  color: #606266;
  display: block;
  margin-bottom: 8px;
}

.detail-item .value {
  color: #303133;
}

.content-text {
  line-height: 1.8;
  color: #606266;
  text-align: justify;
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 6px;
}

.suggestions-list {
  margin: 0;
  padding-left: 20px;
}

.suggestions-list li {
  margin-bottom: 8px;
  color: #606266;
  line-height: 1.6;
}

.suggestions-list li::marker {
  color: #409eff;
}
</style>

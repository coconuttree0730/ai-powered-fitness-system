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
          <el-radio-button value="today">本日</el-radio-button>
          <el-radio-button value="month">本月</el-radio-button>
          <el-radio-button value="year">本年</el-radio-button>
        </el-radio-group>
        <el-tag type="info" effect="plain" class="count-tag">
          共 {{ total }} 条记录
        </el-tag>
      </div>

      <!-- 报告列表 -->
      <el-table
        :data="reportList"
        stripe
        style="width: 100%"
        v-loading="loading"
        class="report-table"
      >
        <el-table-column type="index" label="序号" width="80" align="center" />
        <el-table-column prop="reportTitle" label="报告标题" min-width="300" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="report-title-cell">
              <el-icon class="report-icon" :size="18" color="#409eff"><Document /></el-icon>
              <span class="report-title">{{ row.reportTitle }}</span>
            </div>
          </template>
        </el-table-column>
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
          :total="total"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 查看报告详情对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="AI智能分析报告"
      width="1200px"
      class="analysis-dialog"
      destroy-on-close
      top="5vh"
    >
      <div class="analysis-report" v-if="currentReport">
        <div class="report-header">
          <div class="header-icon-wrap">
            <el-icon size="28" color="#fff"><MagicStick /></el-icon>
          </div>
          <div class="header-info">
            <h3>{{ currentReport.reportTitle }}</h3>
            <el-tag type="info" size="small" effect="dark" round>{{ currentReport.analysisType }}</el-tag>
          </div>
        </div>
        <el-divider />
        <div class="report-body" v-html="renderedContent"></div>
        <div class="suggestions" v-if="currentReport.suggestions && renderedSuggestions">
          <div class="suggestions-header">
            <el-icon size="18"><Opportunity /></el-icon>
            <span>优化建议</span>
          </div>
          <div class="suggestions-content" v-html="renderedSuggestions"></div>
        </div>
        <div v-else style="padding: 20px; color: #999; text-align: center;">
          暂无优化建议数据 (suggestions: {{ currentReport.suggestions }})
        </div>
        <div class="report-footer">
          <el-text type="info" size="small">
            <el-icon><Clock /></el-icon>
            生成时间：{{ formatDateTime(currentReport.generateTime) }}
          </el-text>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Clock, View, Delete, MagicStick, Opportunity } from '@element-plus/icons-vue'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import { getAnalysisReportList, deleteAnalysisReport } from '@/api/analysis'

// 加载状态
const loading = ref(false)

// 筛选条件
const timeFilter = ref('today')

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 报告列表
const reportList = ref([])

// 对话框显示状态
const viewDialogVisible = ref(false)

// 当前查看的报告
const currentReport = ref(null)

// Markdown 渲染报告内容
const renderedContent = computed(() => {
  if (!currentReport.value?.reportContent) return ''
  const raw = currentReport.value.reportContent
  const html = marked.parse(raw, {
    breaks: true,
    gfm: true,
    headerIds: false,
    mangle: false
  })
  return DOMPurify.sanitize(html)
})

// Markdown 渲染优化建议
const renderedSuggestions = computed(() => {
  if (!currentReport.value?.suggestions) return ''
  const raw = currentReport.value.suggestions
  const html = marked.parse(raw, {
    breaks: true,
    gfm: true,
    headerIds: false,
    mangle: false
  })
  return DOMPurify.sanitize(html)
})

// 获取分析类型标签样式
function getAnalysisTypeTag(type) {
  const tagMap = {
    'OVERALL': 'primary',
    'MEMBER': 'success',
    'COURSE': 'warning',
    'EQUIPMENT': 'info',
    'REVENUE': 'danger'
  }
  return tagMap[type] || 'info'
}

// 获取分析类型显示文本
function getAnalysisTypeText(type) {
  const textMap = {
    'OVERALL': '综合分析',
    'MEMBER': '会员分析',
    'COURSE': '课程分析',
    'EQUIPMENT': '器材分析',
    'REVENUE': '营收分析'
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

// 获取报告列表
async function fetchReportList() {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      timeFilter: timeFilter.value
    }
    const res = await getAnalysisReportList(params)
    if (res && res.records) {
      reportList.value = res.records
      total.value = res.total
    }
  } catch (error) {
    console.error('获取报告列表失败:', error)
    ElMessage.error('获取报告列表失败')
  } finally {
    loading.value = false
  }
}

// 筛选变化处理
function handleFilterChange() {
  currentPage.value = 1
  fetchReportList()
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
    await deleteAnalysisReport(row.id)
    ElMessage.success('删除成功')
    fetchReportList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除报告失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 分页处理
function handleSizeChange(val) {
  pageSize.value = val
  fetchReportList()
}

function handleCurrentChange(val) {
  currentPage.value = val
  fetchReportList()
}

onMounted(() => {
  fetchReportList()
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

/* ==================== AI分析报告对话框样式 ==================== */
.analysis-dialog :deep(.el-dialog__body) {
  padding: 0;
  max-height: 70vh;
  overflow-y: auto;
}

.analysis-report {
  padding: 24px;
}

.report-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 8px;
}

.header-icon-wrap {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.3);
}

.header-info h3 {
  margin: 0 0 8px 0;
  font-size: 22px;
  font-weight: 600;
  color: #1a1a2e;
}

/* 报告内容样式 */
.report-body {
  line-height: 1.8;
  color: #374151;
  font-size: 15px;
}

.report-body :deep(h1),
.report-body :deep(h2),
.report-body :deep(h3),
.report-body :deep(h4) {
  margin-top: 28px;
  margin-bottom: 16px;
  font-weight: 600;
  color: #1e293b;
}

.report-body :deep(h1) {
  font-size: 24px;
  border-left: 5px solid #667eea;
  padding-left: 16px;
}

.report-body :deep(h2) {
  font-size: 20px;
  border-left: 4px solid #764ba2;
  padding-left: 14px;
}

.report-body :deep(h3) {
  font-size: 17px;
  color: #334155;
}

.report-body :deep(p) {
  margin: 12px 0;
  line-height: 1.9;
}

.report-body :deep(strong),
.report-body :deep(b) {
  color: #4c1d95;
  font-weight: 600;
}

.report-body :deep(ul),
.report-body :deep(ol) {
  margin: 16px 0;
  padding-left: 24px;
}

.report-body :deep(li) {
  margin: 10px 0;
  line-height: 1.8;
}

.report-body :deep(ul li) {
  list-style-type: disc;
}

.report-body :deep(ol li) {
  list-style-type: decimal;
}

.report-body :deep(blockquote) {
  margin: 20px 0;
  padding: 16px 20px;
  background: linear-gradient(135deg, #f0f4ff 0%, #faf5ff 100%);
  border-left: 4px solid #667eea;
  border-radius: 8px;
  font-style: italic;
  color: #4b5563;
}

.report-body :deep(code) {
  background: #f3f4f6;
  padding: 3px 8px;
  border-radius: 4px;
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 13px;
  color: #dc2626;
}

.report-body :deep(pre) {
  background: #1f2937;
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 16px 0;
}

.report-body :deep(pre code) {
  background: transparent;
  color: #e5e7eb;
  padding: 0;
}

.report-body :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 20px 0;
  font-size: 14px;
}

.report-body :deep(th),
.report-body :deep(td) {
  border: 1px solid #e5e7eb;
  padding: 12px;
  text-align: left;
}

.report-body :deep(th) {
  background: #f9fafb;
  font-weight: 600;
  color: #374151;
}

.report-body :deep(tr:nth-child(even)) {
  background: #f9fafb;
}

/* 优化建议区域 */
.suggestions {
  margin-top: 28px;
  padding: 20px;
  background: linear-gradient(135deg, #f0fdf4 0%, #ecfdf5 100%);
  border-radius: 12px;
  border: 1px solid #bbf7d0;
}

.suggestions-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
  color: #15803d;
  font-weight: 600;
  font-size: 17px;
}

.suggestions-content {
  line-height: 1.8;
  color: #374151;
  font-size: 14px;
}

.suggestions-content :deep(h1),
.suggestions-content :deep(h2),
.suggestions-content :deep(h3),
.suggestions-content :deep(h4) {
  margin-top: 16px;
  margin-bottom: 10px;
  font-weight: 600;
  color: #1e293b;
}

.suggestions-content :deep(h1) {
  font-size: 18px;
  border-left: 4px solid #22c55e;
  padding-left: 12px;
}

.suggestions-content :deep(h2) {
  font-size: 16px;
  border-left: 3px solid #4ade80;
  padding-left: 10px;
}

.suggestions-content :deep(h3) {
  font-size: 15px;
  color: #334155;
}

.suggestions-content :deep(p) {
  margin: 8px 0;
  line-height: 1.8;
}

.suggestions-content :deep(strong),
.suggestions-content :deep(b) {
  color: #15803d;
  font-weight: 600;
}

.suggestions-content :deep(ul),
.suggestions-content :deep(ol) {
  margin: 10px 0;
  padding-left: 20px;
}

.suggestions-content :deep(li) {
  margin: 6px 0;
  line-height: 1.7;
}

.suggestions-content :deep(ul li) {
  list-style-type: disc;
}

.suggestions-content :deep(ol li) {
  list-style-type: decimal;
}

/* 报告底部 */
.report-footer {
  margin-top: 28px;
  padding-top: 16px;
  border-top: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.report-footer .el-text {
  display: flex;
  align-items: center;
  gap: 6px;
}
</style>

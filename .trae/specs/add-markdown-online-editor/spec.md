# 知识库文档在线编辑功能 Spec

## Why
当前知识库管理系统仅支持通过上传文件的方式更新文档内容，管理员无法直接在线编辑已存在的 Markdown 文档。这导致每次修改知识库内容都需要重新上传整个文件，操作繁琐且效率低下。需要实现在线编辑能力，让管理员可以直接在浏览器中编辑 Markdown 文档并保存，同时自动触发 RAG 索引重建流程。

## What Changes

### 前端改动
1. **KnowledgeBase.vue 增强**
   - 在操作列新增"在线编辑"按钮（针对 .md/.txt 类型文档）
   - 新建 Markdown 编辑器对话框组件（使用 markdown 编辑器库）
   - 实现左侧编辑区 + 右侧预览区的分屏布局
   - 添加保存按钮，调用新的后端接口

2. **knowledge.js API 扩展**
   - 新增 `getDocumentContent(id)` 获取文档原始内容接口
   - 新增 `updateDocumentContent(id, content)` 更新文档内容并重建索引接口

### 后端改动
3. **KnowledgeDocumentController.java 扩展**
   - 新增 `GET /{id}/content` 接口返回文档原始文本内容
   - 新增 `PUT /{id}/content` 接口接收新内容并触发 RAG 处理流程

4. **KnowledgeDocumentService.java 扩展**
   - 新增 `getContentById(Long id)` 方法读取 MinIO 文件内容并返回
   - 新增 `updateContent(Long id, String content)` 方法更新内容并重建索引

5. **KnowledgeDocumentServiceImpl.java 实现**
   - getContentById: 从 MinIO 读取文件 → 返回文本内容
   - updateContent: 将新内容写入 MinIO → 调用现有 reindex() 流程

### 数据流设计
```
用户点击"在线编辑"
    ↓
前端调用 GET /api/v1/knowledge/documents/{id}/content
    ↓
后端从 MinIO 读取原始文件内容返回
    ↓
前端渲染到 Markdown 编辑器
    ↓
用户编辑完成后点击"保存"
    ↓
前端调用 PUT /api/v1/knowledge/documents/{id}/content { content: "..." }
    ↓
后端接收新内容 → 写入 MinIO → 触发 reindex()
    ↓
reindex(): 删除旧切片 → 切片新内容 → 向量化 → 存数据库
    ↓
返回成功响应 → 前端提示成功并刷新列表
```

## Impact
- Affected specs: 无（独立新功能）
- Affected code:
  - 前端: `frontend/src/views/admin/KnowledgeBase.vue`, `frontend/src/api/knowledge.js`
  - 后端: `KnowledgeDocumentController.java`, `KnowledgeDocumentService.java`, `KnowledgeDocumentServiceImpl.java`

## ADDED Requirements

### Requirement: 在线 Markdown 编辑器
系统 SHALL 提供在线 Markdown 编辑功能，允许管理员直接在浏览器中编辑知识库文档。

#### Scenario: 打开文档进行编辑
- **WHEN** 管理员点击文档列表中 .md 或 .txt 类型文档的"在线编辑"按钮
- **THEN** 系统应打开全屏编辑器对话框，从后端加载文档原始内容并显示在编辑区
- **AND** 右侧实时显示 Markdown 渲染预览
- **AND** 编辑器支持语法高亮、工具栏快捷操作

#### Scenario: 保存编辑后的内容
- **WHEN** 管理员在编辑器中修改了内容并点击"保存"按钮
- **THEN** 系统应将新内容发送到后端
- **AND** 后端应更新 MinIO 中的文件内容
- **AND** 后端应自动触发 RAG 索引重建流程（chunk → 向量化 → 存储）
- **AND** 前端显示保存成功的提示并刷新文档列表

#### Scenario: 编辑权限控制
- **WHEN** 非管理员用户尝试访问编辑接口
- **THEN** 系统应返回 403 权限不足错误

## MODIFIED Requirements
无（纯增量功能）

## REMOVED Requirements
无

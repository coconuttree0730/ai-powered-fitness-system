# Tasks

- [ ] Task 1: 后端实现 - 获取文档内容接口
  - [ ] 1.1 在 KnowledgeDocumentService 接口添加 `getContentById(Long id)` 方法声明
  - [ ] 1.2 在 KnowledgeDocumentServiceImpl 实现 `getContentById` 方法（从 MinIO 读取文件内容并返回文本）
  - [ ] 1.3 在 KnowledgeDocumentController 添加 `GET /{id}/content` 端点

- [ ] Task 2: 后端实现 - 更新文档内容接口
  - [ ] 2.1 在 KnowledgeDocumentService 接口添加 `updateContent(Long id, String content)` 方法声明
  - [ ] 2.2 在 KnowledgeDocumentServiceImpl 实现 `updateContent` 方法（写入 MinIO + 调用 reindex）
  - [ ] 2.3 创建 UpdateContentDTO 数据传输对象
  - [ ] 2.4 在 KnowledgeDocumentController 添加 `PUT /{id}/content` 端点

- [ ] Task 3: 前端 API 层扩展
  - [ ] 3.1 在 knowledge.js 添加 `getDocumentContent(id)` API 函数
  - [ ] 3.2 在 knowledge.js 添加 `updateDocumentContent(id, content)` API 函数

- [ ] Task 4: 前端编辑器组件开发
  - [ ] 4.1 安装 Markdown 编辑器依赖库（如 @vueup/vue-quill 或 md-editor-v3）
  - [ ] 4.2 在 KnowledgeBase.vue 添加"在线编辑"按钮（仅 .md/.txt 文件显示）
  - [ ] 4.3 实现全屏 Markdown 编辑器对话框（左侧编辑 + 右侧预览分屏布局）
  - [ ] 4.4 实现加载文档内容逻辑
  - [ ] 4.5 实现保存功能（调用更新接口 + 成功提示 + 刷新列表）
  - [ ] 4.6 添加编辑器样式和交互优化

- [ ] Task 5: 功能测试验证
  - [ ] 5.1 测试打开 .md 文件进行在线编辑
  - [ ] 5.2 测试编辑内容后保存成功
  - [ ] 5.3 验证保存后 RAG 索引自动重建
  - [ ] 5.4 验证权限控制（非管理员无法访问）

# Task Dependencies
- [Task 3] depends on [Task 1] and [Task 2] (前端 API 需要后端接口先就绪)
- [Task 4] depends on [Task 3] (编辑器组件需要 API 层支持)
- [Task 5] depends on [Task 4] (测试需要在功能开发完成后进行)

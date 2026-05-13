package com.fitness.modules.knowledge.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.constants.ErrorCode;
import com.fitness.common.exception.BusinessException;
import com.fitness.integration.minio.service.FileService;
import com.fitness.integration.security.SecurityUtils;
import com.fitness.modules.knowledge.mapper.KnowledgeDocumentMapper;
import com.fitness.modules.knowledge.model.dto.KnowledgeDocumentDTO;
import com.fitness.modules.knowledge.model.dto.KnowledgeDocumentQueryDTO;
import com.fitness.modules.knowledge.model.entity.KnowledgeChunk;
import com.fitness.modules.knowledge.model.entity.KnowledgeDocument;
import com.fitness.modules.knowledge.model.enums.DocumentStatus;
import com.fitness.modules.knowledge.model.vo.KnowledgeDocumentDetailVO;
import com.fitness.modules.knowledge.model.vo.KnowledgeDocumentVO;
import com.fitness.modules.knowledge.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeDocumentServiceImpl implements KnowledgeDocumentService {

    private final KnowledgeDocumentMapper documentMapper;
    private final KnowledgeChunkService chunkService;
    private final DocumentProcessorService documentProcessorService;
    private final EmbeddingService embeddingService;
    private final FileService fileService;
    private final KnowledgeCategoryService categoryService;

    private static final List<String> ALLOWED_FILE_TYPES = Arrays.asList("md", "txt");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024L;

    @Override
    public Page<KnowledgeDocumentVO> page(KnowledgeDocumentQueryDTO queryDTO) {
        Page<KnowledgeDocument> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        LambdaQueryWrapper<KnowledgeDocument> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeDocument::getDeleted, false)
               .eq(queryDTO.getStatus() != null, KnowledgeDocument::getStatus, queryDTO.getStatus())
               .like(StrUtil.isNotBlank(queryDTO.getKeyword()), KnowledgeDocument::getTitle, queryDTO.getKeyword())
               .orderByDesc(KnowledgeDocument::getCreateTime);

        Page<KnowledgeDocument> resultPage = documentMapper.selectPage(page, wrapper);

        return (Page<KnowledgeDocumentVO>) resultPage.convert(this::toVO);
    }

    @Override
    public KnowledgeDocumentDetailVO getDetailById(Long id) {
        KnowledgeDocument document = getEntityById(id);
        return toDetailVO(document);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(KnowledgeDocumentDTO dto) {
        KnowledgeDocument document = new KnowledgeDocument();
        BeanUtil.copyProperties(dto, document);
        document.setStatus(DocumentStatus.DRAFT.getCode());
        document.setCreateBy(SecurityUtils.getCurrentUserId());

        documentMapper.insert(document);
        return document.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long uploadDocument(MultipartFile file, String title, Long categoryId) {
        log.info("【文档上传】开始上传文档，文件名: {}, 标题: {}, 分类ID: {}", file.getOriginalFilename(), title, categoryId);

        validateFile(file);

        String originalFilename = file.getOriginalFilename();
        String fileType = getFileExtension(originalFilename);

        log.info("【文档上传】文件验证通过，文件类型: {}", fileType);

        var fileUploadVO = fileService.uploadFile(file, "knowledge");
        log.info("【文档上传】文件上传至MinIO成功，URL: {}", fileUploadVO.getFileUrl());

        KnowledgeDocument document = new KnowledgeDocument();
        document.setTitle(StrUtil.isBlank(title) ? documentProcessorService.extractTitle(null, originalFilename) : title);
        document.setFileUrl(fileUploadVO.getFileUrl());
        document.setFileName(originalFilename);
        document.setFileType(fileType);
        document.setFileSize(file.getSize());
        document.setCategoryId(categoryId);  // 设置分类ID
        document.setStatus(DocumentStatus.DRAFT.getCode());
        document.setCreateBy(SecurityUtils.getCurrentUserId());

        documentMapper.insert(document);
        log.info("【文档上传】文档记录创建成功，文档ID: {}, 分类ID: {}", document.getId(), categoryId);

        return document.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(KnowledgeDocumentDTO dto) {
        KnowledgeDocument document = getEntityById(dto.getId());
        document.setTitle(dto.getTitle());
        document.setCategoryId(dto.getCategoryId());
        documentMapper.updateById(document);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        KnowledgeDocument document = getEntityById(id);

        chunkService.deleteByDocumentId(id);

        if (StrUtil.isNotBlank(document.getFileUrl())) {
            fileService.deleteFile(document.getFileUrl());
        }

        documentMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publish(Long id) {
        log.info("【文档发布】开始发布文档，文档ID: {}", id);

        KnowledgeDocument document = getEntityById(id);

        if (StrUtil.isBlank(document.getFileUrl())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "文档文件为空，无法发布");
        }

        document.setStatus(DocumentStatus.PUBLISHED.getCode());
        documentMapper.updateById(document);
        log.info("【文档发布】文档状态已更新为已发布，文档ID: {}", id);

        reindex(id);
    }

    @Override
    public void archive(Long id) {
        KnowledgeDocument document = getEntityById(id);
        document.setStatus(DocumentStatus.ARCHIVED.getCode());
        documentMapper.updateById(document);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reindex(Long id) {
        log.info("【文档索引】开始重建文档索引，文档ID: {}", id);

        KnowledgeDocument document = getEntityById(id);

        chunkService.deleteByDocumentId(id);
        log.info("【文档索引】已清除旧索引数据，文档ID: {}", id);

        String content = null;
        if (StrUtil.isNotBlank(document.getFileUrl())) {
            log.info("【文档索引】从文件解析内容，文件URL: {}", document.getFileUrl());
            content = documentProcessorService.parseFile(document.getFileUrl(), document.getFileType());
            log.info("【文档索引】文件内容解析完成，内容长度: {} 字符", content != null ? content.length() : 0);
        }

        if (content == null || content.isBlank()) {
            log.warn("【文档索引】文档内容为空，无法建立索引，文档ID: {}", id);
            return;
        }

        log.info("【文档索引】开始文档切片，文档ID: {}, 内容长度: {} 字符", id, content.length());
        // 生成切片
        List<KnowledgeChunk> chunks = documentProcessorService.chunkDocument(document, content);
        log.info("【文档索引】文档切片完成，共生成 {} 个切片", chunks.size());

        // 打印切片内容日志
        for (int i = 0; i < chunks.size(); i++) {
            KnowledgeChunk chunk = chunks.get(i);
            log.info("【文档索引】切片 #{} - 长度: {} 字符，内容预览: {}",
                i,
                chunk.getContent().length(),
                chunk.getContent().substring(0, Math.min(100, chunk.getContent().length())) + "..."
            );
        }

        log.info("【文档索引】开始向量化处理，共 {} 个切片", chunks.size());
        for (int i = 0; i < chunks.size(); i++) {
            KnowledgeChunk chunk = chunks.get(i);
            log.info("【文档索引】正在处理切片 #{}/{} 的向量化", i + 1, chunks.size());

            float[] embedding = embeddingService.embed(chunk.getContent());
            chunk.setEmbedding(embedding);

            log.info("【文档索引】切片 #{} 向量化完成，向量维度: {}", i, embedding.length);
        }

        chunkService.saveChunks(chunks);
        log.info("【文档索引】所有切片已保存到数据库");

        updateChunkCount(id, chunks.size());

        log.info("【文档索引】文档重新索引完成，文档ID: {}, 切片数量: {}", id, chunks.size());
    }

    @Override
    public KnowledgeDocument getEntityById(Long id) {
        KnowledgeDocument document = documentMapper.selectById(id);
        if (document == null || document.getDeleted()) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "文档不存在");
        }
        return document;
    }

    @Override
    public void updateChunkCount(Long documentId, Integer chunkCount) {
        KnowledgeDocument document = new KnowledgeDocument();
        document.setId(documentId);
        document.setChunkCount(chunkCount);
        documentMapper.updateById(document);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.FILE_TOO_LARGE);
        }

        String fileType = getFileExtension(file.getOriginalFilename());
        if (!ALLOWED_FILE_TYPES.contains(fileType.toLowerCase())) {
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED, "仅支持 md、txt 格式文件");
        }
    }

    private String getFileExtension(String filename) {
        if (StrUtil.isBlank(filename) || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    private KnowledgeDocumentVO toVO(KnowledgeDocument document) {
        KnowledgeDocumentVO vo = new KnowledgeDocumentVO();
        BeanUtil.copyProperties(document, vo);

        DocumentStatus status = DocumentStatus.fromCode(document.getStatus());
        if (status != null) {
            vo.setStatusDesc(status.getDescription());
        }

        // 设置分类名称
        if (document.getCategoryId() != null) {
            var category = categoryService.getById(document.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }

        return vo;
    }

    private KnowledgeDocumentDetailVO toDetailVO(KnowledgeDocument document) {
        KnowledgeDocumentDetailVO vo = new KnowledgeDocumentDetailVO();
        BeanUtil.copyProperties(document, vo);

        DocumentStatus status = DocumentStatus.fromCode(document.getStatus());
        if (status != null) {
            vo.setStatusDesc(status.getDescription());
        }

        return vo;
    }
}

package com.fitness.modules.knowledge.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.HttpRequest;
import com.fitness.common.exception.BusinessException;
import com.fitness.common.constants.ErrorCode;
import com.fitness.modules.knowledge.model.entity.KnowledgeChunk;
import com.fitness.modules.knowledge.model.entity.KnowledgeDocument;
import com.fitness.modules.knowledge.service.DocumentProcessorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class DocumentProcessorServiceImpl implements DocumentProcessorService {

    // 增大切片大小，保留更多完整信息
    private static final int DEFAULT_CHUNK_SIZE = 800;
    // 增大重叠区域，保持上下文连贯性
    private static final int DEFAULT_CHUNK_OVERLAP = 150;
    private static final int MIN_CHUNK_SIZE = 100;

    @Value("${knowledge.chunk.size:800}")
    private int chunkSize;

    @Value("${knowledge.chunk.overlap:150}")
    private int chunkOverlap;

    // Markdown 标题正则
    private static final Pattern HEADING_PATTERN = Pattern.compile("^(#{1,6}\\s+.+)$", Pattern.MULTILINE);
    // 列表项正则
    private static final Pattern LIST_ITEM_PATTERN = Pattern.compile("^([\\s]*[-*+]|\\d+\\.)\\s+(.+)$", Pattern.MULTILINE);

    /**
     * 解析文件并返回内容
     *
     * @param fileUrl 文件URL
     * @param fileType 文件类型
     * @return 文件内容
     */
    @Override
    public String parseFile(String fileUrl, String fileType) {
        if (StrUtil.isBlank(fileUrl)) {
            return "";
        }
        try {
            //获取文件内容
            String content = HttpRequest.get(fileUrl)
                    .timeout(30000)
                    .execute()
                    .body();

            content = cleanContent(content);

            log.info("文件解析成功，文件类型: {}, 内容长度: {}", fileType, content.length());
            return content;
        } catch (Exception e) {
            log.error("文件解析失败，文件URL: {}", fileUrl, e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR.getCode(), "文件解析失败: " + e.getMessage());
        }
    }

    @Override
    public List<KnowledgeChunk> chunkDocument(KnowledgeDocument document, String content) {
        List<KnowledgeChunk> chunks = new ArrayList<>();

        if (StrUtil.isBlank(content)) {
            return chunks;
        }

        // 使用语义感知的切分策略
        List<String> textChunks = splitTextWithSemanticBoundaries(content);

        for (int i = 0; i < textChunks.size(); i++) {
            String chunkContent = textChunks.get(i);

            KnowledgeChunk chunk = new KnowledgeChunk();
            chunk.setDocumentId(document.getId());
            chunk.setChunkIndex(i);
            chunk.setContent(chunkContent);
            chunk.setContentHash(DigestUtil.md5Hex(chunkContent));
            chunk.setCharCount(chunkContent.length());

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("document_id", document.getId());
            metadata.put("document_title", document.getTitle());
            metadata.put("chunk_index", i);
            metadata.put("chunk_size", chunkSize);
            metadata.put("chunk_overlap", chunkOverlap);
            chunk.setMetadata(metadata);

            chunks.add(chunk);
        }

        log.info("文档切片完成，文档ID: {}, 切片数量: {}, 平均切片大小: {} 字符",
                document.getId(), chunks.size(),
                chunks.isEmpty() ? 0 : content.length() / chunks.size());
        return chunks;
    }

    /**
     * 基于语义边界的智能切分
     * 优先在以下位置切分：
     * 1. 二级标题 (##)
     * 2. 段落边界
     * 3. 列表项边界
     * 4. 句子边界
     */
    private List<String> splitTextWithSemanticBoundaries(String content) {
        List<String> chunks = new ArrayList<>();

        // 首先尝试按二级标题切分
        List<String> sections = splitByHeadings(content);

        for (String section : sections) {
            if (section.length() <= chunkSize) {
                // 小段落直接作为一个切片
                if (section.length() >= MIN_CHUNK_SIZE) {
                    chunks.add(section.trim());
                } else {
                    // 小段落合并到前一个切片
                    mergeToLastChunk(chunks, section);
                }
            } else {
                // 大段落需要进一步切分
                List<String> subChunks = splitLargeSection(section);
                chunks.addAll(subChunks);
            }
        }

        return chunks;
    }

    /**
     * 按标题切分文档
     */
    private List<String> splitByHeadings(String content) {
        List<String> sections = new ArrayList<>();

        // 按二级标题 (##) 切分
        String[] parts = content.split("(?=\\n##\\s+)");

        for (String part : parts) {
            part = part.trim();
            if (StrUtil.isNotBlank(part)) {
                sections.add(part);
            }
        }

        return sections;
    }

    /**
     * 切分大的章节
     */
    private List<String> splitLargeSection(String section) {
        List<String> chunks = new ArrayList<>();

        // 按段落切分
        String[] paragraphs = section.split("\\n\\n+");

        StringBuilder currentChunk = new StringBuilder();
        int currentSize = 0;

        for (String paragraph : paragraphs) {
            paragraph = paragraph.trim();
            if (StrUtil.isBlank(paragraph)) {
                continue;
            }

            // 如果当前段落本身超过限制，需要进一步切分
            if (paragraph.length() > chunkSize) {
                if (currentSize > 0) {
                    chunks.add(currentChunk.toString().trim());
                    currentChunk = new StringBuilder();
                    currentSize = 0;
                }

                List<String> subChunks = splitLongParagraph(paragraph);
                chunks.addAll(subChunks);
                continue;
            }

            // 检查是否需要切分（考虑重叠）
            if (currentSize + paragraph.length() + 2 > chunkSize && currentSize >= MIN_CHUNK_SIZE) {
                // 保存当前切片
                chunks.add(currentChunk.toString().trim());

                // 获取重叠文本，保持上下文
                String overlapText = getOverlapTextWithContext(currentChunk.toString());
                currentChunk = new StringBuilder(overlapText);
                currentSize = overlapText.length();
            }

            // 添加段落
            if (currentSize > 0) {
                currentChunk.append("\n\n");
                currentSize += 2;
            }
            currentChunk.append(paragraph);
            currentSize += paragraph.length();
        }

        // 保存最后一个切片
        if (currentSize > 0) {
            chunks.add(currentChunk.toString().trim());
        }

        return chunks;
    }

    /**
     * 切分长段落（按句子）
     */
    private List<String> splitLongParagraph(String paragraph) {
        List<String> chunks = new ArrayList<>();

        // 按句子切分（保留标点）
        String[] sentences = paragraph.split("(?<=[。！？.!?])");

        StringBuilder currentChunk = new StringBuilder();
        int currentSize = 0;

        for (String sentence : sentences) {
            sentence = sentence.trim();
            if (StrUtil.isBlank(sentence)) {
                continue;
            }

            // 如果单个句子就超过限制，强制切分
            if (sentence.length() > chunkSize) {
                if (currentSize > 0) {
                    chunks.add(currentChunk.toString().trim());
                    currentChunk = new StringBuilder();
                    currentSize = 0;
                }

                // 强制按字符切分
                for (int i = 0; i < sentence.length(); i += chunkSize - chunkOverlap) {
                    int end = Math.min(i + chunkSize, sentence.length());
                    String subChunk = sentence.substring(i, end).trim();
                    if (subChunk.length() >= MIN_CHUNK_SIZE) {
                        chunks.add(subChunk);
                    }
                }
                continue;
            }

            // 检查是否需要切分
            if (currentSize + sentence.length() > chunkSize && currentSize >= MIN_CHUNK_SIZE) {
                chunks.add(currentChunk.toString().trim());

                // 保留重叠的句子
                String overlap = getSentenceOverlap(currentChunk.toString());
                currentChunk = new StringBuilder(overlap);
                currentSize = overlap.length();
            }

            currentChunk.append(sentence);
            currentSize += sentence.length();
        }

        if (currentSize > 0) {
            chunks.add(currentChunk.toString().trim());
        }

        return chunks;
    }

    /**
     * 获取带上下文的重叠文本
     */
    private String getOverlapTextWithContext(String text) {
        if (text.length() <= chunkOverlap) {
            return text;
        }

        // 尝试在段落边界切分
        String overlap = text.substring(text.length() - chunkOverlap);
        int paragraphBreak = overlap.indexOf("\n\n");
        if (paragraphBreak > 0 && paragraphBreak < overlap.length() - 20) {
            return overlap.substring(paragraphBreak + 2);
        }

        // 尝试在句子边界切分
        int sentenceBreak = Math.max(
                overlap.lastIndexOf("。"),
                Math.max(overlap.lastIndexOf("！"), overlap.lastIndexOf("？"))
        );
        if (sentenceBreak > overlap.length() / 2) {
            return overlap.substring(sentenceBreak + 1).trim();
        }

        return overlap;
    }

    /**
     * 获取句子级别的重叠
     */
    private String getSentenceOverlap(String text) {
        if (text.length() <= chunkOverlap) {
            return text;
        }

        String overlap = text.substring(text.length() - chunkOverlap);

        // 找到最后一个完整的句子
        int lastSentenceEnd = Math.max(
                overlap.lastIndexOf("。"),
                Math.max(overlap.lastIndexOf("！"), overlap.lastIndexOf("？"))
        );

        if (lastSentenceEnd > 0) {
            return overlap.substring(lastSentenceEnd + 1).trim();
        }

        return overlap;
    }

    /**
     * 合并小段落到最后一个切片
     */
    private void mergeToLastChunk(List<String> chunks, String smallText) {
        if (chunks.isEmpty()) {
            if (smallText.trim().length() >= MIN_CHUNK_SIZE) {
                chunks.add(smallText.trim());
            }
            return;
        }

        String lastChunk = chunks.get(chunks.size() - 1);
        if (lastChunk.length() + smallText.length() + 2 <= chunkSize) {
            chunks.set(chunks.size() - 1, lastChunk + "\n\n" + smallText.trim());
        } else if (smallText.trim().length() >= MIN_CHUNK_SIZE) {
            chunks.add(smallText.trim());
        }
    }

    @Override
    public String generateSummary(String content) {
        if (StrUtil.isBlank(content)) {
            return "";
        }

        String cleanContent = cleanContent(content);

        if (cleanContent.length() <= 200) {
            return cleanContent;
        }

        int end = Math.min(200, cleanContent.length());
        int lastPeriod = cleanContent.lastIndexOf("。", end);
        int lastQuestion = cleanContent.lastIndexOf("？", end);
        int lastExclamation = cleanContent.lastIndexOf("！", end);

        int breakPoint = Math.max(Math.max(lastPeriod, lastQuestion), lastExclamation);
        if (breakPoint > 100) {
            return cleanContent.substring(0, breakPoint + 1);
        }

        return cleanContent.substring(0, end) + "...";
    }

    @Override
    public String extractTitle(String content, String fileName) {
        if (StrUtil.isNotBlank(content)) {
            String[] lines = content.split("\n");
            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("# ")) {
                    return line.substring(2).trim();
                }
                if (StrUtil.isNotBlank(line) && line.length() <= 100) {
                    return line;
                }
            }
        }

        if (StrUtil.isNotBlank(fileName)) {
            int dotIndex = fileName.lastIndexOf(".");
            if (dotIndex > 0) {
                return fileName.substring(0, dotIndex);
            }
            return fileName;
        }

        return "未命名文档";
    }

    private String cleanContent(String content) {
        if (StrUtil.isBlank(content)) {
            return "";
        }

        content = content.replaceAll("\r\n", "\n");
        content = content.replaceAll("\r", "\n");
        content = content.replaceAll("[ \t]+", " ");
        content = content.replaceAll("\n{3,}", "\n\n");
        content = content.trim();

        return content;
    }
}

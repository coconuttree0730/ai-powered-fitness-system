package com.fitness.modules.user.service.impl;

import cn.hutool.json.JSONUtil;
import com.fitness.common.constants.ErrorCode;
import com.fitness.common.exception.BusinessException;
import com.fitness.integration.minio.model.vo.FileUploadVO;
import com.fitness.integration.minio.service.FileService;
import com.fitness.integration.security.SecurityUtils;
import com.fitness.modules.user.mapper.CoachDetailMapper;
import com.fitness.modules.user.mapper.UserMapper;
import com.fitness.modules.user.model.dto.CoachDetailDTO;
import com.fitness.modules.user.model.entity.CoachDetail;
import com.fitness.modules.user.model.entity.User;
import com.fitness.modules.user.model.vo.CoachDetailVO;
import com.fitness.modules.user.model.vo.HomePageCoachVO;
import com.fitness.modules.user.service.CoachDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * 教练详情Service实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CoachDetailServiceImpl implements CoachDetailService {

    private final CoachDetailMapper coachDetailMapper;
    private final UserMapper userMapper;
    private final FileService fileService;

    /**
     * 标签最大数量
     */
    private static final int MAX_TAG_COUNT = 5;

    /**
     * 标签最大长度
     */
    private static final int MAX_TAG_LENGTH = 10;

    @Override
    public CoachDetailVO getCoachDetail(Long userId) {
        CoachDetail detail = coachDetailMapper.selectByUserId(userId);
        if (detail == null) {
            // 如果没有详情记录，返回空对象
            return new CoachDetailVO();
        }

        User user = userMapper.selectById(userId);
        return convertToVO(detail, user);
    }

    @Override
    public CoachDetailVO getCurrentCoachDetail() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return getCoachDetail(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CoachDetailVO updateCoachDetail(CoachDetailDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        CoachDetail detail = coachDetailMapper.selectByUserId(userId);
        if (detail == null) {
            // 首次更新，创建新记录
            detail = new CoachDetail();
            detail.setUserId(userId);
            setDetailFromDTO(detail, dto);
            coachDetailMapper.insert(detail);
        } else {
            // 更新现有记录
            setDetailFromDTO(detail, dto);
            coachDetailMapper.updateById(detail);
        }

        User user = userMapper.selectById(userId);
        return convertToVO(detail, user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String uploadPersonalImage(MultipartFile file) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        // 校验文件
        validateImageFile(file);

        CoachDetail detail = coachDetailMapper.selectByUserId(userId);

        // 如果已有图片，先删除旧图片
        if (detail != null && detail.getPersonalImageUrl() != null) {
            try {
                fileService.deleteFile(detail.getPersonalImageUrl());
            } catch (Exception e) {
                log.warn("删除旧图片失败: {}", e.getMessage());
            }
        }

        // 上传新图片到coach-images目录
        FileUploadVO uploadResult = fileService.uploadFile(file, "coach-images/" + userId);
        String imageUrl = uploadResult.getFileUrl();

        // 更新数据库
        if (detail == null) {
            detail = new CoachDetail();
            detail.setUserId(userId);
            detail.setPersonalImageUrl(imageUrl);
            coachDetailMapper.insert(detail);
        } else {
            detail.setPersonalImageUrl(imageUrl);
            coachDetailMapper.updateById(detail);
        }

        log.info("教练个人图片上传成功: userId={}, url={}", userId, imageUrl);
        return imageUrl;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePersonalImage() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        CoachDetail detail = coachDetailMapper.selectByUserId(userId);
        if (detail == null || detail.getPersonalImageUrl() == null) {
            return;
        }

        // 删除MinIO中的文件
        try {
            fileService.deleteFile(detail.getPersonalImageUrl());
        } catch (Exception e) {
            log.warn("删除图片文件失败: {}", e.getMessage());
        }

        // 更新数据库
        detail.setPersonalImageUrl(null);
        coachDetailMapper.updateById(detail);

        log.info("教练个人图片删除成功: userId={}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CoachDetailVO updateTags(List<String> tags) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        // 校验标签
        validateTags(tags);

        CoachDetail detail = coachDetailMapper.selectByUserId(userId);

        String tagsJson = JSONUtil.toJsonStr(tags != null ? tags : new ArrayList<>());

        if (detail == null) {
            detail = new CoachDetail();
            detail.setUserId(userId);
            detail.setTags(tagsJson);
            coachDetailMapper.insert(detail);
        } else {
            detail.setTags(tagsJson);
            coachDetailMapper.updateById(detail);
        }

        User user = userMapper.selectById(userId);
        return convertToVO(detail, user);
    }

    @Override
    public List<HomePageCoachVO> getHomePageCoaches(int limit) {
        if (limit <= 0) {
            limit = 4;
        }

        List<CoachDetail> details = coachDetailMapper.selectHomePageCoaches(limit);
        List<HomePageCoachVO> result = new ArrayList<>();

        for (CoachDetail detail : details) {
            User user = userMapper.selectById(detail.getUserId());
            if (user != null) {
                result.add(convertToHomePageVO(detail, user));
            }
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initCoachDetail(Long userId) {
        CoachDetail existing = coachDetailMapper.selectByUserId(userId);
        if (existing != null) {
            return;
        }

        CoachDetail detail = new CoachDetail();
        detail.setUserId(userId);
        detail.setTags("[]");
        detail.setSpecialties("[]");
        detail.setLanguages("[]");
        detail.setHonors("[]");
        detail.setCertifications("[]");
        detail.setEmergencyContact("{}");
        detail.setAvailability("{}");
        detail.setWorkYears(0);
        detail.setStudentCount(0);
        detail.setRating("99%");

        coachDetailMapper.insert(detail);
        log.info("初始化教练详情成功: userId={}", userId);
    }

    /**
     * 校验图片文件
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }

        // 文件大小限制10MB
        long maxSize = 10 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new BusinessException(ErrorCode.FILE_TOO_LARGE);
        }

        // 校验文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED);
        }
    }

    /**
     * 校验标签
     */
    private void validateTags(List<String> tags) {
        if (tags == null) {
            return;
        }

        if (tags.size() > MAX_TAG_COUNT) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "标签数量不能超过" + MAX_TAG_COUNT + "个");
        }

        for (String tag : tags) {
            if (tag == null || tag.trim().isEmpty()) {
                throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "标签不能为空");
            }
            if (tag.length() > MAX_TAG_LENGTH) {
                throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "标签长度不能超过" + MAX_TAG_LENGTH + "个字符");
            }
        }
    }

    /**
     * 将DTO设置到实体
     */
    private void setDetailFromDTO(CoachDetail detail, CoachDetailDTO dto) {
        if (dto.getWorkYears() != null) {
            detail.setWorkYears(dto.getWorkYears());
        }
        if (dto.getSpecialties() != null) {
            detail.setSpecialties(JSONUtil.toJsonStr(dto.getSpecialties()));
        }
        if (dto.getTeachingStyle() != null) {
            detail.setTeachingStyle(dto.getTeachingStyle());
        }
        if (dto.getEducation() != null) {
            detail.setEducation(dto.getEducation());
        }
        if (dto.getTraining() != null) {
            detail.setTraining(dto.getTraining());
        }
        if (dto.getLanguages() != null) {
            detail.setLanguages(JSONUtil.toJsonStr(dto.getLanguages()));
        }
        if (dto.getBio() != null) {
            detail.setBio(dto.getBio());
        }
        if (dto.getExperience() != null) {
            detail.setExperience(dto.getExperience());
        }
        if (dto.getHonors() != null) {
            detail.setHonors(JSONUtil.toJsonStr(dto.getHonors()));
        }
        if (dto.getEmergencyContact() != null) {
            detail.setEmergencyContact(JSONUtil.toJsonStr(dto.getEmergencyContact()));
        }
        if (dto.getCertifications() != null) {
            detail.setCertifications(JSONUtil.toJsonStr(dto.getCertifications()));
        }
        if (dto.getAvailability() != null) {
            detail.setAvailability(JSONUtil.toJsonStr(dto.getAvailability()));
        }
    }

    /**
     * 转换为VO
     */
    private CoachDetailVO convertToVO(CoachDetail detail, User user) {
        CoachDetailVO vo = new CoachDetailVO();

        if (user != null) {
            vo.setUserId(user.getId());
            vo.setUsername(user.getUsername());
            vo.setAvatar(user.getAvatar());
            vo.setPhone(user.getPhone());
            vo.setEmail(user.getEmail());
        }

        if (detail == null) {
            return vo;
        }

        vo.setPersonalImageUrl(detail.getPersonalImageUrl());
        vo.setWorkYears(detail.getWorkYears());
        vo.setTeachingStyle(detail.getTeachingStyle());
        vo.setEducation(detail.getEducation());
        vo.setTraining(detail.getTraining());
        vo.setBio(detail.getBio());
        vo.setExperience(detail.getExperience());
        vo.setStudentCount(detail.getStudentCount());
        vo.setRating(detail.getRating());

        // 解析JSON数组
        if (detail.getTags() != null) {
            vo.setTags(JSONUtil.parseArray(detail.getTags()).toList(String.class));
        }
        if (detail.getSpecialties() != null) {
            vo.setSpecialties(JSONUtil.parseArray(detail.getSpecialties()).toList(String.class));
        }
        if (detail.getLanguages() != null) {
            vo.setLanguages(JSONUtil.parseArray(detail.getLanguages()).toList(String.class));
        }
        if (detail.getHonors() != null) {
            vo.setHonors(JSONUtil.parseArray(detail.getHonors()).toList(String.class));
        }

        return vo;
    }

    /**
     * 转换为首页展示VO
     */
    private HomePageCoachVO convertToHomePageVO(CoachDetail detail, User user) {
        HomePageCoachVO vo = new HomePageCoachVO();

        vo.setId(user.getId());
        vo.setName(user.getUsername());
        vo.setTitle("专业教练"); // 可以根据需要扩展

        // 优先使用个人展示图片，如果没有则使用头像
        if (detail.getPersonalImageUrl() != null && !detail.getPersonalImageUrl().isEmpty()) {
            vo.setImage(detail.getPersonalImageUrl());
        } else {
            vo.setImage(user.getAvatar());
        }

        // 经验年限
        if (detail.getWorkYears() != null && detail.getWorkYears() > 0) {
            vo.setExperience(detail.getWorkYears() + "+");
        } else {
            vo.setExperience("5+");
        }

        // 学员数量
        if (detail.getStudentCount() != null && detail.getStudentCount() > 0) {
            vo.setStudents(detail.getStudentCount() + "+");
        } else {
            vo.setStudents("1000+");
        }

        // 好评率
        vo.setRating(detail.getRating() != null ? detail.getRating() : "99%");

        // 标签
        if (detail.getTags() != null) {
            List<String> tags = JSONUtil.parseArray(detail.getTags()).toList(String.class);
            // 最多显示3个标签在首页
            if (tags.size() > 3) {
                tags = tags.subList(0, 3);
            }
            vo.setTags(tags);
        } else {
            vo.setTags(List.of("专业教练"));
        }

        return vo;
    }
}

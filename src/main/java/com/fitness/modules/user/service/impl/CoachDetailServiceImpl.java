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
import com.fitness.modules.user.model.vo.CoachHomePageVO;
import com.fitness.modules.user.model.vo.HomePageCoachVO;
import com.fitness.modules.user.model.vo.MyPrivateCoachVO;
import com.fitness.modules.user.service.CoachDetailService;
import com.fitness.modules.user.model.entity.UserFitnessProfile;
import com.fitness.modules.user.mapper.UserFitnessProfileMapper;
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
    private final UserFitnessProfileMapper userFitnessProfileMapper;
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
        User user = userMapper.selectById(userId);

        if (detail == null) {
            // 如果没有详情记录，返回只包含用户信息的空对象
            return convertToVO(null, user);
        }

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

        // 校验并更新用户名
        if (dto.getUsername() != null && !dto.getUsername().isBlank()) {
            validateUsername(dto.getUsername(), userId);
            updateUsername(userId, dto.getUsername());
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

    /**
     * 校验用户名是否可用
     *
     * @param username 用户名
     * @param userId   当前用户ID
     */
    private void validateUsername(String username, Long userId) {
        // 校验用户名长度和格式
        if (username.length() < 6 || username.length() > 20) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "用户名长度必须在6-20个字符之间");
        }
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "用户名只能包含字母、数字和下划线");
        }

        // 校验用户名唯一性
        User existingUser = userMapper.selectByUsername(username);
        if (existingUser != null && !existingUser.getId().equals(userId)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "用户名已被使用");
        }
    }

    /**
     * 更新用户名
     *
     * @param userId   用户ID
     * @param username 新用户名
     */
    private void updateUsername(Long userId, String username) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 如果用户名没有变化，不执行更新
        if (username.equals(user.getUsername())) {
            return;
        }

        user.setUsername(username);
        userMapper.updateById(user);
        log.info("更新用户名成功: userId={}, username={}", userId, username);
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

        List<CoachHomePageVO> details = coachDetailMapper.selectHomePageCoaches(limit);
        List<HomePageCoachVO> result = new ArrayList<>();

        for (CoachHomePageVO detail : details) {
            result.add(convertToHomePageVO(detail));
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
        if (dto.getCertifications() != null) {
            detail.setCertifications(JSONUtil.toJsonStr(dto.getCertifications()));
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
        String tagsJson = detail.getTagsJson();
        if (tagsJson != null) {
            vo.setTags(JSONUtil.parseArray(tagsJson).toList(String.class));
        }
        String specialtiesJson = detail.getSpecialtiesJson();
        if (specialtiesJson != null) {
            vo.setSpecialties(JSONUtil.parseArray(specialtiesJson).toList(String.class));
        }
        String languagesJson = detail.getLanguagesJson();
        if (languagesJson != null) {
            vo.setLanguages(JSONUtil.parseArray(languagesJson).toList(String.class));
        }
        String honorsJson = detail.getHonorsJson();
        if (honorsJson != null) {
            vo.setHonors(JSONUtil.parseArray(honorsJson).toList(String.class));
        }

        return vo;
    }

    /**
     * 转换为首页展示VO
     */
    private HomePageCoachVO convertToHomePageVO(CoachHomePageVO detail) {
        HomePageCoachVO vo = new HomePageCoachVO();

        vo.setId(detail.getUserId());
        vo.setName(detail.getUsername());
        
        // 根据专业领域生成职称
        vo.setTitle(generateCoachTitle(detail));

        // 优先使用个人展示图片，如果没有则使用头像
        if (detail.getPersonalImageUrl() != null && !detail.getPersonalImageUrl().isEmpty()) {
            vo.setImage(detail.getPersonalImageUrl());
        } else {
            vo.setImage(detail.getAvatar());
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

        // 评分（从好评率转换，如99% -> 4.9）
        vo.setRatingScore(convertRatingToScore(detail.getRating()));

        // 个人简介
        vo.setBio(detail.getBio() != null ? detail.getBio() : "专业健身教练，拥有丰富的教学经验，致力于帮助学员达成健身目标。");

        // 标签
        String tagsJson = detail.getTagsJson();
        if (tagsJson != null) {
            List<String> tags = JSONUtil.parseArray(tagsJson).toList(String.class);
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

    /**
     * 将好评率转换为评分（如99% -> 4.9）
     */
    private Double convertRatingToScore(String rating) {
        if (rating == null || rating.isEmpty()) {
            return 4.9;
        }
        // 去掉百分号
        String numberStr = rating.replace("%", "");
        try {
            int percentage = Integer.parseInt(numberStr);
            // 将百分比转换为5分制评分
            // 100% -> 5.0, 90% -> 4.5, 80% -> 4.0
            double score = percentage / 20.0;
            // 保留一位小数
            return Math.round(score * 10) / 10.0;
        } catch (NumberFormatException e) {
            return 4.9;
        }
    }

    /**
     * 根据教练详情生成职称
     */
    private String generateCoachTitle(CoachHomePageVO detail) {
        // 根据专业领域生成职称
        String specialtiesJson = detail.getSpecialtiesJson();
        if (specialtiesJson != null) {
            List<String> specialties = JSONUtil.parseArray(specialtiesJson).toList(String.class);
            if (!specialties.isEmpty()) {
                String mainSpecialty = specialties.get(0);
                // 根据主要专业领域返回对应职称
                return switch (mainSpecialty) {
                    case "瑜伽" -> "瑜伽认证导师";
                    case "普拉提" -> "普拉提认证教练";
                    case "力量训练" -> "力量训练专家";
                    case "康复训练" -> "运动康复师";
                    case "CrossFit" -> "CrossFit认证教练";
                    case "拳击" -> "拳击格斗教练";
                    case "游泳" -> "游泳教练";
                    case "减脂" -> "减脂塑形专家";
                    case "增肌" -> "增肌训练专家";
                    default -> mainSpecialty + "教练";
                };
            }
        }
        
        // 根据教学风格生成职称
        if (detail.getTeachingStyle() != null) {
            return switch (detail.getTeachingStyle()) {
                case "encouraging" -> "激励型私教";
                case "strict" -> "严格型教练";
                case "gentle" -> "温和型导师";
                case "professional" -> "专业认证教练";
                case "interactive" -> "互动式教练";
                default -> "专业教练";
            };
        }
        
        // 根据从业年限生成职称
        if (detail.getWorkYears() != null && detail.getWorkYears() >= 10) {
            return "高级私人教练";
        } else if (detail.getWorkYears() != null && detail.getWorkYears() >= 5) {
            return "资深健身教练";
        }
        
        return "专业教练";
    }

    @Override
    public MyPrivateCoachVO getMyPrivateCoach() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        // 查询用户的健身档案，获取专属教练ID
        UserFitnessProfile profile = userFitnessProfileMapper.selectByUserId(userId);
        if (profile == null || profile.getPrivateCoachId() == null) {
            return null;
        }

        Long coachId = profile.getPrivateCoachId();

        // 查询教练详情
        CoachDetail coachDetail = coachDetailMapper.selectByUserId(coachId);
        User coachUser = userMapper.selectById(coachId);

        if (coachUser == null) {
            return null;
        }

        // 转换为VO
        MyPrivateCoachVO vo = new MyPrivateCoachVO();
        vo.setId(coachId);
        vo.setName(coachUser.getUsername());
        vo.setAvatar(coachUser.getAvatar());

        if (coachDetail != null) {
            vo.setPersonalImageUrl(coachDetail.getPersonalImageUrl());
            vo.setWorkYears(coachDetail.getWorkYears());
            vo.setStudentCount(coachDetail.getStudentCount());
            vo.setRating(coachDetail.getRating());
            vo.setBio(coachDetail.getBio());
            vo.setTeachingStyle(coachDetail.getTeachingStyle());

            // 解析JSON数组
            String tagsJson = coachDetail.getTagsJson();
            if (tagsJson != null) {
                vo.setTags(JSONUtil.parseArray(tagsJson).toList(String.class));
            }
            String specialtiesJson = coachDetail.getSpecialtiesJson();
            if (specialtiesJson != null) {
                vo.setSpecialties(JSONUtil.parseArray(specialtiesJson).toList(String.class));
            }
        }

        return vo;
    }
}

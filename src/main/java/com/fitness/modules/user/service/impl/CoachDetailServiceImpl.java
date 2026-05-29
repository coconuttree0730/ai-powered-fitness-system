package com.fitness.modules.user.service.impl;

import cn.hutool.json.JSONUtil;
import com.fitness.common.cache.RedisCacheNames;
import com.fitness.common.cache.RedisTemplateCacheSupport;
import com.fitness.common.constants.ErrorCode;
import com.fitness.common.exception.BusinessException;
import com.fitness.integration.minio.model.vo.FileUploadVO;
import com.fitness.integration.minio.service.FileService;
import com.fitness.integration.security.SecurityUtils;
import com.fitness.modules.coach.mapper.CoachPackageMapper;
import com.fitness.modules.coach.model.entity.CoachPackage;
import com.fitness.modules.coach.model.vo.CoachPackageVO;
import com.fitness.modules.user.mapper.CoachDetailMapper;
import com.fitness.modules.user.mapper.UserFitnessProfileMapper;
import com.fitness.modules.user.mapper.UserMapper;
import com.fitness.modules.user.model.dto.CoachDetailDTO;
import com.fitness.modules.user.model.entity.CoachDetail;
import com.fitness.modules.user.model.entity.User;
import com.fitness.modules.user.model.entity.UserFitnessProfile;
import com.fitness.modules.user.model.vo.CoachDetailVO;
import com.fitness.modules.user.model.vo.CoachHomePageVO;
import com.fitness.modules.user.model.vo.HomePageCoachVO;
import com.fitness.modules.user.model.vo.MyPrivateCoachVO;
import com.fitness.modules.user.service.CoachDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoachDetailServiceImpl implements CoachDetailService {

    private static final int MAX_TAG_COUNT = 5;
    private static final int MAX_TAG_LENGTH = 10;

    private final CoachDetailMapper coachDetailMapper;
    private final UserMapper userMapper;
    private final UserFitnessProfileMapper userFitnessProfileMapper;
    private final FileService fileService;
    private final CoachPackageMapper coachPackageMapper;
    private final RedisTemplateCacheSupport redisTemplateCacheSupport;

    @Override
    public CoachDetailVO getCoachDetail(Long userId) {
        CoachDetail detail = coachDetailMapper.selectByUserId(userId);
        User user = userMapper.selectById(userId);
        return convertToVO(detail, user);
    }

    @Override
    public CoachDetailVO getCurrentCoachDetail() {
        return getCoachDetail(requireCurrentUserId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = RedisCacheNames.COACH_HOME, allEntries = true)
    public CoachDetailVO updateCoachDetail(CoachDetailDTO dto) {
        Long userId = requireCurrentUserId();

        if (StringUtils.hasText(dto.getUsername())) {
            validateUsername(dto.getUsername(), userId);
            updateUsername(userId, dto.getUsername());
        }

        CoachDetail detail = coachDetailMapper.selectByUserId(userId);
        boolean isNew = detail == null;
        if (detail == null) {
            detail = new CoachDetail();
            detail.setUserId(userId);
        }

        applyDetailUpdates(detail, dto);
        saveCoachDetail(detail, isNew);
        redisTemplateCacheSupport.evictAll(RedisCacheNames.COACH_HOME);
        return convertToVO(detail, userMapper.selectById(userId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = RedisCacheNames.COACH_HOME, allEntries = true)
    public String uploadPersonalImage(MultipartFile file) {
        Long userId = requireCurrentUserId();
        validateImageFile(file);

        CoachDetail detail = coachDetailMapper.selectByUserId(userId);
        if (detail != null && StringUtils.hasText(detail.getPersonalImageUrl())) {
            deleteImageQuietly(detail.getPersonalImageUrl());
        }

        FileUploadVO uploadResult = fileService.uploadFile(file, "coach-images/" + userId);
        String imageUrl = uploadResult.getFileUrl();

        boolean isNew = detail == null;
        if (detail == null) {
            detail = new CoachDetail();
            detail.setUserId(userId);
        }
        detail.setPersonalImageUrl(imageUrl);
        saveCoachDetail(detail, isNew);

        redisTemplateCacheSupport.evictAll(RedisCacheNames.COACH_HOME);
        log.info("教练个人图片上传成功: userId={}, url={}", userId, imageUrl);
        return imageUrl;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = RedisCacheNames.COACH_HOME, allEntries = true)
    public void deletePersonalImage() {
        Long userId = requireCurrentUserId();
        CoachDetail detail = coachDetailMapper.selectByUserId(userId);
        if (detail == null || !StringUtils.hasText(detail.getPersonalImageUrl())) {
            return;
        }

        deleteImageQuietly(detail.getPersonalImageUrl());
        detail.setPersonalImageUrl(null);
        coachDetailMapper.updateById(detail);

        redisTemplateCacheSupport.evictAll(RedisCacheNames.COACH_HOME);
        log.info("教练个人图片删除成功: userId={}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = RedisCacheNames.COACH_HOME, allEntries = true)
    public CoachDetailVO updateTags(List<String> tags) {
        Long userId = requireCurrentUserId();
        validateTags(tags);

        CoachDetail detail = coachDetailMapper.selectByUserId(userId);
        boolean isNew = detail == null;
        if (detail == null) {
            detail = new CoachDetail();
            detail.setUserId(userId);
        }

        detail.setTags(JSONUtil.toJsonStr(tags != null ? tags : new ArrayList<>()));
        saveCoachDetail(detail, isNew);
        redisTemplateCacheSupport.evictAll(RedisCacheNames.COACH_HOME);
        return convertToVO(detail, userMapper.selectById(userId));
    }

    @Override
    public List<HomePageCoachVO> getHomePageCoaches(int limit) {
        int safeLimit = limit <= 0 ? 4 : limit;
        return redisTemplateCacheSupport.getOrLoad(RedisCacheNames.COACH_HOME, String.valueOf(safeLimit), () -> {
            List<HomePageCoachVO> coaches = coachDetailMapper.selectHomePageCoaches(safeLimit).stream()
                    .map(this::convertToHomePageVO)
                    .collect(Collectors.toList());
            log.debug("[DB LOAD] homepage coaches, limit={}, count={}", safeLimit, coaches.size());
            return coaches;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = RedisCacheNames.COACH_HOME, allEntries = true)
    public void initCoachDetail(Long userId) {
        if (coachDetailMapper.selectByUserId(userId) != null) {
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

        redisTemplateCacheSupport.evictAll(RedisCacheNames.COACH_HOME);
        log.info("初始化教练详情成功: userId={}", userId);
    }

    @Override
    public MyPrivateCoachVO getMyPrivateCoach() {
        Long userId = requireCurrentUserId();
        UserFitnessProfile profile = userFitnessProfileMapper.selectByUserId(userId);
        if (profile == null || profile.getPrivateCoachId() == null) {
            return null;
        }

        Long coachId = profile.getPrivateCoachId();
        CoachDetail coachDetail = coachDetailMapper.selectByUserId(coachId);
        User coachUser = userMapper.selectById(coachId);
        if (coachUser == null) {
            return null;
        }

        MyPrivateCoachVO vo = new MyPrivateCoachVO();
        vo.setId(coachId);
        vo.setAvatar(coachUser.getAvatar());

        if (coachDetail == null) {
            vo.setName(coachUser.getUsername());
            return vo;
        }

        vo.setName(StringUtils.hasText(coachDetail.getRealName()) ? coachDetail.getRealName() : coachUser.getUsername());
        vo.setPersonalImageUrl(coachDetail.getPersonalImageUrl());
        vo.setWorkYears(coachDetail.getWorkYears());
        vo.setStudentCount(coachDetail.getStudentCount());
        vo.setRating(coachDetail.getRating());
        vo.setBio(coachDetail.getBio());
        vo.setTeachingStyle(coachDetail.getTeachingStyle());
        vo.setTags(parseJsonList(coachDetail.getTags()));
        vo.setSpecialties(parseJsonList(coachDetail.getSpecialties()));
        return vo;
    }

    @Override
    public List<CoachPackageVO> getCoachPackages(Long coachId) {
        List<CoachPackage> packages = coachPackageMapper.selectByCoachId(coachId);

        return packages.stream().map(pkg -> {
            CoachPackageVO vo = new CoachPackageVO();
            vo.setId(pkg.getId());
            vo.setName(pkg.getName());
            vo.setPackageCode(pkg.getPackageCode());
            vo.setDescription(pkg.getDescription());
            vo.setCoverImage(pkg.getCoverImage());
            vo.setOriginalPrice(pkg.getOriginalPrice());
            vo.setTotalSessions(pkg.getTotalSessions());
            vo.setValidityDays(pkg.getValidityDays());
            vo.setStatus(pkg.getStatus());
            return vo;
        }).collect(Collectors.toList());
    }

    private Long requireCurrentUserId() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return userId;
    }

    private void validateUsername(String username, Long userId) {
        if (username.length() < 6 || username.length() > 20) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户名长度必须在6-20个字符之间");
        }
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户名只能包含字母、数字和下划线");
        }

        User existingUser = userMapper.selectByUsername(username);
        if (existingUser != null && !existingUser.getId().equals(userId)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户名已被使用");
        }
    }

    private void updateUsername(Long userId, String username) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (username.equals(user.getUsername())) {
            return;
        }

        user.setUsername(username);
        userMapper.updateById(user);
        log.info("更新用户名成功: userId={}, username={}", userId, username);
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }
        if (file.getSize() > 10 * 1024 * 1024L) {
            throw new BusinessException(ErrorCode.FILE_TOO_LARGE);
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED);
        }
    }

    private void validateTags(List<String> tags) {
        if (tags == null) {
            return;
        }
        if (tags.size() > MAX_TAG_COUNT) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "标签数量不能超过" + MAX_TAG_COUNT + "个");
        }

        for (String tag : tags) {
            if (!StringUtils.hasText(tag)) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "标签不能为空");
            }
            if (tag.length() > MAX_TAG_LENGTH) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "标签长度不能超过" + MAX_TAG_LENGTH + "个字符");
            }
        }
    }

    private void applyDetailUpdates(CoachDetail detail, CoachDetailDTO dto) {
        if (dto.getRealName() != null) {
            detail.setRealName(dto.getRealName());
        }
        if (dto.getGender() != null) {
            detail.setGender(dto.getGender());
        }
        if (dto.getAge() != null) {
            detail.setAge(dto.getAge());
        }
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

        vo.setRealName(detail.getRealName());
        vo.setGender(detail.getGender());
        vo.setAge(detail.getAge());
        vo.setPersonalImageUrl(detail.getPersonalImageUrl());
        vo.setWorkYears(detail.getWorkYears());
        vo.setTeachingStyle(detail.getTeachingStyle());
        vo.setEducation(detail.getEducation());
        vo.setTraining(detail.getTraining());
        vo.setBio(detail.getBio());
        vo.setExperience(detail.getExperience());
        vo.setStudentCount(detail.getStudentCount());
        vo.setRating(detail.getRating());
        vo.setTags(parseJsonList(detail.getTags()));
        vo.setSpecialties(parseJsonList(detail.getSpecialties()));
        vo.setLanguages(parseJsonList(detail.getLanguages()));
        vo.setHonors(parseJsonList(detail.getHonors()));
        return vo;
    }

    private HomePageCoachVO convertToHomePageVO(CoachHomePageVO detail) {
        HomePageCoachVO vo = new HomePageCoachVO();
        vo.setId(detail.getUserId());
        vo.setName(StringUtils.hasText(detail.getRealName()) ? detail.getRealName() : detail.getUsername());
        vo.setTitle(generateCoachTitle(detail));
        vo.setImage(StringUtils.hasText(detail.getPersonalImageUrl()) ? detail.getPersonalImageUrl() : detail.getAvatar());
        vo.setExperience(detail.getWorkYears() != null && detail.getWorkYears() > 0 ? detail.getWorkYears() + "+" : "5+");
        vo.setStudents(detail.getStudentCount() != null && detail.getStudentCount() > 0 ? detail.getStudentCount() + "+" : "1000+");
        vo.setRating(detail.getRating() != null ? detail.getRating() : "99%");
        vo.setRatingScore(convertRatingToScore(detail.getRating()));
        vo.setBio(detail.getBio() != null ? detail.getBio() : "专业健身教练，拥有丰富的教学经验，致力于帮助学员达成健身目标。");

        List<String> tags = parseJsonList(detail.getTags());
        if (tags == null || tags.isEmpty()) {
            vo.setTags(List.of("专业教练"));
        } else {
            vo.setTags(tags.size() > 3 ? tags.subList(0, 3) : tags);
        }
        return vo;
    }

    private List<String> parseJsonList(Object value) {
        if (value == null) {
            return new ArrayList<>();
        }
        if (value instanceof List<?> list) {
            return list.stream()
                    .filter(item -> item != null && StringUtils.hasText(item.toString()))
                    .map(Object::toString)
                    .collect(Collectors.toList());
        }
        String json = toJsonString(value);
        if (!StringUtils.hasText(json)) {
            return new ArrayList<>();
        }
        String trimmedJson = json.trim();
        if (!trimmedJson.startsWith("[") && !trimmedJson.startsWith("{")) {
            return List.of(trimmedJson);
        }
        try {
            return JSONUtil.parseArray(trimmedJson).toList(String.class);
        } catch (Exception e) {
            log.warn("Failed to parse coach detail list field, fallback to empty list: {}", trimmedJson, e);
            return new ArrayList<>();
        }
    }

    private void deleteImageQuietly(String imageUrl) {
        try {
            fileService.deleteFile(imageUrl);
        } catch (Exception e) {
            log.warn("删除图片失败: {}", e.getMessage());
        }
    }

    private void saveCoachDetail(CoachDetail detail, boolean isNew) {
        if (isNew) {
            coachDetailMapper.insert(detail);
            return;
        }
        coachDetailMapper.updateById(detail);
    }

    private Double convertRatingToScore(String rating) {
        if (!StringUtils.hasText(rating)) {
            return 4.9;
        }
        String numberStr = rating.replace("%", "");
        try {
            int percentage = Integer.parseInt(numberStr);
            double score = percentage / 20.0;
            return Math.round(score * 10) / 10.0;
        } catch (NumberFormatException e) {
            return 4.9;
        }
    }

    private String generateCoachTitle(CoachHomePageVO detail) {
        List<String> specialties = parseJsonList(detail.getSpecialties());
        if (specialties != null && !specialties.isEmpty()) {
            String mainSpecialty = specialties.get(0);
            return switch (mainSpecialty) {
                case "瑜伽" -> "瑜伽认证导师";
                case "普拉提" -> "普拉提认证教练";
                case "力量训练" -> "力量训练专家";
                case "康复训练" -> "运动康复师";
                case "CrossFit" -> "CrossFit认证教练";
                case "搏击" -> "搏击格斗教练";
                case "游泳" -> "游泳教练";
                case "减脂" -> "减脂塑形专家";
                case "增肌" -> "增肌训练专家";
                default -> mainSpecialty + "教练";
            };
        }

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

        if (detail.getWorkYears() != null && detail.getWorkYears() >= 10) {
            return "高级私人教练";
        }
        if (detail.getWorkYears() != null && detail.getWorkYears() >= 5) {
            return "资深健身教练";
        }
        return "专业教练";
    }

    private String toJsonString(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof String) {
            return (String) obj;
        }
        return obj.toString();
    }
}

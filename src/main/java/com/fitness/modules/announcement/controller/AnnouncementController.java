package com.fitness.modules.announcement.controller;

import com.fitness.common.result.Result;
import com.fitness.modules.announcement.model.dto.AnnouncementDTO;
import com.fitness.modules.announcement.model.vo.AnnouncementVO;
import com.fitness.modules.announcement.service.AnnouncementService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公告管理 Controller
 * 基础路径：/api/v1/announcements
 */
@RestController
@RequestMapping("/api/v1/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    /**
     * 获取所有已发布的公告（公开接口，供首页使用）
     *
     * @return 公告列表
     */
    @GetMapping("/published")
    public Result<List<AnnouncementVO>> getPublishedAnnouncements() {
        List<AnnouncementVO> announcements = announcementService.getPublishedAnnouncements();
        return Result.success(announcements);
    }

    /**
     * 获取所有公告（管理后台使用）
     *
     * @return 公告列表
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<AnnouncementVO>> getAllAnnouncements() {
        List<AnnouncementVO> announcements = announcementService.getAllAnnouncements();
        return Result.success(announcements);
    }

    /**
     * 根据ID获取公告详情
     *
     * @param id 公告ID
     * @return 公告详情
     */
    @GetMapping("/{id}")
    public Result<AnnouncementVO> getAnnouncementById(@PathVariable Long id) {
        AnnouncementVO announcement = announcementService.getAnnouncementById(id);
        return Result.success(announcement);
    }

    /**
     * 创建公告
     *
     * @param announcementDTO 公告信息
     * @return 创建后的公告
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<AnnouncementVO> createAnnouncement(@Valid @RequestBody AnnouncementDTO announcementDTO) {
        AnnouncementVO announcement = announcementService.createAnnouncement(announcementDTO);
        return Result.success(announcement);
    }

    /**
     * 更新公告
     *
     * @param id              公告ID
     * @param announcementDTO 公告信息
     * @return 更新后的公告
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<AnnouncementVO> updateAnnouncement(@PathVariable Long id, @Valid @RequestBody AnnouncementDTO announcementDTO) {
        AnnouncementVO announcement = announcementService.updateAnnouncement(id, announcementDTO);
        return Result.success(announcement);
    }

    /**
     * 删除公告
     *
     * @param id 公告ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteAnnouncement(@PathVariable Long id) {
        announcementService.deleteAnnouncement(id);
        return Result.success();
    }

    /**
     * 批量删除公告
     *
     * @param ids 公告ID列表
     * @return 成功响应
     */
    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteAnnouncements(@RequestBody @NotEmpty(message = "ID列表不能为空") List<Long> ids) {
        announcementService.deleteAnnouncements(ids);
        return Result.success();
    }

    /**
     * 发布公告
     *
     * @param id 公告ID
     * @return 成功响应
     */
    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> publishAnnouncement(@PathVariable Long id) {
        announcementService.publishAnnouncement(id);
        return Result.success();
    }

    /**
     * 下架公告
     *
     * @param id 公告ID
     * @return 成功响应
     */
    @PatchMapping("/{id}/unpublish")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> unpublishAnnouncement(@PathVariable Long id) {
        announcementService.unpublishAnnouncement(id);
        return Result.success();
    }

    /**
     * 增加浏览量
     *
     * @param id 公告ID
     * @return 成功响应
     */
    @PatchMapping("/{id}/view")
    public Result<Void> incrementViewCount(@PathVariable Long id) {
        announcementService.incrementViewCount(id);
        return Result.success();
    }
}

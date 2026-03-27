package com.fitness.modules.user.controller;

import com.fitness.common.result.Result;
import com.fitness.modules.user.model.dto.CoachDetailDTO;
import com.fitness.modules.user.model.vo.CoachDetailVO;
import com.fitness.modules.user.model.vo.HomePageCoachVO;
import com.fitness.modules.user.service.CoachDetailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 教练详情Controller
 * 处理教练个人信息管理相关请求
 */
@RestController
@RequestMapping("/api/v1/coaches")
@RequiredArgsConstructor
public class CoachDetailController {

    private final CoachDetailService coachDetailService;

    /**
     * 获取当前登录教练的详情
     *
     * @return 教练详情
     */
    @GetMapping("/detail")
    public Result<CoachDetailVO> getCurrentCoachDetail() {
        CoachDetailVO vo = coachDetailService.getCurrentCoachDetail();
        return Result.success(vo);
    }

    /**
     * 获取指定教练的详情
     *
     * @param id 教练ID
     * @return 教练详情
     */
    @GetMapping("/{id}/detail")
    public Result<CoachDetailVO> getCoachDetail(@PathVariable Long id) {
        CoachDetailVO vo = coachDetailService.getCoachDetail(id);
        return Result.success(vo);
    }

    /**
     * 更新教练详情
     *
     * @param dto 教练详情DTO
     * @return 更新后的教练详情
     */
    @PutMapping("/detail")
    public Result<CoachDetailVO> updateCoachDetail(@Valid @RequestBody CoachDetailDTO dto) {
        CoachDetailVO vo = coachDetailService.updateCoachDetail(dto);
        return Result.success(vo);
    }

    /**
     * 上传个人展示图片
     *
     * @param file 图片文件
     * @return 图片URL
     */
    @PostMapping("/detail/image")
    public Result<String> uploadPersonalImage(@RequestParam("file") MultipartFile file) {
        String imageUrl = coachDetailService.uploadPersonalImage(file);
        return Result.success(imageUrl);
    }

    /**
     * 删除个人展示图片
     *
     * @return 操作结果
     */
    @DeleteMapping("/detail/image")
    public Result<Void> deletePersonalImage() {
        coachDetailService.deletePersonalImage();
        return Result.success();
    }

    /**
     * 更新教练标签
     *
     * @param tags 标签列表
     * @return 更新后的教练详情
     */
    @PutMapping("/detail/tags")
    public Result<CoachDetailVO> updateTags(@RequestBody List<String> tags) {
        CoachDetailVO vo = coachDetailService.updateTags(tags);
        return Result.success(vo);
    }

    /**
     * 获取首页展示的教练列表
     *
     * @param limit 限制数量（默认4个）
     * @return 首页教练列表
     */
    @GetMapping("/home")
    public Result<List<HomePageCoachVO>> getHomePageCoaches(
            @RequestParam(defaultValue = "4") int limit) {
        List<HomePageCoachVO> list = coachDetailService.getHomePageCoaches(limit);
        return Result.success(list);
    }
}

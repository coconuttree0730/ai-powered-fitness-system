package com.fitness.modules.user.controller;

import com.fitness.common.result.Result;
import com.fitness.modules.user.model.dto.CoachDetailDTO;
import com.fitness.modules.user.model.vo.CoachDetailVO;
import com.fitness.modules.user.model.vo.HomePageCoachVO;
import com.fitness.modules.user.model.vo.MyPrivateCoachVO;
import com.fitness.modules.user.service.CoachDetailService;
import com.fitness.modules.product.model.vo.ProductVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/coaches")
@RequiredArgsConstructor
@Tag(name = "教练详情", description = "教练个人资料管理、图片上传、标签及首页展示接口")
public class CoachDetailController {

    private final CoachDetailService coachDetailService;

    @Operation(summary = "获取当前登录教练的详情")
    @GetMapping("/detail")
    @PreAuthorize("hasRole('COACH')")
    public Result<CoachDetailVO> getCurrentCoachDetail() {
        CoachDetailVO vo = coachDetailService.getCurrentCoachDetail();
        return Result.success(vo);
    }

    @Operation(summary = "获取指定教练的详情")
    @GetMapping("/{id}/detail")
    public Result<CoachDetailVO> getCoachDetail(@PathVariable Long id) {
        CoachDetailVO vo = coachDetailService.getCoachDetail(id);
        return Result.success(vo);
    }

    @Operation(summary = "更新教练详情")
    @PutMapping("/detail")
    @PreAuthorize("hasRole('COACH')")
    public Result<CoachDetailVO> updateCoachDetail(@Valid @RequestBody CoachDetailDTO dto) {
        CoachDetailVO vo = coachDetailService.updateCoachDetail(dto);
        return Result.success(vo);
    }

    @Operation(summary = "上传个人展示图片")
    @PostMapping("/detail/image")
    @PreAuthorize("hasRole('COACH')")
    public Result<String> uploadPersonalImage(@RequestParam("file") MultipartFile file) {
        String imageUrl = coachDetailService.uploadPersonalImage(file);
        return Result.success(imageUrl);
    }

    @Operation(summary = "删除个人展示图片")
    @DeleteMapping("/detail/image")
    @PreAuthorize("hasRole('COACH')")
    public Result<Void> deletePersonalImage() {
        coachDetailService.deletePersonalImage();
        return Result.success();
    }

    @Operation(summary = "更新教练标签")
    @PutMapping("/detail/tags")
    @PreAuthorize("hasRole('COACH')")
    public Result<CoachDetailVO> updateTags(@RequestBody List<String> tags) {
        CoachDetailVO vo = coachDetailService.updateTags(tags);
        return Result.success(vo);
    }

    @Operation(summary = "获取首页展示的教练列表")
    @GetMapping("/home")
    public Result<List<HomePageCoachVO>> getHomePageCoaches(
            @RequestParam(defaultValue = "4") int limit) {
        List<HomePageCoachVO> list = coachDetailService.getHomePageCoaches(limit);
        return Result.success(list);
    }

    @Operation(summary = "获取当前会员的专属教练")
    @GetMapping("/my-private-coach")
    @PreAuthorize("isAuthenticated()")
    public Result<MyPrivateCoachVO> getMyPrivateCoach() {
        MyPrivateCoachVO vo = coachDetailService.getMyPrivateCoach();
        return Result.success(vo);
    }

    @Operation(summary = "获取指定教练的可购买私教套餐列表")
    @GetMapping("/{id}/packages")
    @PreAuthorize("isAuthenticated()")
    public Result<List<ProductVO>> getCoachPackages(@PathVariable Long id) {
        List<ProductVO> packages = coachDetailService.getCoachPackages(id);
        return Result.success(packages);
    }
}

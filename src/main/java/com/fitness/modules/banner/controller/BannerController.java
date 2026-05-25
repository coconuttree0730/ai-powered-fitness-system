package com.fitness.modules.banner.controller;

import com.fitness.common.result.Result;
import com.fitness.modules.banner.model.dto.BannerDTO;
import com.fitness.modules.banner.model.vo.BannerVO;
import com.fitness.modules.banner.service.BannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 轮播图管理 Controller
 * 基础路径：/api/v1/banners
 */
@Tag(name = "轮播图管理", description = "首页轮播图的增删改查与状态管理")
@RestController
@RequestMapping("/api/v1/banners")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    /**
     * 获取所有显示的轮播图（公开接口，供首页使用）
     *
     * @return 轮播图列表
     */
    @Operation(summary = "获取所有显示的轮播图")
    @GetMapping("/active")
    public Result<List<BannerVO>> getActiveBanners() {
        List<BannerVO> banners = bannerService.getActiveBanners();
        return Result.success(banners);
    }

    /**
     * 获取所有轮播图（管理后台使用）
     *
     * @return 轮播图列表
     */
    @Operation(summary = "获取所有轮播图")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<BannerVO>> getAllBanners() {
        List<BannerVO> banners = bannerService.getAllBanners();
        return Result.success(banners);
    }

    /**
     * 根据ID获取轮播图详情
     *
     * @param id 轮播图ID
     * @return 轮播图详情
     */
    @Operation(summary = "根据ID获取轮播图详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<BannerVO> getBannerById(@PathVariable Long id) {
        BannerVO banner = bannerService.getBannerById(id);
        return Result.success(banner);
    }

    /**
     * 创建轮播图
     *
     * @param bannerDTO 轮播图信息
     * @return 创建后的轮播图
     */
    @Operation(summary = "创建轮播图")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<BannerVO> createBanner(@Valid @RequestBody BannerDTO bannerDTO) {
        BannerVO banner = bannerService.createBanner(bannerDTO);
        return Result.success(banner);
    }

    /**
     * 更新轮播图
     *
     * @param id        轮播图ID
     * @param bannerDTO 轮播图信息
     * @return 更新后的轮播图
     */
    @Operation(summary = "更新轮播图")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<BannerVO> updateBanner(@PathVariable Long id, @Valid @RequestBody BannerDTO bannerDTO) {
        BannerVO banner = bannerService.updateBanner(id, bannerDTO);
        return Result.success(banner);
    }

    /**
     * 删除轮播图
     *
     * @param id 轮播图ID
     * @return 成功响应
     */
    @Operation(summary = "删除轮播图")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteBanner(@PathVariable Long id) {
        bannerService.deleteBanner(id);
        return Result.success();
    }

    /**
     * 批量删除轮播图
     *
     * @param ids 轮播图ID列表
     * @return 成功响应
     */
    @Operation(summary = "批量删除轮播图")
    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteBanners(@RequestBody @NotEmpty(message = "ID列表不能为空") List<Long> ids) {
        bannerService.deleteBanners(ids);
        return Result.success();
    }

    /**
     * 更新轮播图状态
     *
     * @param id     轮播图ID
     * @param status 状态：0-隐藏，1-显示
     * @return 成功响应
     */
    @Operation(summary = "更新轮播图状态")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateBannerStatus(@PathVariable Long id, @RequestParam Integer status) {
        bannerService.updateBannerStatus(id, status);
        return Result.success();
    }
}

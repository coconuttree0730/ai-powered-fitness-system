package com.fitness.modules.banner.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.modules.banner.model.entity.Banner;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 轮播图Mapper
 */
@Mapper
public interface BannerMapper extends BaseMapper<Banner> {

    /**
     * 查询所有显示的轮播图，按排序顺序排列
     *
     * @return 轮播图列表
     */
    @Select("SELECT * FROM sys_banner WHERE status = 1 AND deleted = false ORDER BY sort_order ASC, id DESC")
    List<Banner> selectActiveBanners();
}

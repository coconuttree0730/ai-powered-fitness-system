package com.fitness.modules.coach.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.modules.coach.model.entity.CoachPackage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CoachPackageMapper extends BaseMapper<CoachPackage> {

    @Select("SELECT * FROM coach_package WHERE coach_id = #{coachId} AND status = 'ACTIVE' ORDER BY sort_order")
    List<CoachPackage> selectByCoachId(@Param("coachId") Long coachId);

    @Select("SELECT * FROM coach_package WHERE status = 'ACTIVE' ORDER BY sort_order")
    List<CoachPackage> selectAllActive();

    @Select("<script>"
            + "SELECT * FROM coach_package WHERE 1=1"
            + "<if test='keyword != null and keyword != \"\"'> AND name LIKE CONCAT('%', #{keyword}, '%')</if>"
            + "<if test='status != null and status != \"\"'> AND status = #{status}</if>"
            + "<if test='coachId != null'> AND coach_id = #{coachId}</if>"
            + " ORDER BY sort_order"
            + "</script>")
    IPage<CoachPackage> selectAdminPage(Page<CoachPackage> page,
                                        @Param("keyword") String keyword,
                                        @Param("status") String status,
                                        @Param("coachId") Long coachId);

    @Select("SELECT * FROM coach_package ORDER BY sort_order")
    List<CoachPackage> selectAll();
}
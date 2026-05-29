package com.fitness.modules.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.modules.user.model.entity.CoachStudent;
import com.fitness.modules.user.model.vo.CoachStudentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CoachStudentMapper extends BaseMapper<CoachStudent> {

    @Select("SELECT cs.*, COALESCE(su.nickname, su.username, CONCAT('学员', cs.member_id)) AS studentName, " +
            "su.avatar AS studentAvatar, COALESCE(su.phone, '') AS studentPhone " +
            "FROM coach_student cs " +
            "LEFT JOIN sys_user su ON cs.member_id = su.id " +
            "WHERE cs.coach_id = #{coachId} AND cs.status = 'ACTIVE' AND cs.deleted = false " +
            "ORDER BY cs.bind_time DESC")
    List<CoachStudentVO> selectByCoachId(@Param("coachId") Long coachId);

    @Select("SELECT * FROM coach_student " +
            "WHERE member_id = #{memberId} AND coach_id = #{coachId} " +
            "AND status = 'ACTIVE' AND deleted = false " +
            "LIMIT 1")
    CoachStudent selectByMemberAndCoach(@Param("memberId") Long memberId, @Param("coachId") Long coachId);
}

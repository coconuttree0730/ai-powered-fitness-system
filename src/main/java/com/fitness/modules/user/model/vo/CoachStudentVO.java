package com.fitness.modules.user.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CoachStudentVO {

    private Long id;

    private Long memberId;

    private Long coachId;

    private Long coachPackageId;

    private String packageCode;

    private LocalDateTime bindTime;

    private LocalDateTime expireTime;

    private String status;

    private String studentName;

    private String studentAvatar;

    private String studentPhone;
}

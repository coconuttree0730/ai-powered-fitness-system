package com.fitness.modules.equipment.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.constants.ErrorCode;
import com.fitness.common.exception.BusinessException;
import com.fitness.modules.equipment.mapper.EquipmentMapper;
import com.fitness.modules.equipment.mapper.EquipmentRepairMapper;
import com.fitness.modules.equipment.mapper.EquipmentTypeMapper;
import com.fitness.modules.equipment.mapper.RepairRecordMapper;
import com.fitness.modules.equipment.model.dto.EquipmentDTO;
import com.fitness.modules.equipment.model.dto.EquipmentQueryDTO;
import com.fitness.modules.equipment.model.dto.RepairDTO;
import com.fitness.modules.equipment.model.dto.RepairHandleDTO;
import com.fitness.modules.equipment.model.entity.Equipment;
import com.fitness.modules.equipment.model.entity.EquipmentRepair;
import com.fitness.modules.equipment.model.entity.EquipmentType;
import com.fitness.modules.equipment.model.entity.RepairRecord;
import com.fitness.modules.equipment.model.enums.EquipmentStatus;
import com.fitness.modules.equipment.model.enums.RepairRecordType;
import com.fitness.modules.equipment.model.enums.RepairStatus;
import com.fitness.modules.equipment.model.vo.EquipmentVO;
import com.fitness.modules.equipment.model.vo.MyRepairVO;
import com.fitness.modules.equipment.model.vo.RepairRecordVO;
import com.fitness.modules.equipment.model.vo.RepairVO;
import com.fitness.modules.equipment.service.EquipmentService;
import com.fitness.integration.minio.service.FileService;
import cn.hutool.core.bean.BeanUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 器材服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EquipmentServiceImpl implements EquipmentService {

    private final EquipmentMapper equipmentMapper;
    private final EquipmentRepairMapper equipmentRepairMapper;
    private final EquipmentTypeMapper equipmentTypeMapper;
    private final RepairRecordMapper repairRecordMapper;
    private final FileService fileService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createEquipment(EquipmentDTO dto) {

        Equipment equipment = new Equipment();
        BeanUtil.copyProperties(dto, equipment);
        equipment.setStatus(dto.getStatus() != null ? dto.getStatus() : EquipmentStatus.NORMAL.getCode());
        equipment.setCreateTime(LocalDateTime.now());
        equipment.setUpdateTime(LocalDateTime.now());
        equipment.setDeleted(false);

        equipmentMapper.insert(equipment);
        log.info("器材创建成功: equipmentId={}, equipmentName={}", equipment.getId(), equipment.getEquipmentName());

        return equipment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEquipment(Long id, EquipmentDTO dto) {
        Equipment existingEquipment = equipmentMapper.selectById(id);
        if (existingEquipment == null || existingEquipment.getDeleted()) {
            throw new BusinessException(ErrorCode.EQUIPMENT_NOT_FOUND);
        }

        // 如果更换了图片，删除旧图片
        if (StringUtils.hasText(dto.getImageUrl())
                && !dto.getImageUrl().equals(existingEquipment.getImageUrl())
                && StringUtils.hasText(existingEquipment.getImageUrl())) {
            try {
                fileService.deleteFile(existingEquipment.getImageUrl());
                log.info("器材旧图片删除成功: equipmentId={}, oldImageUrl={}", id, existingEquipment.getImageUrl());
            } catch (Exception e) {
                log.warn("器材旧图片删除失败: equipmentId={}, oldImageUrl={}, error={}", id, existingEquipment.getImageUrl(), e.getMessage());
            }
        }

        Integer originalStatus = existingEquipment.getStatus();
        BeanUtil.copyProperties(dto, existingEquipment);
        if (dto.getStatus() == null) {
            existingEquipment.setStatus(originalStatus);
        }
        existingEquipment.setUpdateTime(LocalDateTime.now());

        equipmentMapper.updateById(existingEquipment);
        log.info("器材更新成功: equipmentId={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteEquipment(Long id) {
        Equipment existingEquipment = equipmentMapper.selectById(id);
        if (existingEquipment == null || existingEquipment.getDeleted()) {
            throw new BusinessException(ErrorCode.EQUIPMENT_NOT_FOUND);
        }

        // 检查是否有待处理的报修记录
        List<RepairVO> repairs = equipmentRepairMapper.selectAllRepairs();
        boolean hasPendingRepair = repairs.stream()
                .anyMatch(r -> r.getStatus().equals(RepairStatus.PENDING.getCode())
                        || r.getStatus().equals(RepairStatus.PROCESSING.getCode()));

        if (hasPendingRepair) {
            throw new BusinessException(ErrorCode.EQUIPMENT_IN_USE);
        }

        // 删除器材图片
        if (StringUtils.hasText(existingEquipment.getImageUrl())) {
            try {
                fileService.deleteFile(existingEquipment.getImageUrl());
                log.info("器材图片删除成功: equipmentId={}, imageUrl={}", id, existingEquipment.getImageUrl());
            } catch (Exception e) {
                log.warn("器材图片删除失败: equipmentId={}, imageUrl={}, error={}", id, existingEquipment.getImageUrl(), e.getMessage());
            }
        }

        equipmentMapper.deleteById(id);
        log.info("器材删除成功: equipmentId={}", id);
    }

    @Override
    public EquipmentVO getEquipmentById(Long id) {
        EquipmentVO equipmentVO = equipmentMapper.selectEquipmentDetail(id);
        if (equipmentVO == null) {
            throw new BusinessException(ErrorCode.EQUIPMENT_NOT_FOUND);
        }
        return equipmentVO;
    }

    @Override
    public Page<EquipmentVO> getEquipmentList(EquipmentQueryDTO query) {
        Page<Equipment> page = new Page<>(query.getPageNum(), query.getPageSize());
        return equipmentMapper.selectEquipmentList(page, query);
    }

    @Override
    public List<RepairVO> getRepairList(Long equipmentId) {
        // 由于报修记录不再关联器材，返回所有报修记录
        return equipmentRepairMapper.selectAllRepairs();
    }

    @Override
    public List<RepairVO> getAllRepairs() {
        return equipmentRepairMapper.selectAllRepairs();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleRepair(Long repairId, RepairHandleDTO dto, Long handlerId) {
        EquipmentRepair repair = equipmentRepairMapper.selectById(repairId);
        if (repair == null) {
            throw new BusinessException(ErrorCode.REPAIR_NOT_FOUND);
        }

        Integer oldStatus = repair.getStatus();

        // 验证状态转换是否合法
        RepairStatus newStatus = RepairStatus.getByCode(dto.getStatus());

        if (newStatus == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "无效的状态值");
        }

        // 如果是关闭状态，删除报修图片
        if (dto.getStatus().equals(RepairStatus.CLOSED.getCode()) && StringUtils.hasText(repair.getImageUrls())) {
            deleteRepairImages(repair.getImageUrls());
        }

        repair.setStatus(dto.getStatus());

        // 如果提供了处理备注，更新备注
        if (StringUtils.hasText(dto.getRemark())) {
            repair.setHandleRemark(dto.getRemark());
        }

        // 如果是完成或关闭状态，设置处理时间
        if (dto.getStatus().equals(RepairStatus.COMPLETED.getCode()) || dto.getStatus().equals(RepairStatus.CLOSED.getCode())) {
            repair.setHandleTime(LocalDateTime.now());
        }

        repair.setUpdateTime(LocalDateTime.now());
        equipmentRepairMapper.updateById(repair);

        // 添加处理记录
        RepairRecord record = new RepairRecord();
        record.setRepairId(repairId);
        record.setHandlerId(handlerId);
        record.setRecordType(RepairRecordType.STATUS_CHANGE.getCode());
        record.setBeforeStatus(oldStatus);
        record.setAfterStatus(dto.getStatus());
        record.setContent("状态变更: " + RepairStatus.getNameByCode(oldStatus) + " -> " + RepairStatus.getNameByCode(dto.getStatus())
                + (StringUtils.hasText(dto.getRemark()) ? "，备注: " + dto.getRemark() : ""));
        record.setCreateTime(LocalDateTime.now());
        record.setDeleted(false);
        repairRecordMapper.insert(record);

        log.info("报修处理成功: repairId={}, status={}, handlerId={}", repairId, dto.getStatus(), handlerId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitRepair(Long userId, RepairDTO dto) {
        // 创建报修记录
        EquipmentRepair repair = new EquipmentRepair();
        repair.setUserId(userId);
        repair.setDescription(dto.getDescription());

        // 处理图片URL列表
        if (!CollectionUtils.isEmpty(dto.getImageUrls())) {
            repair.setImageUrls(String.join(",", dto.getImageUrls()));
        }

        repair.setStatus(RepairStatus.PENDING.getCode());
        repair.setCreateTime(LocalDateTime.now());
        repair.setUpdateTime(LocalDateTime.now());
        repair.setDeleted(false);

        equipmentRepairMapper.insert(repair);

        // 添加初始处理记录
        RepairRecord record = new RepairRecord();
        record.setRepairId(repair.getId());
        record.setHandlerId(userId);
        record.setRecordType(RepairRecordType.SUBMIT.getCode());
        record.setBeforeStatus(null);
        record.setAfterStatus(RepairStatus.PENDING.getCode());
        record.setContent("提交报修申请");
        record.setCreateTime(LocalDateTime.now());
        record.setDeleted(false);
        repairRecordMapper.insert(record);

        log.info("报修提交成功: repairId={}, userId={}", repair.getId(), userId);
        return repair.getId();
    }

    @Override
    public List<MyRepairVO> getMyRepairs(Long userId) {
        List<MyRepairVO> repairs = equipmentRepairMapper.selectMyRepairs(userId);
        // 为每个报修记录加载处理记录
        for (MyRepairVO repair : repairs) {
            List<RepairRecordVO> records = repairRecordMapper.selectRecordsByRepairId(repair.getId());
            repair.setRecords(records);
        }
        return repairs;
    }

    @Override
    public RepairVO getRepairDetail(Long userId, Long repairId) {
        RepairVO repair = equipmentRepairMapper.selectRepairById(repairId);
        if (repair == null) {
            throw new BusinessException(ErrorCode.REPAIR_NOT_FOUND);
        }

        // 加载处理记录
        List<RepairRecordVO> records = repairRecordMapper.selectRecordsByRepairId(repairId);
        repair.setRecords(records);

        return repair;
    }

    @Override
    public RepairVO getRepairDetailAdmin(Long repairId) {
        RepairVO repair = equipmentRepairMapper.selectRepairById(repairId);
        if (repair == null) {
            throw new BusinessException(ErrorCode.REPAIR_NOT_FOUND);
        }

        // 加载处理记录
        List<RepairRecordVO> records = repairRecordMapper.selectRecordsByRepairId(repairId);
        repair.setRecords(records);

        return repair;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelRepair(Long userId, Long repairId) {
        EquipmentRepair repair = equipmentRepairMapper.selectById(repairId);
        if (repair == null) {
            throw new BusinessException(ErrorCode.REPAIR_NOT_FOUND);
        }

        // 验证是否是该用户的报修记录
        if (!repair.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // 只能取消待处理的报修
        if (!repair.getStatus().equals(RepairStatus.PENDING.getCode())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "只能取消待处理的报修");
        }

        Integer oldStatus = repair.getStatus();

        // 删除报修图片
        if (StringUtils.hasText(repair.getImageUrls())) {
            deleteRepairImages(repair.getImageUrls());
        }

        // 更新报修状态为已关闭
        repair.setStatus(RepairStatus.CLOSED.getCode());
        repair.setHandleTime(LocalDateTime.now());
        repair.setUpdateTime(LocalDateTime.now());
        equipmentRepairMapper.updateById(repair);

        // 添加处理记录
        RepairRecord record = new RepairRecord();
        record.setRepairId(repairId);
        record.setHandlerId(userId);
        record.setRecordType(RepairRecordType.CANCEL.getCode());
        record.setBeforeStatus(oldStatus);
        record.setAfterStatus(RepairStatus.CLOSED.getCode());
        record.setContent("用户取消报修");
        record.setCreateTime(LocalDateTime.now());
        record.setDeleted(false);
        repairRecordMapper.insert(record);

        log.info("报修取消成功: repairId={}, userId={}", repairId, userId);
    }

    @Override
    public List<EquipmentType> getAllEquipmentTypes() {
        return equipmentTypeMapper.selectAllActiveTypes();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRepairRecord(Long repairId, String content, Long handlerId) {
        EquipmentRepair repair = equipmentRepairMapper.selectById(repairId);
        if (repair == null) {
            throw new BusinessException(ErrorCode.REPAIR_NOT_FOUND);
        }

        RepairRecord record = new RepairRecord();
        record.setRepairId(repairId);
        record.setHandlerId(handlerId);
        record.setRecordType(RepairRecordType.REMARK.getCode());
        record.setBeforeStatus(repair.getStatus());
        record.setAfterStatus(repair.getStatus());
        record.setContent(content);
        record.setCreateTime(LocalDateTime.now());
        record.setDeleted(false);
        repairRecordMapper.insert(record);

        log.info("添加处理记录成功: repairId={}, handlerId={}", repairId, handlerId);
    }

    @Override
    public List<RepairRecordVO> getRepairRecords(Long repairId) {
        return repairRecordMapper.selectRecordsByRepairId(repairId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRepair(Long repairId) {
        EquipmentRepair repair = equipmentRepairMapper.selectById(repairId);
        if (repair == null) {
            throw new BusinessException(ErrorCode.REPAIR_NOT_FOUND);
        }

        // 删除报修图片
        if (StringUtils.hasText(repair.getImageUrls())) {
            deleteRepairImages(repair.getImageUrls());
        }

        // 逻辑删除报修记录
        equipmentRepairMapper.deleteById(repairId);

        // 逻辑删除关联的处理记录
        repairRecordMapper.deleteByRepairId(repairId);

        log.info("报修记录删除成功: repairId={}", repairId);
    }

    /**
     * 删除报修图片
     */
    private void deleteRepairImages(String imageUrls) {
        if (!StringUtils.hasText(imageUrls)) {
            return;
        }

        List<String> urls = List.of(imageUrls.split(","));
        for (String url : urls) {
            if (StringUtils.hasText(url)) {
                try {
                    fileService.deleteFile(url);
                    log.info("报修图片删除成功: imageUrl={}", url);
                } catch (Exception e) {
                    log.warn("报修图片删除失败: imageUrl={}, error={}", url, e.getMessage());
                }
            }
        }
    }
}

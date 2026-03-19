package com.fitness.modules.equipment.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.constants.ErrorCode;
import com.fitness.common.exception.BusinessException;
import com.fitness.modules.equipment.mapper.EquipmentMapper;
import com.fitness.modules.equipment.mapper.EquipmentRepairMapper;
import com.fitness.modules.equipment.mapper.EquipmentTypeMapper;
import com.fitness.modules.equipment.model.dto.EquipmentDTO;
import com.fitness.modules.equipment.model.dto.EquipmentQueryDTO;
import com.fitness.modules.equipment.model.dto.RepairDTO;
import com.fitness.modules.equipment.model.entity.Equipment;
import com.fitness.modules.equipment.model.entity.EquipmentRepair;
import com.fitness.modules.equipment.model.enums.EquipmentStatus;
import com.fitness.modules.equipment.model.enums.RepairStatus;
import com.fitness.modules.equipment.model.vo.EquipmentVO;
import com.fitness.modules.equipment.model.vo.MyRepairVO;
import com.fitness.modules.equipment.model.vo.RepairVO;
import com.fitness.integration.minio.service.FileService;
import com.fitness.modules.equipment.service.EquipmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final FileService fileService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createEquipment(EquipmentDTO dto) {
        Equipment equipment = new Equipment();
        equipment.setEquipmentName(dto.getEquipmentName());
        equipment.setLocation(dto.getLocation());
        equipment.setStatus(dto.getStatus() != null ? dto.getStatus() : EquipmentStatus.NORMAL.getCode());
        equipment.setDescription(dto.getDescription());
        equipment.setImageUrl(dto.getImageUrl());
        equipment.setPurchaseDate(dto.getPurchaseDate());
        equipment.setTypeCode(dto.getTypeCode());
        equipment.setEquipmentNo(dto.getEquipmentNo());
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

        existingEquipment.setEquipmentName(dto.getEquipmentName());
        existingEquipment.setLocation(dto.getLocation());
        if (dto.getStatus() != null) {
            existingEquipment.setStatus(dto.getStatus());
        }
        existingEquipment.setDescription(dto.getDescription());
        existingEquipment.setImageUrl(dto.getImageUrl());
        existingEquipment.setPurchaseDate(dto.getPurchaseDate());
        existingEquipment.setTypeCode(dto.getTypeCode());
        existingEquipment.setEquipmentNo(dto.getEquipmentNo());
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
        List<RepairVO> repairs = equipmentRepairMapper.selectRepairList(id);
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
        return equipmentRepairMapper.selectRepairList(equipmentId);
    }

    @Override
    public List<RepairVO> getAllRepairs() {
        return equipmentRepairMapper.selectAllRepairs();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleRepair(Long repairId, Integer status) {
        EquipmentRepair repair = equipmentRepairMapper.selectById(repairId);
        if (repair == null) {
            throw new BusinessException(ErrorCode.REPAIR_NOT_FOUND);
        }

        // 验证状态转换是否合法
        RepairStatus newStatus = RepairStatus.getByCode(status);
        
        if (newStatus == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "无效的状态值");
        }

        // 如果是关闭状态，删除报修图片
        if (status.equals(RepairStatus.CLOSED.getCode()) && StringUtils.hasText(repair.getImageUrl())) {
            try {
                fileService.deleteFile(repair.getImageUrl());
                log.info("报修图片删除成功: repairId={}, imageUrl={}", repairId, repair.getImageUrl());
            } catch (Exception e) {
                log.warn("报修图片删除失败: repairId={}, imageUrl={}, error={}", repairId, repair.getImageUrl(), e.getMessage());
            }
        }

        repair.setStatus(status);
        
        // 如果是完成或关闭状态，设置处理时间
        if (status.equals(RepairStatus.COMPLETED.getCode()) || status.equals(RepairStatus.CLOSED.getCode())) {
            repair.setHandleTime(LocalDateTime.now());
        }

        equipmentRepairMapper.updateById(repair);
        
        // 如果报修完成，将器材状态更新为正常
        if (status.equals(RepairStatus.COMPLETED.getCode())) {
            Equipment equipment = equipmentMapper.selectById(repair.getEquipmentId());
            if (equipment != null && equipment.getStatus().equals(EquipmentStatus.MAINTENANCE.getCode())) {
                equipment.setStatus(EquipmentStatus.NORMAL.getCode());
                equipment.setUpdateTime(LocalDateTime.now());
                equipmentMapper.updateById(equipment);
            }
        }
        
        log.info("报修处理成功: repairId={}, status={}", repairId, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitRepair(Long userId, RepairDTO dto) {
        // 检查器材是否存在
        Equipment equipment = equipmentMapper.selectById(dto.getEquipmentId());
        if (equipment == null || equipment.getDeleted()) {
            throw new BusinessException(ErrorCode.EQUIPMENT_NOT_FOUND);
        }

        // 创建报修记录
        EquipmentRepair repair = new EquipmentRepair();
        repair.setEquipmentId(dto.getEquipmentId());
        repair.setUserId(userId);
        repair.setDescription(dto.getDescription());
        repair.setImageUrl(dto.getImageUrl());
        repair.setStatus(RepairStatus.PENDING.getCode());
        repair.setCreateTime(LocalDateTime.now());
        repair.setUpdateTime(LocalDateTime.now());

        equipmentRepairMapper.insert(repair);

        // 更新器材状态为维修中
        equipment.setStatus(EquipmentStatus.MAINTENANCE.getCode());
        equipment.setUpdateTime(LocalDateTime.now());
        equipmentMapper.updateById(equipment);

        log.info("报修提交成功: repairId={}, userId={}, equipmentId={}", repair.getId(), userId, dto.getEquipmentId());
        return repair.getId();
    }

    @Override
    public List<MyRepairVO> getMyRepairs(Long userId) {
        return equipmentRepairMapper.selectMyRepairs(userId);
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

        // 删除报修图片
        if (StringUtils.hasText(repair.getImageUrl())) {
            try {
                fileService.deleteFile(repair.getImageUrl());
                log.info("报修图片删除成功: repairId={}, imageUrl={}", repairId, repair.getImageUrl());
            } catch (Exception e) {
                log.warn("报修图片删除失败: repairId={}, imageUrl={}, error={}", repairId, repair.getImageUrl(), e.getMessage());
            }
        }

        // 更新报修状态为已关闭
        repair.setStatus(RepairStatus.CLOSED.getCode());
        repair.setHandleTime(LocalDateTime.now());
        repair.setUpdateTime(LocalDateTime.now());
        equipmentRepairMapper.updateById(repair);

        // 检查该器材是否还有其他待处理的报修
        List<RepairVO> repairs = equipmentRepairMapper.selectRepairList(repair.getEquipmentId());
        boolean hasPendingRepair = repairs.stream()
                .anyMatch(r -> r.getStatus().equals(RepairStatus.PENDING.getCode())
                        || r.getStatus().equals(RepairStatus.PROCESSING.getCode()));

        // 如果没有其他待处理报修，将器材状态恢复为正常
        if (!hasPendingRepair) {
            Equipment equipment = equipmentMapper.selectById(repair.getEquipmentId());
            if (equipment != null && equipment.getStatus().equals(EquipmentStatus.MAINTENANCE.getCode())) {
                equipment.setStatus(EquipmentStatus.NORMAL.getCode());
                equipment.setUpdateTime(LocalDateTime.now());
                equipmentMapper.updateById(equipment);
            }
        }

        log.info("报修取消成功: repairId={}, userId={}", repairId, userId);
    }

    @Override
    public List<com.fitness.modules.equipment.model.entity.EquipmentType> getAllEquipmentTypes() {
        return equipmentTypeMapper.selectAllActiveTypes();
    }
}

package com.fitness.modules.equipment.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.modules.equipment.model.dto.EquipmentDTO;
import com.fitness.modules.equipment.model.dto.EquipmentQueryDTO;
import com.fitness.modules.equipment.model.dto.RepairDTO;
import com.fitness.modules.equipment.model.dto.RepairHandleDTO;
import com.fitness.modules.equipment.model.entity.EquipmentType;
import com.fitness.modules.equipment.model.vo.EquipmentVO;
import com.fitness.modules.equipment.model.vo.MyRepairVO;
import com.fitness.modules.equipment.model.vo.RepairRecordVO;
import com.fitness.modules.equipment.model.vo.RepairVO;

import java.util.List;

/**
 * 器材服务接口
 */
public interface EquipmentService {

    /**
     * 创建器材
     *
     * @param dto 器材信息
     * @return 创建的器材ID
     */
    Long createEquipment(EquipmentDTO dto);

    /**
     * 更新器材
     *
     * @param id  器材ID
     * @param dto 器材信息
     */
    void updateEquipment(Long id, EquipmentDTO dto);

    /**
     * 删除器材（逻辑删除）
     *
     * @param id 器材ID
     */
    void deleteEquipment(Long id);

    /**
     * 获取器材详情
     *
     * @param id 器材ID
     * @return 器材详情
     */
    EquipmentVO getEquipmentById(Long id);

    /**
     * 分页查询器材列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    Page<EquipmentVO> getEquipmentList(EquipmentQueryDTO query);

    /**
     * 获取器材报修记录
     *
     * @param equipmentId 器材ID
     * @return 报修记录列表
     */
    List<RepairVO> getRepairList(Long equipmentId);

    /**
     * 获取所有报修记录
     *
     * @return 报修记录列表
     */
    List<RepairVO> getAllRepairs();

    /**
     * 处理报修
     *
     * @param repairId 报修ID
     * @param dto      处理信息
     * @param handlerId 处理人ID
     */
    void handleRepair(Long repairId, RepairHandleDTO dto, Long handlerId);

    /**
     * 提交报修申请
     *
     * @param userId 用户ID
     * @param dto    报修信息
     * @return 报修记录ID
     */
    Long submitRepair(Long userId, RepairDTO dto);

    /**
     * 获取我的报修记录
     *
     * @param userId 用户ID
     * @return 报修记录列表
     */
    List<MyRepairVO> getMyRepairs(Long userId);

    /**
     * 获取报修详情（会员端）
     *
     * @param userId   用户ID
     * @param repairId 报修ID
     * @return 报修详情
     */
    RepairVO getRepairDetail(Long userId, Long repairId);

    /**
     * 获取报修详情（管理端）
     *
     * @param repairId 报修ID
     * @return 报修详情
     */
    RepairVO getRepairDetailAdmin(Long repairId);

    /**
     * 取消报修
     *
     * @param userId   用户ID
     * @param repairId 报修ID
     */
    void cancelRepair(Long userId, Long repairId);

    /**
     * 获取所有器材类型
     *
     * @return 器材类型列表
     */
    List<EquipmentType> getAllEquipmentTypes();

    /**
     * 添加处理记录
     *
     * @param repairId  报修ID
     * @param content   处理内容
     * @param handlerId 处理人ID
     */
    void addRepairRecord(Long repairId, String content, Long handlerId);

    /**
     * 获取报修处理记录
     *
     * @param repairId 报修ID
     * @return 处理记录列表
     */
    List<RepairRecordVO> getRepairRecords(Long repairId);

    /**
     * 删除报修记录
     *
     * @param repairId 报修ID
     */
    void deleteRepair(Long repairId);
}

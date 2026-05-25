package com.fitness.modules.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitness.common.cache.RedisCacheNames;
import com.fitness.common.cache.RedisTemplateCacheSupport;
import com.fitness.common.constants.ErrorCode;
import com.fitness.common.exception.BusinessException;
import com.fitness.modules.system.mapper.SysDictItemMapper;
import com.fitness.modules.system.mapper.SysDictMapper;
import com.fitness.modules.system.model.dto.SysDictDTO;
import com.fitness.modules.system.model.dto.SysDictItemDTO;
import com.fitness.modules.system.model.entity.SysDict;
import com.fitness.modules.system.model.entity.SysDictItem;
import com.fitness.modules.system.model.vo.SysDictItemVO;
import com.fitness.modules.system.model.vo.SysDictVO;
import com.fitness.modules.system.service.SysDictService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysDictServiceImpl extends ServiceImpl<SysDictMapper, SysDict> implements SysDictService {

    private final SysDictItemMapper itemMapper;
    private final RedisTemplateCacheSupport redisTemplateCacheSupport;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysDictVO createDict(SysDictDTO dto) {
        log.debug("创建数据字典: dictCode={}", dto.getDictCode());

        LambdaQueryWrapper<SysDict> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDict::getDictCode, dto.getDictCode());
        if (count(wrapper) > 0) {
            throw new BusinessException(ErrorCode.DATA_ALREADY_EXISTS, "字典编码已存在");
        }

        SysDict dict = new SysDict();
        BeanUtil.copyProperties(dto, dict);
        if (dict.getStatus() == null) {
            dict.setStatus("ACTIVE");
        }
        save(dict);

        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            saveItems(dict.getId(), dto.getItems());
        }

        redisTemplateCacheSupport.evictAll(RedisCacheNames.DICT_OPTIONS);
        return getDictById(dict.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysDictVO updateDict(Long id, SysDictDTO dto) {
        log.debug("更新数据字典: id={}", id);

        SysDict existing = getById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "字典不存在");
        }

        BeanUtil.copyProperties(dto, existing, "id", "createdAt");
        updateById(existing);

        if (dto.getItems() != null) {
            LambdaQueryWrapper<SysDictItem> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(SysDictItem::getDictId, id);
            itemMapper.delete(deleteWrapper);
            saveItems(id, dto.getItems());
        }

        redisTemplateCacheSupport.evictAll(RedisCacheNames.DICT_OPTIONS);
        return getDictById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDict(Long id) {
        log.debug("删除数据字典: id={}", id);

        LambdaQueryWrapper<SysDictItem> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(SysDictItem::getDictId, id);
        itemMapper.delete(deleteWrapper);

        removeById(id);
        redisTemplateCacheSupport.evictAll(RedisCacheNames.DICT_OPTIONS);
    }

    @Override
    public SysDictVO getDictById(Long id) {
        SysDict dict = getById(id);
        if (dict == null) {
            return null;
        }
        return convertToVO(dict);
    }

    @Override
    public SysDictVO getDictByCode(String dictCode) {
        LambdaQueryWrapper<SysDict> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDict::getDictCode, dictCode);
        SysDict dict = getOne(wrapper);
        if (dict == null) {
            return null;
        }
        return convertToVO(dict);
    }

    @Override
    public List<SysDictVO> listAllDicts() {
        LambdaQueryWrapper<SysDict> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SysDict::getSortOrder);
        List<SysDict> dicts = list(wrapper);
        return dicts.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getOptionsByCode(String dictCode) {
        return redisTemplateCacheSupport.getOrLoad(RedisCacheNames.DICT_OPTIONS, dictCode, () -> {
            LambdaQueryWrapper<SysDict> dictWrapper = new LambdaQueryWrapper<>();
            dictWrapper.eq(SysDict::getDictCode, dictCode).eq(SysDict::getStatus, "ACTIVE");
            SysDict dict = getOne(dictWrapper);
            if (dict == null) {
                log.warn("字典不存在: dictCode={}", dictCode);
                return Collections.emptyList();
            }

            LambdaQueryWrapper<SysDictItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.eq(SysDictItem::getDictId, dict.getId())
                    .eq(SysDictItem::getStatus, "ACTIVE")
                    .orderByAsc(SysDictItem::getSortOrder);
            List<SysDictItem> items = itemMapper.selectList(itemWrapper);

            List<Map<String, Object>> options = items.stream().map(item -> {
                Map<String, Object> map = new HashMap<>();
                map.put("label", item.getLabel());
                map.put("value", item.getValue());
                map.put("id", item.getId());
                return map;
            }).collect(Collectors.toList());
            log.debug("[DB LOAD] dict options, dictCode={}, count={}", dictCode, options.size());
            return options;
        });
    }

    private void saveItems(Long dictId, List<SysDictItemDTO> items) {
        for (SysDictItemDTO itemDto : items) {
            SysDictItem item = new SysDictItem();
            item.setDictId(dictId);
            item.setLabel(itemDto.getLabel());
            item.setValue(itemDto.getValue());
            item.setDescription(itemDto.getDescription());
            item.setStatus(itemDto.getStatus() != null ? itemDto.getStatus() : "ACTIVE");
            item.setSortOrder(itemDto.getSortOrder() != null ? itemDto.getSortOrder() : 0);
            itemMapper.insert(item);
        }
    }

    private SysDictVO convertToVO(SysDict dict) {
        SysDictVO vo = new SysDictVO();
        BeanUtil.copyProperties(dict, vo);

        LambdaQueryWrapper<SysDictItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(SysDictItem::getDictId, dict.getId())
                .orderByAsc(SysDictItem::getSortOrder);
        List<SysDictItem> items = itemMapper.selectList(itemWrapper);

        List<SysDictItemVO> itemVOs = items.stream().map(item -> {
            SysDictItemVO itemVO = new SysDictItemVO();
            BeanUtil.copyProperties(item, itemVO);
            return itemVO;
        }).collect(Collectors.toList());

        vo.setItems(itemVOs);
        return vo;
    }
}

package com.fitness.modules.product.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitness.common.cache.RedisCacheNames;
import com.fitness.common.cache.RedisTemplateCacheSupport;
import com.fitness.common.exception.BusinessException;
import com.fitness.common.constants.ErrorCode;
import com.fitness.modules.product.mapper.ProductMapper;
import com.fitness.modules.product.model.dto.ProductDTO;
import com.fitness.modules.product.model.dto.StockUpdateDTO;
import com.fitness.modules.product.model.entity.Product;
import com.fitness.modules.product.model.vo.ProductVO;
import com.fitness.modules.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private final RedisTemplateCacheSupport redisTemplateCacheSupport;

    @Override
    public List<ProductVO> getProductList(String category) {
        String normalizedCategory = category == null ? "all" : category;
        return redisTemplateCacheSupport.getOrLoad(RedisCacheNames.PRODUCT_PUBLIC_LIST, normalizedCategory, () -> {
            List<Product> products;
            if ("all".equals(normalizedCategory)) {
                products = lambdaQuery()
                        .eq(Product::getStatus, "ACTIVE")
                        .orderByDesc(Product::getSortOrder)
                        .list();
            } else {
                products = lambdaQuery()
                        .eq(Product::getStatus, "ACTIVE")
                        .eq(Product::getCategory, normalizedCategory)
                        .orderByDesc(Product::getSortOrder)
                        .list();
            }
            List<ProductVO> productVOs = products.stream().map(this::convertToVO).collect(Collectors.toList());
            log.debug("[DB LOAD] public product list, category={}, count={}", normalizedCategory, productVOs.size());
            return productVOs;
        });
    }

    @Override
    public List<ProductVO> getAllProducts(String category, String status, String keyword) {
        List<Product> products;
        var query = lambdaQuery();

        if (category != null && !category.isEmpty() && !"all".equals(category)) {
            query.eq(Product::getCategory, category);
        }
        if (status != null && !status.isEmpty()) {
            query.eq(Product::getStatus, status);
        }
        if (keyword != null && !keyword.isEmpty()) {
            query.and(q -> q.like(Product::getName, keyword)
                    .or()
                    .like(Product::getCode, keyword));
        }

        products = query.orderByDesc(Product::getSortOrder).list();
        List<ProductVO> productVOs = products.stream().map(this::convertToVO).collect(Collectors.toList());
        log.debug("[DB QUERY] admin product list, category={}, status={}, keyword={}, count={}",
                category, status, keyword, productVOs.size());
        return productVOs;
    }

    @Override
    public ProductVO getProductDetail(Long id) {
        Product product = getById(id);
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return convertToVO(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductVO createProduct(ProductDTO dto) {
        Product product = new Product();
        BeanUtil.copyProperties(dto, product);
        save(product);
        redisTemplateCacheSupport.evictAll(RedisCacheNames.PRODUCT_PUBLIC_LIST);
        return convertToVO(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductVO updateProduct(ProductDTO dto) {
        Product product = getById(dto.getId());
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        BeanUtil.copyProperties(dto, product);
        updateById(product);
        redisTemplateCacheSupport.evictAll(RedisCacheNames.PRODUCT_PUBLIC_LIST);
        return convertToVO(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProduct(Long id) {
        removeById(id);
        redisTemplateCacheSupport.evictAll(RedisCacheNames.PRODUCT_PUBLIC_LIST);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProductStatus(Long id, String status) {
        Product product = getById(id);
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        product.setStatus(status);
        updateById(product);
        redisTemplateCacheSupport.evictAll(RedisCacheNames.PRODUCT_PUBLIC_LIST);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductVO updateStock(Long id, StockUpdateDTO dto) {
        Product product = getById(id);
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        int currentStock = product.getStock() != null ? product.getStock() : 0;
        if ("IN".equals(dto.getType())) {
            product.setStock(currentStock + dto.getQuantity());
        } else if ("OUT".equals(dto.getType())) {
            if (currentStock < dto.getQuantity()) {
                throw new BusinessException(ErrorCode.PRODUCT_STOCK_INSUFFICIENT);
            }
            product.setStock(currentStock - dto.getQuantity());
        } else {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        updateById(product);
        redisTemplateCacheSupport.evictAll(RedisCacheNames.PRODUCT_PUBLIC_LIST);
        return convertToVO(product);
    }

    private ProductVO convertToVO(Product product) {
        ProductVO vo = new ProductVO();
        BeanUtil.copyProperties(product, vo);
        vo.setCategoryLabel(getCategoryLabel(product.getCategory()));
        return vo;
    }

    private String getCategoryLabel(String category) {
        return switch (category) {
            case "EQUIPMENT" -> "运动装备";
            case "SUPPLEMENT" -> "营养补剂";
            case "COURSE" -> "课程优惠";
            case "OTHER" -> "其他";
            default -> category;
        };
    }
}

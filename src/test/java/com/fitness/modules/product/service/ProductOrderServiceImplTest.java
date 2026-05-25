package com.fitness.modules.product.service;

import com.fitness.common.constants.ErrorCode;
import com.fitness.common.exception.BusinessException;
import com.fitness.modules.product.mapper.ProductMapper;
import com.fitness.modules.product.mapper.ProductOrderMapper;
import com.fitness.modules.product.model.entity.ProductOrder;
import com.fitness.modules.product.service.impl.ProductOrderServiceImpl;
import com.fitness.modules.ranking.service.RedisRankingService;
import com.fitness.modules.user.mapper.UserFitnessProfileMapper;
import com.fitness.modules.user.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductOrderServiceImplTest {

    @Mock
    private ProductOrderMapper productOrderMapper;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserFitnessProfileMapper userFitnessProfileMapper;

    @Mock
    private RedisRankingService redisRankingService;

    @InjectMocks
    private ProductOrderServiceImpl productOrderService;

    @Test
    void payOrderUpdatesProductRankingOnSuccess() {
        ReflectionTestUtils.setField(productOrderService, "baseMapper", productOrderMapper);

        ProductOrder order = new ProductOrder();
        order.setOrderNo("PO202605190001");
        order.setProductId(7L);
        order.setQuantity(3);
        order.setStatus("PENDING");

        when(productOrderMapper.selectByOrderNo("PO202605190001")).thenReturn(order);

        productOrderService.payOrder("PO202605190001", "ALIPAY");

        verify(redisRankingService).incrementProductSalesScore(7L, 3D);
    }

    @Test
    void payOrderRejectsMissingOrder() {
        ReflectionTestUtils.setField(productOrderService, "baseMapper", productOrderMapper);

        when(productOrderMapper.selectByOrderNo("missing")).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> productOrderService.payOrder("missing", "ALIPAY"));

        assertEquals(ErrorCode.ORDER_NOT_FOUND.getCode(), exception.getCode());
    }
}

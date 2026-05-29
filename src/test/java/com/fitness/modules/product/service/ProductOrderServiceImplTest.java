package com.fitness.modules.product.service;

import com.fitness.common.constants.ErrorCode;
import com.fitness.common.exception.BusinessException;
import com.fitness.modules.order.mapper.OrderMapper;
import com.fitness.modules.order.mapper.ProductOrderExtMapper;
import com.fitness.modules.order.model.dto.OrderDTO;
import com.fitness.modules.order.model.entity.Order;
import com.fitness.modules.order.model.entity.ProductOrderExt;
import com.fitness.modules.order.model.vo.OrderVO;
import com.fitness.modules.order.service.OrderService;
import com.fitness.modules.product.mapper.ProductMapper;
import com.fitness.modules.product.mapper.ProductOrderMapper;
import com.fitness.modules.product.model.dto.ProductOrderDTO;
import com.fitness.modules.product.model.entity.Product;
import com.fitness.modules.product.model.entity.ProductOrder;
import com.fitness.modules.product.model.vo.ProductOrderVO;
import com.fitness.modules.product.service.impl.ProductOrderServiceImpl;
import com.fitness.modules.user.mapper.UserMapper;
import com.fitness.modules.user.model.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductOrderServiceImplTest {

    @Mock
    private ProductOrderMapper productOrderMapper;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private ProductOrderExtMapper productOrderExtMapper;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private ProductOrderServiceImpl productOrderService;

    @Test
    void createOrderDelegatesToOrderService() {
        ReflectionTestUtils.setField(productOrderService, "baseMapper", productOrderMapper);

        Product product = new Product();
        product.setId(1L);
        product.setStatus("ACTIVE");
        product.setOriginalPrice(new BigDecimal("99.00"));

        when(productMapper.selectById(1L)).thenReturn(product);

        OrderVO orderVO = new OrderVO();
        orderVO.setId(100L);
        orderVO.setOrderNo("ORD20260528000001");
        orderVO.setOrderType("PRODUCT");
        orderVO.setOriginalAmount(new BigDecimal("198.00"));
        orderVO.setPayAmount(new BigDecimal("198.00"));
        orderVO.setStatus("PENDING");
        orderVO.setCreateTime(java.time.LocalDateTime.now());

        ProductOrderExt ext = new ProductOrderExt();
        ext.setProductId(1L);
        ext.setProductName("Test Product");
        ext.setQuantity(2);
        orderVO.setProductExt(ext);

        when(orderService.createOrder(any(OrderDTO.class), eq(1L))).thenReturn(orderVO);

        ProductOrderDTO dto = new ProductOrderDTO();
        dto.setProductId(1L);
        dto.setQuantity(2);

        ProductOrderVO result = productOrderService.createOrder(dto, 1L);

        assertNotNull(result);
        assertEquals("ORD20260528000001", result.getOrderNo());
        assertEquals(1L, result.getProductId());
        assertEquals(2, result.getQuantity());
    }

    @Test
    void createOrderRejectsNotFoundProduct() {
        ReflectionTestUtils.setField(productOrderService, "baseMapper", productOrderMapper);

        when(productMapper.selectById(999L)).thenReturn(null);

        ProductOrderDTO dto = new ProductOrderDTO();
        dto.setProductId(999L);
        dto.setQuantity(1);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> productOrderService.createOrder(dto, 1L));

        assertEquals(ErrorCode.PRODUCT_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void createOrderRejectsInactiveProduct() {
        ReflectionTestUtils.setField(productOrderService, "baseMapper", productOrderMapper);

        Product product = new Product();
        product.setId(1L);
        product.setStatus("INACTIVE");

        when(productMapper.selectById(1L)).thenReturn(product);

        ProductOrderDTO dto = new ProductOrderDTO();
        dto.setProductId(1L);
        dto.setQuantity(1);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> productOrderService.createOrder(dto, 1L));

        assertEquals(ErrorCode.PRODUCT_NOT_AVAILABLE.getCode(), exception.getCode());
    }

    @Test
    void getOrderDetailDelegatesToOrderService() {
        OrderVO orderVO = new OrderVO();
        orderVO.setId(100L);
        orderVO.setOrderNo("ORD20260528000001");
        orderVO.setOrderType("PRODUCT");
        orderVO.setStatus("PAID");
        orderVO.setPayAmount(new BigDecimal("99.00"));

        ProductOrderExt ext = new ProductOrderExt();
        ext.setProductId(1L);
        ext.setProductName("Test");
        ext.setQuantity(1);
        ext.setPickupCode("ABC123");
        ext.setPickupStatus("NOT_PICKED");
        orderVO.setProductExt(ext);

        when(orderService.getOrderDetail("ORD20260528000001")).thenReturn(orderVO);

        ProductOrderVO result = productOrderService.getOrderDetail("ORD20260528000001");

        assertNotNull(result);
        assertEquals("ORD20260528000001", result.getOrderNo());
        assertEquals(1L, result.getProductId());
        assertEquals("ABC123", result.getPickupCode());
        assertEquals("NOT_PICKED", result.getPickupStatus());
    }

@Test
    void getUserOrdersFiltersProductTypeOnly() {
        OrderVO productOrder = new OrderVO();
        productOrder.setId(1L);
        productOrder.setOrderNo("ORD001");
        productOrder.setOrderType("PRODUCT");
        productOrder.setStatus("PAID");
        ProductOrderExt ext1 = new ProductOrderExt();
        ext1.setProductId(1L);
        ext1.setQuantity(1);
        productOrder.setProductExt(ext1);

        OrderVO membershipOrder = new OrderVO();
        membershipOrder.setId(2L);
        membershipOrder.setOrderNo("ORD002");
        membershipOrder.setOrderType("MEMBERSHIP");
        membershipOrder.setStatus("PAID");

        when(orderService.getUserOrders(1L)).thenReturn(List.of(productOrder, membershipOrder));

        List<ProductOrderVO> results = productOrderService.getUserOrders(1L);

        assertEquals(1, results.size());
        assertEquals("ORD001", results.get(0).getOrderNo());
    }

    @Test
    void cancelOrderDelegatesToOrderService() {
        productOrderService.cancelOrder("ORD001", 1L);

        verify(orderService).cancelOrder("ORD001", 1L);
    }

    @Test
    void shipOrderUpdatesStatus() {
        ReflectionTestUtils.setField(productOrderService, "baseMapper", productOrderMapper);

        Order order = new Order();
        order.setId(1L);
        order.setOrderNo("ORD001");
        order.setOrderType("PRODUCT");
        order.setStatus("NOT_PICKED");

        ProductOrderExt ext = new ProductOrderExt();
        ext.setId(10L);
        ext.setOrderId(1L);

        when(orderMapper.selectByOrderNo("ORD001")).thenReturn(order);
        when(productOrderExtMapper.selectByOrderId(1L)).thenReturn(ext);

        productOrderService.shipOrder("ORD001", "SF123456", "顺丰");

        assertEquals("SHIPPED", order.getStatus());
        assertEquals("SF123456", ext.getTrackingNo());
        assertEquals("顺丰", ext.getCarrier());
    }

    @Test
    void shipOrderRejectsNonPaidOrder() {
        ReflectionTestUtils.setField(productOrderService, "baseMapper", productOrderMapper);

        Order order = new Order();
        order.setId(1L);
        order.setOrderNo("ORD001");
        order.setOrderType("PRODUCT");
        order.setStatus("PENDING");

        when(orderMapper.selectByOrderNo("ORD001")).thenReturn(order);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> productOrderService.shipOrder("ORD001", "SF123456", "顺丰"));

        assertEquals(ErrorCode.ORDER_STATUS_ERROR.getCode(), exception.getCode());
    }

    @Test
    void completeOrderUpdatesStatus() {
        ReflectionTestUtils.setField(productOrderService, "baseMapper", productOrderMapper);

        Order order = new Order();
        order.setId(1L);
        order.setOrderNo("ORD001");
        order.setOrderType("PRODUCT");
        order.setStatus("SHIPPED");

        when(orderMapper.selectByOrderNo("ORD001")).thenReturn(order);

        productOrderService.completeOrder("ORD001");

        assertEquals("COMPLETED", order.getStatus());
    }

    @Test
    void confirmPickupUpdatesStatus() {
        ReflectionTestUtils.setField(productOrderService, "baseMapper", productOrderMapper);

        Order order = new Order();
        order.setId(1L);
        order.setOrderNo("ORD001");
        order.setOrderType("PRODUCT");
        order.setStatus("NOT_PICKED");

        ProductOrderExt ext = new ProductOrderExt();
        ext.setId(10L);
        ext.setOrderId(1L);
        ext.setPickupCode("ABC123");
        ext.setPickupStatus("NOT_PICKED");

        when(orderMapper.selectByOrderNo("ORD001")).thenReturn(order);
        when(productOrderExtMapper.selectByOrderId(1L)).thenReturn(ext);

        productOrderService.confirmPickup("ORD001", "ABC123");

        assertEquals("COMPLETED", order.getStatus());
        assertEquals("PICKED", ext.getPickupStatus());
        assertNotNull(ext.getPickupTime());
    }

    @Test
    void confirmPickupRejectsNonPaidOrder() {
        ReflectionTestUtils.setField(productOrderService, "baseMapper", productOrderMapper);

        Order order = new Order();
        order.setId(1L);
        order.setOrderNo("ORD001");
        order.setOrderType("PRODUCT");
        order.setStatus("PENDING");

        when(orderMapper.selectByOrderNo("ORD001")).thenReturn(order);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> productOrderService.confirmPickup("ORD001", "ABC123"));

        assertEquals(ErrorCode.ORDER_NOT_PAID.getCode(), exception.getCode());
    }

    @Test
    void confirmPickupRejectsMismatchedPickupCode() {
        ReflectionTestUtils.setField(productOrderService, "baseMapper", productOrderMapper);

        Order order = new Order();
        order.setId(1L);
        order.setOrderNo("ORD001");
        order.setOrderType("PRODUCT");
        order.setStatus("NOT_PICKED");

        ProductOrderExt ext = new ProductOrderExt();
        ext.setId(10L);
        ext.setOrderId(1L);
        ext.setPickupCode("ABC123");
        ext.setPickupStatus("NOT_PICKED");

        when(orderMapper.selectByOrderNo("ORD001")).thenReturn(order);
        when(productOrderExtMapper.selectByOrderId(1L)).thenReturn(ext);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> productOrderService.confirmPickup("ORD001", "WRONG1"));

        assertEquals(ErrorCode.PARAM_ERROR.getCode(), exception.getCode());
    }
}

package com.fitness.modules.order.service;

import com.fitness.common.constants.ErrorCode;
import com.fitness.common.exception.BusinessException;
import com.fitness.integration.payment.service.AlipayService;
import com.fitness.modules.coach.mapper.CoachPackageMapper;
import com.fitness.modules.membership.mapper.MembershipCardMapper;
import com.fitness.modules.order.mapper.CoachPackageOrderExtMapper;
import com.fitness.modules.order.mapper.MembershipOrderExtMapper;
import com.fitness.modules.order.mapper.OrderMapper;
import com.fitness.modules.order.mapper.ProductOrderExtMapper;
import com.fitness.modules.order.model.entity.CoachPackageOrderExt;
import com.fitness.modules.order.model.entity.Order;
import com.fitness.modules.order.model.entity.ProductOrderExt;
import com.fitness.modules.order.model.vo.OrderVO;
import com.fitness.modules.order.service.impl.OrderServiceImpl;
import com.fitness.modules.product.mapper.ProductMapper;
import com.fitness.modules.user.mapper.UserMapper;
import com.fitness.modules.user.service.CoachStudentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private ProductOrderExtMapper productOrderExtMapper;

    @Mock
    private CoachPackageOrderExtMapper coachPackageOrderExtMapper;

    @Mock
    private MembershipOrderExtMapper membershipOrderExtMapper;

    @Mock
    private AlipayService alipayService;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private CoachPackageMapper coachPackageMapper;

    @Mock
    private MembershipCardMapper membershipCardMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private CoachStudentService coachStudentService;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void productCallbackMarksOrderNotPickedAndGeneratesPickupCode() {
        ReflectionTestUtils.setField(orderService, "baseMapper", orderMapper);
        Order order = pendingProductOrder();
        ProductOrderExt ext = productExt();

        when(alipayService.verifyNotify(any())).thenReturn(true);
        when(orderMapper.selectByOrderNo("ORD001")).thenReturn(order);
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);
        when(productOrderExtMapper.selectByOrderId(1L)).thenReturn(ext);
        when(productOrderExtMapper.updateById(any(ProductOrderExt.class))).thenReturn(1);
        when(productMapper.decreaseStock(10L, 2)).thenReturn(1);

        orderService.handleAlipayCallback(successCallback());

        assertEquals("NOT_PICKED", order.getStatus());
        assertEquals("NOT_PICKED", ext.getPickupStatus());
        assertNotNull(ext.getPickupCode());
    }

    @Test
    void productCallbackFailsWhenStockCannotBeDecreased() {
        ReflectionTestUtils.setField(orderService, "baseMapper", orderMapper);
        Order order = pendingProductOrder();
        ProductOrderExt ext = productExt();

        when(alipayService.verifyNotify(any())).thenReturn(true);
        when(orderMapper.selectByOrderNo("ORD001")).thenReturn(order);
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);
        when(productOrderExtMapper.selectByOrderId(1L)).thenReturn(ext);
        when(productMapper.decreaseStock(10L, 2)).thenReturn(0);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> orderService.handleAlipayCallback(successCallback()));

        assertEquals(ErrorCode.PRODUCT_STOCK_INSUFFICIENT.getCode(), exception.getCode());
    }

    @Test
    void payOrderRejectsOtherUsersOrder() {
        Order order = pendingProductOrder();
        when(orderMapper.selectByOrderNo("ORD001")).thenReturn(order);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> orderService.payOrder("ORD001", "ALIPAY", 999L));

        assertEquals(ErrorCode.FORBIDDEN.getCode(), exception.getCode());
    }

    @Test
    void getOrderDetailRejectsOtherUsersOrder() {
        Order order = pendingProductOrder();
        when(orderMapper.selectByOrderNo("ORD001")).thenReturn(order);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> orderService.getOrderDetail("ORD001", 999L));

        assertEquals(ErrorCode.FORBIDDEN.getCode(), exception.getCode());
    }

    @Test
    void getUserOrdersSyncsPaidProductOrderWhenNotifyWasMissed() {
        ReflectionTestUtils.setField(orderService, "baseMapper", orderMapper);
        Order order = pendingProductOrder();
        ProductOrderExt ext = productExt();

        when(orderMapper.selectByUserId(100L)).thenReturn(List.of(order));
        when(alipayService.queryOrderStatus("ORD001")).thenReturn("TRADE_SUCCESS");
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);
        when(productOrderExtMapper.selectByOrderId(1L)).thenReturn(ext);
        when(productOrderExtMapper.updateById(any(ProductOrderExt.class))).thenReturn(1);
        when(productMapper.decreaseStock(10L, 2)).thenReturn(1);

        List<OrderVO> results = orderService.getUserOrders(100L);

        assertEquals(1, results.size());
        assertEquals("NOT_PICKED", results.get(0).getStatus());
        assertEquals("NOT_PICKED", ext.getPickupStatus());
        assertNotNull(ext.getPickupCode());
    }

    @Test
    void getUserOrdersSyncsPaidCoachPackageOrderWhenNotifyWasMissed() {
        ReflectionTestUtils.setField(orderService, "baseMapper", orderMapper);
        Order order = pendingCoachPackageOrder();
        CoachPackageOrderExt ext = coachPackageExt();

        when(orderMapper.selectByUserId(100L)).thenReturn(List.of(order));
        when(alipayService.queryOrderStatus("ORD002")).thenReturn("TRADE_FINISHED");
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);
        when(coachPackageOrderExtMapper.selectByOrderId(2L)).thenReturn(ext);

        List<OrderVO> results = orderService.getUserOrders(100L);

        assertEquals(1, results.size());
        assertEquals("PAID", results.get(0).getStatus());
        verify(coachStudentService).bindStudent(eq(100L), eq(200L), eq(20L), eq(null), eq(null));
    }

    private Order pendingProductOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setOrderNo("ORD001");
        order.setUserId(100L);
        order.setOrderType("PRODUCT");
        order.setPayAmount(new BigDecimal("20.00"));
        order.setStatus("PENDING");
        return order;
    }

    private Order pendingCoachPackageOrder() {
        Order order = new Order();
        order.setId(2L);
        order.setOrderNo("ORD002");
        order.setUserId(100L);
        order.setOrderType("COACH_PACKAGE");
        order.setPayAmount(new BigDecimal("200.00"));
        order.setStatus("PENDING");
        return order;
    }

    private ProductOrderExt productExt() {
        ProductOrderExt ext = new ProductOrderExt();
        ext.setId(11L);
        ext.setOrderId(1L);
        ext.setProductId(10L);
        ext.setProductName("蛋白粉");
        ext.setQuantity(2);
        return ext;
    }

    private CoachPackageOrderExt coachPackageExt() {
        CoachPackageOrderExt ext = new CoachPackageOrderExt();
        ext.setId(22L);
        ext.setOrderId(2L);
        ext.setCoachPackageId(20L);
        ext.setCoachId(200L);
        ext.setPackageName("Private Coach 10");
        return ext;
    }

    private Map<String, String> successCallback() {
        return Map.of(
                "out_trade_no", "ORD001",
                "trade_no", "202605290001",
                "trade_status", "TRADE_SUCCESS",
                "total_amount", "20.00"
        );
    }
}

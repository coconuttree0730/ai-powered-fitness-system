# Backend Foundation Phase 1 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Refactor the backend foundation layers (`common`, `config`, `integration`, `security`) to remove duplicated JWT logic, standardize external service integration structure, and preserve current external behavior.

**Architecture:** Keep all controller contracts and database schema unchanged while refactoring internal boundaries. Consolidate JWT into `JwtTokenProvider`, decompose `SecurityConfig` by concern, and introduce explicit integration adapters/support classes so SDK details and exception translation stop leaking into application code.

**Tech Stack:** Java 17, Spring Boot 3.5, Spring Security 6, Spring AI, MyBatis-Plus, MinIO SDK, Alipay SDK, Aliyun Dypnsapi SDK, JUnit 5, Mockito

---

### Task 1: Consolidate JWT And Decompose Security Configuration

**Files:**
- Create: `src/test/java/com/fitness/integration/security/JwtTokenProviderTest.java`
- Create: `src/test/java/com/fitness/modules/user/service/UserServiceImplAuthTest.java`
- Create: `src/main/java/com/fitness/config/security/SecurityWhitelist.java`
- Create: `src/main/java/com/fitness/config/security/CorsConfig.java`
- Create: `src/main/java/com/fitness/config/security/AuthenticationBeansConfig.java`
- Create: `src/main/java/com/fitness/config/security/MethodSecurityConfig.java`
- Create: `src/main/java/com/fitness/config/security/SecurityContextStrategyConfig.java`
- Modify: `src/main/java/com/fitness/integration/security/JwtTokenProvider.java`
- Modify: `src/main/java/com/fitness/modules/user/service/impl/UserServiceImpl.java`
- Modify: `src/main/java/com/fitness/config/SecurityConfig.java`
- Delete: `src/main/java/com/fitness/common/utils/JwtUtils.java`

- [ ] **Step 1: Write the failing JWT and login regression tests**

```java
package com.fitness.integration.security;

import com.fitness.common.config.JwtProperties;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtTokenProviderTest {

    @Test
    void generateTokenShouldPreserveLegacyClaims() {
        JwtProperties properties = mock(JwtProperties.class);
        when(properties.getSecret()).thenReturn("01234567890123456789012345678901");
        when(properties.getExpiration()).thenReturn(3600000L);
        when(properties.getIssuer()).thenReturn("fitness");
        when(properties.getAudience()).thenReturn("fitness-web");

        JwtTokenProvider provider = new JwtTokenProvider(properties);

        String token = provider.generateToken(1L, "coach001", List.of("COACH"));
        Claims claims = provider.parseToken(token);

        assertEquals("coach001", claims.getSubject());
        assertEquals(1L, ((Number) claims.get("userId")).longValue());
        assertEquals(List.of("COACH"), claims.get("roles"));
        assertTrue(provider.validateToken(token));
    }
}
```

```java
package com.fitness.modules.user.service;

import com.fitness.integration.minio.service.FileService;
import com.fitness.integration.security.JwtTokenProvider;
import com.fitness.modules.user.mapper.RoleMapper;
import com.fitness.modules.user.mapper.UserMapper;
import com.fitness.modules.user.model.dto.LoginDTO;
import com.fitness.modules.user.model.entity.Role;
import com.fitness.modules.user.model.entity.User;
import com.fitness.modules.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplAuthTest {

    @Mock private UserMapper userMapper;
    @Mock private RoleMapper roleMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private FileService fileService;
    @Mock private SmsCodeService smsCodeService;
    @Mock private EmailCodeService emailCodeService;
    @Mock private CoachDetailService coachDetailService;

    @InjectMocks
    private UserServiceImpl service;

    @Test
    void loginShouldReturnTokenFromJwtTokenProvider() {
        User user = new User();
        user.setId(1L);
        user.setUsername("member001");
        user.setPassword("encoded");
        user.setStatus(1);

        Role role = new Role();
        role.setRoleCode("MEMBER");

        LoginDTO dto = new LoginDTO();
        dto.setUsername("member001");
        dto.setPassword("123456");

        when(userMapper.selectByUsername("member001")).thenReturn(user);
        when(passwordEncoder.matches("123456", "encoded")).thenReturn(true);
        when(userMapper.selectRolesByUserId(1L)).thenReturn(List.of(role));
        when(jwtTokenProvider.generateToken(1L, "member001", List.of("MEMBER"))).thenReturn("jwt-token");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(3600000L);

        Map<String, Object> result = service.login(dto);

        assertEquals("jwt-token", result.get("token"));
        assertEquals(3600000L, result.get("expiresIn"));
    }
}
```

- [ ] **Step 2: Run tests to verify they fail against the current structure**

Run:

```bash
mvn -q -Dtest=JwtTokenProviderTest,UserServiceImplAuthTest test
```

Expected:

- `UserServiceImplAuthTest` fails because `UserServiceImpl` still injects `JwtUtils`
- compilation or assertion failures show the migration is not implemented yet

- [ ] **Step 3: Write the minimal JWT and security refactor**

Update `JwtTokenProvider` so it becomes the single JWT entry point:

```java
public String generateToken(Long userId, String username, List<String> roles) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + jwtProperties.getExpiration());

    Map<String, Object> claims = new HashMap<>();
    claims.put(CLAIM_USER_ID, userId);
    claims.put(CLAIM_USERNAME, username);
    claims.put(CLAIM_ROLES, roles);

    return Jwts.builder()
            .claims(claims)
            .subject(username)
            .issuedAt(now)
            .expiration(expiryDate)
            .issuer(jwtProperties.getIssuer())
            .audience().add(jwtProperties.getAudience()).and()
            .signWith(getSigningKey())
            .compact();
}

public long getExpirationTime() {
    return jwtProperties.getExpiration();
}

public long getRefreshExpirationTime() {
    return jwtProperties.getRefreshExpiration();
}
```

Update `UserServiceImpl` to depend on `JwtTokenProvider` instead of `JwtUtils`:

```java
private final JwtTokenProvider jwtTokenProvider;

List<String> roleCodes = roles.stream()
        .map(Role::getRoleCode)
        .collect(Collectors.toList());

String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), roleCodes);

Map<String, Object> result = new HashMap<>();
result.put("token", token);
result.put("tokenType", "Bearer");
result.put("expiresIn", jwtTokenProvider.getExpirationTime());
```

Create `SecurityWhitelist`:

```java
package com.fitness.config.security;

public final class SecurityWhitelist {

    private SecurityWhitelist() {
    }

    public static final String[] URLS = {
            "/api/v1/auth/login",
            "/api/v1/auth/login/sms",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh",
            "/api/v1/auth/slider-verify/**",
            "/api/v1/auth/sms-code",
            "/api/v1/courses/public/**",
            "/api/v1/banners/active",
            "/api/v1/announcements/published",
            "/api/v1/coaches/home",
            "/api/v1/coaches/*/detail",
            "/api/v1/equipment/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**",
            "/error",
            "/favicon.ico",
            "/api/v1/payment/alipay/notify"
    };
}
```

Create `CorsConfig`:

```java
package com.fitness.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

Create `AuthenticationBeansConfig`:

```java
package com.fitness.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class AuthenticationBeansConfig {

    private final UserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @SuppressWarnings("deprecation")
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(passwordEncoder());
        authProvider.setUserDetailsService(userDetailsService);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
```

Create `MethodSecurityConfig`:

```java
package com.fitness.config.security;

import com.fitness.integration.security.CustomPermissionEvaluator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;

@Configuration
public class MethodSecurityConfig {

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler(
            CustomPermissionEvaluator permissionEvaluator) {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setPermissionEvaluator(permissionEvaluator);
        return expressionHandler;
    }
}
```

Create `SecurityContextStrategyConfig`:

```java
package com.fitness.config.security;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
public class SecurityContextStrategyConfig {

    @PostConstruct
    public void init() {
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }
}
```

Shrink `SecurityConfig` to the filter chain only:

```java
package com.fitness.config;

import com.fitness.config.security.SecurityWhitelist;
import com.fitness.integration.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAuthorizationDeniedHandler authorizationDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {})
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .securityContext(securityContext -> securityContext.requireExplicitSave(false))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(authorizationDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(SecurityWhitelist.URLS).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/courses/**").permitAll()
                        .requestMatchers("/static/**", "/uploads/**").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

Delete `JwtUtils.java` after migration.

- [ ] **Step 4: Run the focused tests and compile**

Run:

```bash
mvn -q -Dtest=JwtTokenProviderTest,UserServiceImplAuthTest test
mvn -q -DskipTests compile
```

Expected:

- both tests PASS
- compile exits with code `0`

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/fitness/config/SecurityConfig.java src/main/java/com/fitness/config/security src/main/java/com/fitness/integration/security/JwtTokenProvider.java src/main/java/com/fitness/modules/user/service/impl/UserServiceImpl.java src/test/java/com/fitness/integration/security/JwtTokenProviderTest.java src/test/java/com/fitness/modules/user/service/UserServiceImplAuthTest.java
git rm src/main/java/com/fitness/common/utils/JwtUtils.java
git commit -m "refactor: consolidate jwt and split security config"
```

### Task 2: Standardize SMS And Payment Integration Boundaries

**Files:**
- Create: `src/test/java/com/fitness/integration/payment/service/AlipayServiceImplTest.java`
- Create: `src/test/java/com/fitness/integration/sms/service/AliyunSmsSenderTest.java`
- Create: `src/main/java/com/fitness/integration/common/exception/IntegrationException.java`
- Create: `src/main/java/com/fitness/integration/payment/exception/PaymentIntegrationException.java`
- Create: `src/main/java/com/fitness/integration/sms/exception/SmsIntegrationException.java`
- Create: `src/main/java/com/fitness/integration/payment/gateway/AlipayGateway.java`
- Create: `src/main/java/com/fitness/integration/payment/model/AlipayCreateOrderResult.java`
- Create: `src/main/java/com/fitness/integration/payment/model/AlipayQueryResult.java`
- Create: `src/main/java/com/fitness/integration/payment/model/AlipayRefundResult.java`
- Create: `src/main/java/com/fitness/integration/sms/service/SmsSender.java`
- Create: `src/main/java/com/fitness/integration/sms/service/AliyunSmsSender.java`
- Create: `src/main/java/com/fitness/integration/sms/model/SmsSendResult.java`
- Create: `src/main/java/com/fitness/integration/sms/model/SmsVerifyResult.java`
- Modify: `src/main/java/com/fitness/integration/payment/service/impl/AlipayServiceImpl.java`
- Modify: `src/main/java/com/fitness/modules/user/service/impl/SmsCodeServiceImpl.java`
- Delete: `src/main/java/com/fitness/integration/sms/service/AliyunSmsService.java`

- [ ] **Step 1: Write the failing regression tests**

```java
package com.fitness.integration.payment.service;

import com.fitness.integration.payment.gateway.AlipayGateway;
import com.fitness.integration.payment.model.AlipayCreateOrderResult;
import com.fitness.integration.payment.service.impl.AlipayServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlipayServiceImplTest {

    @Mock
    private AlipayGateway alipayGateway;

    @InjectMocks
    private AlipayServiceImpl service;

    @Test
    void createPayOrderShouldReturnGatewayBody() {
        when(alipayGateway.createPageOrder("P20260513001", new BigDecimal("99.00"), "会员卡", "年卡"))
                .thenReturn(new AlipayCreateOrderResult(true, "<form>pay</form>", null, null));

        String html = service.createPayOrder("P20260513001", new BigDecimal("99.00"), "会员卡", "年卡");

        assertEquals("<form>pay</form>", html);
    }
}
```

```java
package com.fitness.integration.sms.service;

import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponseBody;
import com.fitness.integration.sms.config.AliyunSmsProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AliyunSmsSenderTest {

    @Mock
    private Client client;

    @Mock
    private AliyunSmsProperties properties;

    @Test
    void sendVerifyCodeShouldReturnSuccessResult() throws Exception {
        SendSmsVerifyCodeResponseBody body = new SendSmsVerifyCodeResponseBody();
        body.setCode("OK");
        SendSmsVerifyCodeResponse response = new SendSmsVerifyCodeResponse();
        response.setBody(body);

        when(properties.getSignName()).thenReturn("fitness");
        when(properties.getTemplateCode()).thenReturn("SMS_001");
        when(client.sendSmsVerifyCode(any())).thenReturn(response);

        AliyunSmsSender sender = new AliyunSmsSender(client, properties);

        assertTrue(sender.sendVerifyCode("13800138000", "123456", 5).success());
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run:

```bash
mvn -q -Dtest=AlipayServiceImplTest,AliyunSmsSenderTest test
```

Expected:

- compilation failure because `AlipayGateway`, `AliyunSmsSender`, and result models do not exist yet

- [ ] **Step 3: Write the minimal payment and SMS refactor**

Create the shared base exception:

```java
package com.fitness.integration.common.exception;

public class IntegrationException extends RuntimeException {

    public IntegrationException(String message) {
        super(message);
    }

    public IntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

Create `PaymentIntegrationException`:

```java
package com.fitness.integration.payment.exception;

import com.fitness.integration.common.exception.IntegrationException;

public class PaymentIntegrationException extends IntegrationException {

    public PaymentIntegrationException(String message) {
        super(message);
    }

    public PaymentIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

Create `SmsIntegrationException`:

```java
package com.fitness.integration.sms.exception;

import com.fitness.integration.common.exception.IntegrationException;

public class SmsIntegrationException extends IntegrationException {

    public SmsIntegrationException(String message) {
        super(message);
    }

    public SmsIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

Create payment result records:

```java
package com.fitness.integration.payment.model;

public record AlipayCreateOrderResult(boolean success, String body, String code, String message) {
}
```

```java
package com.fitness.integration.payment.model;

public record AlipayQueryResult(boolean success, String tradeStatus, String code, String message) {
}
```

```java
package com.fitness.integration.payment.model;

public record AlipayRefundResult(boolean success, String refundFee, String code, String message) {
}
```

Create `AlipayGateway`:

```java
package com.fitness.integration.payment.gateway;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.fitness.integration.payment.config.AlipayProperties;
import com.fitness.integration.payment.exception.PaymentIntegrationException;
import com.fitness.integration.payment.model.AlipayCreateOrderResult;
import com.fitness.integration.payment.model.AlipayQueryResult;
import com.fitness.integration.payment.model.AlipayRefundResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AlipayGateway {

    private final AlipayClient alipayClient;
    private final AlipayProperties alipayProperties;

    public AlipayCreateOrderResult createPageOrder(String orderNo, BigDecimal amount, String subject, String body) {
        try {
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            request.setNotifyUrl(alipayProperties.getNotifyUrl());
            request.setReturnUrl(alipayProperties.getReturnUrl());

            AlipayTradePagePayModel model = new AlipayTradePagePayModel();
            model.setOutTradeNo(orderNo);
            model.setTotalAmount(amount.toString());
            model.setSubject(subject);
            model.setBody(body);
            model.setProductCode("FAST_INSTANT_TRADE_PAY");
            request.setBizModel(model);

            AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
            return new AlipayCreateOrderResult(response.isSuccess(), response.getBody(), response.getCode(), response.getMsg());
        } catch (AlipayApiException e) {
            throw new PaymentIntegrationException("支付宝下单调用失败", e);
        }
    }

    public boolean verifyNotify(Map<String, String> params) {
        try {
            return AlipaySignature.rsaCheckV1(
                    params,
                    alipayProperties.getAlipayPublicKey(),
                    alipayProperties.getCharset(),
                    alipayProperties.getSignType()
            );
        } catch (AlipayApiException e) {
            throw new PaymentIntegrationException("支付宝回调验签失败", e);
        }
    }

    public AlipayQueryResult queryOrder(String orderNo) {
        try {
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            AlipayTradeQueryModel model = new AlipayTradeQueryModel();
            model.setOutTradeNo(orderNo);
            request.setBizModel(model);

            AlipayTradeQueryResponse response = alipayClient.execute(request);
            return new AlipayQueryResult(response.isSuccess(), response.getTradeStatus(), response.getCode(), response.getMsg());
        } catch (AlipayApiException e) {
            throw new PaymentIntegrationException("支付宝订单查询失败", e);
        }
    }

    public AlipayRefundResult refund(String orderNo, String refundNo, BigDecimal refundAmount, String reason) {
        try {
            AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
            AlipayTradeRefundModel model = new AlipayTradeRefundModel();
            model.setOutTradeNo(orderNo);
            model.setRefundAmount(refundAmount.toString());
            model.setRefundReason(reason);
            model.setOutRequestNo(refundNo);
            request.setBizModel(model);

            AlipayTradeRefundResponse response = alipayClient.execute(request);
            return new AlipayRefundResult(response.isSuccess(), response.getRefundFee(), response.getCode(), response.getMsg());
        } catch (AlipayApiException e) {
            throw new PaymentIntegrationException("支付宝退款失败", e);
        }
    }
}
```

Update `AlipayServiceImpl`:

```java
private final AlipayGateway alipayGateway;

@Override
public String createPayOrder(String orderNo, BigDecimal amount, String subject, String body) {
    AlipayCreateOrderResult result = alipayGateway.createPageOrder(orderNo, amount, subject, body);
    if (!result.success()) {
        throw new PaymentIntegrationException("支付宝订单创建失败: " + result.message());
    }
    return result.body();
}

@Override
public boolean verifyNotify(Map<String, String> params) {
    return alipayGateway.verifyNotify(params);
}

@Override
public String queryOrderStatus(String orderNo) {
    AlipayQueryResult result = alipayGateway.queryOrder(orderNo);
    return result.success() ? result.tradeStatus() : null;
}

@Override
public boolean refund(String orderNo, String refundNo, BigDecimal refundAmount, String reason) {
    return alipayGateway.refund(orderNo, refundNo, refundAmount, reason).success();
}
```

Create SMS result records:

```java
package com.fitness.integration.sms.model;

public record SmsSendResult(boolean success, String code, String message) {
}
```

```java
package com.fitness.integration.sms.model;

public record SmsVerifyResult(boolean success, String code, String message) {
}
```

Create `SmsSender`:

```java
package com.fitness.integration.sms.service;

import com.fitness.integration.sms.model.SmsSendResult;
import com.fitness.integration.sms.model.SmsVerifyResult;

public interface SmsSender {

    SmsSendResult sendVerifyCode(String phone, String code, int validMinutes);

    SmsVerifyResult checkVerifyCode(String phone, String code);
}
```

Create `AliyunSmsSender`:

```java
package com.fitness.integration.sms.service;

import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.dypnsapi20170525.models.CheckSmsVerifyCodeRequest;
import com.aliyun.dypnsapi20170525.models.CheckSmsVerifyCodeResponse;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.integration.sms.config.AliyunSmsProperties;
import com.fitness.integration.sms.exception.SmsIntegrationException;
import com.fitness.integration.sms.model.SmsSendResult;
import com.fitness.integration.sms.model.SmsVerifyResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AliyunSmsSender implements SmsSender {

    private final Client dypnsApiClient;
    private final AliyunSmsProperties smsProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public SmsSendResult sendVerifyCode(String phone, String code, int validMinutes) {
        try {
            String templateParam = objectMapper.writeValueAsString(Map.of("code", code, "min", String.valueOf(validMinutes)));

            SendSmsVerifyCodeRequest request = new SendSmsVerifyCodeRequest()
                    .setPhoneNumber(phone)
                    .setSignName(smsProperties.getSignName())
                    .setTemplateCode(smsProperties.getTemplateCode())
                    .setTemplateParam(templateParam)
                    .setValidTime((long) validMinutes * 60)
                    .setInterval(60L);

            SendSmsVerifyCodeResponse response = dypnsApiClient.sendSmsVerifyCode(request);
            String providerCode = response.getBody() != null ? response.getBody().getCode() : null;
            String providerMessage = response.getBody() != null ? response.getBody().getMessage() : null;
            return new SmsSendResult("OK".equals(providerCode), providerCode, providerMessage);
        } catch (JsonProcessingException e) {
            throw new SmsIntegrationException("短信模板参数序列化失败", e);
        } catch (Exception e) {
            throw new SmsIntegrationException("短信发送失败", e);
        }
    }

    @Override
    public SmsVerifyResult checkVerifyCode(String phone, String code) {
        try {
            CheckSmsVerifyCodeRequest request = new CheckSmsVerifyCodeRequest()
                    .setPhoneNumber(phone)
                    .setVerifyCode(code)
                    .setCountryCode("cn");

            CheckSmsVerifyCodeResponse response = dypnsApiClient.checkSmsVerifyCode(request);
            String providerCode = response.getBody() != null ? response.getBody().getCode() : null;
            String providerMessage = response.getBody() != null ? response.getBody().getMessage() : null;
            return new SmsVerifyResult("OK".equals(providerCode), providerCode, providerMessage);
        } catch (Exception e) {
            throw new SmsIntegrationException("短信校验失败", e);
        }
    }
}
```

Update `SmsCodeServiceImpl` to use `SmsSender`:

```java
private final SmsSender smsSender;

SmsSendResult sendResult = smsSender.sendVerifyCode(phone, code, (int) CODE_EXPIRE_MINUTES);
if (!sendResult.success()) {
    redisTemplate.delete(SMS_CODE_KEY_PREFIX + phone);
    redisTemplate.delete(SMS_CODE_COOLDOWN_KEY_PREFIX + phone);
    throw new BusinessException("短信发送失败，请检查手机号是否正确");
}
```

- [ ] **Step 4: Run the focused tests and compile**

Run:

```bash
mvn -q -Dtest=AlipayServiceImplTest,AliyunSmsSenderTest test
mvn -q -DskipTests compile
```

Expected:

- both tests PASS
- compile exits with code `0`

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/fitness/integration/common/exception src/main/java/com/fitness/integration/payment src/main/java/com/fitness/integration/sms src/main/java/com/fitness/modules/user/service/impl/SmsCodeServiceImpl.java src/test/java/com/fitness/integration/payment/service/AlipayServiceImplTest.java src/test/java/com/fitness/integration/sms/service/AliyunSmsSenderTest.java
git rm src/main/java/com/fitness/integration/sms/service/AliyunSmsService.java
git commit -m "refactor: standardize payment and sms integration"
```

### Task 3: Decompose MinIO Storage, Validation, And Metadata Persistence

**Files:**
- Create: `src/test/java/com/fitness/integration/minio/service/FileServiceImplTest.java`
- Create: `src/main/java/com/fitness/integration/minio/exception/StorageIntegrationException.java`
- Create: `src/main/java/com/fitness/integration/minio/client/MinioStorageClient.java`
- Create: `src/main/java/com/fitness/integration/minio/service/FileMetadataService.java`
- Create: `src/main/java/com/fitness/integration/minio/service/FileValidationService.java`
- Modify: `src/main/java/com/fitness/integration/minio/service/impl/FileServiceImpl.java`

- [ ] **Step 1: Write the failing orchestration test**

```java
package com.fitness.integration.minio.service;

import com.fitness.integration.minio.client.MinioStorageClient;
import com.fitness.integration.minio.model.vo.FileUploadVO;
import com.fitness.integration.minio.service.impl.FileServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceImplTest {

    @Mock private MinioStorageClient minioStorageClient;
    @Mock private FileValidationService fileValidationService;
    @Mock private FileMetadataService fileMetadataService;

    @InjectMocks
    private FileServiceImpl fileService;

    @Test
    void uploadImageShouldValidateStoreAndPersistMetadata() {
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", "png".getBytes());
        when(minioStorageClient.upload(file, "images")).thenReturn("images/avatar.png");
        when(minioStorageClient.getFileUrl("images/avatar.png")).thenReturn("http://minio/bucket/images/avatar.png");

        FileUploadVO result = fileService.uploadImage(file);

        verify(fileValidationService).validateImage(file);
        verify(fileMetadataService).saveMetadata(eq(file), eq("avatar.png"), eq("http://minio/bucket/images/avatar.png"));
        assertEquals("http://minio/bucket/images/avatar.png", result.getFileUrl());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
mvn -q -Dtest=FileServiceImplTest test
```

Expected:

- compilation failure because `MinioStorageClient`, `FileValidationService`, and `FileMetadataService` do not exist yet

- [ ] **Step 3: Write the minimal storage decomposition**

Create `StorageIntegrationException`:

```java
package com.fitness.integration.minio.exception;

import com.fitness.integration.common.exception.IntegrationException;

public class StorageIntegrationException extends IntegrationException {

    public StorageIntegrationException(String message) {
        super(message);
    }

    public StorageIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

Create `MinioStorageClient`:

```java
package com.fitness.integration.minio.client;

import com.fitness.integration.minio.config.MinioProperties;
import com.fitness.integration.minio.exception.StorageIntegrationException;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class MinioStorageClient {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    public String upload(MultipartFile file, String objectName) {
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            return objectName;
        } catch (Exception e) {
            throw new StorageIntegrationException("MinIO上传失败", e);
        }
    }

    public void delete(String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            throw new StorageIntegrationException("MinIO删除失败", e);
        }
    }

    public String getFileUrl(String objectName) {
        return minioProperties.getEndpoint() + "/" + minioProperties.getBucketName() + "/" + objectName;
    }

    public String presignedUploadUrl(String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(minioProperties.getBucketName())
                            .object(objectName)
                            .expiry(1, TimeUnit.HOURS)
                            .build()
            );
        } catch (Exception e) {
            throw new StorageIntegrationException("MinIO预签名URL生成失败", e);
        }
    }
}
```

Create `FileValidationService`:

```java
package com.fitness.integration.minio.service;

import com.fitness.common.constants.ErrorCode;
import com.fitness.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class FileValidationService {

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024L;
    private static final List<String> ALLOWED_IMAGE_TYPES = List.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.FILE_TOO_LARGE);
        }
    }

    public void validateImage(MultipartFile file) {
        validateFile(file);
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED);
        }
    }
}
```

Create `FileMetadataService`:

```java
package com.fitness.integration.minio.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.integration.minio.mapper.FileMapper;
import com.fitness.integration.minio.model.SysFile;
import com.fitness.integration.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FileMetadataService {

    private final FileMapper fileMapper;

    public void saveMetadata(MultipartFile file, String fileName, String fileUrl) {
        SysFile sysFile = new SysFile();
        sysFile.setFileName(fileName);
        sysFile.setOriginalName(file.getOriginalFilename());
        sysFile.setFileUrl(fileUrl);
        sysFile.setFileType(file.getContentType());
        sysFile.setFileSize(file.getSize());
        sysFile.setCreateBy(SecurityUtils.getCurrentUserId());
        sysFile.setCreateTime(LocalDateTime.now());
        fileMapper.insert(sysFile);
    }

    public int deleteByUrl(String fileUrl) {
        return fileMapper.delete(new LambdaQueryWrapper<SysFile>().eq(SysFile::getFileUrl, fileUrl));
    }
}
```

Update `FileServiceImpl`:

```java
private final MinioStorageClient minioStorageClient;
private final FileValidationService fileValidationService;
private final FileMetadataService fileMetadataService;

@Override
public FileUploadVO uploadFile(MultipartFile file, String folder) {
    fileValidationService.validateFile(file);

    String originalFilename = file.getOriginalFilename();
    String extension = getFileExtension(originalFilename);
    String fileName = UUID.randomUUID() + extension;
    String objectName = folder.endsWith("/") ? folder + fileName : folder + "/" + fileName;

    String storedObjectName = minioStorageClient.upload(file, objectName);
    String fileUrl = minioStorageClient.getFileUrl(storedObjectName);
    fileMetadataService.saveMetadata(file, fileName, fileUrl);

    return FileUploadVO.builder()
            .fileName(fileName)
            .fileUrl(fileUrl)
            .fileType(file.getContentType())
            .fileSize(file.getSize())
            .build();
}

@Override
public FileUploadVO uploadImage(MultipartFile file) {
    fileValidationService.validateImage(file);
    return uploadFile(file, "images");
}

@Override
public void deleteFile(String fileUrl) {
    String objectName = extractObjectName(fileUrl);
    if (objectName == null) {
        throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
    }
    minioStorageClient.delete(objectName);
    fileMetadataService.deleteByUrl(fileUrl);
}

@Override
public String getFileUrl(String objectName) {
    return minioStorageClient.getFileUrl(objectName);
}

@Override
public String presignedUploadUrl(String objectName) {
    return minioStorageClient.presignedUploadUrl(objectName);
}
```

- [ ] **Step 4: Run the focused test and compile**

Run:

```bash
mvn -q -Dtest=FileServiceImplTest test
mvn -q -DskipTests compile
```

Expected:

- test PASS
- compile exits with code `0`

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/fitness/integration/minio src/test/java/com/fitness/integration/minio/service/FileServiceImplTest.java
git commit -m "refactor: separate minio storage validation and metadata"
```

### Task 4: Split AI Invocation, Sanitization, And Validation Responsibilities

**Files:**
- Create: `src/test/java/com/fitness/integration/ai/support/AiSupportTest.java`
- Create: `src/main/java/com/fitness/integration/ai/exception/AiIntegrationException.java`
- Create: `src/main/java/com/fitness/integration/ai/service/AiChatService.java`
- Create: `src/main/java/com/fitness/integration/ai/service/AiFitnessPlanService.java`
- Create: `src/main/java/com/fitness/integration/ai/support/AiResponseSanitizer.java`
- Create: `src/main/java/com/fitness/integration/ai/support/AiFitnessPlanValidator.java`
- Modify: `src/main/java/com/fitness/integration/ai/service/impl/AIServiceImpl.java`

- [ ] **Step 1: Write the failing support-layer tests**

```java
package com.fitness.integration.ai.support;

import com.fitness.integration.ai.model.dto.FitnessPlanResponseDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AiSupportTest {

    @Test
    void sanitizeJsonShouldRemoveMarkdownFence() {
        AiResponseSanitizer sanitizer = new AiResponseSanitizer();
        assertEquals("{\"weeklyPlan\":[]}", sanitizer.cleanJsonResponse("```json\n{\"weeklyPlan\":[]}\n```"));
    }

    @Test
    void validatorShouldRejectMissingWeeklyPlan() {
        AiFitnessPlanValidator validator = new AiFitnessPlanValidator();
        FitnessPlanResponseDTO dto = new FitnessPlanResponseDTO();
        assertThrows(IllegalArgumentException.class, () -> validator.validateFitnessPlan(dto));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
mvn -q -Dtest=AiSupportTest test
```

Expected:

- compilation failure because support classes do not exist yet

- [ ] **Step 3: Write the minimal AI decomposition**

Create `AiIntegrationException`:

```java
package com.fitness.integration.ai.exception;

import com.fitness.integration.common.exception.IntegrationException;

public class AiIntegrationException extends IntegrationException {

    public AiIntegrationException(String message) {
        super(message);
    }

    public AiIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

Create `AiResponseSanitizer`:

```java
package com.fitness.integration.ai.support;

import org.springframework.stereotype.Component;

@Component
public class AiResponseSanitizer {

    public String cleanJsonResponse(String response) {
        if (response == null || response.isBlank()) {
            return response;
        }

        String cleaned = response.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        return cleaned.trim();
    }

    public String cleanPolishedResponse(String response) {
        return response == null ? null : response.trim();
    }
}
```

Create `AiFitnessPlanValidator`:

```java
package com.fitness.integration.ai.support;

import com.fitness.integration.ai.model.dto.FitnessPlanResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class AiFitnessPlanValidator {

    public void validateFitnessPlan(FitnessPlanResponseDTO response) {
        if (response == null) {
            throw new IllegalArgumentException("生成的计划为空");
        }
        if (response.getWeeklyPlan() == null) {
            throw new IllegalArgumentException("生成的计划缺少weeklyPlan字段");
        }
        if (response.getWeeklyPlan().size() > 7) {
            response.setWeeklyPlan(response.getWeeklyPlan().subList(0, 7));
        }
    }
}
```

Create `AiChatService`:

```java
package com.fitness.integration.ai.service;

import reactor.core.publisher.Flux;

import java.util.Map;

public interface AiChatService {

    String chat(String message);

    String chatWithPrompt(String prompt, Map<String, Object> variables);

    Flux<String> streamChat(String message);

    Flux<String> streamChatWithPrompt(String prompt, Map<String, Object> variables);
}
```

Create `AiFitnessPlanService`:

```java
package com.fitness.integration.ai.service;

import com.fitness.integration.ai.model.dto.FitnessPlanResponseDTO;

import java.util.Map;

public interface AiFitnessPlanService {

    String generateFitnessPlan(String goal, String bodyPart, String experience, Integer height, Integer weight, Integer age);

    FitnessPlanResponseDTO generateFitnessPlanFromProfile(Map<String, Object> profile);
}
```

Refactor `AIServiceImpl` to compose support classes and throw typed exceptions:

```java
private final ChatClient chatClient;
private final PromptTemplates promptTemplates;
private final AiResponseSanitizer aiResponseSanitizer;
private final AiFitnessPlanValidator aiFitnessPlanValidator;

@Override
public String chat(String message) {
    try {
        return chatClient.prompt().user(message).call().content();
    } catch (Exception e) {
        throw new AiIntegrationException("AI 服务调用失败", e);
    }
}

@Override
public FitnessPlanResponseDTO generateFitnessPlanFromProfile(Map<String, Object> profile) {
    try {
        String prompt = promptTemplates.generateFitnessPlanJson(profile);
        BeanOutputConverter<FitnessPlanResponseDTO> converter = new BeanOutputConverter<>(FitnessPlanResponseDTO.class);
        String jsonResponse = chatClient.prompt().user(prompt + "\n\n" + converter.getFormat()).call().content();
        String cleanedJson = aiResponseSanitizer.cleanJsonResponse(jsonResponse);
        FitnessPlanResponseDTO response = converter.convert(cleanedJson);
        aiFitnessPlanValidator.validateFitnessPlan(response);
        return response;
    } catch (Exception e) {
        throw new AiIntegrationException("生成健身计划失败", e);
    }
}

@Override
public String polishText(String text) {
    try {
        String response = chatClient.prompt().user(promptTemplates.polishText(text)).call().content();
        return aiResponseSanitizer.cleanPolishedResponse(response);
    } catch (Exception e) {
        throw new AiIntegrationException("文本润色失败", e);
    }
}
```

- [ ] **Step 4: Run the focused test and compile**

Run:

```bash
mvn -q -Dtest=AiSupportTest test
mvn -q -DskipTests compile
```

Expected:

- test PASS
- compile exits with code `0`

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/fitness/integration/ai src/test/java/com/fitness/integration/ai/support/AiSupportTest.java
git commit -m "refactor: separate ai invocation sanitation and validation"
```

### Task 5: Final Verification For Phase 1

**Files:**
- Test: `src/test/java/com/fitness/integration/security/JwtTokenProviderTest.java`
- Test: `src/test/java/com/fitness/modules/user/service/UserServiceImplAuthTest.java`
- Test: `src/test/java/com/fitness/integration/payment/service/AlipayServiceImplTest.java`
- Test: `src/test/java/com/fitness/integration/sms/service/AliyunSmsSenderTest.java`
- Test: `src/test/java/com/fitness/integration/minio/service/FileServiceImplTest.java`
- Test: `src/test/java/com/fitness/integration/ai/support/AiSupportTest.java`
- Regression: `src/test/java/com/fitness/modules/user/service/CoachDetailServiceTest.java`
- Regression: `src/test/java/com/fitness/modules/chat/service/ChatAssistantServiceImplTest.java`

- [ ] **Step 1: Run the focused Phase 1 test suite**

Run:

```bash
mvn -q -Dtest=JwtTokenProviderTest,UserServiceImplAuthTest,AlipayServiceImplTest,AliyunSmsSenderTest,FileServiceImplTest,AiSupportTest,CoachDetailServiceTest,ChatAssistantServiceImplTest test
```

Expected:

- all listed tests PASS

- [ ] **Step 2: Run the full compile verification**

Run:

```bash
mvn -q -DskipTests compile
```

Expected:

- compile exits with code `0`

- [ ] **Step 3: Review the requirement checklist**

Verify each item before marking Phase 1 complete:

```text
[ ] JwtUtils removed and no longer referenced
[ ] UserServiceImpl issues tokens through JwtTokenProvider only
[ ] SecurityConfig only owns filter-chain behavior
[ ] whitelist/CORS/auth beans/method security split by concern
[ ] payment integration no longer talks to SDK directly from service methods
[ ] sms integration uses interface + typed result objects
[ ] file integration separates validation, storage, and metadata persistence
[ ] ai integration separates sanitization and validation helpers
[ ] no external controller contract changed
```

- [ ] **Step 4: Commit the final verification checkpoint**

```bash
git add .
git commit -m "test: verify backend foundation phase1 refactor"
```

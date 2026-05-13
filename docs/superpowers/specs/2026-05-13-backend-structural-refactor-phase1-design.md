# Backend Structural Refactor Phase 1 Design

**Goal:** Refactor the backend foundation layers (`common`, `config`, `integration`, `security`) to remove duplicated responsibilities, standardize third-party SDK integration patterns, and improve maintainability without changing external API behavior, database schema, or business logic outcomes.

**Scope:** This phase only touches foundational backend code. It does not change REST paths, request/response JSON structures, database tables or fields, or frontend contracts. It also does not yet refactor the large business services in `modules/*`.

---

## 1. Context

The current backend compiles successfully and already contains active business development. The main maintainability risks are concentrated in the foundational layers rather than isolated syntax issues:

- JWT responsibilities are duplicated between `JwtUtils` and `JwtTokenProvider`.
- `SecurityConfig` mixes multiple concerns: whitelist management, CORS, authentication beans, method security, and security context strategy.
- Third-party integration services couple SDK request/response details with application logic.
- Integration code frequently uses `catch (Exception)` and `RuntimeException`, making failure semantics inconsistent.
- Some integration services combine transport, validation, persistence, and current-user concerns in one class.

This phase establishes a stable technical foundation before deeper refactoring of `user`, `chat`, `plan`, and other domain modules.

---

## 2. Constraints

The refactor must preserve all of the following:

- Existing REST endpoints and HTTP methods
- Existing request and response JSON field names
- Existing database schema and field names
- Existing authentication behavior and token semantics visible to clients
- Existing business outcomes for AI, payment, SMS, and file operations

Verification baseline:

- `mvn -q -DskipTests compile` must succeed after the refactor
- Existing relevant tests must still pass

---

## 3. Recommended Approach

### Option A: Minimal cleanup only

Only remove unused code, trim imports, and make localized style fixes.

Pros:

- Lowest implementation risk
- Fast to finish

Cons:

- Leaves structural duplication in place
- Does not solve repeated JWT logic or integration inconsistency

### Option B: Phased structural refactor (recommended)

Refactor only the foundation layers in a bounded first phase, standardize responsibility boundaries, then move to domain services in later phases.

Pros:

- Strong payoff with controlled risk
- Creates a clean base for later service decomposition
- Avoids mixing foundational refactor with domain behavior changes

Cons:

- Requires introducing a few small support classes
- Needs careful verification

### Option C: Full architecture rewrite

Reorganize the backend into a new layered or hexagonal architecture in one pass.

Pros:

- Highest long-term structural purity

Cons:

- Too risky for the current active codebase
- High chance of unintentional behavioral regression
- Not aligned with the "do not break logic" constraint

**Recommendation:** Option B.

---

## 4. Target Architecture For Phase 1

Phase 1 will tighten the backend foundation around four principles:

1. One responsibility per class
2. One canonical JWT implementation
3. SDK details stay inside integration adapters/gateways
4. Integration failures are translated into explicit integration exceptions

### 4.1 Security and JWT

Target structure:

- `JwtTokenProvider` becomes the single JWT authority
- `JwtUtils` is removed after all usages are migrated
- `SecurityConfig` only defines the filter chain
- auxiliary configuration concerns are split into focused config classes

Planned supporting classes:

- `SecurityWhitelist`
- `CorsConfig`
- `AuthenticationBeansConfig`
- `MethodSecurityConfig`
- optional `SecurityContextStrategyConfig`

Expected outcome:

- JWT generation, parsing, validation, and expiration semantics are implemented once
- security configuration becomes readable and testable
- whitelist definitions no longer need to be duplicated across configuration logic

### 4.2 Integration Layer Boundaries

Each integration package should expose application-oriented services while hiding SDK-level ceremony.

General pattern:

- config classes construct SDK clients and configuration properties
- gateway/client/adapter classes talk directly to the SDK
- integration services expose business-friendly operations
- support classes handle formatting, validation, and response cleanup
- exceptions are translated into typed integration exceptions

### 4.3 Exception Model

Current issues:

- inconsistent use of `RuntimeException`
- boolean/null result signaling mixed with thrown exceptions
- `catch (Exception)` blocks that hide intent

Target model:

- `IntegrationException` as the base exception for external service failures
- subtype exceptions by channel:
  - `AiIntegrationException`
  - `PaymentIntegrationException`
  - `SmsIntegrationException`
  - `StorageIntegrationException`

Rules:

- integration code should not throw raw `RuntimeException`
- domain/business code can still translate integration failures into `BusinessException` where needed
- logging remains at the integration boundary where external interaction happens

---

## 5. Detailed Design By Area

### 5.1 JWT Consolidation

Current state:

- `JwtUtils` is used by `UserServiceImpl` for token issuance
- `JwtTokenProvider` is used by `JwtAuthenticationFilter` for validation and parsing

Design:

- migrate token creation in `UserServiceImpl` from `JwtUtils` to `JwtTokenProvider`
- standardize issued token claims around:
  - `userId`
  - `username`
  - `roles`
- preserve the externally visible token behavior expected by the filter chain
- once migration is complete, delete `JwtUtils`

Reasoning:

- the system should not have one implementation for issuing tokens and another for consuming them
- consolidating the logic reduces drift risk if claims, issuer, or expiration settings change later

### 5.2 Security Configuration Decomposition

Current state:

- `SecurityConfig` mixes several unrelated responsibilities

Design:

- keep only the `SecurityFilterChain` assembly in `SecurityConfig`
- move whitelist constants to a dedicated holder
- move CORS configuration bean to a separate config class
- move authentication-related beans to a separate config class
- move method-security expression handler bean to a separate config class
- keep `MODE_INHERITABLETHREADLOCAL` strategy initialization isolated if still required for SSE behavior

Reasoning:

- Spring Security configuration is easier to maintain when each bean group is isolated by concern
- this reduces accidental coupling between filter-chain rules and bean wiring

### 5.3 AI Integration Refactor

Current state:

- `AIServiceImpl` mixes prompt rendering, Spring AI invocation, JSON cleanup, response validation, and multiple unrelated AI use cases

Design:

- keep the existing external `AIService` contract stable for now
- internally split responsibilities into narrower collaborators:
  - `AiChatService`
  - `AiFitnessPlanService`
  - `AiResponseSanitizer`
  - `AiFitnessPlanValidator`
- keep `PromptTemplates` responsible only for prompt generation
- centralize Spring AI invocation semantics around `ChatClient`
- replace raw runtime failures with `AiIntegrationException`

Reasoning:

- this follows common Spring AI usage patterns more closely
- cleanup and validation logic become independently testable
- later `chat` and `plan` refactors can depend on narrower AI services

### 5.4 Alipay Integration Refactor

Current state:

- `AlipayServiceImpl` directly handles SDK request/response objects and returns raw `String`, `boolean`, or `null`

Design:

- introduce `AlipayGateway` for direct SDK interaction
- keep `AlipayService` as the application-facing abstraction
- introduce result models:
  - `AlipayCreateOrderResult`
  - `AlipayQueryResult`
  - `AlipayRefundResult`
- translate SDK failures into `PaymentIntegrationException`
- keep controller and upper-layer behavior unchanged by adapting result objects back to the current flow as needed

Reasoning:

- SDK models should not leak throughout the application
- explicit result objects carry clearer semantics than `boolean` or `null`

### 5.5 SMS Integration Refactor

Current state:

- `AliyunSmsService` has no interface abstraction
- template JSON is manually concatenated
- results are reduced to booleans

Design:

- introduce `SmsSender` interface
- convert `AliyunSmsService` into an implementation such as `AliyunSmsSender`
- add result models:
  - `SmsSendResult`
  - `SmsVerifyResult`
- generate template parameters through structured data rather than string concatenation
- translate provider failures into `SmsIntegrationException` where a thrown failure is more appropriate than a silent `false`

Reasoning:

- this aligns the code with common Spring provider-integration structure
- future provider replacement becomes manageable

### 5.6 MinIO Integration Refactor

Current state:

- `FileServiceImpl` combines file validation, MinIO operations, URL building, persistence, and current-user metadata recording

Design:

- introduce `MinioStorageClient` for direct storage operations
- introduce `FileValidationService` for size/type checks
- introduce `FileMetadataService` for `sys_file` persistence concerns
- keep `FileService` as the orchestrating application-facing contract
- translate storage failures into `StorageIntegrationException`

Reasoning:

- object storage interaction should be isolated from metadata persistence
- file validation rules should not be embedded inside storage transport logic

---

## 6. File-Level Change Plan

### Files to modify

- `src/main/java/com/fitness/config/SecurityConfig.java`
- `src/main/java/com/fitness/integration/security/JwtTokenProvider.java`
- `src/main/java/com/fitness/integration/security/JwtAuthenticationFilter.java`
- `src/main/java/com/fitness/modules/user/service/impl/UserServiceImpl.java`
- `src/main/java/com/fitness/integration/ai/service/impl/AIServiceImpl.java`
- `src/main/java/com/fitness/integration/payment/service/impl/AlipayServiceImpl.java`
- `src/main/java/com/fitness/integration/sms/service/AliyunSmsService.java`
- `src/main/java/com/fitness/integration/minio/service/impl/FileServiceImpl.java`

### Files to add

- `src/main/java/com/fitness/config/security/SecurityWhitelist.java`
- `src/main/java/com/fitness/config/security/CorsConfig.java`
- `src/main/java/com/fitness/config/security/AuthenticationBeansConfig.java`
- `src/main/java/com/fitness/config/security/MethodSecurityConfig.java`
- `src/main/java/com/fitness/config/security/SecurityContextStrategyConfig.java` if still needed
- `src/main/java/com/fitness/integration/common/exception/IntegrationException.java`
- `src/main/java/com/fitness/integration/ai/exception/AiIntegrationException.java`
- `src/main/java/com/fitness/integration/payment/exception/PaymentIntegrationException.java`
- `src/main/java/com/fitness/integration/sms/exception/SmsIntegrationException.java`
- `src/main/java/com/fitness/integration/minio/exception/StorageIntegrationException.java`
- `src/main/java/com/fitness/integration/ai/service/AiChatService.java`
- `src/main/java/com/fitness/integration/ai/service/AiFitnessPlanService.java`
- `src/main/java/com/fitness/integration/ai/support/AiResponseSanitizer.java`
- `src/main/java/com/fitness/integration/ai/support/AiFitnessPlanValidator.java`
- `src/main/java/com/fitness/integration/payment/gateway/AlipayGateway.java`
- `src/main/java/com/fitness/integration/payment/model/AlipayCreateOrderResult.java`
- `src/main/java/com/fitness/integration/payment/model/AlipayQueryResult.java`
- `src/main/java/com/fitness/integration/payment/model/AlipayRefundResult.java`
- `src/main/java/com/fitness/integration/sms/service/SmsSender.java`
- `src/main/java/com/fitness/integration/sms/model/SmsSendResult.java`
- `src/main/java/com/fitness/integration/sms/model/SmsVerifyResult.java`
- `src/main/java/com/fitness/integration/minio/client/MinioStorageClient.java`
- `src/main/java/com/fitness/integration/minio/service/FileMetadataService.java`
- `src/main/java/com/fitness/integration/minio/service/FileValidationService.java`

### Files to remove or reduce

- `src/main/java/com/fitness/common/utils/JwtUtils.java` after successful migration

---

## 7. Success Criteria

Phase 1 is complete when all of the following are true:

- the project still compiles successfully
- `JwtUtils` is no longer required
- JWT behavior is implemented in one place only
- security configuration is split by concern
- integration code no longer throws raw `RuntimeException`
- third-party SDK details are more isolated from upper layers
- no external API contract has changed

---

## 8. Non-Goals

This phase explicitly does not:

- refactor `modules/*` large service implementations in depth
- redesign the domain model
- migrate to a new architectural style such as full hexagonal architecture
- change Flyway migrations or database naming conventions
- change frontend behavior or payload contracts

---

## 9. Risks and Mitigations

### Risk: JWT behavior drift

Mitigation:

- preserve existing claim names used by the authentication filter
- compile and run targeted authentication-related tests after migration

### Risk: Hidden coupling with current integration return types

Mitigation:

- keep existing public service contracts stable where possible during Phase 1
- adapt new result objects internally before exposing wider surface changes

### Risk: SSE or async security context regressions

Mitigation:

- isolate security context strategy configuration
- verify compilation and existing chat-related tests after change

### Risk: Over-scoping

Mitigation:

- keep all business-module decomposition out of Phase 1
- postpone `user/chat/plan` service splitting to later phases

---

## 10. Follow-Up Phases

Planned later phases:

- Phase 2: `user + coach + auth`
- Phase 3: `chat + plan + knowledge`
- Phase 4: remaining modules standardization

Phase 1 exists to make those later refactors safer and more predictable.

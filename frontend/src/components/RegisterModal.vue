<template>
  <Teleport to="body">
    <Transition name="modal-fade">
      <div v-if="visible" class="register-modal-overlay" @click.self="handleClose">
        <Transition name="modal-scale">
          <div v-if="visible" class="register-modal-container">
            <!-- 左侧图片区域 -->
            <div class="register-modal-left">
              <div class="left-bg-image">
                <img src="https://images.unsplash.com/photo-1571902943202-507ec2618e8f?w=600&h=800&fit=crop" alt="健身房" />
                <div class="left-overlay"></div>
              </div>
              <div class="left-content">
                <div class="left-logo">
                  <div class="logo-icon">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                      <path d="M12 2L2 7L12 12L22 7L12 2Z" fill="#FF6B35"/>
                      <path d="M2 17L12 22L22 17" stroke="#FF6B35" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                      <path d="M2 12L12 17L22 12" stroke="#FF6B35" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                  </div>
                  <div class="logo-text">智健<span>AI</span></div>
                </div>
                <div class="left-main-content">
                  <div class="left-title">
                    <h2>加入我们</h2>
                    <h2 class="highlight">开启智能健身</h2>
                  </div>
                  <p class="left-desc">注册即享7天VIP免费体验，让AI成为您的私人健身专家，量身定制科学训练计划。</p>
                  <div class="left-features">
                    <div class="feature-item">
                      <div class="feature-icon">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <path d="M12 2L2 7l10 5 10-5-10-5z"/>
                          <path d="M2 17l10 5 10-5"/>
                          <path d="M2 12l10 5 10-5"/>
                        </svg>
                      </div>
                      <span>7天VIP免费体验</span>
                    </div>
                    <div class="feature-item">
                      <div class="feature-icon">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
                          <path d="m9 12 2 2 4-4"/>
                        </svg>
                      </div>
                      <span>安全可靠的账户保护</span>
                    </div>
                    <div class="feature-item">
                      <div class="feature-icon">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <circle cx="12" cy="12" r="10"/>
                          <path d="M12 6v6l4 2"/>
                        </svg>
                      </div>
                      <span>1分钟快速注册</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 右侧表单区域 -->
            <div class="register-modal-right">
              <!-- 关闭按钮 -->
              <button class="modal-close-btn" @click="handleClose">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M18 6L6 18M6 6l12 12"/>
                </svg>
              </button>

              <div class="right-content">
                <div class="right-header">
                  <h2 class="right-title">创建账户</h2>
                  <p class="right-subtitle">
                    已有账号？
                    <a href="#" class="login-link" @click.prevent="goToLogin">立即登录</a>
                  </p>
                </div>

                <!-- 注册表单 -->
                <div class="register-form">
                  <!-- 手机号 -->
                  <div class="form-group">
                    <label>手机号</label>
                    <div class="input-wrapper" :class="{ 'error': phoneError }">
                      <span class="input-icon">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <rect x="5" y="2" width="14" height="20" rx="2" ry="2"/>
                          <path d="M12 18h.01"/>
                        </svg>
                      </span>
                      <input 
                        v-model="form.phone" 
                        type="text" 
                        placeholder="请输入手机号"
                        maxlength="11"
                        @blur="validatePhone"
                        @input="clearPhoneError"
                      />
                      <span v-if="phoneValid" class="valid-icon">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#07C160" stroke-width="2">
                          <path d="M20 6L9 17l-5-5"/>
                        </svg>
                      </span>
                    </div>
                    <span v-if="phoneError" class="error-text">{{ phoneError }}</span>
                  </div>

                  <!-- 验证码 -->
                  <div class="form-group">
                    <label>验证码</label>
                    <div class="input-wrapper code-input-wrapper" :class="{ 'error': codeError }">
                      <span class="input-icon">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                          <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                          <circle cx="12" cy="16" r="1"/>
                        </svg>
                      </span>
                      <input 
                        v-model="form.code" 
                        type="text" 
                        placeholder="请输入6位验证码"
                        maxlength="6"
                        @blur="validateCode"
                        @input="clearCodeError"
                      />
                      <button 
                        type="button"
                        class="send-code-btn" 
                        :class="{ 'sending': countdown > 0, 'disabled': !canSendCode }"
                        :disabled="countdown > 0 || !canSendCode"
                        @click="sendCode"
                      >
                        <span v-if="countdown > 0">{{ countdown }}s</span>
                        <span v-else-if="codeSent">重新发送</span>
                        <span v-else>获取验证码</span>
                      </button>
                    </div>
                    <span v-if="codeError" class="error-text">{{ codeError }}</span>
                  </div>

                  <!-- 密码 -->
                  <div class="form-group">
                    <label>设置密码</label>
                    <div class="input-wrapper" :class="{ 'error': passwordError }">
                      <span class="input-icon">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                          <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                        </svg>
                      </span>
                      <input 
                        v-model="form.password" 
                        :type="showPassword ? 'text' : 'password'" 
                        placeholder="请设置8-20位密码"
                        @input="checkPasswordStrength"
                        @blur="validatePassword"
                      />
                      <button type="button" class="toggle-password" @click="showPassword = !showPassword">
                        <svg v-if="!showPassword" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                          <circle cx="12" cy="12" r="3"/>
                        </svg>
                        <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
                          <line x1="1" y1="1" x2="23" y2="23"/>
                        </svg>
                      </button>
                    </div>
                    <!-- 密码强度指示器 -->
                    <div v-if="form.password" class="password-strength">
                      <div class="strength-bar">
                        <div class="strength-fill" :class="passwordStrengthClass" :style="{ width: passwordStrengthPercent + '%' }"></div>
                      </div>
                      <span class="strength-text" :class="passwordStrengthClass">{{ passwordStrengthText }}</span>
                    </div>
                    <span v-if="passwordError" class="error-text">{{ passwordError }}</span>
                  </div>

                  <!-- 确认密码 -->
                  <div class="form-group">
                    <label>确认密码</label>
                    <div class="input-wrapper" :class="{ 'error': confirmPasswordError }">
                      <span class="input-icon">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
                          <path d="m9 12 2 2 4-4"/>
                        </svg>
                      </span>
                      <input 
                        v-model="form.confirmPassword" 
                        :type="showConfirmPassword ? 'text' : 'password'" 
                        placeholder="请再次输入密码"
                        @input="checkPasswordMatch"
                        @blur="validateConfirmPassword"
                      />
                      <button type="button" class="toggle-password" @click="showConfirmPassword = !showConfirmPassword">
                        <svg v-if="!showConfirmPassword" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                          <circle cx="12" cy="12" r="3"/>
                        </svg>
                        <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
                          <line x1="1" y1="1" x2="23" y2="23"/>
                        </svg>
                      </button>
                      <span v-if="passwordMatch && form.confirmPassword" class="valid-icon">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#07C160" stroke-width="2">
                          <path d="M20 6L9 17l-5-5"/>
                        </svg>
                      </span>
                    </div>
                    <span v-if="confirmPasswordError" class="error-text">{{ confirmPasswordError }}</span>
                  </div>

                  <!-- 用户协议 -->
                  <div class="form-group agreement-group">
                    <label class="agreement-checkbox">
                      <input type="checkbox" v-model="form.agreement" />
                      <span class="checkmark"></span>
                      <span class="agreement-label">
                        我已阅读并同意
                        <a href="#" @click.prevent>用户协议</a>
                        和
                        <a href="#" @click.prevent>隐私政策</a>
                      </span>
                    </label>
                    <span v-if="agreementError" class="error-text">{{ agreementError }}</span>
                  </div>

                  <button 
                    class="register-submit-btn" 
                    :disabled="loading || !isFormValid"
                    @click="handleRegister"
                  >
                    <span v-if="loading" class="loading-spinner"></span>
                    <span v-else>立即注册</span>
                  </button>
                </div>

                <!-- 其他注册方式 -->
                <div class="other-register">
                  <div class="divider">
                    <span>其他注册方式</span>
                  </div>
                  <div class="social-login">
                    <button type="button" class="social-btn wechat" @click="handleWechatRegister" title="微信注册">
                      <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
                        <path d="M8.691 2.188C3.891 2.188 0 5.476 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 0 1 .213.665l-.39 1.48c-.019.07-.048.141-.048.213 0 .163.13.295.29.295a.326.326 0 0 0 .167-.054l1.903-1.114a.864.864 0 0 1 .717-.098 10.16 10.16 0 0 0 2.837.403c.276 0 .543-.027.811-.05-.857-2.578.157-4.972 1.932-6.446 1.703-1.415 3.882-1.98 5.853-1.838-.576-3.583-4.196-6.348-8.596-6.348zM5.785 5.991c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 0 1-1.162 1.178A1.17 1.17 0 0 1 4.623 7.17c0-.651.52-1.18 1.162-1.18zm5.813 0c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 0 1-1.162 1.178 1.17 1.17 0 0 1-1.162-1.178c0-.651.52-1.18 1.162-1.18zm5.34 2.867c-1.797-.052-3.746.512-5.28 1.786-1.72 1.428-2.687 3.72-1.78 6.22.942 2.453 3.666 4.229 6.884 4.229.826 0 1.622-.12 2.361-.336a.722.722 0 0 1 .598.082l1.584.926a.272.272 0 0 0 .14.047c.134 0 .24-.111.24-.247 0-.06-.023-.12-.038-.177l-.327-1.233a.582.582 0 0 1-.023-.156.49.49 0 0 1 .201-.398C23.024 18.48 24 16.82 24 14.98c0-3.21-2.931-5.837-6.656-6.088V8.89c-.135-.01-.269-.03-.407-.03zm-2.53 3.274c.535 0 .969.44.969.982a.976.976 0 0 1-.969.983.976.976 0 0 1-.969-.983c0-.542.434-.982.97-.982zm4.844 0c.535 0 .969.44.969.982a.976.976 0 0 1-.969.983.976.976 0 0 1-.969-.983c0-.542.434-.982.969-.982z"/>
                      </svg>
                    </button>
                    <button type="button" class="social-btn alipay" @click="handleAlipayRegister" title="支付宝注册">
                      <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
                        <path d="M5.5 2h13C19.88 2 21 3.12 21 4.5v15c0 1.38-1.12 2.5-2.5 2.5h-13C4.12 22 3 20.88 3 19.5v-15C3 3.12 4.12 2 5.5 2zM12 18c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-3-5c0-1.66 1.34-3 3-3s3 1.34 3 3-1.34 3-3 3-3-1.34-3-3z"/>
                      </svg>
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </Transition>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:visible', 'register-success', 'go-login'])

const router = useRouter()
const message = useMessage()

// 表单数据
const form = reactive({
  phone: '',
  code: '',
  password: '',
  confirmPassword: '',
  agreement: false
})

// 状态
const showPassword = ref(false)
const showConfirmPassword = ref(false)
const loading = ref(false)
const countdown = ref(0)
const codeSent = ref(false)

// 错误信息
const phoneError = ref('')
const codeError = ref('')
const passwordError = ref('')
const confirmPasswordError = ref('')
const agreementError = ref('')

// 密码强度
const passwordStrength = ref(0)
const passwordMatch = ref(false)

// 计算属性
const phoneValid = computed(() => {
  return /^1[3-9]\d{9}$/.test(form.phone)
})

const canSendCode = computed(() => {
  return phoneValid.value && countdown.value === 0
})

const passwordStrengthClass = computed(() => {
  if (passwordStrength.value <= 25) return 'weak'
  if (passwordStrength.value <= 50) return 'fair'
  if (passwordStrength.value <= 75) return 'good'
  return 'strong'
})

const passwordStrengthText = computed(() => {
  if (passwordStrength.value <= 25) return '弱'
  if (passwordStrength.value <= 50) return '一般'
  if (passwordStrength.value <= 75) return '良好'
  return '强'
})

const passwordStrengthPercent = computed(() => {
  return passwordStrength.value
})

const isFormValid = computed(() => {
  return phoneValid.value && 
         form.code.length === 6 && 
         form.password.length >= 8 && 
         form.password.length <= 20 &&
         form.password === form.confirmPassword &&
         form.agreement
})

// 监听visible变化
watch(() => props.visible, (newVal) => {
  if (newVal) {
    document.body.style.overflow = 'hidden'
  } else {
    document.body.style.overflow = ''
    resetForm()
  }
})

// 关闭弹窗
function handleClose() {
  emit('update:visible', false)
}

// 去登录
function goToLogin() {
  handleClose()
  emit('go-login')
}

// 重置表单
function resetForm() {
  form.phone = ''
  form.code = ''
  form.password = ''
  form.confirmPassword = ''
  form.agreement = false
  phoneError.value = ''
  codeError.value = ''
  passwordError.value = ''
  confirmPasswordError.value = ''
  agreementError.value = ''
  passwordStrength.value = 0
  passwordMatch.value = false
  codeSent.value = false
}

// 验证手机号
function validatePhone() {
  if (!form.phone) {
    phoneError.value = '请输入手机号'
    return false
  }
  if (!phoneValid.value) {
    phoneError.value = '请输入正确的11位手机号'
    return false
  }
  phoneError.value = ''
  return true
}

function clearPhoneError() {
  phoneError.value = ''
}

// 验证验证码
function validateCode() {
  if (!form.code) {
    codeError.value = '请输入验证码'
    return false
  }
  if (form.code.length !== 6) {
    codeError.value = '验证码为6位数字'
    return false
  }
  codeError.value = ''
  return true
}

function clearCodeError() {
  codeError.value = ''
}

// 发送验证码
async function sendCode() {
  if (!validatePhone()) return
  
  countdown.value = 60
  codeSent.value = true
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
  
  message.success('验证码已发送，请注意查收')
}

// 检查密码强度
function checkPasswordStrength() {
  const pwd = form.password
  let strength = 0
  
  if (pwd.length >= 8) strength += 20
  if (pwd.length >= 12) strength += 10
  if (/[a-z]/.test(pwd)) strength += 15
  if (/[A-Z]/.test(pwd)) strength += 15
  if (/\d/.test(pwd)) strength += 20
  if (/[^a-zA-Z0-9]/.test(pwd)) strength += 20
  
  passwordStrength.value = Math.min(strength, 100)
  
  // 实时检查密码匹配
  if (form.confirmPassword) {
    checkPasswordMatch()
  }
}

// 验证密码
function validatePassword() {
  if (!form.password) {
    passwordError.value = '请设置密码'
    return false
  }
  if (form.password.length < 8) {
    passwordError.value = '密码长度至少8位'
    return false
  }
  if (form.password.length > 20) {
    passwordError.value = '密码长度最多20位'
    return false
  }
  passwordError.value = ''
  return true
}

// 检查密码匹配
function checkPasswordMatch() {
  if (!form.confirmPassword) {
    passwordMatch.value = false
    return
  }
  passwordMatch.value = form.password === form.confirmPassword
}

// 验证确认密码
function validateConfirmPassword() {
  if (!form.confirmPassword) {
    confirmPasswordError.value = '请确认密码'
    return false
  }
  if (form.password !== form.confirmPassword) {
    confirmPasswordError.value = '两次输入的密码不一致'
    return false
  }
  confirmPasswordError.value = ''
  return true
}

// 注册
async function handleRegister() {
  // 表单验证
  const isPhoneValid = validatePhone()
  const isCodeValid = validateCode()
  const isPasswordValid = validatePassword()
  const isConfirmValid = validateConfirmPassword()
  
  if (!form.agreement) {
    agreementError.value = '请阅读并同意用户协议和隐私政策'
    return
  }
  
  if (!isPhoneValid || !isCodeValid || !isPasswordValid || !isConfirmValid) {
    return
  }

  loading.value = true
  
  try {
    // 模拟注册请求
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    message.success('注册成功！欢迎加入智健AI')
    emit('register-success')
    handleClose()
  } catch (error) {
    console.error('注册失败:', error)
    message.error('注册失败，请重试')
  } finally {
    loading.value = false
  }
}

// 微信注册
function handleWechatRegister() {
  message.info('微信注册功能开发中...')
}

// 支付宝注册
function handleAlipayRegister() {
  message.info('支付宝注册功能开发中...')
}
</script>

<style scoped>
/* 遮罩层 */
.register-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(10, 10, 15, 0.85);
  backdrop-filter: blur(12px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

/* 弹窗容器 */
.register-modal-container {
  display: flex;
  width: 900px;
  max-width: 100%;
  height: 640px;
  background: #1A1A25;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 25px 80px rgba(0, 0, 0, 0.6), 0 0 0 1px rgba(255, 255, 255, 0.05);
}

/* 左侧区域 */
.register-modal-left {
  position: relative;
  width: 45%;
  min-width: 360px;
  overflow: hidden;
}

.left-bg-image {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

.left-bg-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.left-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(26, 26, 37, 0.95) 0%, rgba(26, 26, 37, 0.8) 50%, rgba(255, 107, 53, 0.3) 100%);
}

.left-content {
  position: relative;
  z-index: 1;
  padding: 40px;
  height: 100%;
  display: flex;
  flex-direction: column;
  color: #fff;
}

.left-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 60px;
}

.logo-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C61 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
}

.logo-text span {
  color: #FF6B35;
}

.left-main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.left-title {
  margin-bottom: 20px;
}

.left-title h2 {
  font-size: 32px;
  font-weight: 700;
  line-height: 1.3;
  margin: 0;
}

.left-title h2.highlight {
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C61 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.left-desc {
  font-size: 14px;
  line-height: 1.7;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 40px;
  max-width: 280px;
}

.left-features {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.feature-icon {
  width: 36px;
  height: 36px;
  background: rgba(255, 107, 53, 0.15);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #FF6B35;
}

.feature-item span {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
}

/* 右侧区域 */
.register-modal-right {
  flex: 1;
  position: relative;
  padding: 40px 48px;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

.modal-close-btn {
  position: absolute;
  top: 20px;
  right: 20px;
  width: 36px;
  height: 36px;
  border: none;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.5);
  transition: all 0.3s ease;
}

.modal-close-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

.right-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding-top: 20px;
}

.right-header {
  margin-bottom: 28px;
}

.right-title {
  font-size: 28px;
  font-weight: 700;
  color: #fff;
  margin: 0 0 12px 0;
}

.right-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.5);
  margin: 0;
}

.login-link {
  color: #FF6B35;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s ease;
}

.login-link:hover {
  color: #FF8C61;
}

/* 表单 */
.register-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-group label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
  font-weight: 500;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 0 16px;
  transition: all 0.3s ease;
}

.input-wrapper:focus-within {
  border-color: #FF6B35;
  background: rgba(255, 107, 53, 0.05);
}

.input-wrapper.error {
  border-color: #EF4444;
  background: rgba(239, 68, 68, 0.05);
}

.input-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.4);
  margin-right: 12px;
}

.input-wrapper input {
  flex: 1;
  height: 44px;
  border: none;
  background: transparent;
  color: #fff;
  font-size: 15px;
  outline: none;
}

.input-wrapper input::placeholder {
  color: rgba(255, 255, 255, 0.35);
}

.valid-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: 8px;
}

.toggle-password {
  background: none;
  border: none;
  color: rgba(255, 255, 255, 0.4);
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: color 0.3s ease;
}

.toggle-password:hover {
  color: rgba(255, 255, 255, 0.7);
}

.code-input-wrapper {
  padding-right: 6px;
}

.send-code-btn {
  padding: 8px 14px;
  border: none;
  background: rgba(255, 107, 53, 0.15);
  color: #FF6B35;
  font-size: 13px;
  font-weight: 500;
  border-radius: 8px;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.3s ease;
  min-width: 90px;
}

.send-code-btn:hover:not(:disabled) {
  background: rgba(255, 107, 53, 0.25);
}

.send-code-btn:disabled {
  cursor: not-allowed;
}

.send-code-btn.sending {
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.5);
}

.send-code-btn.disabled {
  opacity: 0.5;
}

.error-text {
  font-size: 12px;
  color: #EF4444;
  margin-top: 2px;
}

/* 密码强度 */
.password-strength {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 6px;
}

.strength-bar {
  flex: 1;
  height: 4px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
  overflow: hidden;
}

.strength-fill {
  height: 100%;
  border-radius: 2px;
  transition: all 0.3s ease;
}

.strength-fill.weak {
  background: #EF4444;
}

.strength-fill.fair {
  background: #F59E0B;
}

.strength-fill.good {
  background: #3B82F6;
}

.strength-fill.strong {
  background: #07C160;
}

.strength-text {
  font-size: 12px;
  font-weight: 500;
}

.strength-text.weak {
  color: #EF4444;
}

.strength-text.fair {
  color: #F59E0B;
}

.strength-text.good {
  color: #3B82F6;
}

.strength-text.strong {
  color: #07C160;
}

/* 用户协议 */
.agreement-group {
  margin-top: 4px;
}

.agreement-checkbox {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  cursor: pointer;
}

.agreement-checkbox input {
  display: none;
}

.checkmark {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  flex-shrink: 0;
  margin-top: 1px;
}

.agreement-checkbox input:checked + .checkmark {
  background: #FF6B35;
  border-color: #FF6B35;
}

.agreement-checkbox input:checked + .checkmark::after {
  content: '✓';
  color: #fff;
  font-size: 12px;
  font-weight: bold;
}

.agreement-label {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
  line-height: 1.5;
}

.agreement-label a {
  color: #FF6B35;
  text-decoration: none;
}

.agreement-label a:hover {
  text-decoration: underline;
}

/* 注册按钮 */
.register-submit-btn {
  height: 48px;
  border: none;
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C61 100%);
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  cursor: pointer;
  margin-top: 4px;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.register-submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(255, 107, 53, 0.4);
}

.register-submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 其他注册方式 */
.other-register {
  margin-top: auto;
  padding-top: 24px;
}

.divider {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: rgba(255, 255, 255, 0.1);
}

.divider span {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.4);
}

.social-login {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.social-btn {
  width: 52px;
  height: 52px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.05);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.social-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  transform: translateY(-3px);
}

.social-btn.wechat {
  color: #07C160;
}

.social-btn.wechat:hover {
  background: rgba(7, 193, 96, 0.15);
  border-color: rgba(7, 193, 96, 0.3);
}

.social-btn.alipay {
  color: #1677FF;
}

.social-btn.alipay:hover {
  background: rgba(22, 119, 255, 0.15);
  border-color: rgba(22, 119, 255, 0.3);
}

/* 动画 */
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.3s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

.modal-scale-enter-active,
.modal-scale-leave-active {
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.modal-scale-enter-from,
.modal-scale-leave-to {
  opacity: 0;
  transform: scale(0.9) translateY(20px);
}

/* 响应式 */
@media (max-width: 768px) {
  .register-modal-container {
    width: 100%;
    max-width: 420px;
    height: auto;
    max-height: 90vh;
    overflow-y: auto;
  }
  
  .register-modal-left {
    display: none;
  }
  
  .register-modal-right {
    padding: 32px 24px;
  }
  
  .right-header {
    margin-bottom: 24px;
  }
  
  .other-register {
    padding-top: 20px;
  }
}
</style>

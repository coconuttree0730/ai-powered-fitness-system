<template>
  <Teleport to="body">
    <Transition name="modal-fade">
      <div v-if="visible" class="login-modal-overlay">
        <Transition name="modal-scale">
          <div v-if="visible" class="login-modal-container">
            <!-- 左侧图片区域 -->
            <div class="login-modal-left">
              <div class="left-bg-image">
                <img src="https://images.unsplash.com/photo-1517836357463-d25dfeac3438?w=600&h=800&fit=crop" alt="健身房" />
                <div class="left-overlay"></div>
              </div>
              <div class="left-content">
                <div class="left-logo">
                  <div class="logo-icon">
                    <img src="/weightlifting.svg" alt="Logo" style="width: 24px; height: 24px;" />
                  </div>
                  <div class="logo-text">FutureFit<span>·未来健身</span></div>
                </div>
                <div class="left-main-content">
                  <div class="left-title">
                    <h2>加入我们</h2>
                    <h2 class="highlight">开启智能健身</h2>
                  </div>
                  <p class="left-desc">注册成为FutureFit 未来健身会员，享受AI智能训练计划、专业教练指导和实时数据追踪服务。</p>
                  <div class="left-features">
                    <div class="feature-item">
                      <div class="feature-icon">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
                        </svg>
                      </div>
                      <span>免费7天VIP体验</span>
                    </div>
                    <div class="feature-item">
                      <div class="feature-icon">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                          <circle cx="9" cy="7" r="4"/>
                          <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                          <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
                        </svg>
                      </div>
                      <span>专属健身社区</span>
                    </div>
                    <div class="feature-item">
                      <div class="feature-icon">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
                        </svg>
                      </div>
                      <span>积分奖励计划</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 右侧表单区域 -->
            <div class="login-modal-right">
              <!-- 关闭按钮 -->
              <button class="modal-close-btn" @click="handleClose">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M18 6L6 18M6 6l12 12"/>
                </svg>
              </button>

              <div class="right-content">
                <div class="right-header">
                  <h2 class="right-title">创建账号</h2>
                  <p class="right-subtitle">
                    已有账号？
                    <a href="#" class="register-link" @click.prevent="goToLogin">立即登录</a>
                  </p>
                </div>

                <!-- 注册表单 -->
                <div class="login-form">
                  <div class="form-group">
                    <label>用户名</label>
                    <div class="input-wrapper">
                      <span class="input-icon">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                          <circle cx="12" cy="7" r="4"/>
                        </svg>
                      </span>
                      <input
                        v-model="form.username"
                        type="text"
                        placeholder="请输入用户名（3-20个字符）"
                        @keyup.enter="handleRegister"
                      />
                    </div>
                  </div>

                  <div class="form-group">
                    <label>手机号</label>
                    <div class="input-wrapper">
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
                        @keyup.enter="handleRegister"
                      />
                    </div>
                  </div>

                  <!-- 验证码 + 滑块验证 紧凑布局 -->
                  <div class="form-group verify-group">
                    <label>验证码</label>
                    <div class="verify-compact">
                      <div class="input-wrapper code-input-wrapper">
                        <span class="input-icon">
                          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                            <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                            <circle cx="12" cy="16" r="1"/>
                          </svg>
                        </span>
                        <input
                          v-model="form.code"
                          type="text"
                          placeholder="请输入验证码"
                          maxlength="6"
                          @keyup.enter="handleRegister"
                        />
                      </div>
                      <button
                        type="button"
                        class="send-code-btn-compact"
                        :disabled="countdown > 0 || !isPhoneValid || isSendingCode"
                        @click="sendCode"
                      >
                        <span v-if="countdown > 0">{{ countdown }}s</span>
                        <span v-else-if="isSliderVerified">重新获取</span>
                        <span v-else>获取验证码</span>
                      </button>
                    </div>

                    <!-- 滑块验证（紧凑内嵌） -->
                    <Transition name="slider-compact">
                      <div v-if="showSliderVerify && !isSliderVerified" class="slider-compact-wrapper">
                        <div ref="sliderTrackRef" class="slider-compact-track">
                          <div class="slider-compact-progress" :style="{ width: sliderProgressPercent + '%' }"></div>
                          <div class="slider-glow" :style="{ left: sliderLeft + 'px' }"></div>
                          <div
                            class="slider-compact-handle"
                            :class="{ moving: isSliderMoving }"
                            :style="{ left: sliderLeft + 'px' }"
                            @mousedown="startSliderDrag"
                            @touchstart="startSliderDrag"
                          >
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                              <path d="M9 18l6-6-6-6"/>
                            </svg>
                          </div>
                        </div>
                      </div>
                    </Transition>
                  </div>

                  <div class="form-group">
                    <label>密码</label>
                    <div class="input-wrapper">
                      <span class="input-icon">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                          <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                        </svg>
                      </span>
                      <input
                        v-model="form.password"
                        :type="showPassword ? 'text' : 'password'"
                        placeholder="请输入密码（6-20个字符）"
                        @keyup.enter="handleRegister"
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
                  </div>

                  <div class="form-group">
                    <label>确认密码</label>
                    <div class="input-wrapper">
                      <span class="input-icon">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                          <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                          <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                          <circle cx="12" cy="16" r="1"/>
                        </svg>
                      </span>
                      <input
                        v-model="form.confirmPassword"
                        :type="showConfirmPassword ? 'text' : 'password'"
                        placeholder="请再次输入密码"
                        @keyup.enter="handleRegister"
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
                    </div>
                  </div>

                  <button
                    class="login-submit-btn"
                    :disabled="loading"
                    @click="handleRegister"
                  >
                    <span v-if="loading" class="loading-spinner"></span>
                    <span v-else>注册</span>
                  </button>
                </div>

                <!-- 其他注册方式 -->
                <div class="other-login-compact">
                  <div class="divider">
                    <span>其他注册方式</span>
                  </div>
                  <div class="social-login-compact">
                    <button type="button" class="social-btn-compact wechat" @click="handleWechatLogin" title="微信登录">
                      <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                        <path d="M8.691 2.188C3.891 2.188 0 5.476 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 0 1 .213.665l-.39 1.48c-.019.07-.048.141-.048.213 0 .163.13.295.29.295a.326.326 0 0 0 .167-.054l1.903-1.114a.864.864 0 0 1 .717-.098 10.16 10.16 0 0 0 2.837.403c.276 0 .543-.027.811-.05-.857-2.578.157-4.972 1.932-6.446 1.703-1.415 3.882-1.98 5.853-1.838-.576-3.583-4.196-6.348-8.596-6.348zM5.785 5.991c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 0 1-1.162 1.178A1.17 1.17 0 0 1 4.623 7.17c0-.651.52-1.18 1.162-1.18zm5.813 0c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 0 1-1.162 1.178 1.17 1.17 0 0 1-1.162-1.178c0-.651.52-1.18 1.162-1.18zm5.34 2.867c-1.797-.052-3.746.512-5.28 1.786-1.72 1.428-2.687 3.72-1.78 6.22.942 2.453 3.666 4.229 6.884 4.229.826 0 1.622-.12 2.361-.336a.722.722 0 0 1 .598.082l1.584.926a.272.272 0 0 0 .14.047c.134 0 .24-.111.24-.247 0-.06-.023-.12-.038-.177l-.327-1.233a.582.582 0 0 1-.023-.156.49.49 0 0 1 .201-.398C23.024 18.48 24 16.82 24 14.98c0-3.21-2.931-5.837-6.656-6.088V8.89c-.135-.01-.269-.03-.407-.03zm-2.53 3.274c.535 0 .969.44.969.982a.976.976 0 0 1-.969.983.976.976 0 0 1-.969-.983c0-.542.434-.982.97-.982zm4.844 0c.535 0 .969.44.969.982a.976.976 0 0 1-.969.983.976.976 0 0 1-.969-.983c0-.542.434-.982.969-.982z"/>
                      </svg>
                    </button>

                  </div>
                </div>

                <!-- 协议 -->
                <p class="agreement-text">
                  注册即表示您同意我们的
                  <a href="#" @click.prevent>用户协议</a>
                  和
                  <a href="#" @click.prevent>隐私政策</a>
                </p>
              </div>
            </div>
          </div>
        </Transition>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, reactive, watch, computed, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { register, getSliderVerifyToken, verifySlider, sendSmsCode } from '@/api/auth'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:visible', 'register-success', 'go-login'])

const router = useRouter()
const message = useMessage()

const showPassword = ref(false)
const showConfirmPassword = ref(false)
const loading = ref(false)
const countdown = ref(0)

// 滑块验证相关（嵌入表单）
const showSliderVerify = ref(false)
const sliderVerifyToken = ref('')
const isSliderVerified = ref(false)
const isSliderSuccess = ref(false)
const isSliderMoving = ref(false)
const sliderLeft = ref(0)
const sliderStartX = ref(0)
const sliderCurrentX = ref(0)
const sliderBoxWidth = ref(280)  // 紧凑宽度，自适应容器
const sliderBtnWidth = 32  // 紧凑按钮
const pendingPhone = ref('')
const isSendingCode = ref(false)

// 表单数据
const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  phone: '',
  code: '',
  roleCode: 'MEMBER'
})

// 计算属性
const isPhoneValid = computed(() => {
  return /^1[3-9]\d{9}$/.test(form.phone)
})

// 滑块进度百分比
const sliderProgressPercent = computed(() => {
  const maxLeft = sliderBoxWidth.value - sliderBtnWidth
  return (sliderLeft.value / maxLeft) * 100
})

// 滑块轨道引用
const sliderTrackRef = ref(null)

// 更新滑块容器宽度
function updateSliderWidth() {
  if (sliderTrackRef.value) {
    sliderBoxWidth.value = sliderTrackRef.value.offsetWidth
  }
}

// 监听visible变化
watch(() => props.visible, (newVal) => {
  if (newVal) {
    document.body.style.overflow = 'hidden'
  } else {
    document.body.style.overflow = ''
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

// 发送验证码 - 显示滑块验证
async function sendCode() {
  if (!isPhoneValid.value) return
  if (isSendingCode.value) return

  // 如果已经验证过，直接发送
  if (isSliderVerified.value) {
    await doSendSmsCode()
    return
  }

  // 保存当前手机号
  pendingPhone.value = form.phone

  // 显示滑块验证（嵌入表单）
  showSliderVerify.value = true

  // 等待DOM更新后获取容器宽度
  setTimeout(() => {
    updateSliderWidth()
  }, 50)

  try {
    // 获取滑块验证令牌
    const data = await getSliderVerifyToken()
    if (data && data.token) {
      sliderVerifyToken.value = data.token
    } else {
      message.error('获取验证令牌失败，请重试')
      showSliderVerify.value = false
    }
  } catch (error) {
    console.error('获取滑块验证令牌失败:', error)
    message.error('验证服务暂不可用，请稍后重试')
    showSliderVerify.value = false
  }
}

// 滑块拖动开始
function startSliderDrag(e) {
  if (isSliderSuccess.value) return
  isSliderMoving.value = true
  sliderStartX.value = e.type.includes('touch') ? e.touches[0].clientX : e.clientX
  sliderCurrentX.value = sliderLeft.value

  document.addEventListener('mousemove', onSliderDrag)
  document.addEventListener('mouseup', endSliderDrag)
  document.addEventListener('touchmove', onSliderDrag)
  document.addEventListener('touchend', endSliderDrag)
}

// 滑块拖动中
function onSliderDrag(e) {
  if (!isSliderMoving.value) return
  e.preventDefault()

  const clientX = e.type.includes('touch') ? e.touches[0].clientX : e.clientX
  const diff = clientX - sliderStartX.value
  let newLeft = sliderCurrentX.value + diff

  // 限制滑动范围
  const maxLeft = sliderBoxWidth.value - sliderBtnWidth
  newLeft = Math.max(0, Math.min(newLeft, maxLeft))

  sliderLeft.value = newLeft
}

// 滑块拖动结束
async function endSliderDrag() {
  if (!isSliderMoving.value) return
  isSliderMoving.value = false

  const maxLeft = sliderBoxWidth.value - sliderBtnWidth
  const progress = sliderLeft.value / maxLeft

  if (progress >= 0.9) {
    // 滑动到位，执行后端验证
    sliderLeft.value = maxLeft
    await verifySliderWithBackend()
  } else {
    // 未到位，回弹
    resetSlider()
    message.error('请拖动滑块到最右侧')
  }

  document.removeEventListener('mousemove', onSliderDrag)
  document.removeEventListener('mouseup', endSliderDrag)
  document.removeEventListener('touchmove', onSliderDrag)
  document.removeEventListener('touchend', endSliderDrag)
}

// 后端验证滑块
async function verifySliderWithBackend() {
  try {
    const result = await verifySlider({
      token: sliderVerifyToken.value,
      sliderValue: Math.round(sliderLeft.value),
      timestamp: Date.now()
    })

    if (result && result.verified) {
      // 验证成功
      isSliderSuccess.value = true
      isSliderVerified.value = true

      // 自动发送短信验证码
      await doSendSmsCode()
    } else {
      // 验证失败
      message.error(result?.message || '验证失败，请重新尝试')
      resetSlider()
    }
  } catch (error) {
    console.error('滑块验证请求失败:', error)
    message.error('验证请求失败，请重试')
    resetSlider()
  }
}

// 重置滑块
function resetSlider() {
  sliderLeft.value = 0
  isSliderSuccess.value = false
  isSliderMoving.value = false
}

// 实际发送短信验证码
async function doSendSmsCode() {
  if (isSendingCode.value) return
  isSendingCode.value = true

  try {
    // 调用发送短信验证码接口
    const data = await sendSmsCode({
      phone: pendingPhone.value,
      verifyToken: sliderVerifyToken.value
    })

    if (data && data.sent) {
      // 开始倒计时
      countdown.value = 60
      const timer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
          clearInterval(timer)
        }
      }, 1000)

      message.success('验证码已发送，请注意查收')
    } else {
      message.error(data?.message || '验证码发送失败，请重试')
    }
  } catch (error) {
    console.error('发送验证码失败:', error)
    message.error('验证码发送失败，请稍后重试')
  } finally {
    isSendingCode.value = false
    // 清理验证令牌
    sliderVerifyToken.value = ''
    pendingPhone.value = ''
  }
}

// 注册
async function handleRegister() {
  // 表单验证
  if (!form.username.trim()) {
    message.error('请输入用户名')
    return
  }
  if (form.username.length < 3 || form.username.length > 20) {
    message.error('用户名长度3-20个字符')
    return
  }
  if (!form.phone.trim()) {
    message.error('请输入手机号')
    return
  }
  if (!/^1[3-9]\d{9}$/.test(form.phone)) {
    message.error('请输入正确的手机号')
    return
  }
  if (!form.code || form.code.length !== 6) {
    message.error('请输入6位验证码')
    return
  }
  if (!form.password) {
    message.error('请输入密码')
    return
  }
  if (form.password.length < 6 || form.password.length > 20) {
    message.error('密码长度6-20个字符')
    return
  }
  if (form.password !== form.confirmPassword) {
    message.error('两次密码输入不一致')
    return
  }

  loading.value = true

  try {
    const res = await register({
      username: form.username,
      password: form.password,
      phone: form.phone,
      code: form.code,
      roleCode: form.roleCode
    })

    if (res.code === 200) {
      message.success('注册成功，请登录')
      emit('register-success')
      handleClose()
      emit('go-login')
    } else {
      message.error(res.message || '注册失败')
    }
  } catch (error) {
    console.error('注册失败:', error)
    message.error(error.message || '注册失败，请重试')
  } finally {
    loading.value = false
  }
}

// 微信登录
function handleWechatLogin() {
  message.info('微信登录功能开发中...')
}

// 支付宝登录
function handleAlipayLogin() {
  message.info('支付宝登录功能开发中...')
}

// 清理事件监听
onUnmounted(() => {
  document.removeEventListener('mousemove', onSliderDrag)
  document.removeEventListener('mouseup', endSliderDrag)
  document.removeEventListener('touchmove', onSliderDrag)
  document.removeEventListener('touchend', endSliderDrag)
})
</script>

<style scoped>
/* 遮罩层 */
.login-modal-overlay {
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
  z-index: 5000;
  padding: 20px;
}

/* 弹窗容器 */
.login-modal-container {
  display: flex;
  width: 900px;
  max-width: 100%;
  height: 720px;
  background: #1A1A25;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 25px 80px rgba(0, 0, 0, 0.6), 0 0 0 1px rgba(255, 255, 255, 0.05);
}

/* 左侧区域 */
.login-modal-left {
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
  padding: 32px 40px;
  height: 100%;
  display: flex;
  flex-direction: column;
  color: #fff;
}

.left-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 40px;
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
  margin-bottom: 16px;
}

.left-title h2 {
  font-size: 28px;
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
  font-size: 13px;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 32px;
  max-width: 280px;
}

.left-features {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.feature-icon {
  width: 32px;
  height: 32px;
  background: rgba(255, 107, 53, 0.15);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #FF6B35;
}

.feature-item span {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.9);
}

/* 右侧区域 */
.login-modal-right {
  flex: 1;
  position: relative;
  padding: 20px 32px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
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
  padding-top: 4px;
  justify-content: flex-start;
}

.right-header {
  margin-bottom: 8px;
}

.right-title {
  font-size: 22px;
  font-weight: 700;
  color: #fff;
  margin: 0 0 4px 0;
}

.right-subtitle {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.5);
  margin: 0;
}

.register-link {
  color: #FF6B35;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s ease;
}

.register-link:hover {
  color: #FF8C61;
}

/* 表单 */
.login-form {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.form-group label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
  font-weight: 500;
  margin-bottom: 2px;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 10px;
  padding: 0 12px;
  transition: all 0.3s ease;
}

.input-wrapper:focus-within {
  border-color: #FF6B35;
  background: rgba(255, 107, 53, 0.05);
}

.input-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.4);
  margin-right: 10px;
}

.input-wrapper input {
  flex: 1;
  height: 36px;
  border: none;
  background: transparent;
  color: #fff;
  font-size: 13px;
  outline: none;
}

.input-wrapper input::placeholder {
  color: rgba(255, 255, 255, 0.35);
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

/* 验证码 + 滑块 紧凑布局 */
.verify-group {
  margin-bottom: 8px;
}

.verify-compact {
  display: flex;
  gap: 8px;
  align-items: stretch;
}

.verify-compact .code-input-wrapper {
  flex: 1;
  position: relative;
}

.verify-compact .code-input-wrapper input {
  height: 40px;
  padding-left: 36px;
  padding-right: 12px;
}

.verify-compact .input-icon {
  left: 10px;
}

.send-code-btn-compact {
  height: 40px;
  padding: 0 14px;
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C61 100%);
  border: none;
  border-radius: 10px;
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
  white-space: nowrap;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 90px;
  box-shadow: 0 2px 8px rgba(255, 107, 53, 0.25);
}

.send-code-btn-compact:hover:not(:disabled) {
  transform: translateY(-2px) scale(1.02);
  box-shadow: 0 6px 16px rgba(255, 107, 53, 0.4);
}

.send-code-btn-compact:disabled {
  background: rgba(255, 255, 255, 0.06);
  color: rgba(255, 255, 255, 0.3);
  cursor: not-allowed;
  box-shadow: none;
}

/* 紧凑滑块验证 - 创意设计 */
.slider-compact-wrapper {
  margin-top: 10px;
  height: 36px;
}

.slider-compact-track {
  position: relative;
  height: 100%;
  background:
    linear-gradient(90deg,
      rgba(255, 107, 53, 0.05) 0%,
      rgba(255, 107, 53, 0.02) 50%,
      rgba(255, 107, 53, 0.05) 100%);
  border-radius: 18px;
  overflow: hidden;
  border: 1px solid rgba(255, 107, 53, 0.15);
  box-shadow:
    inset 0 1px 3px rgba(0, 0, 0, 0.4),
    0 1px 0 rgba(255, 255, 255, 0.03);
}

/* 动态条纹背景 */
.slider-compact-track::before {
  content: '';
  position: absolute;
  inset: 0;
  background: repeating-linear-gradient(
    90deg,
    transparent,
    transparent 8px,
    rgba(255, 107, 53, 0.03) 8px,
    rgba(255, 107, 53, 0.03) 16px
  );
  pointer-events: none;
}

.slider-compact-progress {
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  background: linear-gradient(90deg,
    rgba(255, 107, 53, 0.2) 0%,
    rgba(255, 140, 97, 0.35) 100%);
  border-radius: 18px;
  transition: width 0.05s linear;
  box-shadow: inset 0 0 20px rgba(255, 107, 53, 0.1);
}

/* 滑块光效 */
.slider-glow {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 60px;
  height: 60px;
  background: radial-gradient(circle,
    rgba(255, 107, 53, 0.3) 0%,
    rgba(255, 107, 53, 0.1) 40%,
    transparent 70%);
  border-radius: 50%;
  pointer-events: none;
  transition: left 0.05s linear;
  filter: blur(4px);
}

.slider-compact-handle {
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 32px;
  height: 32px;
  background: linear-gradient(145deg, #FF6B35 0%, #FF8C61 50%, #FF6B35 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: grab;
  color: #fff;
  z-index: 3;
  transition:
    transform 0.2s cubic-bezier(0.34, 1.56, 0.64, 1),
    box-shadow 0.2s ease;
  box-shadow:
    0 3px 10px rgba(255, 107, 53, 0.5),
    0 0 0 1px rgba(255, 255, 255, 0.2) inset,
    0 -2px 4px rgba(0, 0, 0, 0.1) inset;
}

.slider-compact-handle::before {
  content: '';
  position: absolute;
  inset: 3px;
  border-radius: 50%;
  background: linear-gradient(145deg,
    rgba(255, 255, 255, 0.3) 0%,
    transparent 50%,
    rgba(0, 0, 0, 0.1) 100%);
  pointer-events: none;
}

.slider-compact-handle:hover {
  transform: translateY(-50%) scale(1.1);
  box-shadow:
    0 5px 15px rgba(255, 107, 53, 0.6),
    0 0 0 1px rgba(255, 255, 255, 0.25) inset,
    0 -2px 4px rgba(0, 0, 0, 0.1) inset;
}

.slider-compact-handle.moving {
  cursor: grabbing;
  transform: translateY(-50%) scale(0.95);
  box-shadow:
    0 2px 6px rgba(255, 107, 53, 0.4),
    0 0 0 1px rgba(255, 255, 255, 0.15) inset,
    0 -2px 4px rgba(0, 0, 0, 0.1) inset;
}

.slider-compact-handle svg {
  filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.3));
  margin-left: 1px;
}

/* 紧凑滑块动画 */
.slider-compact-enter-active,
.slider-compact-leave-active {
  transition: all 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.slider-compact-enter-from {
  opacity: 0;
  transform: translateY(-8px) scaleX(0.95);
  max-height: 0;
}

.slider-compact-leave-to {
  opacity: 0;
  transform: translateY(-4px) scaleX(0.98);
  max-height: 0;
  margin-top: 0;
}

/* 角色选择器 */
.role-selector {
  display: flex;
  gap: 10px;
}

.role-btn {
  flex: 1;
  height: 40px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.05);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: rgba(255, 255, 255, 0.6);
  font-size: 13px;
}

.role-btn svg {
  color: rgba(255, 255, 255, 0.4);
}

.role-btn.active {
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C61 100%);
  border-color: transparent;
  color: #fff;
}

.role-btn.active svg {
  color: #fff;
}

.role-btn:not(.active):hover {
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.9);
}

/* 注册按钮 */
.login-submit-btn {
  height: 38px;
  border: none;
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C61 100%);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  border-radius: 10px;
  cursor: pointer;
  margin-top: 4px;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.login-submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(255, 107, 53, 0.4);
}

.login-submit-btn:disabled {
  opacity: 0.7;
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

/* 协议 */
.agreement-text {
  text-align: center;
  font-size: 10px;
  color: rgba(255, 255, 255, 0.4);
  margin-top: 8px;
  margin-bottom: 0;
}

.agreement-text a {
  color: #FF6B35;
  text-decoration: none;
}

.agreement-text a:hover {
  text-decoration: underline;
}

/* 紧凑版其他登录方式 */
.other-login-compact {
  margin-top: 8px;
  padding-top: 8px;
}

.other-login-compact .divider {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.other-login-compact .divider::before,
.other-login-compact .divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: rgba(255, 255, 255, 0.1);
}

.other-login-compact .divider span {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.35);
}

.social-login-compact {
  display: flex;
  justify-content: center;
  gap: 10px;
}

.social-btn-compact {
  width: 40px;
  height: 40px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  background: rgba(255, 255, 255, 0.05);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.social-btn-compact:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.social-btn-compact.wechat {
  color: #07C160;
  border-color: rgba(7, 193, 96, 0.3);
}

.social-btn-compact.wechat:hover {
  background: rgba(7, 193, 96, 0.15);
  border-color: rgba(7, 193, 96, 0.5);
  box-shadow: 0 4px 12px rgba(7, 193, 96, 0.2);
}

.social-btn-compact.alipay {
  color: #1677FF;
  border-color: rgba(22, 119, 255, 0.3);
}

.social-btn-compact.alipay:hover {
  background: rgba(22, 119, 255, 0.15);
  border-color: rgba(22, 119, 255, 0.5);
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.2);
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
  .login-modal-container {
    width: 100%;
    max-width: 420px;
    height: auto;
    max-height: 90vh;
    overflow-y: auto;
  }

  .login-modal-left {
    display: none;
  }

  .login-modal-right {
    padding: 32px 24px;
  }

  .right-header {
    margin-bottom: 24px;
  }

  .login-form {
    gap: 16px;
  }
}
</style>

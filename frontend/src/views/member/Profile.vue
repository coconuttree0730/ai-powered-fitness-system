<template>
  <div class="profile-page">
    <!-- 左侧导航栏 -->
    <div class="profile-sidebar">
      <div class="sidebar-menu">
        <div
          v-for="item in sidebarMenus"
          :key="item.key"
          :class="['menu-item', activeMenu === item.key ? 'active' : '']"
          @click="activeMenu = item.key"
        >
          <n-icon :size="18" class="menu-icon">
            <component :is="item.icon" />
          </n-icon>
          <span class="menu-label">{{ item.label }}</span>
        </div>
      </div>
    </div>

    <!-- 右侧内容区 -->
    <div class="profile-content">
      <!-- 账户设置 -->
      <div v-if="activeMenu === 'account'" class="content-section">
        <div class="section-header">
          <h2 class="section-title">账户设置</h2>
        </div>

        <!-- 头像区域 -->
        <div class="avatar-section">
          <div class="avatar-wrapper">
            <img
              v-if="userInfo.avatar"
              :src="userInfo.avatar"
              class="avatar-img"
              @error="$event.target.style.display='none'"
            />
            <div v-else class="avatar-placeholder">
              <span class="avatar-text">{{ usernameInitial }}</span>
            </div>
          </div>
          <div class="avatar-actions">
            <n-button type="primary" ghost @click="handleChangeAvatar">
              修改头像
            </n-button>
            <p class="avatar-tip">图片需大于 96×96 像素，支持 JPEG、PNG 和 SVG 格式</p>
          </div>
          <input
            ref="avatarInput"
            type="file"
            accept="image/jpeg,image/png,image/svg+xml"
            style="display: none"
            @change="handleAvatarUpload"
          />
        </div>

        <n-divider />

        <!-- 信息列表 -->
        <div class="info-list">
          <!-- 昵称 -->
          <div class="info-item">
            <div class="info-main">
              <span class="info-label">昵称</span>
              <div class="info-value-wrapper">
                <span v-if="!editingField.nickname" class="info-value">{{ userInfo.nickname || userInfo.username || '未设置' }}</span>
                <n-input
                  v-else
                  v-model:value="editForm.nickname"
                  size="small"
                  style="width: 200px"
                  placeholder="请输入昵称"
                  @blur="saveField('nickname')"
                  @keyup.enter="saveField('nickname')"
                />
              </div>
            </div>
            <n-button text type="primary" @click="toggleEdit('nickname')">
              {{ editingField.nickname ? '保存' : '编辑' }}
            </n-button>
          </div>

          <!-- 用户名 -->
          <div class="info-item">
            <div class="info-main">
              <span class="info-label">用户名</span>
              <div class="info-value-wrapper">
                <span v-if="!editingField.username" class="info-value">{{ userInfo.username || '未设置' }}</span>
                <div v-else class="username-edit-wrapper">
                  <n-input
                    v-model:value="editForm.username"
                    size="small"
                    style="width: 200px"
                    placeholder="请输入用户名"
                    @input="onUsernameInput"
                    @blur="saveField('username')"
                    @keyup.enter="saveField('username')"
                  />
                  <div v-if="usernameCheckStatus.checking" class="check-status">
                    <n-spin size="small" /> 检查中...
                  </div>
                  <div v-else-if="usernameCheckStatus.error" class="check-status error">
                    {{ usernameCheckStatus.error }}
                  </div>
                </div>
              </div>
            </div>
            <n-button text type="primary" @click="toggleEdit('username')">
              {{ editingField.username ? '保存' : '编辑' }}
            </n-button>
          </div>

          <!-- 用户ID -->
          <div class="info-item">
            <div class="info-main">
              <span class="info-label">用户ID</span>
              <span class="info-value">{{ userInfo.id || '34593544' }}</span>
            </div>
            <n-button text @click="copyUserId">
              复制
            </n-button>
          </div>

          <!-- 手机 -->
          <div class="info-item">
            <div class="info-main">
              <span class="info-label">手机</span>
              <div class="info-value-wrapper">
                <n-tag v-if="userInfo.phone" type="success" size="small">已绑定</n-tag>
                <n-tag v-else type="warning" size="small">未绑定</n-tag>
                <span class="info-value phone-value">{{ maskPhone(userInfo.phone) || '未绑定手机号' }}</span>
              </div>
            </div>
            <n-button text type="primary" @click="showBindPhoneModal = true">
              {{ userInfo.phone ? '更换' : '立即绑定' }}
            </n-button>
          </div>

          <!-- 微信 -->
          <div class="info-item">
            <div class="info-main">
              <span class="info-label">微信</span>
              <div class="info-value-wrapper">
                <n-tag :type="userInfo.wechat ? 'success' : 'warning'" size="small">
                  {{ userInfo.wechat ? '已绑定' : '未绑定' }}
                </n-tag>
                <span class="info-value">{{ userInfo.wechat || '未绑定微信' }}</span>
              </div>
            </div>
            <div class="info-actions">
              <n-button v-if="userInfo.wechat" text type="primary" @click="unbindWechat">
                解绑
              </n-button>
              <n-button v-else text type="primary" @click="bindWechat">
                立即绑定
              </n-button>
            </div>
          </div>

          <!-- 邮箱 -->
          <div class="info-item">
            <div class="info-main">
              <span class="info-label">邮箱</span>
              <div class="info-value-wrapper">
                <n-tag :type="userInfo.email ? 'success' : 'warning'" size="small">
                  {{ userInfo.email ? '已绑定' : '未绑定' }}
                </n-tag>
                <span class="info-value">{{ userInfo.email || '未绑定邮箱' }}</span>
              </div>
            </div>
            <n-button text type="primary" @click="showBindEmailModal = true">
              {{ userInfo.email ? '更换' : '立即绑定' }}
            </n-button>
          </div>

          <!-- 实名认证 -->
          <div class="info-item">
            <div class="info-main">
              <span class="info-label">实名认证</span>
              <div class="info-value-wrapper">
                <n-tag :type="userInfo.verified ? 'success' : 'default'" size="small">
                  {{ userInfo.verified ? '已认证' : '未认证' }}
                </n-tag>
                <span class="info-value">{{ userInfo.realName || '未完成实名认证' }}</span>
              </div>
            </div>
            <n-button text type="primary" @click="showVerifyModal = true">
              {{ userInfo.verified ? '查看' : '去认证' }}
            </n-button>
          </div>

          <!-- 登录密码 -->
          <div class="info-item">
            <div class="info-main">
              <span class="info-label">登录密码</span>
              <div class="info-value-wrapper">
                <n-tag type="success" size="small">已设置</n-tag>
                <span class="info-value">********</span>
              </div>
            </div>
            <n-button text type="primary" @click="showChangePasswordModal = true">
              修改密码
            </n-button>
          </div>
        </div>
      </div>

      <!-- 账户钱包 -->
      <div v-else-if="activeMenu === 'wallet'" class="content-section">
        <div class="section-header">
          <h2 class="section-title">账户钱包</h2>
        </div>
        <div class="wallet-overview">
          <div class="wallet-card">
            <div class="wallet-label">账户余额</div>
            <div class="wallet-amount">¥{{ walletInfo.balance || '0.00' }}</div>
            <n-button type="primary" @click="showRechargeModal = true">充值</n-button>
          </div>
          <div class="wallet-card points-card">
            <div class="wallet-label">积分余额</div>
            <div class="wallet-amount points">{{ walletInfo.points || '580' }}</div>
            <n-button @click="router.push('/member/store')">去使用</n-button>
          </div>
        </div>
        <n-divider />
        <div class="transaction-list">
          <h3 class="subsection-title">最近交易</h3>
          <n-empty v-if="!transactions.length" description="暂无交易记录" />
          <div v-else class="transaction-items">
            <div v-for="(item, index) in transactions" :key="index" class="transaction-item">
              <div class="transaction-info">
                <span class="transaction-title">{{ item.title }}</span>
                <span class="transaction-time">{{ item.time }}</span>
              </div>
              <span :class="['transaction-amount', item.type]">{{ item.amount }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 订单记录 -->
      <div v-else-if="activeMenu === 'orders'" class="content-section">
        <div class="section-header">
          <h2 class="section-title">订单记录</h2>
        </div>

        <n-spin :show="orderLoading">
          <n-tabs v-model:value="orderTypeTab" type="line" animated>
            <n-tab-pane name="membership" tab="会员卡订单" />
            <n-tab-pane name="product" tab="商品订单" />
            <n-tab-pane name="coach" tab="私教课订单" />
          </n-tabs>

          <n-empty v-if="currentOrderList.length === 0 && !orderLoading" description="暂无订单记录" />

          <div v-else class="order-cards">
            <div
              v-for="order in currentOrderList"
              :key="order.orderNo"
              class="order-card"
              :class="{ clickable: isCardClickable(order) }"
              @click="handleOrderCardClick(order)"
            >
              <!-- 会员卡订单卡片 -->
              <template v-if="orderTypeTab === 'membership'">
                <div class="card-left">
                  <div class="member-card-icon">
                    <n-icon size="28" color="#FF6B35" :component="CardIcon" />
                  </div>
                </div>
                <div class="card-mid">
                  <div class="card-title">{{ order.membershipExt?.cardName || '会员卡' }}</div>
                  <div class="card-meta">
                    <span v-if="order.membershipExt?.expireTime">有效期至 {{ formatDate(order.membershipExt.expireTime) }}</span>
                    <span>{{ order.payMethodLabel || '' }}</span>
                  </div>
                </div>
                <div class="card-right">
                  <div class="card-amount">¥{{ order.payAmount }}</div>
                  <n-tag :type="getOrderStatusType(order.status)" size="small" round>{{ order.statusLabel }}</n-tag>
                  <n-button v-if="canContinuePay(order)" type="primary" size="tiny" ghost @click.stop="continuePay(order)">
                    继续支付
                  </n-button>
                </div>
              </template>

              <!-- 商品订单卡片 -->
              <template v-if="orderTypeTab === 'product'">
                <div class="card-left">
                  <div class="product-thumb-placeholder">
                    <n-icon size="28" color="#999"><CartOutline /></n-icon>
                  </div>
                </div>
                <div class="card-mid">
                  <div class="card-title">{{ order.productExt?.productName || '商品' }}</div>
                  <div class="card-meta">
                    <span>×{{ order.productExt?.quantity || 1 }}</span>
                    <n-tag v-if="order.productExt?.pickupStatus"
                      :type="order.productExt.pickupStatus === 'PICKED' ? 'success' : 'warning'"
                      size="tiny" round>{{ order.productExt.pickupStatus === 'PICKED' ? '已取货' : '待取货' }}</n-tag>
                  </div>
                </div>
                <div class="card-right">
                  <div class="card-amount">¥{{ order.payAmount }}</div>
                  <n-tag :type="getOrderStatusType(order.status)" size="small" round>{{ order.statusLabel }}</n-tag>
                  <n-button v-if="canContinuePay(order)" type="primary" size="tiny" ghost @click.stop="continuePay(order)">
                    继续支付
                  </n-button>
                </div>
              </template>

              <!-- 私教课订单卡片 -->
              <template v-if="orderTypeTab === 'coach'">
                <div class="card-left">
                  <n-avatar round :size="40" :src="order.coachPackageExt?.coachAvatar">
                    {{ order.coachPackageExt?.coachName?.charAt(0) || '教' }}
                  </n-avatar>
                </div>
                <div class="card-mid">
                  <div class="card-title">{{ order.coachPackageExt?.coachName || '教练' }}</div>
                  <div class="card-meta">
                    <span>{{ order.coachPackageExt?.packageName || '私教套餐' }}</span>
                    <span v-if="order.coachPackageExt?.expireTime">有效期至 {{ formatDate(order.coachPackageExt.expireTime) }}</span>
                  </div>
                </div>
                <div class="card-right">
                  <div class="card-amount">¥{{ order.payAmount }}</div>
                  <n-tag :type="getOrderStatusType(order.status)" size="small" round>{{ order.statusLabel }}</n-tag>
                  <n-button v-if="canContinuePay(order)" type="primary" size="tiny" ghost @click.stop="continuePay(order)">
                    继续支付
                  </n-button>
                </div>
              </template>
            </div>
          </div>
        </n-spin>
      </div>

      <!-- 健身档案 -->
      <div v-else-if="activeMenu === 'fitness'" class="content-section">
        <div class="section-header">
          <h2 class="section-title">健身档案</h2>
        </div>
        <n-form :model="fitnessForm" label-placement="left" label-width="100" class="fitness-form">
          <n-form-item label="身高">
            <n-input-number v-model:value="fitnessForm.height" :min="50" :max="250">
              <template #suffix>cm</template>
            </n-input-number>
          </n-form-item>
          <n-form-item label="体重">
            <n-input-number v-model:value="fitnessForm.weight" :min="20" :max="300" :precision="1">
              <template #suffix>kg</template>
            </n-input-number>
          </n-form-item>
          <n-form-item label="年龄">
            <n-input-number v-model:value="fitnessForm.age" :min="1" :max="150" />
          </n-form-item>
          <n-form-item label="性别">
            <n-select v-model:value="fitnessForm.gender" :options="genderOptions" />
          </n-form-item>
          <n-form-item>
            <template #label>
              <span class="form-label-with-help">
                健身经验
                <n-popover trigger="click" placement="right" :show-arrow="false" class="help-popover">
                  <template #trigger>
                    <n-icon class="help-icon" :component="InformationCircleOutline" />
                  </template>
                  <div class="help-content">
                    <div class="help-title">健身经验说明</div>
                    <div class="help-item">
                      <n-tag size="small" type="success">初学者</n-tag>
                      <span>刚开始接触健身，基础动作需要学习</span>
                    </div>
                    <div class="help-item">
                      <n-tag size="small" type="warning">中级</n-tag>
                      <span>有一定训练基础，能独立完成标准动作</span>
                    </div>
                    <div class="help-item">
                      <n-tag size="small" type="error">高级</n-tag>
                      <span>训练经验丰富，追求更高强度和难度</span>
                    </div>
                  </div>
                </n-popover>
              </span>
            </template>
            <n-select v-model:value="fitnessForm.experience" :options="experienceOptions" />
          </n-form-item>
          <n-form-item>
            <template #label>
              <span class="form-label-with-help">
                健身目标
                <n-popover trigger="click" placement="right" :show-arrow="false" class="help-popover">
                  <template #trigger>
                    <n-icon class="help-icon" :component="InformationCircleOutline" />
                  </template>
                  <div class="help-content">
                    <div class="help-title">健身目标说明</div>
                    <div class="help-item">
                      <n-tag size="small" type="info">减脂</n-tag>
                      <span>降低体脂率，塑造苗条身材</span>
                    </div>
                    <div class="help-item">
                      <n-tag size="small" type="error">增肌</n-tag>
                      <span>增加肌肉量，提升力量和体型</span>
                    </div>
                    <div class="help-item">
                      <n-tag size="small" type="warning">塑形</n-tag>
                      <span>改善身体线条，提升整体美感</span>
                    </div>
                    <div class="help-item">
                      <n-tag size="small" type="success">增强体能</n-tag>
                      <span>提升心肺功能和运动表现</span>
                    </div>
                    <div class="help-item">
                      <n-tag size="small">保持健康</n-tag>
                      <span>维持健康状态，预防疾病</span>
                    </div>
                  </div>
                </n-popover>
              </span>
            </template>
            <n-select v-model:value="fitnessForm.fitnessGoal" :options="goalOptions" />
          </n-form-item>
          <n-form-item>
            <n-button type="primary" :loading="saving" @click="saveFitnessProfile">保存档案</n-button>
          </n-form-item>
        </n-form>
      </div>
    </div>

    <!-- 商品取货二维码弹窗 -->
    <n-modal v-model:show="showPickupQrModal" preset="card" style="width: 90%; max-width: 380px" :show-header="false">
      <div class="qr-content">
        <h3>取货码</h3>
        <p class="qr-hint">出示此二维码给工作人员核销取货</p>
        <div class="qr-wrapper">
          <canvas ref="qrCanvasRef" width="220" height="220"></canvas>
        </div>
        <div class="qr-info">
          <p>{{ pickupInfo.productName }}</p>
          <p>商品数量：{{ pickupInfo.quantity }}</p>
          <p>订单号：{{ pickupInfo.orderNo }}</p>
          <p>取货码：{{ pickupInfo.pickupCode }}</p>
        </div>
      </div>
    </n-modal>

    <!-- 绑定/更换手机弹窗 -->
    <n-modal v-model:show="showBindPhoneModal" :title="userInfo.phone ? '更换手机号' : '绑定手机号'" preset="card" style="width: 400px">
      <n-steps :current="phoneStep" size="small">
        <n-step title="验证原手机号" v-if="userInfo.phone" />
        <n-step title="绑定新手机号" />
      </n-steps>
      <n-divider />
      <!-- 步骤1: 验证原手机号 -->
      <n-form v-if="userInfo.phone && phoneStep === 1" :model="phoneForm">
        <n-form-item label="原手机号">
          <n-input :value="maskPhone(userInfo.phone)" disabled />
        </n-form-item>
        <n-form-item label="验证码">
          <n-input-group>
            <n-input v-model:value="phoneForm.oldCode" placeholder="请输入原手机号验证码" />
            <n-button :disabled="oldCountdown > 0" @click="sendOldPhoneCodeFn">
              {{ oldCountdown > 0 ? `${oldCountdown}s` : '获取验证码' }}
            </n-button>
          </n-input-group>
        </n-form-item>
      </n-form>
      <!-- 步骤2: 绑定新手机号 -->
      <n-form v-else :model="phoneForm" :rules="phoneRules">
        <n-form-item label="新手机号" path="phone">
          <n-input v-model:value="phoneForm.phone" placeholder="请输入新手机号" />
        </n-form-item>
        <n-form-item label="验证码" path="code">
          <n-input-group>
            <n-input v-model:value="phoneForm.code" placeholder="请输入验证码" />
            <n-button :disabled="countdown > 0" @click="sendNewPhoneCodeFn">
              {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
            </n-button>
          </n-input-group>
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="closePhoneModal">取消</n-button>
          <n-button v-if="userInfo.phone && phoneStep === 1" type="primary" @click="goToNextPhoneStep">
            下一步
          </n-button>
          <n-button v-else type="primary" :loading="bindingPhone" @click="confirmBindPhone">确认绑定</n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- 绑定邮箱弹窗 -->
    <n-modal v-model:show="showBindEmailModal" title="绑定邮箱" preset="card" style="width: 400px">
      <n-form :model="emailForm">
        <n-form-item label="邮箱地址">
          <n-input v-model:value="emailForm.email" placeholder="请输入邮箱地址" />
        </n-form-item>
        <n-form-item label="验证码">
          <n-input-group>
            <n-input v-model:value="emailForm.code" placeholder="请输入验证码" />
            <n-button :disabled="emailCountdown > 0" @click="sendEmailCodeFn">
              {{ emailCountdown > 0 ? `${emailCountdown}s` : '获取验证码' }}
            </n-button>
          </n-input-group>
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showBindEmailModal = false">取消</n-button>
          <n-button type="primary" @click="confirmBindEmail">确认绑定</n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- 实名认证弹窗 -->
    <n-modal v-model:show="showVerifyModal" title="实名认证" preset="card" style="width: 450px">
      <n-form :model="verifyForm">
        <n-form-item label="真实姓名">
          <n-input v-model:value="verifyForm.realName" placeholder="请输入真实姓名" />
        </n-form-item>
        <n-form-item label="身份证号">
          <n-input v-model:value="verifyForm.idCard" placeholder="请输入身份证号" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showVerifyModal = false">取消</n-button>
          <n-button type="primary" @click="confirmVerify">提交认证</n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- 充值弹窗 -->
    <n-modal v-model:show="showRechargeModal" title="账户充值" preset="card" style="width: 400px">
      <n-form :model="rechargeForm">
        <n-form-item label="充值金额">
          <n-input-number v-model:value="rechargeForm.amount" :min="1" :precision="2" style="width: 100%">
            <template #prefix>¥</template>
          </n-input-number>
        </n-form-item>
        <n-form-item label="支付方式">
          <n-radio-group v-model:value="rechargeForm.payment">
            <n-space>
              <n-radio value="alipay">支付宝</n-radio>
              <n-radio value="wechat">微信支付</n-radio>
            </n-space>
          </n-radio-group>
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showRechargeModal = false">取消</n-button>
          <n-button type="primary" @click="confirmRecharge">立即充值</n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- 修改密码弹窗 -->
    <n-modal v-model:show="showChangePasswordModal" title="修改密码" preset="card" style="width: 400px">
      <n-alert v-if="passwordDailyLimitReached" type="warning" style="margin-bottom: 16px;">
        今日短信验证码发送次数已达上限（5次），请明天再试
      </n-alert>

      <n-form ref="smsPasswordFormRef" :model="smsPasswordForm" :rules="smsPasswordRules">
        <n-form-item label="手机号">
          <n-input :value="maskPhone(userInfo.phone)" disabled />
        </n-form-item>
        <n-form-item label="验证码" path="smsCode">
          <n-input-group>
            <n-input
              v-model:value="smsPasswordForm.smsCode"
              placeholder="请输入短信验证码"
              :disabled="passwordDailyLimitReached"
            />
            <n-button
              :disabled="passwordSmsCountdown > 0 || passwordDailyLimitReached"
              @click="sendPasswordSmsCode"
            >
              <template v-if="passwordDailyLimitReached">
                今日已达上限
              </template>
              <template v-else-if="passwordSmsCountdown > 0">
                {{ passwordSmsCountdown }}s
              </template>
              <template v-else>
                获取验证码
              </template>
            </n-button>
          </n-input-group>
          <div v-if="!passwordDailyLimitReached && passwordRemainingDailyCount > 0" class="daily-limit-hint">
            今日还可发送 {{ passwordRemainingDailyCount }} 次
          </div>
        </n-form-item>
        <n-form-item label="新密码" path="newPassword">
          <n-input
            v-model:value="smsPasswordForm.newPassword"
            type="password"
            placeholder="请输入新密码（6-20位）"
            show-password-on="mousedown"
            :disabled="passwordDailyLimitReached"
          />
        </n-form-item>
        <n-form-item label="确认新密码" path="confirmPassword">
          <n-input
            v-model:value="smsPasswordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password-on="mousedown"
            :disabled="passwordDailyLimitReached"
          />
        </n-form-item>
      </n-form>

      <template #footer>
        <n-space justify="end">
          <n-button @click="showChangePasswordModal = false">取消</n-button>
          <n-button
            type="primary"
            :loading="changingPassword"
            :disabled="passwordDailyLimitReached"
            @click="confirmChangePassword"
          >
            确认修改
          </n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage, NTag } from 'naive-ui'
import { useAuthStore } from '@/stores/auth'
import {
  PersonOutline,
  WalletOutline,
  CartOutline,
  FitnessOutline,
  CopyOutline,
  CheckmarkCircleOutline,
  InformationCircleOutline,
  Card as CardIcon
} from '@vicons/ionicons5'
import { getProfile, updateProfile } from '@/api/plan'
import { getMyAllOrders, payUnifiedOrder, submitAlipayForm } from '@/api/membership'
import {
  clearPaymentMarker,
  isPaymentFinished,
  markPaymentStarted,
  readPaymentMarker
} from '@/utils/paymentMarker'
import {
  getCurrentUser,
  updateUsername,
  sendOldPhoneCode,
  sendNewPhoneCode,
  updatePhone,
  sendEmailCode,
  updateEmail,
  sendPasswordChangeCode,
  updatePasswordBySms,
  uploadAvatar,
  checkUsernameExists,
  updateNickname
} from '@/api/user'

const router = useRouter()
const message = useMessage()
const authStore = useAuthStore()

// 侧边栏菜单
const sidebarMenus = [
  { key: 'account', label: '账户设置', icon: PersonOutline },
  { key: 'wallet', label: '账户钱包', icon: WalletOutline },
  { key: 'orders', label: '订单记录', icon: CartOutline },
  { key: 'fitness', label: '健身档案', icon: FitnessOutline }
]
const activeMenu = ref('account')

// 用户信息
const userInfo = reactive({
  id: '',
  username: '',
  nickname: '',
  avatar: '',
  phone: '',
  email: '',
  wechat: '',
  realName: '',
  verified: false
})

const usernameInitial = computed(() => {
  return userInfo.username ? userInfo.username.charAt(0).toUpperCase() : 'U'
})

// 编辑状态
const editingField = reactive({
  nickname: false,
  username: false
})

const editForm = reactive({
  nickname: '',
  username: ''
})

// 用户名检查状态
const usernameCheckStatus = reactive({
  checking: false,
  exists: false,
  error: ''
})

// 弹窗显示状态
const showBindPhoneModal = ref(false)
const showBindEmailModal = ref(false)
const showVerifyModal = ref(false)
const showRechargeModal = ref(false)
const showChangePasswordModal = ref(false)

// 表单数据
const phoneForm = reactive({ phone: '', code: '', oldCode: '' })
const emailForm = reactive({ email: '', code: '' })
const verifyForm = reactive({ realName: '', idCard: '' })
const rechargeForm = reactive({ amount: 100, payment: 'alipay' })
// 短信验证码修改密码表单
const smsPasswordForm = reactive({
  smsCode: '',
  newPassword: '',
  confirmPassword: ''
})
const smsPasswordFormRef = ref(null)
const changingPassword = ref(false)
const passwordSmsCountdown = ref(0)
const passwordDailyLimitReached = ref(false)
const passwordRemainingDailyCount = ref(5)

// 手机绑定步骤
const phoneStep = ref(1)
const bindingPhone = ref(false)

// 倒计时
const countdown = ref(0)
const oldCountdown = ref(0)
const emailCountdown = ref(0)

// 钱包信息
const walletInfo = reactive({
  balance: '0.00',
  points: '580'
})

// 交易记录
const transactions = ref([])

// 健身档案
const fitnessForm = reactive({
  height: null,
  weight: null,
  age: null,
  gender: null,
  experience: null,
  fitnessGoal: null
})
const saving = ref(false)

const experienceOptions = [
  { label: '初学者', value: 'BEGINNER' },
  { label: '中级', value: 'INTERMEDIATE' },
  { label: '高级', value: 'ADVANCED' }
]

const genderOptions = [
  { label: '男', value: 'MALE' },
  { label: '女', value: 'FEMALE' }
]

const goalOptions = [
  { label: '减脂', value: 'WEIGHT_LOSS' },
  { label: '增肌', value: 'MUSCLE_GAIN' },
  { label: '塑形', value: 'BODY_SHAPING' },
  { label: '增强体能', value: 'ENDURANCE' },
  { label: '保持健康', value: 'HEALTH' }
]

const phoneRules = {
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

const smsPasswordRules = {
  smsCode: [
    { required: true, message: '请输入短信验证码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度应为6-20位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value) => {
        return value === smsPasswordForm.newPassword
      },
      message: '两次输入的密码不一致',
      trigger: 'blur'
    }
  ]
}

// 订单记录
const orderLoading = ref(false)
const orderTypeTab = ref('membership')
const showPickupQrModal = ref(false)
const qrCanvasRef = ref(null)
const pickupInfo = reactive({
  productName: '',
  orderNo: '',
  quantity: 1,
  pickupCode: ''
})

const allOrders = ref([])

const currentOrderList = computed(() => {
  let list = []
  switch (orderTypeTab.value) {
    case 'membership':
      list = allOrders.value.filter(o => o.orderType === 'MEMBERSHIP')
      return list.filter(o => {
        if (o.status !== 'PAID') return true
        const expireTime = o.membershipExt?.expireTime
        if (!expireTime) return true
        return new Date(expireTime) > new Date()
      })
    case 'product':
      return allOrders.value.filter(o => o.orderType === 'PRODUCT')
    case 'coach':
      return allOrders.value.filter(o => o.orderType === 'COACH_PACKAGE')
    default:
      return []
  }
})

function getOrderStatusType(status) {
  const map = {
    'PAID': 'info',
    'NOT_PICKED': 'warning',
    'PENDING': 'warning',
    'COMPLETED': 'success',
    'CANCELLED': 'default',
    'TIMEOUT': 'default'
  }
  return map[status] || 'default'
}

function isCardClickable(order) {
  return orderTypeTab.value === 'product'
    && ['NOT_PICKED', 'PAID'].includes(order.status)
    && order.productExt?.pickupCode
}

function canContinuePay(order) {
  return order?.status === 'PENDING'
}

async function continuePay(order) {
  if (!order?.orderNo) return

  try {
    message.info('开始支付，请在支付宝完成付款')
    const payData = await payUnifiedOrder(order.orderNo, 'ALIPAY')
    if (payData?.payForm) {
      markPaymentStarted(order)
      submitAlipayForm(payData.payForm)
    } else {
      message.error('获取支付信息失败')
    }
  } catch (error) {
    message.error(error.response?.data?.message || error.message || '支付请求失败，请重试')
  }
}

async function handleOrderCardClick(order) {
  if (!isCardClickable(order)) return
  pickupInfo.productName = order.productExt.productName
  pickupInfo.orderNo = order.orderNo
  pickupInfo.quantity = order.productExt.quantity || 1
  pickupInfo.pickupCode = order.productExt.pickupCode
  showPickupQrModal.value = true
  await nextTick()
  if (qrCanvasRef.value) {
    try {
      const QRCode = await import('qrcode')
      const data = JSON.stringify({
        type: 'PRODUCT_PICKUP',
        orderNo: order.orderNo,
        pickupCode: order.productExt.pickupCode,
        quantity: order.productExt.quantity || 1
      })
      QRCode.toCanvas(qrCanvasRef.value, data, { width: 220 })
    } catch (e) {
      console.error('生成二维码失败:', e)
    }
  }
}

function notifyPaymentCompletion(orders) {
  const marker = readPaymentMarker()
  if (!marker?.orderNo) return

  const paidOrder = orders.find(order =>
    order.orderNo === marker.orderNo && isPaymentFinished(order)
  )
  if (!paidOrder) return

  clearPaymentMarker()
  message.success('支付完成，订单状态已更新')
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit'
  })
}

async function loadOrders() {
  orderLoading.value = true
  try {
    const data = await getMyAllOrders()
    allOrders.value = data || []
    notifyPaymentCompletion(allOrders.value)
  } catch (error) {
    console.error('加载订单记录失败:', error)
  } finally {
    orderLoading.value = false
  }
}

// 初始化
onMounted(() => {
  // 获取最新用户信息
  fetchUserInfo()
  // 获取健身档案
  fetchFitnessProfile()
  // 获取订单记录
  loadOrders()
})

// 获取用户信息
async function fetchUserInfo() {
  try {
    const res = await getCurrentUser()
    if (res) {
      Object.assign(userInfo, res)
      // 同步更新 authStore
      authStore.userInfo = res
      localStorage.setItem('userInfo', JSON.stringify(res))
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

// 获取健身档案
async function fetchFitnessProfile() {
  try {
    const data = await getProfile()
    if (data) {
      Object.assign(fitnessForm, data)
    }
  } catch (error) {
    console.error('获取健身档案失败:', error)
  }
}

// 保存健身档案
async function saveFitnessProfile() {
  try {
    saving.value = true
    await updateProfile(fitnessForm)
    message.success('健身档案保存成功')
  } catch (error) {
    message.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 切换编辑状态
function toggleEdit(field) {
  if (editingField[field]) {
    saveField(field)
  } else {
    editingField[field] = true
    if (field === 'nickname') {
      editForm.nickname = userInfo.nickname || ''
    } else if (field === 'username') {
      editForm.username = userInfo.username || ''
      // 重置检查状态
      usernameCheckStatus.checking = false
      usernameCheckStatus.exists = false
      usernameCheckStatus.error = ''
    }
  }
}

// 用户名输入时的检查（防抖）
let checkTimeout = null
async function onUsernameInput() {
  const username = editForm.username.trim()

  // 重置状态
  usernameCheckStatus.error = ''
  usernameCheckStatus.exists = false

  // 如果为空或与当前用户名相同，不检查
  if (!username || username === userInfo.username) {
    usernameCheckStatus.checking = false
    return
  }

  // 清除之前的定时器
  if (checkTimeout) {
    clearTimeout(checkTimeout)
  }

  // 延迟检查，避免频繁请求
  usernameCheckStatus.checking = true
  checkTimeout = setTimeout(async () => {
    try {
      const res = await checkUsernameExists(username)
      if (res && res.exists) {
        usernameCheckStatus.exists = true
        usernameCheckStatus.error = '该用户名已被使用，请更换其他用户名'
      } else {
        usernameCheckStatus.exists = false
        usernameCheckStatus.error = ''
      }
    } catch (error) {
      console.error('检查用户名失败:', error)
    } finally {
      usernameCheckStatus.checking = false
    }
  }, 500)
}

// 保存字段
async function saveField(field) {
  if (field === 'nickname') {
    const nickname = editForm.nickname.trim()
    if (nickname && nickname !== userInfo.nickname) {
      try {
        const res = await updateNickname(nickname)
        if (res) {
          Object.assign(userInfo, res)
          // 同步更新 authStore
          authStore.userInfo = res
          // 同时更新两种存储，避免数据不一致
          localStorage.setItem('userInfo', JSON.stringify(res))
          sessionStorage.setItem('userInfo', JSON.stringify(res))
          message.success('昵称已更新')
        }
      } catch (error) {
        message.error(error.message || '昵称更新失败')
        // 恢复原昵称
        editForm.nickname = userInfo.nickname || ''
      }
    }
  } else if (field === 'username') {
    const username = editForm.username.trim()

    // 如果为空或与当前用户名相同，不保存
    if (!username || username === userInfo.username) {
      editingField[field] = false
      return
    }

    // 如果用户名已被占用，不保存
    if (usernameCheckStatus.exists) {
      message.error('该用户名已被使用，请更换其他用户名')
      return
    }

    try {
      const res = await updateUsername(username)
      if (res) {
        Object.assign(userInfo, res)
        // 同步更新 authStore
        authStore.userInfo = res
        // 同时更新两种存储，避免数据不一致
        localStorage.setItem('userInfo', JSON.stringify(res))
        sessionStorage.setItem('userInfo', JSON.stringify(res))
        message.success('用户名已更新')
      }
    } catch (error) {
      message.error(error.message || '用户名更新失败')
      // 恢复原用户名
      editForm.username = userInfo.username
    }
  }
  editingField[field] = false
}

// 复制用户ID
function copyUserId() {
  navigator.clipboard.writeText(userInfo.id || '34593544')
  message.success('用户ID已复制')
}

// 手机号脱敏
function maskPhone(phone) {
  if (!phone) return ''
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

// 头像上传
const avatarInput = ref(null)
const uploadingAvatar = ref(false)

function handleChangeAvatar() {
  avatarInput.value?.click()
}

async function handleAvatarUpload(e) {
  const file = e.target.files[0]
  if (!file) return

  // 验证文件类型
  const validTypes = ['image/jpeg', 'image/png', 'image/svg+xml']
  if (!validTypes.includes(file.type)) {
    message.error('请上传 JPEG、PNG 或 SVG 格式的图片')
    return
  }

  // 验证文件大小 (10MB)
  if (file.size > 10 * 1024 * 1024) {
    message.error('图片大小不能超过 10MB')
    return
  }

  // 创建 FormData
  const formData = new FormData()
  formData.append('file', file)

  try {
    uploadingAvatar.value = true
    // 调用后端接口上传头像
    const res = await uploadAvatar(formData)
    if (res) {
      // 更新本地用户信息
      Object.assign(userInfo, res)
      // 同步更新 authStore
      authStore.userInfo = res
      localStorage.setItem('userInfo', JSON.stringify(res))
      message.success('头像上传成功')
    }
  } catch (error) {
    message.error(error.message || '头像上传失败')
  } finally {
    uploadingAvatar.value = false
    // 清空 input 值，允许重复选择同一文件
    e.target.value = ''
  }
}

// 关闭手机弹窗
function closePhoneModal() {
  showBindPhoneModal.value = false
  phoneStep.value = 1
  phoneForm.phone = ''
  phoneForm.code = ''
  phoneForm.oldCode = ''
}

// 进入下一步
function goToNextPhoneStep() {
  if (!phoneForm.oldCode) {
    message.error('请输入原手机号验证码')
    return
  }
  phoneStep.value = 2
}

// 发送原手机号验证码
async function sendOldPhoneCodeFn() {
  try {
    const res = await sendOldPhoneCode()
    if (res && res.remainingSeconds) {
      oldCountdown.value = res.remainingSeconds
      const timer = setInterval(() => {
        oldCountdown.value--
        if (oldCountdown.value <= 0) {
          clearInterval(timer)
        }
      }, 1000)
    }
    message.success('验证码已发送到原手机号')
  } catch (error) {
    message.error(error.message || '验证码发送失败')
  }
}

// 发送新手机号验证码
async function sendNewPhoneCodeFn() {
  if (!phoneForm.phone) {
    message.error('请输入新手机号')
    return
  }
  // 简单验证手机号格式
  const phoneRegex = /^1[3-9]\d{9}$/
  if (!phoneRegex.test(phoneForm.phone)) {
    message.error('手机号格式不正确')
    return
  }
  try {
    const res = await sendNewPhoneCode(phoneForm.phone)
    if (res && res.remainingSeconds) {
      countdown.value = res.remainingSeconds
      const timer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
          clearInterval(timer)
        }
      }, 1000)
    }
    message.success('验证码已发送到新手机号')
  } catch (error) {
    message.error(error.message || '验证码发送失败')
  }
}

// 发送邮箱验证码
async function sendEmailCodeFn() {
  if (!emailForm.email) {
    message.error('请输入邮箱地址')
    return
  }
  // 简单验证邮箱格式
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(emailForm.email)) {
    message.error('邮箱格式不正确')
    return
  }
  try {
    const res = await sendEmailCode(emailForm.email)
    if (res && res.remainingSeconds) {
      emailCountdown.value = res.remainingSeconds
      const timer = setInterval(() => {
        emailCountdown.value--
        if (emailCountdown.value <= 0) {
          clearInterval(timer)
        }
      }, 1000)
    }
    message.success('验证码已发送到邮箱')
  } catch (error) {
    message.error(error.message || '验证码发送失败')
  }
}

// 确认绑定手机号
async function confirmBindPhone() {
  if (!phoneForm.phone) {
    message.error('请输入新手机号')
    return
  }
  if (!phoneForm.code) {
    message.error('请输入验证码')
    return
  }
  // 如果是更换手机号，需要验证原手机号验证码
  const oldCode = userInfo.phone ? phoneForm.oldCode : ''
  if (userInfo.phone && !oldCode) {
    message.error('请先完成原手机号验证')
    phoneStep.value = 1
    return
  }

  try {
    bindingPhone.value = true
    const res = await updatePhone({
      phone: phoneForm.phone,
      code: phoneForm.code,
      oldCode: oldCode
    })
    if (res) {
      Object.assign(userInfo, res)
      // 同步更新 authStore
      authStore.userInfo = res
      localStorage.setItem('userInfo', JSON.stringify(res))
      message.success(userInfo.phone ? '手机号更换成功' : '手机号绑定成功')
      closePhoneModal()
    }
  } catch (error) {
    message.error(error.message || '手机号绑定失败')
  } finally {
    bindingPhone.value = false
  }
}

// 确认绑定邮箱
async function confirmBindEmail() {
  if (!emailForm.email) {
    message.error('请输入邮箱地址')
    return
  }
  if (!emailForm.code) {
    message.error('请输入验证码')
    return
  }

  try {
    const res = await updateEmail({
      email: emailForm.email,
      code: emailForm.code
    })
    if (res) {
      Object.assign(userInfo, res)
      // 同步更新 authStore
      authStore.userInfo = res
      localStorage.setItem('userInfo', JSON.stringify(res))
      message.success(userInfo.email ? '邮箱更换成功' : '邮箱绑定成功')
      showBindEmailModal.value = false
      emailForm.email = ''
      emailForm.code = ''
    }
  } catch (error) {
    message.error(error.message || '邮箱绑定失败')
  }
}

// 微信绑定/解绑
function bindWechat() {
  message.info('请使用微信扫描二维码完成绑定')
}

function unbindWechat() {
  userInfo.wechat = ''
  message.success('微信已解绑')
}

// 实名认证
function confirmVerify() {
  if (!verifyForm.realName || !verifyForm.idCard) {
    message.error('请填写完整信息')
    return
  }
  userInfo.realName = verifyForm.realName
  userInfo.verified = true
  showVerifyModal.value = false
  message.success('实名认证提交成功，审核中')
}

// 充值
function confirmRecharge() {
  message.success(`成功充值 ¥${rechargeForm.amount}`)
  walletInfo.balance = (parseFloat(walletInfo.balance) + rechargeForm.amount).toFixed(2)
  showRechargeModal.value = false
}

// 发送修改密码短信验证码
async function sendPasswordSmsCode() {
  if (!userInfo.phone) {
    message.error('当前用户未绑定手机号')
    return
  }
  try {
    const res = await sendPasswordChangeCode()
    if (res && res.remainingSeconds) {
      passwordSmsCountdown.value = res.remainingSeconds
      const timer = setInterval(() => {
        passwordSmsCountdown.value--
        if (passwordSmsCountdown.value <= 0) {
          clearInterval(timer)
        }
      }, 1000)
    }
    // 更新剩余次数
    if (res && res.remainingDailyCount !== undefined) {
      passwordRemainingDailyCount.value = res.remainingDailyCount
      if (res.remainingDailyCount === 0) {
        passwordDailyLimitReached.value = true
      }
    }
    message.success('验证码已发送到您的手机号')
  } catch (error) {
    // 检查是否是每日限制错误
    if (error.code === 1013) {
      passwordDailyLimitReached.value = true
      passwordRemainingDailyCount.value = 0
    }
    message.error(error.message || '验证码发送失败')
  }
}

// 修改密码
async function confirmChangePassword() {
  try {
    changingPassword.value = true

    await smsPasswordFormRef.value?.validate()
    await updatePasswordBySms({
      smsCode: smsPasswordForm.smsCode,
      newPassword: smsPasswordForm.newPassword
    })

    message.success('密码修改成功')
    showChangePasswordModal.value = false
    // 清空表单
    smsPasswordForm.smsCode = ''
    smsPasswordForm.newPassword = ''
    smsPasswordForm.confirmPassword = ''
  } catch (error) {
    message.error(error.message || '密码修改失败')
  } finally {
    changingPassword.value = false
  }
}
</script>

<style scoped>
.profile-page {
  display: flex;
  min-height: calc(100vh - 94px);
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

/* 左侧导航栏 */
.profile-sidebar {
  width: 220px;
  background: #fafbfc;
  border-right: 1px solid #e8e8e8;
  padding: 20px 0;
  flex-shrink: 0;
}

.sidebar-menu {
  padding: 0 12px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  margin: 4px 0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #666;
}

.menu-item:hover {
  background: rgba(255, 107, 53, 0.08);
  color: #FF6B35;
}

.menu-item.active {
  background: linear-gradient(90deg, #FF6B35, #E55A2B);
  color: white;
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.3);
}

.menu-icon {
  flex-shrink: 0;
}

.menu-label {
  font-size: 14px;
  font-weight: 500;
}

/* 右侧内容区 */
.profile-content {
  flex: 1;
  padding: 32px 40px;
  overflow-y: auto;
}

.content-section {
  max-width: 800px;
}

.section-header {
  margin-bottom: 24px;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0;
}

.subsection-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 16px 0;
}

/* 头像区域 */
.avatar-section {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 8px 0;
}

.avatar-wrapper {
  position: relative;
}

.avatar-img,
.avatar-placeholder {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-placeholder {
  background: linear-gradient(135deg, #FF6B35, #FF8C61);
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-text {
  font-size: 32px;
  font-weight: 600;
  color: white;
}

.avatar-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.avatar-tip {
  font-size: 12px;
  color: #999;
  margin: 0;
}

/* 信息列表 */
.info-list {
  padding: 8px 0;
}

.info-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-item:last-child {
  border-bottom: none;
}

.info-main {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-label {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.info-value-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.username-edit-wrapper {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.check-status {
  font-size: 12px;
  color: #666;
  display: flex;
  align-items: center;
  gap: 4px;
}

.check-status.error {
  color: #ff4d4f;
}

.info-value {
  font-size: 14px;
  color: #666;
}

.phone-value {
  font-family: monospace;
}

.info-actions {
  display: flex;
  gap: 16px;
}

/* 钱包概览 */
.wallet-overview {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.wallet-card {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 12px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.points-card {
  background: linear-gradient(135deg, #fff8e1 0%, #ffecb3 100%);
}

.wallet-label {
  font-size: 14px;
  color: #666;
}

.wallet-amount {
  font-size: 32px;
  font-weight: 700;
  color: #1a1a2e;
}

.wallet-amount.points {
  color: #FF6B35;
}

/* 交易记录 */
.transaction-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.transaction-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  background: #fafbfc;
  border-radius: 8px;
}

.transaction-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.transaction-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.transaction-time {
  font-size: 12px;
  color: #999;
}

.transaction-amount {
  font-size: 16px;
  font-weight: 600;
}

.transaction-amount.income {
  color: #52c41a;
}

.transaction-amount.expense {
  color: #ff4d4f;
}

/* 健身档案表单 */
.fitness-form {
  max-width: 500px;
}

/* 表单标签帮助图标 */
.form-label-with-help {
  display: flex;
  align-items: center;
  gap: 6px;
}

.help-icon {
  font-size: 16px;
  color: #999;
  cursor: pointer;
  transition: color 0.3s;
}

.help-icon:hover {
  color: #FF6B35;
}

/* 帮助内容样式 */
.help-content {
  padding: 12px;
  min-width: 240px;
  background: #fff;
  border-radius: 8px;
}

:deep(.n-tooltip) {
  background: #fff !important;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15) !important;
  border: none !important;
}

:deep(.n-tooltip .n-tooltip-content) {
  background: #fff !important;
  color: #333;
  border: none !important;
}

:deep(.n-tooltip-arrow) {
  background: #fff !important;
  border: none !important;
}

:deep(.n-popover) {
  background: #fff !important;
  border: none !important;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15) !important;
}

:deep(.n-popover-shared) {
  background: #fff !important;
  border: none !important;
}

:deep(.n-popover-shared__popover) {
  background: #fff !important;
  border: none !important;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15) !important;
}

:deep(.n-popover-content) {
  background: #fff !important;
  border: none !important;
}

:deep(.n-popover-body) {
  background: #fff !important;
  border: none !important;
}

:deep(.help-popover) {
  background: #fff !important;
  border: none !important;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15) !important;
}

:deep(.help-popover .n-popover-content) {
  background: #fff !important;
  border: none !important;
}

:deep(.help-popover.n-popover) {
  background: #fff !important;
  border: none !important;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15) !important;
}

:deep(.n-popover, .n-popover *) {
  border: none !important;
  outline: none !important;
}

:deep(.n-popover > div) {
  border: none !important;
  background: #fff !important;
}

:deep([role="tooltip"]) {
  border: none !important;
  background: #fff !important;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15) !important;
}

.help-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #eee;
}

.help-item {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
  font-size: 13px;
  color: #666;
}

.help-item:last-child {
  margin-bottom: 0;
}

.help-item span {
  flex: 1;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .profile-page {
    flex-direction: column;
  }

  .profile-sidebar {
    width: 100%;
    border-right: none;
    border-bottom: 1px solid #e8e8e8;
    padding: 12px;
  }

  .sidebar-menu {
    display: flex;
    gap: 8px;
    padding: 0;
    overflow-x: auto;
  }

  .menu-item {
    white-space: nowrap;
    margin: 0;
  }

  .profile-content {
    padding: 20px;
  }

  .avatar-section {
    flex-direction: column;
    align-items: flex-start;
  }

  .wallet-overview {
    grid-template-columns: 1fr;
  }

  .info-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .info-actions {
    width: 100%;
    justify-content: flex-end;
  }
}

/* 每日限制提示 */
.daily-limit-hint {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

/* ===== 订单卡片 ===== */
.order-cards {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 16px;
}

.order-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  background: #fafbfc;
  border-radius: 12px;
  border: 1px solid #f0f0f0;
  transition: all 0.3s ease;
}

.order-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  border-color: #e0e0e0;
}

.order-card.clickable {
  cursor: pointer;
}

.order-card.clickable:hover {
  border-color: #FF6B35;
  background: #fff8f6;
}

.card-left {
  flex-shrink: 0;
}

.card-mid {
  flex: 1;
  min-width: 0;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 4px;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 12px;
  color: #999;
}

.card-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
  flex-shrink: 0;
}

.card-amount {
  font-size: 18px;
  font-weight: 700;
  color: #FF6B35;
}

.product-thumb {
  width: 64px;
  height: 64px;
  border-radius: 10px;
  object-fit: cover;
}

.product-thumb-placeholder {
  width: 64px;
  height: 64px;
  border-radius: 10px;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.member-card-icon {
  width: 64px;
  height: 64px;
  border-radius: 10px;
  background: linear-gradient(135deg, #fff5f2, #ffe8e0);
  display: flex;
  align-items: center;
  justify-content: center;
}

.qr-content {
  text-align: center;
  padding: 16px;
}

.qr-content h3 {
  font-size: 20px;
  font-weight: 700;
  color: #1A1A2E;
  margin: 0 0 8px 0;
}

.qr-hint {
  font-size: 14px;
  color: #6B7280;
  margin: 0 0 24px 0;
}

.qr-wrapper {
  background: white;
  padding: 20px;
  border-radius: 16px;
  display: inline-block;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  margin-bottom: 20px;
}

.qr-info p {
  font-size: 14px;
  color: #6B7280;
  margin: 4px 0;
}

@media (max-width: 768px) {
  .order-card {
    padding: 14px 16px;
    gap: 12px;
  }
  .card-amount {
    font-size: 16px;
  }
}
</style>

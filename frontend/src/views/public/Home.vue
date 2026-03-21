<template>
  <div class="home-page">
    <!-- 公告栏 -->
    <div v-if="showAnnouncement" class="announcement-bar">
      <div class="announcement-bar-content">
        <span>🎉</span>
        <span>新用户注册即送7天VIP会员体验！</span>
        <a href="#cta" @click.prevent="scrollToSection('cta')">立即领取 →</a>
      </div>
      <button class="announcement-bar-close" @click="showAnnouncement = false">×</button>
    </div>

    <!-- 导航栏 -->
    <nav :class="['navbar', { scrolled: isScrolled, 'has-announcement': showAnnouncement }]">
      <div class="navbar-container">
        <router-link to="/" class="logo">
          <div class="logo-icon">💪</div>
          <div class="logo-text">智健<span>AI</span></div>
        </router-link>
        <div class="nav-links">
          <a href="#home" :class="{ active: activeSection === 'home' }" @click.prevent="scrollToSection('home')">首页</a>
          <a href="#philosophy" :class="{ active: activeSection === 'philosophy' }" @click.prevent="scrollToSection('philosophy')">品牌理念</a>
          <a href="#ai-features" :class="{ active: activeSection === 'ai-features' }" @click.prevent="scrollToSection('ai-features')">AI功能</a>
          <a href="#courses" :class="{ active: activeSection === 'courses' }" @click.prevent="scrollToSection('courses')">课程体系</a>
          <a href="#membership" :class="{ active: activeSection === 'membership' }" @click.prevent="scrollToSection('membership')">会员卡</a>
          <a href="#coaches" :class="{ active: activeSection === 'coaches' }" @click.prevent="scrollToSection('coaches')">教练团队</a>
        </div>
        <div class="nav-actions">
          <template v-if="!authStore.isLoggedIn">
            <a href="#" class="nav-link" @click.prevent="showLoginModal = true">登录</a>
            <button class="btn btn-primary btn-glow" @click="goToRegister">免费体验</button>
          </template>
          <template v-else>
            <router-link v-if="authStore.isAdmin" to="/admin" class="nav-link">管理后台</router-link>
            <router-link v-else-if="authStore.isCoach" to="/coach" class="nav-link">教练中心</router-link>
            <router-link v-else-if="authStore.isMember" to="/member" class="nav-link">会员中心</router-link>
          </template>
          <button class="mobile-menu-btn" :class="{ active: mobileMenuOpen }" @click="mobileMenuOpen = !mobileMenuOpen">
            <span></span>
            <span></span>
            <span></span>
          </button>
        </div>
      </div>
    </nav>

    <!-- 移动端菜单 -->
    <div class="mobile-menu" :class="{ active: mobileMenuOpen }">
      <a href="#home" @click.prevent="scrollToSection('home'); mobileMenuOpen = false">首页</a>
      <a href="#philosophy" @click.prevent="scrollToSection('philosophy'); mobileMenuOpen = false">品牌理念</a>
      <a href="#ai-features" @click.prevent="scrollToSection('ai-features'); mobileMenuOpen = false">AI功能</a>
      <a href="#courses" @click.prevent="scrollToSection('courses'); mobileMenuOpen = false">课程体系</a>
      <a href="#membership" @click.prevent="scrollToSection('membership'); mobileMenuOpen = false">会员卡</a>
      <a href="#coaches" @click.prevent="scrollToSection('coaches'); mobileMenuOpen = false">教练团队</a>
      <button v-if="!authStore.isLoggedIn" class="btn btn-primary" @click="showLoginModal = true; mobileMenuOpen = false">登录</button>
      <button v-if="!authStore.isLoggedIn" class="btn btn-primary" @click="goToRegister; mobileMenuOpen = false">免费体验</button>
    </div>

    <!-- Hero区域 -->
    <section id="home" class="hero-section" :class="{ 'has-announcement': showAnnouncement }">
      <div class="hero-bg"></div>
      <div class="hero-grid"></div>
      <div class="hero-particles">
        <div v-for="n in 20" :key="n" class="particle" :style="getParticleStyle(n)"></div>
      </div>
      <div class="hero-container">
        <div class="hero-content">
          <div class="hero-text">
            <div class="hero-badge"><span>🚀</span> AI智能健身新时代</div>
            <h1 class="hero-title">让AI成为你的<br><span>私人健身专家</span></h1>
            <p class="hero-desc">智健AI运用先进的人工智能技术，为您量身定制科学训练计划。无论是减脂塑形、增肌力量还是康复训练，我们让每一次锻炼都精准高效，助您快速达成健身目标。</p>
            <div class="hero-actions">
              <button class="btn btn-primary btn-large" @click="goToRegister">开启智能健身之旅</button>
              <a href="#ai-features" class="btn btn-outline btn-large" @click.prevent="scrollToSection('ai-features')">探索AI功能</a>
            </div>
            <div class="hero-stats-preview">
              <div class="hero-stat-item">
                <div class="hero-stat-value">{{ animatedStats.members }}+</div>
                <div class="hero-stat-label">活跃会员</div>
              </div>
              <div class="hero-stat-item">
                <div class="hero-stat-value">{{ animatedStats.satisfaction }}%</div>
                <div class="hero-stat-label">满意度</div>
              </div>
              <div class="hero-stat-item">
                <div class="hero-stat-value">{{ animatedStats.coaches }}+</div>
                <div class="hero-stat-label">专业教练</div>
              </div>
            </div>
          </div>
          <div class="hero-visual">
            <div class="hero-carousel">
              <div class="hero-carousel-track" :style="{ transform: `translateX(-${currentSlide * 100}%)` }">
                <div v-for="(slide, index) in heroSlides" :key="index" class="hero-carousel-slide">
                  <img :src="slide.image" :alt="slide.alt">
                  <div class="hero-carousel-overlay"></div>
                </div>
              </div>
              <button class="hero-carousel-nav prev" @click="prevSlide">‹</button>
              <button class="hero-carousel-nav next" @click="nextSlide">›</button>
              <div class="hero-carousel-indicators">
                <button v-for="(slide, index) in heroSlides" :key="index" 
                        :class="['hero-carousel-dot', { active: currentSlide === index }]" 
                        @click="goToSlide(index)"></button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 醒目滚动公告栏 -->
    <section v-if="showMarquee" class="marquee-notice-section">
      <div class="marquee-notice-container">
        <div class="marquee-notice-label">重要通知</div>
        <div class="marquee-notice-wrapper">
          <div class="marquee-notice-track">
            <span v-for="(item, index) in marqueeItems" :key="index" class="marquee-notice-item" v-html="item"></span>
          </div>
        </div>
        <button class="marquee-notice-close" @click="showMarquee = false" title="关闭通知">×</button>
      </div>
    </section>

    <!-- 统计数据 -->
    <section class="stats-section">
      <div class="stats-container">
        <div class="stats-grid">
          <div v-for="(stat, index) in stats" :key="index" class="stat-item" v-intersect="onStatIntersect">
            <div class="stat-icon">{{ stat.icon }}</div>
            <div class="stat-value">{{ formatNumber(stat.value) }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </div>
      </div>
    </section>

    <!-- 品牌理念 -->
    <section id="philosophy" class="section philosophy-section">
      <div class="philosophy-container">
        <div class="philosophy-grid">
          <div class="philosophy-content" v-intersect="onReveal">
            <div class="section-tag">Our Philosophy</div>
            <h2 class="section-title">科技赋能<br><span>让健身更智能</span></h2>
            <p class="philosophy-quote">"我们相信，每个人都值得拥有个性化的健身体验。"</p>
            <p class="philosophy-text">智健AI诞生于对健身行业痛点的深度思考。传统健身往往面临计划千篇一律、效果难以追踪、动力容易消退等问题。我们融合人工智能技术与专业健身知识，打造真正懂你的智能健身伙伴。</p>
            <div class="philosophy-values">
              <div v-for="(value, index) in philosophyValues" :key="index" class="value-item">
                <div class="value-icon">{{ value.icon }}</div>
                <div class="value-content">
                  <h4>{{ value.title }}</h4>
                  <p>{{ value.desc }}</p>
                </div>
              </div>
            </div>
          </div>
          <div class="philosophy-visual" v-intersect="onReveal">
            <div class="philosophy-image">
              <img src="https://images.unsplash.com/photo-1571902943202-507ec2618e8f?w=800&h=600&fit=crop" alt="健身房环境">
            </div>
            <div class="philosophy-experience">
              <div class="experience-number">8+</div>
              <div class="experience-text">年行业深耕经验</div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- AI特色功能区 -->
    <section id="ai-features" class="section ai-features-section">
      <div class="ai-features-container">
        <div class="section-header" v-intersect="onReveal">
          <div class="section-tag">AI Powered</div>
          <h2 class="section-title">四大核心AI功能<br><span>重新定义智能健身</span></h2>
          <p class="section-desc">融合前沿AI技术与专业健身知识，为您打造全方位的智能健身体验</p>
        </div>
        <div class="ai-features-grid">
          <div v-for="(feature, index) in aiFeatures" :key="index" 
               class="ai-feature-card" 
               :class="`reveal-delay-${index}`"
               v-intersect="onReveal">
            <div class="ai-feature-icon">{{ feature.icon }}</div>
            <h3 class="ai-feature-title">{{ feature.title }}</h3>
            <p class="ai-feature-desc">{{ feature.desc }}</p>
            <ul class="ai-feature-list">
              <li v-for="(item, i) in feature.items" :key="i">{{ item }}</li>
            </ul>
          </div>
        </div>
      </div>
    </section>

    <!-- 课程体系 -->
    <section id="courses" class="section courses-section">
      <div class="courses-container">
        <div class="section-header" v-intersect="onReveal">
          <div class="section-tag">Courses</div>
          <h2 class="section-title">多元化课程体系<br><span>满足全场景健身需求</span></h2>
          <p class="section-desc">从入门到专业，从团体到私教，总有一款课程适合您</p>
        </div>
        <div class="courses-tabs">
          <button v-for="tab in courseTabs" :key="tab.key"
                  :class="['course-tab', { active: activeCourseTab === tab.key }]"
                  @click="activeCourseTab = tab.key">
            {{ tab.label }}
          </button>
        </div>
        <div class="courses-grid">
          <div v-for="(course, index) in filteredCourses" :key="index" 
               class="course-card" 
               v-intersect="onReveal">
            <div class="course-image">
              <img :src="course.image" :alt="course.name">
              <div class="course-overlay">
                <span class="course-level">{{ course.level }}</span>
                <span class="course-duration">⏱ {{ course.duration }}</span>
              </div>
            </div>
            <div class="course-content">
              <h3 class="course-name">{{ course.name }}</h3>
              <p class="course-desc">{{ course.desc }}</p>
              <div class="course-meta">
                <span class="course-calories">🔥 {{ course.calories }}</span>
                <span class="course-bookings">{{ course.bookings }}人已预约</span>
              </div>
            </div>
          </div>
        </div>
        <div class="courses-more">
          <router-link to="/courses" class="btn btn-outline">查看全部课程</router-link>
        </div>
      </div>
    </section>

    <!-- 会员方案 -->
    <section id="membership" class="section membership-section">
      <div class="membership-container">
        <div class="section-header" v-intersect="onReveal">
          <div class="section-tag">Membership</div>
          <h2 class="section-title">灵活会员方案<br><span>选择适合您的健身方式</span></h2>
          <p class="section-desc">透明定价，无隐藏费用，随时升级或取消</p>
        </div>
        <div class="membership-slider">
          <button class="membership-slider-btn prev" @click="prevMembershipSlide">‹</button>
          <div class="membership-slider-track">
            <div class="membership-slider-content" :style="{ transform: `translateX(-${membershipSlide * 33.333}%)` }">
              <div v-for="(plan, index) in membershipPlans" :key="index" 
                   class="membership-slide"
                   :class="{ featured: plan.featured }">
                <div class="membership-card">
                  <div v-if="plan.badge" class="membership-badge">{{ plan.badge }}</div>
                  <h3 class="membership-name">{{ plan.name }}</h3>
                  <div class="membership-price">
                    <span class="currency">¥</span>
                    <span class="amount">{{ plan.price }}</span>
                    <span class="period">/{{ plan.period }}</span>
                  </div>
                  <p class="membership-desc">{{ plan.desc }}</p>
                  <ul class="membership-features">
                    <li v-for="(feature, i) in plan.features" :key="i" :class="{ included: feature.included }">
                      {{ feature.included ? '✓' : '×' }} {{ feature.text }}
                    </li>
                  </ul>
                  <button :class="['btn', plan.featured ? 'btn-primary' : 'btn-outline', 'btn-block']" 
                          @click="goToRegister">
                    {{ plan.featured ? '立即开通' : '选择方案' }}
                  </button>
                </div>
              </div>
            </div>
          </div>
          <button class="membership-slider-btn next" @click="nextMembershipSlide">›</button>
        </div>
      </div>
    </section>

    <!-- 教练团队 -->
    <section id="coaches" class="section coaches-section">
      <div class="coaches-container">
        <div class="section-header" v-intersect="onReveal">
          <div class="section-tag">Coaches</div>
          <h2 class="section-title">专业教练团队<br><span>引领您的健身之路</span></h2>
          <p class="section-desc">每一位教练都经过严格筛选与专业认证</p>
        </div>
        <div class="coaches-slider">
          <button class="coaches-slider-btn prev" @click="prevCoachSlide">‹</button>
          <div class="coaches-slider-track">
            <div class="coaches-slider-content" :style="{ transform: `translateX(-${coachSlide * 25}%)` }">
              <div v-for="(coach, index) in coaches" :key="index" class="coaches-slide">
                <div class="coach-card">
                  <div class="coach-image">
                    <img :src="coach.image" :alt="coach.name">
                    <div class="coach-overlay">
                      <div class="coach-stats">
                        <div class="coach-stat">
                          <div class="coach-stat-value">{{ coach.experience }}</div>
                          <div class="coach-stat-label">年经验</div>
                        </div>
                        <div class="coach-stat">
                          <div class="coach-stat-value">{{ coach.students }}</div>
                          <div class="coach-stat-label">学员</div>
                        </div>
                        <div class="coach-stat">
                          <div class="coach-stat-value">{{ coach.rating }}</div>
                          <div class="coach-stat-label">好评</div>
                        </div>
                      </div>
                    </div>
                  </div>
                  <div class="coach-info">
                    <h3 class="coach-name">{{ coach.name }}</h3>
                    <div class="coach-title">{{ coach.title }}</div>
                    <div class="coach-tags">
                      <span v-for="(tag, i) in coach.tags" :key="i" class="coach-tag">{{ tag }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <button class="coaches-slider-btn next" @click="nextCoachSlide">›</button>
        </div>
      </div>
    </section>

    <!-- 成功案例 -->
    <section class="section testimonials-section">
      <div class="testimonials-container">
        <div class="section-header" v-intersect="onReveal">
          <div class="section-tag">Success Stories</div>
          <h2 class="section-title">会员成功案例<br><span>见证蜕变的力量</span></h2>
          <p class="section-desc">他们的改变，是我们最好的证明</p>
        </div>
        <div class="testimonials-carousel">
          <div class="testimonials-track" :style="{ transform: `translateX(-${testimonialSlide * 100}%)` }">
            <div v-for="(testimonial, index) in testimonials" :key="index" class="testimonial-card" v-intersect="onReveal">
              <div class="testimonial-header">
                <img :src="testimonial.avatar" :alt="testimonial.name" class="testimonial-avatar">
                <div class="testimonial-info">
                  <h4>{{ testimonial.name }}</h4>
                  <p>{{ testimonial.occupation }} · {{ testimonial.duration }}</p>
                </div>
                <div class="testimonial-transformation">
                  <div class="transformation-item">
                    <div class="transformation-value">{{ testimonial.before }}</div>
                    <div class="transformation-label">{{ testimonial.beforeLabel }}</div>
                  </div>
                  <div class="transformation-arrow">→</div>
                  <div class="transformation-item">
                    <div class="transformation-value">{{ testimonial.after }}</div>
                    <div class="transformation-label">{{ testimonial.afterLabel }}</div>
                  </div>
                </div>
              </div>
              <p class="testimonial-quote">"{{ testimonial.quote }}"</p>
            </div>
          </div>
        </div>
        <div class="testimonials-nav">
          <button @click="prevTestimonial">←</button>
          <button @click="nextTestimonial">→</button>
        </div>
      </div>
    </section>

    <!-- 健身设备 -->
    <section class="section equipment-section">
      <div class="equipment-container">
        <div class="section-header" v-intersect="onReveal">
          <div class="section-tag">Equipment</div>
          <h2 class="section-title">顶级健身设备<br><span>专业级训练体验</span></h2>
          <p class="section-desc">引进国际顶级健身器械，为您提供专业级的训练体验</p>
        </div>
        <div class="equipment-grid">
          <div class="equipment-main" v-intersect="onReveal">
            <img src="https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=800&h=600&fit=crop" alt="力量区">
            <div class="equipment-info">
              <h3>力量训练区</h3>
              <p>配备Hammer Strength、Life Fitness等国际顶级力量训练设备，满足从入门到专业运动员的全方位训练需求。</p>
            </div>
          </div>
          <div class="equipment-list">
            <div v-for="(item, index) in equipmentList" :key="index" 
                 class="equipment-item" 
                 :class="{ active: activeEquipment === index }"
                 @click="activeEquipment = index"
                 v-intersect="onReveal">
              <div class="equipment-item-image">
                <img :src="item.image" :alt="item.name">
              </div>
              <div class="equipment-item-content">
                <h4>{{ item.name }}</h4>
                <p>{{ item.desc }}</p>
                <div class="equipment-item-meta">
                  <span v-for="(meta, i) in item.meta" :key="i">{{ meta }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 线上线下结合 -->
    <section class="section omnichannel-section">
      <div class="omnichannel-container">
        <div class="omnichannel-grid">
          <div class="omnichannel-content" v-intersect="onReveal">
            <div class="section-tag">Omnichannel</div>
            <h2 class="section-title">线上线下<br><span>无缝健身体验</span></h2>
            <p class="section-desc">智健AI APP与线下健身房完美结合，让健身不受时间地点限制</p>
            <div class="omnichannel-features">
              <div v-for="(feature, index) in omnichannelFeatures" :key="index" class="omnichannel-feature">
                <div class="omnichannel-feature-icon">{{ feature.icon }}</div>
                <div class="omnichannel-feature-content">
                  <h4>{{ feature.title }}</h4>
                  <p>{{ feature.desc }}</p>
                </div>
              </div>
            </div>
          </div>
          <div class="omnichannel-visual" v-intersect="onReveal">
            <div class="phone-mockup">
              <div class="phone-frame">
                <div class="phone-notch"></div>
                <div class="phone-screen">
                  <img src="https://images.unsplash.com/photo-1616348436168-de43ad0db179?w=400&h=800&fit=crop" alt="APP界面">
                </div>
              </div>
              <div class="app-floating-card card-left">
                <div class="app-card-header">
                  <div class="app-card-icon">🔥</div>
                  <div class="app-card-title">今日消耗</div>
                </div>
                <div class="app-card-value">486 kcal</div>
              </div>
              <div class="app-floating-card card-right">
                <div class="app-card-header">
                  <div class="app-card-icon">⏱️</div>
                  <div class="app-card-title">训练时长</div>
                </div>
                <div class="app-card-value">65 分钟</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA区域 -->
    <section id="cta" class="section cta-section">
      <div class="cta-container">
        <h2 class="cta-title" v-intersect="onReveal">开启您的<span>智能健身之旅</span></h2>
        <p class="cta-desc" v-intersect="onReveal">立即注册，免费体验7天VIP会员服务，感受AI智能健身的魅力</p>
        <form class="cta-form" v-intersect="onReveal" @submit.prevent="handleCTASubmit">
          <input 
            ref="ctaInputRef"
            v-model="ctaPhone" 
            type="tel" 
            class="cta-input" 
            placeholder="请输入您的手机号" 
            maxlength="11"
            :disabled="ctaLoading"
          >
          <button 
            type="submit" 
            class="btn btn-primary btn-large" 
            :disabled="ctaLoading"
          >
            {{ ctaLoading ? '提交中...' : '立即领取' }}
          </button>
        </form>
        <div class="cta-guarantee" v-intersect="onReveal">
          <span>✓</span> 7天免费体验 · <span>✓</span> 随时取消 · <span>✓</span> 无需绑定银行卡
        </div>
      </div>
    </section>

    <!-- 页脚 -->
    <footer class="footer">
      <div class="footer-container">
        <div class="footer-grid">
          <div class="footer-brand">
            <div class="footer-logo">
              <div class="footer-logo-icon">💪</div>
              <div class="footer-logo-text">智健AI</div>
            </div>
            <p class="footer-desc">智健AI致力于通过人工智能技术，为每一位用户提供个性化、科学化的健身解决方案，让健身更智能、更高效、更有趣。</p>
            <div class="footer-social">
              <a href="#">📱</a>
              <a href="#">💬</a>
              <a href="#">📷</a>
              <a href="#">▶️</a>
            </div>
          </div>
          <div class="footer-column">
            <h4 class="footer-title">快速链接</h4>
            <ul class="footer-links">
              <li><a href="#home" @click.prevent="scrollToSection('home')">首页</a></li>
              <li><a href="#ai-features" @click.prevent="scrollToSection('ai-features')">AI功能</a></li>
              <li><a href="#courses" @click.prevent="scrollToSection('courses')">课程体系</a></li>
              <li><a href="#membership" @click.prevent="scrollToSection('membership')">会员方案</a></li>
              <li><a href="#coaches" @click.prevent="scrollToSection('coaches')">教练团队</a></li>
            </ul>
          </div>
          <div class="footer-column">
            <h4 class="footer-title">服务项目</h4>
            <ul class="footer-links">
              <li><a href="#">AI训练计划</a></li>
              <li><a href="#">私教课程</a></li>
              <li><a href="#">团体课程</a></li>
              <li><a href="#">营养指导</a></li>
              <li><a href="#">企业健身</a></li>
            </ul>
          </div>
          <div class="footer-column">
            <h4 class="footer-title">帮助中心</h4>
            <ul class="footer-links">
              <li><a href="#">常见问题</a></li>
              <li><a href="#">会员权益</a></li>
              <li><a href="#">退款政策</a></li>
              <li><a href="#">隐私条款</a></li>
              <li><a href="#">用户协议</a></li>
            </ul>
          </div>
          <div class="footer-contact">
            <h4 class="footer-title">联系我们</h4>
            <div class="footer-contact-item">
              <div class="footer-contact-icon">📍</div>
              <div class="footer-contact-content">
                <h4>门店地址</h4>
                <p>北京市朝阳区建国路88号SOHO现代城A座</p>
              </div>
            </div>
            <div class="footer-contact-item">
              <div class="footer-contact-icon">📞</div>
              <div class="footer-contact-content">
                <h4>客服热线</h4>
                <p>400-888-9999</p>
              </div>
            </div>
            <div class="footer-contact-item">
              <div class="footer-contact-icon">⏰</div>
              <div class="footer-contact-content">
                <h4>营业时间</h4>
                <p>周一至周日 06:00-23:00</p>
              </div>
            </div>
          </div>
        </div>
        <div class="footer-bottom">
          <p>&copy; 2024 智健AI. All rights reserved. | 京ICP备XXXXXXXX号</p>
          <div class="footer-bottom-links">
            <a href="#">隐私政策</a>
            <a href="#">服务条款</a>
            <a href="#">Cookie设置</a>
          </div>
        </div>
      </div>
    </footer>

    <!-- 登录模态框 -->
    <LoginModal 
      v-model:visible="showLoginModal" 
      @login-success="handleLoginSuccess"
      @go-register="goToRegisterFromModal"
    />

    <!-- 注册/登录弹窗 -->
    <n-modal v-model:show="showRegisterModal" preset="card" :show-header="false" :closable="false" style="width: 420px" class="register-modal">
      <div class="register-modal-content">
        <button class="modal-close-btn" @click="showRegisterModal = false">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M18 6L6 18M6 6l12 12"/>
          </svg>
        </button>
        <div class="register-modal-header">
          <div class="register-icon">🎁</div>
          <h3 class="register-title">领取您的7天VIP体验</h3>
          <p class="register-subtitle">已有账号将自动登录，新用户将自动注册</p>
        </div>
        <n-form ref="registerFormRef" :model="registerForm" :rules="registerRules" class="register-form">
          <n-form-item path="phone" :show-label="false">
            <n-input 
              v-model:value="registerForm.phone" 
              placeholder="请输入手机号" 
              size="large"
              :maxlength="11"
              class="phone-input"
            >
              <template #prefix>
                <svg class="input-prefix-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#666" stroke-width="2">
                  <rect x="5" y="2" width="14" height="20" rx="2" ry="2"/>
                  <path d="M12 18h.01"/>
                </svg>
              </template>
            </n-input>
          </n-form-item>
          <n-form-item path="code" :show-label="false">
            <div class="code-input-group">
              <n-input 
                v-model:value="registerForm.code" 
                placeholder="请输入验证码" 
                size="large"
                :maxlength="6"
                class="code-input"
              >
                <template #prefix>
                  <svg class="input-prefix-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#666" stroke-width="2">
                    <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                    <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                    <circle cx="12" cy="16" r="1"/>
                  </svg>
                </template>
              </n-input>
              <n-button 
                :disabled="countdown > 0 || !isPhoneValid" 
                :loading="sendingCode"
                @click="sendVerificationCode"
                class="send-code-btn"
                size="large"
              >
                {{ countdown > 0 ? `${countdown}s后重发` : '获取验证码' }}
              </n-button>
            </div>
          </n-form-item>
          <n-form-item :show-label="false">
            <n-button 
              type="primary" 
              block 
              size="large"
              @click="handleRegisterSubmit" 
              :loading="registerLoading"
              class="submit-btn"
            >
              {{ registerLoading ? '处理中...' : '立即领取' }}
            </n-button>
          </n-form-item>
        </n-form>
        <div class="register-footer">
          <p class="agreement-text">
            点击"立即领取"即表示您同意
            <a href="#" @click.prevent>《用户协议》</a>
            和
            <a href="#" @click.prevent>《隐私政策》</a>
          </p>
        </div>
      </div>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { useAuthStore } from '@/stores/auth'
import LoginModal from '@/components/LoginModal.vue'

const router = useRouter()
const message = useMessage()
const authStore = useAuthStore()

// 公告栏显示状态
const showAnnouncement = ref(true)
const showMarquee = ref(true)

// 导航栏滚动状态
const isScrolled = ref(false)
const activeSection = ref('home')
const mobileMenuOpen = ref(false)

// 登录模态框
const showLoginModal = ref(false)

// 注册/登录弹窗
const showRegisterModal = ref(false)
const registerFormRef = ref(null)
const registerLoading = ref(false)
const sendingCode = ref(false)
const countdown = ref(0)
let countdownTimer = null

const registerForm = reactive({
  phone: '',
  code: ''
})

const registerRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '请输入6位验证码', trigger: 'blur' }
  ]
}

// 计算属性：手机号是否有效
const isPhoneValid = computed(() => {
  return /^1[3-9]\d{9}$/.test(registerForm.phone)
})

// Hero轮播图
const currentSlide = ref(0)
const heroSlides = [
  { image: 'https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=800&h=1000&fit=crop', alt: '智能健身房' },
  { image: 'https://images.unsplash.com/photo-1571902943202-507ec2618e8f?w=800&h=1000&fit=crop', alt: '专业训练' },
  { image: 'https://images.unsplash.com/photo-1540497077202-7c8a3999166f?w=800&h=1000&fit=crop', alt: '团体课程' },
  { image: 'https://images.unsplash.com/photo-1593079831268-3381b0db4a77?w=800&h=1000&fit=crop', alt: '器械区' }
]

// 统计数据动画
const animatedStats = reactive({
  members: 0,
  satisfaction: 0,
  coaches: 0
})

const targetStats = {
  members: 50000,
  satisfaction: 98,
  coaches: 200
}

// 统计数据
const stats = [
  { icon: '👥', value: 50000, label: '活跃会员' },
  { icon: '🏆', value: 200, label: '专业教练' },
  { icon: '📚', value: 500, label: '精品课程' },
  { icon: '⭐', value: 98, label: '% 满意度' }
]

// 滚动公告
const marqueeItems = [
  '今日起暂停营业，<strong>3.19 - 3.23</strong> 门店进行设备维护升级',
  '<span class="marquee-notice-divider">|</span>',
  '会员有效期将自动顺延 <strong>5天</strong>，给您带来不便敬请谅解',
  '<span class="marquee-notice-divider">|</span>',
  '线上课程正常开放，欢迎下载APP继续锻炼',
  '<span class="marquee-notice-divider">|</span>',
  '客服热线 <strong>400-888-9999</strong> 全天候为您服务',
  '<span class="marquee-notice-divider">|</span>'
]

// 品牌理念
const philosophyValues = [
  { icon: '🎯', title: '精准定制', desc: 'AI算法分析身体数据，生成专属训练方案' },
  { icon: '📈', title: '科学追踪', desc: '实时监测训练数据，动态调整计划强度' },
  { icon: '🤝', title: '专业陪伴', desc: '认证教练团队全程指导，解答健身疑惑' },
  { icon: '🌟', title: '持续激励', desc: '成就系统与社群互动，保持健身动力' }
]

// AI功能
const aiFeatures = [
  {
    icon: '🤖',
    title: 'AI智能训练计划',
    desc: '基于您的身体数据、健身目标和时间安排，AI算法自动生成最适合您的个性化训练计划，并随进度动态调整。',
    items: ['智能分析身体成分与运动能力', '根据目标自动规划训练周期', '实时调整强度与容量', '智能推荐动作替代方案']
  },
  {
    icon: '📊',
    title: '智能体测分析',
    desc: '通过AI视觉识别技术，精准测量身体围度、姿态评估，生成详细的身体报告与改善建议。',
    items: ['3D身体扫描与围度测量', '体态评估与风险预警', '身体成分趋势分析', '个性化改善方案']
  },
  {
    icon: '📱',
    title: '实时运动追踪',
    desc: '连接智能穿戴设备，实时监测心率、消耗卡路里、运动轨迹等数据，让每一次训练都有据可依。',
    items: ['多设备数据同步', '实时心率区间提醒', '运动表现分析报告', '智能训练建议']
  },
  {
    icon: '🥗',
    title: 'AI营养指导',
    desc: '根据您的身体数据和训练计划，AI营养师为您定制每日饮食方案，科学搭配营养摄入。',
    items: ['个性化热量计算', '营养素比例优化', '健康食谱推荐', '饮食习惯分析']
  }
]

// 课程标签
const courseTabs = [
  { key: 'all', label: '全部课程' },
  { key: 'strength', label: '力量训练' },
  { key: 'cardio', label: '有氧燃脂' },
  { key: 'yoga', label: '瑜伽普拉提' },
  { key: 'boxing', label: '拳击格斗' }
]

const activeCourseTab = ref('all')

const courses = [
  { name: 'HIIT燃脂特训', category: 'cardio', level: '中级', duration: '45分钟', calories: '500-700卡', bookings: 2341, image: 'https://images.unsplash.com/photo-1601422407692-ec4eeec1d9b3?w=400&h=300&fit=crop', desc: '高强度间歇训练，快速燃烧脂肪，提升心肺功能' },
  { name: '力量基础入门', category: 'strength', level: '初级', duration: '60分钟', calories: '300-450卡', bookings: 1856, image: 'https://images.unsplash.com/photo-1581009146145-b5ef050c149a?w=400&h=300&fit=crop', desc: '系统学习基础力量训练动作，建立正确运动模式' },
  { name: '流瑜伽', category: 'yoga', level: '全级别', duration: '75分钟', calories: '200-350卡', bookings: 3124, image: 'https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=400&h=300&fit=crop', desc: '通过流畅的体式串联，提升柔韧性与身心平衡' },
  { name: '拳击格斗', category: 'boxing', level: '中级', duration: '50分钟', calories: '600-800卡', bookings: 1567, image: 'https://images.unsplash.com/photo-1549719386-74dfcbf7dbed?w=400&h=300&fit=crop', desc: '学习拳击技巧，释放压力，提升协调性与爆发力' },
  { name: '普拉提核心', category: 'yoga', level: '初级', duration: '55分钟', calories: '250-400卡', bookings: 2789, image: 'https://images.unsplash.com/photo-1518611012118-696072aa579a?w=400&h=300&fit=crop', desc: '强化核心肌群，改善体态，塑造修长线条' },
  { name: 'CrossFit挑战', category: 'strength', level: '高级', duration: '60分钟', calories: '700-900卡', bookings: 987, image: 'https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=400&h=300&fit=crop', desc: '功能性训练，全面提升力量、耐力与爆发力' }
]

const filteredCourses = computed(() => {
  if (activeCourseTab.value === 'all') return courses
  return courses.filter(c => c.category === activeCourseTab.value)
})

// 会员方案
const membershipSlide = ref(0)
const membershipPlans = [
  {
    name: '体验卡',
    price: '0',
    period: '7天',
    desc: '新用户专属，零门槛体验',
    badge: null,
    featured: false,
    features: [
      { text: '7天全时段通行', included: true },
      { text: '基础器械使用', included: true },
      { text: '2节团体课程', included: true },
      { text: 'AI体测1次', included: true },
      { text: '私教课程', included: false },
      { text: '营养指导', included: false }
    ]
  },
  {
    name: '月卡',
    price: '299',
    period: '月',
    desc: '灵活选择，随时开始',
    badge: '热门',
    featured: true,
    features: [
      { text: '全时段无限次通行', included: true },
      { text: '全部器械使用', included: true },
      { text: '无限团体课程', included: true },
      { text: 'AI体测每月1次', included: true },
      { text: '1节私教体验课', included: true },
      { text: '营养指导', included: false }
    ]
  },
  {
    name: '季卡',
    price: '799',
    period: '季',
    desc: '坚持三个月，见证改变',
    badge: null,
    featured: false,
    features: [
      { text: '全时段无限次通行', included: true },
      { text: '全部器械使用', included: true },
      { text: '无限团体课程', included: true },
      { text: 'AI体测每月1次', included: true },
      { text: '4节私教课程', included: true },
      { text: '基础营养指导', included: true }
    ]
  },
  {
    name: '年卡',
    price: '2599',
    period: '年',
    desc: '最超值选择，全年无忧',
    badge: '最佳价值',
    featured: false,
    features: [
      { text: '全时段无限次通行', included: true },
      { text: '全部器械使用', included: true },
      { text: '无限团体课程', included: true },
      { text: 'AI体测不限次数', included: true },
      { text: '12节私教课程', included: true },
      { text: '专属营养方案', included: true }
    ]
  }
]

// 教练团队
const coachSlide = ref(0)
const coaches = [
  { name: '王强', title: '高级私人教练', experience: '8+', students: '2000+', rating: '99%', image: 'https://images.unsplash.com/photo-1567013127542-490d757e51fc?w=400&h=500&fit=crop', tags: ['增肌塑形', '体态矫正', '运动康复'] },
  { name: '李雪', title: '瑜伽认证导师', experience: '10+', students: '3500+', rating: '98%', image: 'https://images.unsplash.com/photo-1518611012118-696072aa579a?w=400&h=500&fit=crop', tags: ['流瑜伽', '普拉提', '冥想'] },
  { name: '张伟', title: 'CrossFit教练', experience: '6+', students: '1500+', rating: '97%', image: 'https://images.unsplash.com/photo-1583454110551-21f2fa2afe61?w=400&h=500&fit=crop', tags: ['CrossFit', '体能训练', '减脂'] },
  { name: '陈龙', title: '拳击格斗教练', experience: '12+', students: '3000+', rating: '99%', image: 'https://images.unsplash.com/photo-1609899464926-209bc8a4c6f0?w=400&h=500&fit=crop', tags: ['职业拳手', '拳击', '防身术'] }
]

// 成功案例
const testimonialSlide = ref(0)
const testimonials = [
  { name: '李明', occupation: 'IT工程师', duration: '训练12个月', avatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&h=200&fit=crop', before: '85kg', beforeLabel: '减重前', after: '72kg', afterLabel: '现在', quote: '作为一名程序员，长期久坐让我体重飙升。加入智健AI后，AI为我定制了科学的训练计划，配合营养指导，12个月成功减重13kg，现在精力充沛，工作效率也提高了！' },
  { name: '王芳', occupation: '全职妈妈', duration: '训练8个月', avatar: 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=200&h=200&fit=crop', before: '产后', beforeLabel: '开始', after: '马甲线', afterLabel: '现在', quote: '产后身材走形让我很自卑，智健AI的产后恢复课程和教练的专业指导让我重拾自信。现在不仅恢复了产前身材，还练出了马甲线！' },
  { name: '张浩', occupation: '大学生', duration: '训练6个月', avatar: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200&h=200&fit=crop', before: '瘦弱', beforeLabel: '增肌前', after: '+8kg', afterLabel: '肌肉增长', quote: '从小就是瘦弱的体质，一直想要增肌。智健AI的AI训练计划非常科学，6个月增肌8kg，现在终于有了理想的身材，感谢智健AI！' },
  { name: '刘婷', occupation: '企业高管', duration: '训练10个月', avatar: 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=200&h=200&fit=crop', before: '亚健康', beforeLabel: '开始', after: '活力', afterLabel: '现在', quote: '高强度工作让我身心俱疲，瑜伽和普拉提课程帮我找到了工作与健康的平衡。现在睡眠质量好了，工作效率也更高了。' }
]

// 健身设备
const activeEquipment = ref(0)
const equipmentList = [
  { name: '有氧训练区', image: 'https://images.unsplash.com/photo-1517836357463-d25dfeac3438?w=200&h=200&fit=crop', desc: 'Technogym、Precor顶级有氧设备，支持心率监测与数据同步。', meta: ['🏃 跑步机 x 20', '🚴 单车 x 15'] },
  { name: '自由重量区', image: 'https://images.unsplash.com/photo-1593079831268-3381b0db4a77?w=200&h=200&fit=crop', desc: 'Eleiko专业举重器材，从哑铃到杠铃，满足各类力量训练需求。', meta: ['🏋️ 哑铃组 x 30', '💪 杠铃架 x 8'] },
  { name: '功能训练区', image: 'https://images.unsplash.com/photo-1540497077202-7c8a3999166f?w=200&h=200&fit=crop', desc: 'TRX、战绳、药球等多样化功能训练器材，提升运动表现。', meta: ['🎯 TRX x 10', '⚡ 战绳 x 6'] }
]

// 线上线下
const omnichannelFeatures = [
  { icon: '📱', title: '智能预约系统', desc: '一键预约课程、私教、场地，实时查看健身房人流情况，避开高峰。' },
  { icon: '🎥', title: '线上课程库', desc: '500+精品线上课程，在家也能享受专业指导，随时随地开始训练。' },
  { icon: '💬', title: '教练在线指导', desc: '训练遇到问题？随时与教练在线沟通，获取专业建议与动作纠正。' },
  { icon: '🏆', title: '社群挑战赛', desc: '参与线上线下挑战赛，与志同道合的伙伴一起进步，赢取丰厚奖励。' }
]

// CTA
const ctaPhone = ref('')
const ctaLoading = ref(false)
const ctaInputRef = ref(null)

// 方法
function getParticleStyle(n) {
  return {
    left: `${Math.random() * 100}%`,
    animationDelay: `${Math.random() * 15}s`,
    animationDuration: `${15 + Math.random() * 10}s`
  }
}

function scrollToSection(sectionId) {
  const element = document.getElementById(sectionId)
  if (element) {
    const offset = 80
    const elementPosition = element.getBoundingClientRect().top
    const offsetPosition = elementPosition + window.pageYOffset - offset
    window.scrollTo({ top: offsetPosition, behavior: 'smooth' })
  }
}

function nextSlide() {
  currentSlide.value = (currentSlide.value + 1) % heroSlides.length
}

function prevSlide() {
  currentSlide.value = (currentSlide.value - 1 + heroSlides.length) % heroSlides.length
}

function goToSlide(index) {
  currentSlide.value = index
}

function prevMembershipSlide() {
  membershipSlide.value = Math.max(0, membershipSlide.value - 1)
}

function nextMembershipSlide() {
  membershipSlide.value = Math.min(membershipPlans.length - 3, membershipSlide.value + 1)
}

function prevCoachSlide() {
  coachSlide.value = Math.max(0, coachSlide.value - 1)
}

function nextCoachSlide() {
  coachSlide.value = Math.min(coaches.length - 4, coachSlide.value + 1)
}

function prevTestimonial() {
  testimonialSlide.value = (testimonialSlide.value - 1 + testimonials.length) % testimonials.length
}

function nextTestimonial() {
  testimonialSlide.value = (testimonialSlide.value + 1) % testimonials.length
}

function formatNumber(num) {
  return num.toLocaleString()
}

function onReveal(el) {
  el.classList.add('active')
}

function onStatIntersect(el) {
  el.classList.add('active')
}

function goToRegister() {
  // 滚动到CTA区域
  const ctaSection = document.getElementById('cta')
  if (ctaSection) {
    ctaSection.scrollIntoView({ behavior: 'smooth', block: 'center' })
    // 延迟聚焦输入框，等待滚动完成
    setTimeout(() => {
      if (ctaInputRef.value) {
        ctaInputRef.value.focus()
        ctaInputRef.value.classList.add('input-highlight')
        setTimeout(() => ctaInputRef.value.classList.remove('input-highlight'), 2000)
      }
    }, 800)
  }
}

function goToRegisterFromModal() {
  showLoginModal.value = false
  showRegisterModal.value = true
}

function handleLoginSuccess() {
  message.success('登录成功')
}

async function handleLogout() {
  await authStore.logout()
  message.success('已退出登录')
  router.push('/')
}

async function handleCTASubmit() {
  // 手机号格式验证
  const phoneRegex = /^1[3-9]\d{9}$/
  if (!ctaPhone.value || !phoneRegex.test(ctaPhone.value)) {
    message.error('请输入正确的11位手机号')
    return
  }
  
  ctaLoading.value = true
  try {
    // 模拟请求延迟
    await new Promise(resolve => setTimeout(resolve, 500))
    // 打开注册登录弹窗，并预填手机号
    registerForm.phone = ctaPhone.value
    showRegisterModal.value = true
  } finally {
    ctaLoading.value = false
  }
}

// 发送验证码
async function sendVerificationCode() {
  if (!isPhoneValid.value) {
    message.error('请输入正确的手机号')
    return
  }
  
  sendingCode.value = true
  try {
    // 模拟发送验证码请求
    await new Promise(resolve => setTimeout(resolve, 1000))
    message.success('验证码已发送')
    
    // 开始倒计时
    countdown.value = 60
    countdownTimer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(countdownTimer)
      }
    }, 1000)
  } catch (error) {
    message.error('发送失败，请重试')
  } finally {
    sendingCode.value = false
  }
}

// 处理注册/登录提交
async function handleRegisterSubmit() {
  try {
    await registerFormRef.value?.validate()
    registerLoading.value = true
    
    // 模拟注册/登录请求
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    // 模拟登录成功
    message.success('领取成功！欢迎加入智健AI')
    showRegisterModal.value = false
    
    // 清空表单
    registerForm.phone = ''
    registerForm.code = ''
    ctaPhone.value = ''
    
    // 这里可以添加实际的登录逻辑
    // await authStore.loginWithPhone(registerForm.phone, registerForm.code)
  } catch (error) {
    if (error?.message) {
      message.error(error.message)
    }
  } finally {
    registerLoading.value = false
  }
}

// 滚动监听
function handleScroll() {
  isScrolled.value = window.scrollY > 50

  // 更新当前区域
  const sections = ['home', 'philosophy', 'ai-features', 'courses', 'membership', 'coaches']
  for (const section of sections) {
    const element = document.getElementById(section)
    if (element) {
      const rect = element.getBoundingClientRect()
      if (rect.top <= 100 && rect.bottom >= 100) {
        activeSection.value = section
        break
      }
    }
  }
}

// 数字动画
function animateNumbers() {
  const duration = 2000
  const steps = 60
  const interval = duration / steps

  let step = 0
  const timer = setInterval(() => {
    step++
    const progress = step / steps
    const easeOut = 1 - Math.pow(1 - progress, 3)

    animatedStats.members = Math.floor(targetStats.members * easeOut)
    animatedStats.satisfaction = Math.floor(targetStats.satisfaction * easeOut)
    animatedStats.coaches = Math.floor(targetStats.coaches * easeOut)

    if (step >= steps) {
      clearInterval(timer)
      animatedStats.members = targetStats.members
      animatedStats.satisfaction = targetStats.satisfaction
      animatedStats.coaches = targetStats.coaches
    }
  }, interval)
}

// 自动轮播
let heroInterval
onMounted(() => {
  window.addEventListener('scroll', handleScroll)
  animateNumbers()

  // Hero轮播自动播放
  heroInterval = setInterval(() => {
    nextSlide()
  }, 5000)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
  if (heroInterval) clearInterval(heroInterval)
})

// 自定义指令：元素进入视口时触发
const vIntersect = {
  mounted(el, binding) {
    const observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          binding.value(el)
          observer.unobserve(el)
        }
      })
    }, { threshold: 0.1 })
    observer.observe(el)
  }
}
</script>

<style>
/* Global styles for body/html to ensure dark background */
:global(body),
:global(html) {
  background: #0A0A0F !important;
  min-height: 100vh;
}
</style>

<style scoped>
/* CSS Variables */
.home-page {
  --primary: #FF6B35;
  --primary-light: #FF8C61;
  --primary-dark: #E55A2B;
  --primary-gradient: linear-gradient(135deg, #FF6B35 0%, #FF8C61 100%);
  --primary-glow: rgba(255, 107, 53, 0.4);
  --accent-gold: #FFD93D;
  --accent-teal: #2EC4B6;
  --accent-purple: #A855F7;
  --accent-blue: #3B82F6;
  --accent-rose: #F43F5E;
  --bg-dark: #0A0A0F;
  --bg-dark-secondary: #12121A;
  --bg-dark-tertiary: #1A1A25;
  --bg-card: rgba(255, 255, 255, 0.03);
  --bg-card-hover: rgba(255, 255, 255, 0.06);
  --bg-light: #FAFBFC;
  --bg-white: #FFFFFF;
  --text-primary: #FFFFFF;
  --text-secondary: rgba(255, 255, 255, 0.75);
  --text-muted: rgba(255, 255, 255, 0.5);
  --text-dark: #1A1A2E;
  --text-gray: #6B7280;
  --shadow-sm: 0 2px 8px rgba(0, 0, 0, 0.2);
  --shadow-md: 0 8px 30px rgba(0, 0, 0, 0.3);
  --shadow-lg: 0 20px 60px rgba(0, 0, 0, 0.4);
  --shadow-primary: 0 8px 30px rgba(255, 107, 53, 0.35);
  --shadow-glow: 0 0 60px rgba(255, 107, 53, 0.25);
  --radius-sm: 8px;
  --radius-md: 16px;
  --radius-lg: 24px;
  --radius-xl: 32px;
  --transition-fast: 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  --transition-normal: 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  --transition-slow: 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  --transition-bounce: 0.5s cubic-bezier(0.68, -0.55, 0.265, 1.55);
  
  font-family: 'Noto Sans SC', 'Outfit', -apple-system, BlinkMacSystemFont, sans-serif;
  background: #0A0A0F;
  background-color: #0A0A0F;
  color: var(--text-primary);
  line-height: 1.6;
  overflow-x: hidden;
  min-height: 100vh;
}

/* Announcement Bar */
.announcement-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background: linear-gradient(90deg, #FF6B35 0%, #FF8C61 50%, #FF6B35 100%);
  background-size: 200% 100%;
  animation: gradient-shift 3s ease infinite;
  z-index: 1001;
  padding: 12px 0;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(255, 107, 53, 0.3);
}

@keyframes gradient-shift {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

.announcement-bar-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.announcement-bar-content a {
  color: var(--text-primary);
  text-decoration: underline;
  font-weight: 600;
}

.announcement-bar-close {
  position: absolute;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: var(--text-primary);
  cursor: pointer;
  font-size: 18px;
  opacity: 0.8;
  transition: opacity var(--transition-fast);
}

.announcement-bar-close:hover {
  opacity: 1;
}

/* Navbar */
.navbar {
  position: fixed;
  top: 40px;
  left: 0;
  right: 0;
  z-index: 1000;
  padding: 20px 0;
  transition: all var(--transition-normal);
}

.navbar.scrolled {
  background: rgba(10, 10, 15, 0.92);
  backdrop-filter: blur(20px) saturate(180%);
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  padding: 14px 0;
  top: 0;
}

.navbar.has-announcement {
  top: 40px;
}

.navbar-container {
  max-width: 1440px;
  margin: 0 auto;
  padding: 0 40px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  display: flex;
  align-items: center;
  gap: 14px;
  text-decoration: none;
  transition: transform var(--transition-normal);
}

.logo:hover {
  transform: scale(1.02);
}

.logo-icon {
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C61 100%);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  box-shadow: 0 8px 30px rgba(255, 107, 53, 0.35);
}

.logo-text {
  font-size: 26px;
  font-weight: 900;
  color: var(--text-primary);
  letter-spacing: -0.5px;
}

.logo-text span {
  color: #FF6B35;
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 36px;
}

.nav-links a {
  color: var(--text-secondary);
  text-decoration: none;
  font-size: 15px;
  font-weight: 500;
  transition: all var(--transition-normal);
  position: relative;
  padding: 8px 0;
  cursor: pointer;
}

.nav-links a:hover,
.nav-links a.active {
  color: #FF6B35;
}

.nav-links a::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 2.5px;
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C61 100%);
  border-radius: 2px;
  transition: width var(--transition-normal);
}

.nav-links a:hover::after,
.nav-links a.active::after {
  width: 100%;
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.nav-link {
  color: var(--text-secondary);
  text-decoration: none;
  font-size: 15px;
  font-weight: 500;
  transition: all var(--transition-normal);
  position: relative;
  padding: 8px 0;
  cursor: pointer;
}

.nav-link:hover {
  color: #FF6B35;
}

.nav-link::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 2.5px;
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C61 100%);
  border-radius: 2px;
  transition: width var(--transition-normal);
}

.nav-link:hover::after {
  width: 100%;
}

/* Buttons */
.btn {
  padding: 14px 32px;
  border-radius: 16px;
  font-size: 15px;
  font-weight: 600;
  text-decoration: none;
  transition: all var(--transition-normal);
  cursor: pointer;
  border: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  font-family: inherit;
}

.btn-outline {
  background: transparent;
  color: var(--text-primary);
  border: 2px solid rgba(255, 255, 255, 0.15);
}

.btn-outline:hover {
  border-color: #FF6B35;
  background: rgba(255, 107, 53, 0.08);
  color: #FF6B35;
  transform: translateY(-2px);
}

.btn-primary {
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C61 100%);
  color: var(--text-primary);
  box-shadow: 0 8px 30px rgba(255, 107, 53, 0.35);
}

.btn-primary:hover {
  transform: translateY(-3px);
  box-shadow: 0 15px 40px rgba(255, 107, 53, 0.45);
}

.btn-primary:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}

.btn-large {
  padding: 18px 44px;
  font-size: 17px;
  border-radius: 24px;
}

.btn-block {
  width: 100%;
}

.btn-glow {
  animation: pulse-glow 2s ease-in-out infinite;
}

@keyframes pulse-glow {
  0%, 100% { box-shadow: 0 8px 30px rgba(255, 107, 53, 0.35); }
  50% { box-shadow: 0 8px 40px rgba(255, 107, 53, 0.55), 0 0 60px rgba(255, 107, 53, 0.2); }
}

/* Mobile Menu Button */
.mobile-menu-btn {
  display: none;
  flex-direction: column;
  gap: 5px;
  background: none;
  border: none;
  cursor: pointer;
  padding: 8px;
}

.mobile-menu-btn span {
  width: 24px;
  height: 2.5px;
  background: var(--text-primary);
  border-radius: 2px;
  transition: all var(--transition-normal);
}

.mobile-menu-btn.active span:nth-child(1) {
  transform: rotate(45deg) translate(5px, 5px);
}

.mobile-menu-btn.active span:nth-child(2) {
  opacity: 0;
}

.mobile-menu-btn.active span:nth-child(3) {
  transform: rotate(-45deg) translate(5px, -5px);
}

/* Mobile Menu */
.mobile-menu {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(10, 10, 15, 0.98);
  backdrop-filter: blur(20px);
  z-index: 999;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 30px;
  opacity: 0;
  visibility: hidden;
  transition: all var(--transition-normal);
}

.mobile-menu.active {
  opacity: 1;
  visibility: visible;
}

.mobile-menu a {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  text-decoration: none;
  transition: color var(--transition-normal);
}

.mobile-menu a:hover {
  color: #FF6B35;
}

/* Hero Section */
.hero-section {
  min-height: auto;
  height: auto;
  padding: 160px 0 80px;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  background: var(--bg-dark);
}

.hero-section.has-announcement {
  padding-top: 200px;
}

.hero-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(ellipse at 20% 80%, rgba(255, 107, 53, 0.2) 0%, transparent 55%),
    radial-gradient(ellipse at 80% 20%, rgba(168, 85, 247, 0.15) 0%, transparent 50%),
    radial-gradient(ellipse at 50% 50%, rgba(46, 196, 182, 0.1) 0%, transparent 60%),
    radial-gradient(ellipse at 30% 30%, rgba(255, 107, 53, 0.08) 0%, transparent 40%),
    linear-gradient(180deg, var(--bg-dark) 0%, var(--bg-dark-secondary) 50%, var(--bg-dark) 100%);
  z-index: 0;
}

.hero-grid {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: linear-gradient(rgba(255, 255, 255, 0.025) 1px, transparent 1px),
                    linear-gradient(90deg, rgba(255, 255, 255, 0.025) 1px, transparent 1px);
  background-size: 70px 70px;
  z-index: 1;
  mask-image: radial-gradient(ellipse at center, black 0%, transparent 70%);
}

.hero-particles {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1;
  overflow: hidden;
}

.particle {
  position: absolute;
  width: 4px;
  height: 4px;
  background: #FF6B35;
  border-radius: 50%;
  opacity: 0.4;
  animation: float 15s infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(100vh) rotate(0deg); opacity: 0; }
  10% { opacity: 0.4; }
  90% { opacity: 0.4; }
  100% { transform: translateY(-100vh) rotate(720deg); opacity: 0; }
}

.hero-container {
  max-width: 1440px;
  margin: 0 auto;
  padding: 0 40px;
  width: 100%;
  position: relative;
  z-index: 2;
}

.hero-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 60px;
  align-items: center;
}

.hero-text {
  max-width: 620px;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 10px 20px;
  background: rgba(255, 107, 53, 0.12);
  border: 1px solid rgba(255, 107, 53, 0.25);
  border-radius: 100px;
  font-size: 14px;
  font-weight: 600;
  color: #FF6B35;
  margin-bottom: 28px;
  animation: fadeInUp 0.8s ease forwards;
}

.hero-badge::before {
  content: '';
  width: 8px;
  height: 8px;
  background: #FF6B35;
  border-radius: 50%;
  animation: pulse-dot 2s ease-in-out infinite;
}

@keyframes pulse-dot {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.6; transform: scale(1.2); }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.hero-title {
  font-size: 64px;
  font-weight: 900;
  line-height: 1.08;
  margin-bottom: 24px;
  letter-spacing: -2px;
  animation: fadeInUp 0.8s ease 0.1s forwards;
  opacity: 0;
}

.hero-title span {
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C61 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  position: relative;
}

.hero-title span::after {
  content: '';
  position: absolute;
  bottom: 5px;
  left: 0;
  right: 0;
  height: 8px;
  background: rgba(255, 107, 53, 0.2);
  border-radius: 4px;
  z-index: -1;
}

.hero-desc {
  font-size: 19px;
  color: var(--text-secondary);
  margin-bottom: 36px;
  line-height: 1.8;
  animation: fadeInUp 0.8s ease 0.2s forwards;
  opacity: 0;
}

.hero-actions {
  display: flex;
  gap: 16px;
  margin-bottom: 48px;
  animation: fadeInUp 0.8s ease 0.3s forwards;
  opacity: 0;
}

.hero-stats-preview {
  display: flex;
  gap: 40px;
  animation: fadeInUp 0.8s ease 0.4s forwards;
  opacity: 0;
}

.hero-stat-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.hero-stat-value {
  font-size: 32px;
  font-weight: 800;
  color: #FF6B35;
}

.hero-stat-label {
  font-size: 14px;
  color: var(--text-muted);
}

.hero-visual {
  position: relative;
  height: 580px;
  width: 100%;
}

/* Hero Carousel */
.hero-carousel {
  position: relative;
  width: 100%;
  height: 100%;
  border-radius: 32px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.4);
}

.hero-carousel-track {
  display: flex;
  height: 100%;
  transition: transform 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.hero-carousel-slide {
  min-width: 100%;
  height: 100%;
  position: relative;
}

.hero-carousel-slide img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.hero-carousel-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.15) 0%, transparent 50%);
}

.hero-carousel-indicators {
  position: absolute;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 10px;
  z-index: 10;
}

.hero-carousel-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.4);
  cursor: pointer;
  transition: all 0.3s;
  border: none;
}

.hero-carousel-dot.active {
  background: #FF6B35;
  width: 32px;
  border-radius: 5px;
}

.hero-carousel-nav {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 44px;
  height: 44px;
  background: rgba(26, 26, 37, 0.8);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  z-index: 10;
  color: var(--text-primary);
  font-size: 18px;
}

.hero-carousel-nav:hover {
  background: #FF6B35;
  border-color: #FF6B35;
}

.hero-carousel-nav.prev { left: 16px; }
.hero-carousel-nav.next { right: 16px; }

/* Marquee Notice */
.marquee-notice-section {
  background: #1A1A25;
    border-top: 2px solid #FF6B35;
    border-bottom: 2px solid #FF6B35;
    color: #FFFFFF;
  position: relative;
  overflow: hidden;
  padding: 0;
  border-top: 2px solid rgba(255, 255, 255, 0.1);
  border-bottom: 2px solid rgba(255, 255, 255, 0.1);
}

.marquee-notice-container {
  display: flex;
  align-items: center;
  padding: 10px 0;
  position: relative;
}

.marquee-notice-label {
  flex-shrink: 0;
  background: rgba(0, 0, 0, 0.3);
  padding: 1px 1px;
  margin-left: 30px;
  margin-right: 30px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 600;
  font-size: 20px;
  color: #FECACA;
  text-transform: uppercase;
  letter-spacing: 1px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.3);
  z-index: 10;
}

.marquee-notice-wrapper {
  flex: 1;
  overflow: hidden;
  position: relative;
  mask-image: linear-gradient(90deg, transparent, black 5%, black 95%, transparent);
}

.marquee-notice-track {
  display: flex;
  animation: marquee-scroll 25s linear infinite;
  white-space: nowrap;
}

.marquee-notice-track:hover {
  animation-play-state: paused;
}

@keyframes marquee-scroll {
  0% { transform: translateX(0); }
  100% { transform: translateX(-50%); }
}

.marquee-notice-item {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  padding: 0 50px;
  font-size: 16px;
  font-weight: 600;
  color: #FFFFFF;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.marquee-notice-item::before {
  content: '📢';
  font-size: 20px;
}

.marquee-notice-divider {
  color: rgba(255, 255, 255, 0.4);
  font-weight: 300;
}

.marquee-notice-close {
  position: absolute;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
  background: rgba(0, 0, 0, 0.25);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #FFFFFF;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 18px;
  transition: all 0.3s;
  z-index: 10;
}

.marquee-notice-close:hover {
  background: rgba(0, 0, 0, 0.5);
  transform: translateY(-50%) rotate(90deg);
}

/* Stats Section */
.stats-section {
  background: linear-gradient(90deg, rgba(255, 107, 53, 0.1) 0%, rgba(168, 85, 247, 0.1) 50%, rgba(255, 107, 53, 0.1) 100%);
  background-size: 200% 100%;
  animation: stats-gradient 8s ease infinite;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  padding: 80px 0;
  position: relative;
  overflow: hidden;
}

@keyframes stats-gradient {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

.stats-container {
  max-width: 1440px;
  margin: 0 auto;
  padding: 0 40px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 40px;
}

.stat-item {
  text-align: center;
  position: relative;
}

.stat-item:not(:last-child)::after {
  content: '';
  position: absolute;
  right: -20px;
  top: 50%;
  transform: translateY(-50%);
  width: 1px;
  height: 60px;
  background: rgba(255, 255, 255, 0.08);
}

.stat-icon {
  width: 64px;
  height: 64px;
  background: var(--bg-card);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  margin: 0 auto 20px;
  transition: all 0.3s;
}

.stat-value {
  font-size: 48px;
  font-weight: 900;
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C61 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 16px;
  color: var(--text-secondary);
  font-weight: 500;
}

/* Section Common Styles */
.section {
  padding: 120px 0;
  position: relative;
}

.section-header {
  text-align: center;
  margin-bottom: 70px;
  max-width: 700px;
  margin-left: auto;
  margin-right: auto;
}

.section-tag {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 22px;
  background: rgba(255, 107, 53, 0.1);
  border: 1px solid rgba(255, 107, 53, 0.2);
  border-radius: 100px;
  font-size: 13px;
  font-weight: 600;
  color: #FF6B35;
  margin-bottom: 20px;
  text-transform: uppercase;
  letter-spacing: 1.5px;
}

.section-title {
  font-size: 48px;
  font-weight: 900;
  margin-bottom: 20px;
  letter-spacing: -1px;
  line-height: 1.15;
}

.section-title span {
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C61 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.section-desc {
  font-size: 18px;
  color: var(--text-muted);
  line-height: 1.8;
}

/* Philosophy Section */
.philosophy-section {
  background: var(--bg-dark-secondary);
}

.philosophy-container {
  max-width: 1440px;
  margin: 0 auto;
  padding: 0 40px;
}

.philosophy-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 80px;
  align-items: center;
}

.philosophy-content {
  max-width: 560px;
}

.philosophy-quote {
  font-size: 28px;
  font-weight: 700;
  line-height: 1.5;
  margin-bottom: 32px;
  color: var(--text-primary);
  position: relative;
  padding-left: 24px;
  border-left: 4px solid #FF6B35;
}

.philosophy-text {
  font-size: 17px;
  color: var(--text-secondary);
  line-height: 1.9;
  margin-bottom: 40px;
}

.philosophy-values {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

.value-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.value-icon {
  width: 48px;
  height: 48px;
  background: rgba(255, 107, 53, 0.1);
  border: 1px solid rgba(255, 107, 53, 0.2);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
}

.value-content h4 {
  font-size: 17px;
  font-weight: 700;
  margin-bottom: 6px;
}

.value-content p {
  font-size: 14px;
  color: var(--text-muted);
  line-height: 1.6;
}

.philosophy-visual {
  position: relative;
}

.philosophy-image {
  border-radius: 32px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.4);
}

.philosophy-image img {
  width: 100%;
  height: 500px;
  object-fit: cover;
}

.philosophy-experience {
  position: absolute;
  bottom: -30px;
  left: -30px;
  background: var(--bg-dark-tertiary);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 24px;
  padding: 28px 32px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.4);
}

.experience-number {
  font-size: 48px;
  font-weight: 900;
  color: #FF6B35;
  line-height: 1;
}

.experience-text {
  font-size: 14px;
  color: var(--text-secondary);
  margin-top: 4px;
}

/* AI Features Section */
.ai-features-section {
  background: var(--bg-dark);
  position: relative;
  overflow: hidden;
}

.ai-features-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(ellipse at 30% 20%, rgba(168, 85, 247, 0.08) 0%, transparent 50%),
              radial-gradient(ellipse at 70% 80%, rgba(255, 107, 53, 0.08) 0%, transparent 50%);
  pointer-events: none;
}

.ai-features-container {
  max-width: 1440px;
  margin: 0 auto;
  padding: 0 40px;
}

.ai-features-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 30px;
}

.ai-feature-card {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 24px;
  padding: 40px;
  transition: all 0.4s;
}

.ai-feature-card:hover {
  background: rgba(255, 255, 255, 0.06);
  border-color: rgba(255, 107, 53, 0.2);
  transform: translateY(-5px);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.ai-feature-icon {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C61 100%);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  margin-bottom: 24px;
}

.ai-feature-title {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 16px;
}

.ai-feature-desc {
  font-size: 16px;
  color: var(--text-secondary);
  line-height: 1.7;
  margin-bottom: 24px;
}

.ai-feature-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.ai-feature-list li {
  font-size: 15px;
  color: var(--text-muted);
  padding: 8px 0;
  padding-left: 24px;
  position: relative;
}

.ai-feature-list li::before {
  content: '✓';
  position: absolute;
  left: 0;
  color: #FF6B35;
  font-weight: 700;
}

/* Courses Section */
.courses-section {
  background: var(--bg-dark-secondary);
}

.courses-container {
  max-width: 1440px;
  margin: 0 auto;
  padding: 0 40px;
}

.courses-tabs {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-bottom: 50px;
  flex-wrap: wrap;
}

.course-tab {
  padding: 14px 28px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 100px;
  font-size: 15px;
  font-weight: 500;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.3s;
}

.course-tab:hover {
  background: rgba(255, 255, 255, 0.06);
  border-color: rgba(255, 255, 255, 0.12);
}

.course-tab.active {
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C61 100%);
  border-color: transparent;
  color: var(--text-primary);
}

.courses-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 30px;
  margin-bottom: 50px;
}

.course-card {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 24px;
  overflow: hidden;
  transition: all 0.4s;
}

.course-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  border-color: rgba(255, 107, 53, 0.2);
}

.course-image {
  position: relative;
  height: 220px;
  overflow: hidden;
}

.course-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s;
}

.course-card:hover .course-image img {
  transform: scale(1.05);
}

.course-overlay {
  position: absolute;
  top: 16px;
  left: 16px;
  right: 16px;
  display: flex;
  justify-content: space-between;
}

.course-level {
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(10px);
  padding: 6px 14px;
  border-radius: 100px;
  font-size: 13px;
  font-weight: 500;
}

.course-duration {
  background: rgba(255, 107, 53, 0.9);
  padding: 6px 14px;
  border-radius: 100px;
  font-size: 13px;
  font-weight: 500;
}

.course-content {
  padding: 24px;
}

.course-name {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 10px;
}

.course-desc {
  font-size: 15px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 16px;
}

.course-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.course-calories {
  font-size: 14px;
  color: #FF6B35;
  font-weight: 600;
}

.course-bookings {
  font-size: 13px;
  color: var(--text-muted);
}

.courses-more {
  text-align: center;
}

/* Membership Section */
.membership-section {
  background: var(--bg-dark-secondary);
}

.membership-container {
  max-width: 1440px;
  margin: 0 auto;
  padding: 0 40px;
}

.membership-slider {
  position: relative;
}

.membership-slider-track {
  overflow: hidden;
}

.membership-slider-content {
  display: flex;
  transition: transform 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.membership-slide {
  flex: 0 0 33.333%;
  padding: 0 15px;
}

.membership-slide.featured {
  transform: scale(1.05);
  z-index: 10;
}

.membership-card {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 24px;
  padding: 40px 32px;
  position: relative;
  transition: all 0.4s;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.membership-slide.featured .membership-card {
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.1) 0%, rgba(255, 140, 97, 0.05) 100%);
  border-color: rgba(255, 107, 53, 0.3);
  box-shadow: 0 20px 60px rgba(255, 107, 53, 0.15);
}

.membership-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.membership-badge {
  position: absolute;
  top: -12px;
  right: 24px;
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C61 100%);
  padding: 8px 16px;
  border-radius: 100px;
  font-size: 13px;
  font-weight: 700;
}

.membership-name {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 16px;
}

.membership-price {
  display: flex;
  align-items: baseline;
  gap: 4px;
  margin-bottom: 12px;
}

.membership-price .currency {
  font-size: 24px;
  font-weight: 700;
  color: #FF6B35;
}

.membership-price .amount {
  font-size: 56px;
  font-weight: 900;
  color: #FF6B35;
  line-height: 1;
}

.membership-price .period {
  font-size: 16px;
  color: var(--text-muted);
}

.membership-desc {
  font-size: 15px;
  color: var(--text-secondary);
  margin-bottom: 24px;
}

.membership-features {
  list-style: none;
  padding: 0;
  margin: 0 0 32px 0;
  flex-grow: 1;
}

.membership-features li {
  font-size: 15px;
  padding: 12px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  color: var(--text-secondary);
}

.membership-features li.included {
  color: var(--text-primary);
}

.membership-slider-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  color: var(--text-primary);
  font-size: 20px;
  z-index: 10;
}

.membership-slider-btn:hover {
  background: #FF6B35;
  border-color: #FF6B35;
}

.membership-slider-btn.prev { left: -24px; }
.membership-slider-btn.next { right: -24px; }

/* Coaches Section */
.coaches-section {
  background: var(--bg-dark);
}

.coaches-container {
  max-width: 1440px;
  margin: 0 auto;
  padding: 0 40px;
}

.coaches-slider {
  position: relative;
}

.coaches-slider-track {
  overflow: hidden;
}

.coaches-slider-content {
  display: flex;
  transition: transform 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.coaches-slide {
  flex: 0 0 25%;
  padding: 0 15px;
}

.coach-card {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 24px;
  overflow: hidden;
  transition: all 0.4s;
}

.coach-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.coach-image {
  position: relative;
  height: 320px;
  overflow: hidden;
}

.coach-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s;
}

.coach-card:hover .coach-image img {
  transform: scale(1.05);
}

.coach-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(to top, rgba(10, 10, 15, 0.95) 0%, transparent 60%);
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  padding: 24px;
  opacity: 0;
  transition: opacity 0.4s;
}

.coach-card:hover .coach-overlay {
  opacity: 1;
}

.coach-stats {
  display: flex;
  gap: 20px;
  margin-bottom: 16px;
}

.coach-stat {
  text-align: center;
}

.coach-stat-value {
  font-size: 20px;
  font-weight: 800;
  color: #FF6B35;
}

.coach-stat-label {
  font-size: 12px;
  color: var(--text-muted);
}

.coach-info {
  padding: 24px;
}

.coach-name {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 6px;
}

.coach-title {
  font-size: 14px;
  color: var(--text-muted);
  margin-bottom: 12px;
}

.coach-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.coach-tag {
  background: rgba(255, 107, 53, 0.1);
  border: 1px solid rgba(255, 107, 53, 0.2);
  padding: 6px 12px;
  border-radius: 100px;
  font-size: 12px;
  color: #FF6B35;
}

.coaches-slider-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  color: var(--text-primary);
  font-size: 20px;
  z-index: 10;
}

.coaches-slider-btn:hover {
  background: #FF6B35;
  border-color: #FF6B35;
}

.coaches-slider-btn.prev { left: -24px; }
.coaches-slider-btn.next { right: -24px; }

/* Testimonials Section */
.testimonials-section {
  background: var(--bg-dark-secondary);
}

.testimonials-container {
  max-width: 1440px;
  margin: 0 auto;
  padding: 0 40px;
}

.testimonials-carousel {
  overflow: hidden;
}

.testimonials-track {
  display: flex;
  transition: transform 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.testimonial-card {
  min-width: 100%;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 24px;
  padding: 40px;
}

.testimonial-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 24px;
}

.testimonial-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  object-fit: cover;
}

.testimonial-info h4 {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 4px;
}

.testimonial-info p {
  font-size: 14px;
  color: var(--text-muted);
}

.testimonial-transformation {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 16px;
  background: rgba(255, 107, 53, 0.1);
  padding: 16px 24px;
  border-radius: 16px;
}

.transformation-item {
  text-align: center;
}

.transformation-value {
  font-size: 20px;
  font-weight: 800;
  color: #FF6B35;
}

.transformation-label {
  font-size: 12px;
  color: var(--text-muted);
}

.transformation-arrow {
  font-size: 24px;
  color: var(--text-muted);
}

.testimonial-quote {
  font-size: 18px;
  color: var(--text-secondary);
  line-height: 1.8;
  font-style: italic;
}

.testimonials-nav {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 40px;
}

.testimonials-nav button {
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  color: var(--text-primary);
  font-size: 18px;
}

.testimonials-nav button:hover {
  background: #FF6B35;
  border-color: #FF6B35;
}

/* Equipment Section */
.equipment-section {
  background: var(--bg-dark-secondary);
}

.equipment-container {
  max-width: 1440px;
  margin: 0 auto;
  padding: 0 40px;
}

.equipment-grid {
  display: grid;
  grid-template-columns: 1.5fr 1fr;
  gap: 30px;
}

.equipment-main {
  position: relative;
  border-radius: 24px;
  overflow: hidden;
}

.equipment-main img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.equipment-info {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 40px;
  background: linear-gradient(to top, rgba(10, 10, 15, 0.95) 0%, transparent 100%);
}

.equipment-info h3 {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 12px;
}

.equipment-info p {
  font-size: 16px;
  color: var(--text-secondary);
  line-height: 1.7;
}

.equipment-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.equipment-item {
  display: flex;
  gap: 20px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 16px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.equipment-item:hover,
.equipment-item.active {
  background: rgba(255, 107, 53, 0.05);
  border-color: rgba(255, 107, 53, 0.2);
}

.equipment-item-image {
  width: 100px;
  height: 100px;
  border-radius: 12px;
  overflow: hidden;
  flex-shrink: 0;
}

.equipment-item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.equipment-item-content h4 {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 8px;
}

.equipment-item-content p {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 12px;
}

.equipment-item-meta {
  display: flex;
  gap: 16px;
}

.equipment-item-meta span {
  font-size: 13px;
  color: var(--text-muted);
}

/* Omnichannel Section */
.omnichannel-section {
  background: var(--bg-dark-secondary);
}

.omnichannel-container {
  max-width: 1440px;
  margin: 0 auto;
  padding: 0 40px;
}

.omnichannel-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 80px;
  align-items: center;
}

.omnichannel-features {
  display: flex;
  flex-direction: column;
  gap: 32px;
  margin-top: 40px;
}

.omnichannel-feature {
  display: flex;
  gap: 20px;
}

.omnichannel-feature-icon {
  width: 56px;
  height: 56px;
  background: rgba(255, 107, 53, 0.1);
  border: 1px solid rgba(255, 107, 53, 0.2);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  flex-shrink: 0;
}

.omnichannel-feature-content h4 {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 8px;
}

.omnichannel-feature-content p {
  font-size: 15px;
  color: var(--text-secondary);
  line-height: 1.7;
}

.omnichannel-visual {
  display: flex;
  justify-content: center;
}

.phone-mockup {
  position: relative;
  width: 300px;
}

.phone-frame {
  background: var(--bg-dark-tertiary);
  border-radius: 40px;
  padding: 12px;
  box-shadow: 0 40px 80px rgba(0, 0, 0, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.phone-notch {
  width: 120px;
  height: 28px;
  background: var(--bg-dark);
  border-radius: 0 0 20px 20px;
  margin: 0 auto 12px;
}

.phone-screen {
  border-radius: 28px;
  overflow: hidden;
}

.phone-screen img {
  width: 100%;
  height: auto;
  display: block;
}

.app-floating-card {
  position: absolute;
  background: var(--bg-dark-tertiary);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 16px 20px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
}

.app-floating-card.card-left {
  left: -60px;
  top: 30%;
  animation: float-card 3s ease-in-out infinite;
}

.app-floating-card.card-right {
  right: -60px;
  top: 60%;
  animation: float-card 3s ease-in-out infinite 0.5s;
}

@keyframes float-card {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.app-card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.app-card-icon {
  font-size: 20px;
}

.app-card-title {
  font-size: 13px;
  color: var(--text-muted);
}

.app-card-value {
  font-size: 24px;
  font-weight: 800;
  color: #FF6B35;
}

/* CTA Section */
.cta-section {
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.1) 0%, rgba(168, 85, 247, 0.1) 100%);
  position: relative;
  overflow: hidden;
}

.cta-section::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255, 107, 53, 0.05) 0%, transparent 50%);
  animation: rotate 30s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.cta-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 40px;
  text-align: center;
  position: relative;
  z-index: 1;
}

.cta-title {
  font-size: 48px;
  font-weight: 900;
  margin-bottom: 20px;
}

.cta-title span {
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C61 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.cta-desc {
  font-size: 18px;
  color: var(--text-secondary);
  margin-bottom: 40px;
}

.cta-form {
  display: flex;
  gap: 16px;
  max-width: 500px;
  margin: 0 auto 24px;
}

.cta-input {
  flex: 1;
  padding: 18px 24px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  font-size: 16px;
  color: var(--text-primary);
  outline: none;
  transition: all 0.3s;
}

.cta-input:focus {
  border-color: #FF6B35;
  background: rgba(255, 255, 255, 0.08);
}

.cta-input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.cta-input::placeholder {
  color: var(--text-muted);
}

.cta-guarantee {
  font-size: 14px;
  color: var(--text-muted);
}

.cta-guarantee span {
  color: #2EC4B6;
  margin-right: 4px;
}

/* CTA输入框高亮动画 */
.cta-input.input-highlight {
  animation: inputPulse 2s ease;
}

@keyframes inputPulse {
  0%, 100% {
    border-color: rgba(255, 107, 53, 0.3);
    box-shadow: 0 0 0 0 rgba(255, 107, 53, 0.4);
  }
  50% {
    border-color: #FF6B35;
    box-shadow: 0 0 20px rgba(255, 107, 53, 0.4);
  }
}

/* 注册/登录弹窗样式 */
.register-modal :deep(.n-card) {
  background: linear-gradient(145deg, #1a1a2e 0%, #16213e 100%);
  border: 1px solid rgba(255, 107, 53, 0.2);
  border-radius: 24px;
  overflow: hidden;
}

.register-modal :deep(.n-card__content) {
  padding: 0;
}

.register-modal-content {
  position: relative;
  padding: 48px 40px 40px;
}

.modal-close-btn {
  position: absolute;
  top: 20px;
  right: 20px;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.3s;
}

.modal-close-btn:hover {
  background: rgba(255, 107, 53, 0.2);
  border-color: rgba(255, 107, 53, 0.4);
  color: #FF6B35;
  transform: rotate(90deg);
}

.register-modal-header {
  text-align: center;
  margin-bottom: 32px;
}

.register-icon {
  font-size: 56px;
  margin-bottom: 16px;
  animation: giftBounce 2s ease infinite;
}

@keyframes giftBounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

.register-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.register-subtitle {
  font-size: 14px;
  color: var(--text-secondary);
}

.register-form {
  margin-bottom: 24px;
}

.register-form :deep(.n-form-item) {
  margin-bottom: 16px;
}

.input-prefix {
  margin-right: 8px;
  font-size: 18px;
}

.input-prefix-icon {
  margin-right: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.phone-input :deep(.n-input__input),
.code-input :deep(.n-input__input) {
  font-size: 16px;
}

.code-input-group {
  display: flex;
  gap: 12px;
}

.code-input {
  flex: 1;
}

.send-code-btn {
  min-width: 120px;
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.2) 0%, rgba(255, 140, 97, 0.2) 100%);
  border: 1px solid rgba(255, 107, 53, 0.3);
  color: #FF6B35;
  transition: all 0.3s;
}

.send-code-btn:not(:disabled):hover {
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.3) 0%, rgba(255, 140, 97, 0.3) 100%);
  border-color: rgba(255, 107, 53, 0.5);
}

.send-code-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.submit-btn {
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C61 100%);
  border: none;
  font-size: 16px;
  font-weight: 600;
  height: 48px;
  margin-top: 8px;
  transition: all 0.3s;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(255, 107, 53, 0.4);
}

.register-footer {
  text-align: center;
}

.agreement-text {
  font-size: 12px;
  color: var(--text-muted);
  line-height: 1.6;
}

.agreement-text a {
  color: #FF6B35;
  text-decoration: none;
  transition: color 0.3s;
}

.agreement-text a:hover {
  text-decoration: underline;
}

/* 响应式适配 */
@media (max-width: 480px) {
  .register-modal-content {
    padding: 40px 24px 32px;
  }
  
  .register-title {
    font-size: 20px;
  }
  
  .code-input-group {
    flex-direction: column;
  }
  
  .send-code-btn {
    width: 100%;
  }
}

/* Footer */
.footer {
  background: var(--bg-dark-secondary);
  border-top: 1px solid rgba(255, 255, 255, 0.05);
  padding: 80px 0 40px;
}

.footer-container {
  max-width: 1440px;
  margin: 0 auto;
  padding: 0 40px;
}

.footer-grid {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr 2fr;
  gap: 60px;
  margin-bottom: 60px;
}

.footer-brand {
  max-width: 320px;
}

.footer-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.footer-logo-icon {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C61 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
}

.footer-logo-text {
  font-size: 24px;
  font-weight: 900;
}

.footer-desc {
  font-size: 15px;
  color: var(--text-secondary);
  line-height: 1.8;
  margin-bottom: 24px;
}

.footer-social {
  display: flex;
  gap: 12px;
}

.footer-social a {
  width: 44px;
  height: 44px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  transition: all 0.3s;
}

.footer-social a:hover {
  background: #FF6B35;
  border-color: #FF6B35;
  transform: translateY(-3px);
}

.footer-title {
  font-size: 16px;
  font-weight: 700;
  margin-bottom: 24px;
}

.footer-links {
  list-style: none;
  padding: 0;
  margin: 0;
}

.footer-links li {
  margin-bottom: 12px;
}

.footer-links a {
  font-size: 15px;
  color: var(--text-secondary);
  text-decoration: none;
  transition: color 0.3s;
}

.footer-links a:hover {
  color: #FF6B35;
}

.footer-contact-item {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.footer-contact-icon {
  width: 44px;
  height: 44px;
  background: rgba(255, 107, 53, 0.1);
  border: 1px solid rgba(255, 107, 53, 0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.footer-contact-content h4 {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 4px;
}

.footer-contact-content p {
  font-size: 14px;
  color: var(--text-secondary);
}

.footer-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 40px;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
}

.footer-bottom p {
  font-size: 14px;
  color: var(--text-muted);
}

.footer-bottom-links {
  display: flex;
  gap: 24px;
}

.footer-bottom-links a {
  font-size: 14px;
  color: var(--text-muted);
  text-decoration: none;
  transition: color 0.3s;
}

.footer-bottom-links a:hover {
  color: #FF6B35;
}

/* Login Modal */
.login-footer {
  text-align: center;
  margin-top: 16px;
  color: var(--text-gray);
}

.login-footer a {
  color: #FF6B35;
  cursor: pointer;
  margin-left: 4px;
}

.login-footer a:hover {
  text-decoration: underline;
}

/* Reveal Animation */
[v-intersect] {
  opacity: 0;
  transform: translateY(40px);
  transition: all 0.8s cubic-bezier(0.4, 0, 0.2, 1);
}

[v-intersect].active {
  opacity: 1;
  transform: translateY(0);
}

/* Responsive */
@media (max-width: 1200px) {
  .hero-content { grid-template-columns: 1fr; text-align: center; }
  .hero-text { max-width: 100%; }
  .hero-actions { justify-content: center; }
  .hero-stats-preview { justify-content: center; }
  .hero-visual { height: 450px; }
  .philosophy-grid { grid-template-columns: 1fr; gap: 50px; }
  .philosophy-content { max-width: 100%; }
  .ai-features-grid { grid-template-columns: 1fr; }
  .courses-grid { grid-template-columns: repeat(2, 1fr); }
  .membership-slide { flex: 0 0 50%; }
  .coaches-slide { flex: 0 0 33.333%; }
  .equipment-grid { grid-template-columns: 1fr; }
  .omnichannel-grid { grid-template-columns: 1fr; gap: 50px; }
  .omnichannel-visual { order: -1; }
  .footer-grid { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 992px) {
  .hero-title { font-size: 48px; }
  .hero-visual { height: 400px; }
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .stat-item:not(:last-child)::after { display: none; }
  .section-title { font-size: 36px; }
}

@media (max-width: 768px) {
  .navbar-container { padding: 0 20px; }
  .nav-links { display: none; }
  .mobile-menu-btn { display: flex; }
  .hero-section { padding: 120px 0 60px; }
  .hero-section.has-announcement { padding-top: 160px; }
  .hero-container { padding: 0 20px; }
  .hero-title { font-size: 36px; }
  .hero-actions { flex-direction: column; }
  .hero-actions .btn { width: 100%; justify-content: center; }
  .section { padding: 60px 0; }
  .section-header { margin-bottom: 40px; }
  .philosophy-container,
  .ai-features-container,
  .courses-container,
  .membership-container,
  .coaches-container,
  .testimonials-container,
  .equipment-container,
  .omnichannel-container,
  .cta-container,
  .footer-container { padding: 0 20px; }
  .philosophy-values { grid-template-columns: 1fr; }
  .courses-grid { grid-template-columns: 1fr; }
  .membership-slide { flex: 0 0 100%; }
  .membership-slide.featured { transform: scale(1); }
  .coaches-slide { flex: 0 0 50%; }
  .testimonial-header { flex-direction: column; text-align: center; }
  .testimonial-transformation { margin-left: 0; margin-top: 20px; }
  .equipment-item { flex-direction: column; }
  .equipment-item-image { width: 100%; height: 180px; }
  .phone-mockup { width: 260px; }
  .app-floating-card { display: none; }
  .cta-title { font-size: 32px; }
  .cta-form { flex-direction: column; }
  .footer-grid { grid-template-columns: 1fr; gap: 40px; }
  .footer-bottom { flex-direction: column; gap: 20px; text-align: center; }
}

@media (max-width: 480px) {
  .hero-title { font-size: 28px; }
  .hero-badge { font-size: 12px; padding: 8px 16px; }
  .section-title { font-size: 28px; }
  .coaches-slide { flex: 0 0 100%; }
}
</style>
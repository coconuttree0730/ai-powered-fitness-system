<template>
  <div class="home-page">
    <header class="header">
      <div class="container header-content">
        <div class="logo">
          <h1>智能健身房</h1>
        </div>
        <nav class="nav">
          <router-link to="/">首页</router-link>
          <router-link to="/courses">课程</router-link>
          <router-link to="/courses" v-if="!authStore.isLoggedIn">器材</router-link>
          <template v-if="authStore.isLoggedIn">
            <router-link to="/member" v-if="authStore.isMember">会员中心</router-link>
            <router-link to="/coach" v-if="authStore.isCoach">教练中心</router-link>
            <router-link to="/admin" v-if="authStore.isAdmin">管理后台</router-link>
            <a @click="handleLogout">退出</a>
          </template>
          <template v-else>
            <router-link to="/login">登录</router-link>
            <router-link to="/register">注册</router-link>
          </template>
        </nav>
      </div>
    </header>

    <section class="hero">
      <div class="container">
        <h2>开启您的智能健身之旅</h2>
        <p>AI驱动的个性化健身计划，让每一次训练都更有效</p>
        <div class="hero-actions">
          <router-link to="/courses" class="btn btn-primary">浏览课程</router-link>
          <router-link to="/register" class="btn btn-outline" v-if="!authStore.isLoggedIn">立即加入</router-link>
        </div>
      </div>
    </section>

    <section class="carousel-section">
      <div class="container">
        <h3>热门课程</h3>
        <n-carousel autoplay show-arrow>
          <div v-for="course in hotCourses" :key="course.id" class="carousel-item">
            <div class="course-card" @click="goToCourse(course.id)">
              <img :src="course.imageUrl || '/placeholder.jpg'" :alt="course.courseName" />
              <div class="course-info">
                <h4>{{ course.courseName }}</h4>
                <p>{{ course.description }}</p>
                <span class="category">{{ course.category }}</span>
              </div>
            </div>
          </div>
        </n-carousel>
      </div>
    </section>

    <section class="features">
      <div class="container">
        <h3>我们的特色</h3>
        <div class="feature-grid">
          <div class="feature-item">
            <div class="icon">🎯</div>
            <h4>AI智能计划</h4>
            <p>根据您的身体数据和目标，AI生成个性化健身计划</p>
          </div>
          <div class="feature-item">
            <div class="icon">📅</div>
            <h4>课程预约</h4>
            <p>丰富的健身课程，一键预约，轻松管理</p>
          </div>
          <div class="feature-item">
            <div class="icon">💪</div>
            <h4>专业教练</h4>
            <p>经验丰富的教练团队，指导您的每一次训练</p>
          </div>
          <div class="feature-item">
            <div class="icon">📊</div>
            <h4>数据分析</h4>
            <p>追踪训练进度，数据驱动健身效果</p>
          </div>
        </div>
      </div>
    </section>

    <footer class="footer">
      <div class="container">
        <p>&copy; 2024 智能健身房系统 - 您的专属健身伙伴</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getPublicCourses } from '@/api/course'

const router = useRouter()
const authStore = useAuthStore()
const hotCourses = ref([])

onMounted(async () => {
  try {
    const res = await getPublicCourses({ pageNum: 1, pageSize: 5 })
    if (res.data) {
      hotCourses.value = res.data.records || res.data || []
    }
  } catch (error) {
    console.error('获取热门课程失败:', error)
  }
})

function goToCourse(id) {
  router.push(`/courses/${id}`)
}

function handleLogout() {
  authStore.logout()
}
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
}

.logo h1 {
  font-size: 24px;
  color: #1890ff;
  margin: 0;
}

.nav {
  display: flex;
  gap: 24px;
}

.nav a {
  color: #333;
  text-decoration: none;
  font-weight: 500;
  cursor: pointer;
  transition: color 0.3s;
}

.nav a:hover {
  color: #1890ff;
}

.hero {
  background: linear-gradient(135deg, #1890ff 0%, #722ed1 100%);
  color: #fff;
  padding: 80px 20px;
  text-align: center;
}

.hero h2 {
  font-size: 48px;
  margin-bottom: 16px;
}

.hero p {
  font-size: 20px;
  opacity: 0.9;
  margin-bottom: 32px;
}

.hero-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.btn {
  padding: 12px 32px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.3s;
}

.btn-primary {
  background: #fff;
  color: #1890ff;
}

.btn-primary:hover {
  background: #f0f0f0;
}

.btn-outline {
  background: transparent;
  border: 2px solid #fff;
  color: #fff;
}

.btn-outline:hover {
  background: rgba(255, 255, 255, 0.1);
}

.carousel-section {
  padding: 60px 20px;
  background: #f5f5f5;
}

.carousel-section h3 {
  text-align: center;
  font-size: 32px;
  margin-bottom: 32px;
}

.carousel-item {
  padding: 0 10px;
}

.course-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
}

.course-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.course-card img {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.course-info {
  padding: 20px;
}

.course-info h4 {
  font-size: 18px;
  margin-bottom: 8px;
}

.course-info p {
  color: #666;
  font-size: 14px;
  margin-bottom: 12px;
}

.category {
  display: inline-block;
  padding: 4px 12px;
  background: #e6f7ff;
  color: #1890ff;
  border-radius: 4px;
  font-size: 12px;
}

.features {
  padding: 60px 20px;
}

.features h3 {
  text-align: center;
  font-size: 32px;
  margin-bottom: 48px;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 32px;
}

.feature-item {
  text-align: center;
  padding: 32px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.feature-item h4 {
  font-size: 20px;
  margin-bottom: 12px;
}

.feature-item p {
  color: #666;
  font-size: 14px;
}

.footer {
  background: #333;
  color: #fff;
  padding: 24px 20px;
  text-align: center;
  margin-top: auto;
}
</style>

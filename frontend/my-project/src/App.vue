<template>
  <div class="app">
    <header>
      <nav>
        <div class="logo">{{ $t('app.title') }}</div>
        <div class="nav-links">
          <router-link to="/">{{ $t('nav.home') }}</router-link>
          <router-link to="/prizes">{{ $t('nav.prizes') }}</router-link>
          <router-link to="/history">{{ $t('nav.history') }}</router-link>
          <router-link to="/habits">{{ $t('nav.habits') }}</router-link>
          <router-link to="/plans">{{ $t('nav.plans') }}</router-link>
          <router-link to="/plan-points">{{ $t('nav.planPoints') }}</router-link>
        </div>
        
        <!-- 语言切换器 -->
        <div class="language-selector">
          <button @click="toggleLanguage" class="lang-btn">
            {{ currentLanguage === 'zh' ? 'English' : '中文' }}
          </button>
        </div>
      </nav>
    </header>
    
    <main>
      <router-view />
    </main>
  </div>
</template>

<script>
export default {
  name: 'App',
  data() {
    return {
      currentLanguage: this.$i18n.locale
    };
  },
  methods: {
    toggleLanguage() {
      // 切换语言
      const newLocale = this.currentLanguage === 'zh' ? 'en' : 'zh';
      this.$i18n.locale = newLocale;
      this.currentLanguage = newLocale;
      
      // 保存用户偏好到 localStorage
      localStorage.setItem('locale', newLocale);
    }
  }
};
</script>

<style>
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

body {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  line-height: 1.6;
  color: #333;
  background-color: #f8f9fa;
}

.app {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

header {
  background-color: #4CAF50;
  color: white;
  box-shadow: 0 2px 5px rgba(0,0,0,0.1);
}

nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1200px;
  margin: 0 auto;
  padding: 15px;
}

.logo {
  font-size: 1.5rem;
  font-weight: bold;
}

.nav-links {
  display: flex;
  gap: 20px;
}

.nav-links a {
  color: white;
  text-decoration: none;
  padding: 5px 10px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.nav-links a:hover {
  background-color: rgba(255,255,255,0.1);
}

.router-link-active {
  background-color: rgba(255,255,255,0.2);
  font-weight: bold;
}

main {
  flex: 1;
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

.language-selector {
  margin-left: 20px;
}

.lang-btn {
  background-color: #2196F3;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 6px 12px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.lang-btn:hover {
  background-color: #1976D2;
}

/* Responsive styling for mobile */
@media screen and (max-width: 768px) {
  nav {
    flex-direction: column;
    gap: 15px;
  }
  
  .nav-links {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .language-selector {
    margin-left: 0;
    margin-top: 10px;
  }
}
</style>
import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import { createI18n } from 'vue-i18n';
import zh from './i18n/locales/zh.json';
import en from './i18n/locales/en.json';

const i18n = createI18n({
  legacy: true,
  locale: 'zh',
  fallbackLocale: 'en',
  messages: { zh, en },
});

// 创建Vue应用实例
const app = createApp(App);

// 使用插件
app.use(router);
app.use(i18n);

// 挂载到DOM
app.mount('#app');

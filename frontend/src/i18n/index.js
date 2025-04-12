import { createI18n } from 'vue-i18n';
import en from './locales/en.json';
import zh from './locales/zh.json';

// 检查用户之前保存的语言偏好
const savedLocale = localStorage.getItem('locale') || 'zh';

export default createI18n({
  legacy: false, // 使用组合式API
  locale: savedLocale, // 默认语言
  fallbackLocale: 'en', // 回退语言
  messages: {
    en,
    zh
  }
});

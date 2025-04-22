import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import { createPinia } from 'pinia';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';

const app = createApp(App);
app.use(router);
app.use(createPinia());
app.use(ElementPlus);
app.mount('#app');

// 调整后的错误处理函数
const resizeObserverErrHandler = (event) => {
    // 从事件对象中获取错误信息
    const errorMessage = event.error?.message;
    // 检查是否为特定的 ResizeObserver 错误
    if (errorMessage && errorMessage.includes('ResizeObserver loop completed with undelivered notifications')) {
        // 阻止默认的错误处理（忽略该错误）
        event.preventDefault();
    }
};

// 监听全局 error 事件
window.addEventListener('error', resizeObserverErrHandler);
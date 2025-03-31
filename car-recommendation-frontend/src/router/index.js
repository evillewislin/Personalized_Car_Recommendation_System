import { createRouter, createWebHistory } from 'vue-router';
import Home from '../views/Home.vue';
import Login from '../views/Login.vue';
import Cars from '../views/Cars.vue';
import Register from '../views/Register.vue';
import UserCenter from '../views/UserCenter.vue';
import AdminDashboard from '../views/AdminDashboard.vue';
import Recommendations from "@/views/Recommendations.vue";
import AdminLogin from "@/views/AdminLogin.vue";

const routes = [
    { path: '/', name: 'Home', component: Home },
    { path: '/login', name: 'Login', component: Login },
    { path: '/adminlogin', name: 'AdminLogin', component: AdminLogin },
    { path: '/register', name: 'Register', component: Register },
    { path: '/user', name: 'UserCenter', component: UserCenter },
    { path: '/admin', name: 'AdminDashboard', component: AdminDashboard },
    { path: '/recommendations', name: 'Recommendations', component: Recommendations },
    { path: '/cars', name: 'Cars', component: Cars }

];

const router = createRouter({
    history: createWebHistory(),
    routes
});
router.beforeEach((to, from, next) => {
    // 检查可能为空的对象
    if (to.meta.someObject && to.meta.someObject.component) {
        // 执行相关操作
    }
    next();
});
export default router;

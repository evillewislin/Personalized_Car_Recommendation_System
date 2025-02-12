import { createRouter, createWebHistory } from 'vue-router';
import Home from '../views/Home.vue';
import Login from '../views/Login.vue';
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
    { path: '/recommendations', name: 'Recommendations', component: Recommendations }

];

const router = createRouter({
    history: createWebHistory(),
    routes
});

export default router;

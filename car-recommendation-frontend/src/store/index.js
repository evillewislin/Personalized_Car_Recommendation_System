import { defineStore } from 'pinia';

export const useUserStore = defineStore('user', {
    state: () => ({
        token: localStorage.getItem('token') || '',
        username: localStorage.getItem('username') || '',
        role: localStorage.getItem('role') || 'user'
    }),
    actions: {
        setToken(token) {
            this.token = token;
            localStorage.setItem('token', token);
        },
        setUsername(username) {
            this.username = username;
            localStorage.setItem('username', username);
        },
        setRole(role) {
            this.role = role;
            localStorage.setItem('role', role);
        },
        logout() {
            this.token = '';
            this.username = '';
            this.role = 'user';
            localStorage.removeItem('token');
            localStorage.removeItem('username');
            localStorage.removeItem('role');
        }
    }
});

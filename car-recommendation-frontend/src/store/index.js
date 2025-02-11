import { defineStore } from 'pinia';

export const useUserStore = defineStore('user', {
    state: () => ({
        token: '',
        username: ''
    }),
    actions: {
        setToken(token) {
            this.token = token;
        },
        setUsername(username) {
            this.username = username;
        }
    }
});

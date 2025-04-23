module.exports = {
    transpileDependencies: [
        'chartjs-adapter-date-fns'
    ],
    devServer: {
        client: {
            overlay: false // 关闭错误浮层
        },
        proxy: {
            '/api': {
                target: 'http://localhost:8090',
                changeOrigin: true
            }
        }
    }
};
module.exports = {
    transpileDependencies: [
        'chartjs-adapter-date-fns'
    ],
    devServer: {
        proxy: {
            '/api': {
                target: 'http://localhost:8089',
                changeOrigin: true
            }
        }
    }
}

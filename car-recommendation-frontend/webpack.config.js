const webpack = require('webpack');
const path = require('path');

module.exports = {
    plugins: [
        new webpack.DefinePlugin({
            __VUE_PROD_HYDRATION_MISMATCH_DETAILS__: false,
            // 可以根据需要添加其他特性标志
            __VUE_OPTIONS_API__: true,
            __VUE_PROD_DEVTOOLS__: false
        })
    ]
};
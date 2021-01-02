const path = require('path');
const srcRoot = "./src"
const HtmlWebPackPlugin = require('html-webpack-plugin');
const BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin;
const MiniCssPlugin = require("mini-css-extract-plugin");

module.exports = {
    entry: {
        index: './src/index.js'
    },
    output: {
        filename: "./build/bundle.js",
    },

    module: {
        // 加载器配置
        rules: [
            {
                test: /\.(js|jsx)$/,
                use: [{loader: 'babel-loader'}],
                include: path.resolve(srcRoot)
            },
            {
                test: /\.css$/,
                use:[MiniCssPlugin.loader,'css-loader']
            }
        ]
    },

    plugins: [
        new HtmlWebPackPlugin({
            template: './public/index.html',
            filename: './index.html'
        }),
        new MiniCssPlugin({
            filename:'./css/[name].css'
        }),

        new BundleAnalyzerPlugin()
    ],

    // dev 代理
    devServer: {
        proxy: {
            '/**': 'http://localhost:8080'
        }
    }
};
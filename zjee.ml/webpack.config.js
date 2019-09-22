const path = require('path');
const srcRoot = "./src"
const HtmlWebPackPlugin = require('html-webpack-plugin');

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
            use: [{loader:'babel-loader'}] ,
            include: path.resolve(srcRoot)
          },
          {
            test: /\.css$/,
            use: ['style-loader', 'css-loader']
          }
      ]
  },

  plugins: [
    new HtmlWebPackPlugin({
      template: './public/index.html',
      filename: './index.html'
    })
  ]
};
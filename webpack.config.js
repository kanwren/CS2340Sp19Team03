const webpack = require('webpack');
const path = require('path');

module.exports = {
    entry: ['babel-polyfill', './ui/entry.js'],
    output: { path: path.resolve(__dirname, 'public/compiled'), filename: 'bundle.js' },
    module: {
        rules: [ {
                test: /\.jsx?$/,
                include: /ui/,
                use: {
                    loader: 'babel-loader',
                    options: {
                        presets: [ 'react', 'es2015', 'stage-0' ]
                    }
                }
        } ]
    }
};
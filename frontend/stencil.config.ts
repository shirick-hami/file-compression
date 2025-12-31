import {Config} from '@stencil/core';
import replace from "@rollup/plugin-replace";

const isProd = process.env.NODE_ENV === 'production';

export const config: Config = {
    namespace: 'file-compression',
    globalStyle: 'src/global/app.css',
    globalScript: 'src/global/app.ts',
    taskQueue: 'async',
    outputTargets: [
        {
            type: 'dist',
            esmLoaderPath: '../loader',
        },
        {
            type: 'dist-custom-elements',
            customElementsExportBehavior: 'auto-define-custom-elements',
            externalRuntime: false,
        },
        {
            type: 'www',
            dir: 'www',                                    // Output directly to www/
            baseUrl: !isProd ? '/file-compression/' : '/',
            serviceWorker: null,
            empty: true,                                   // Clean the folder before build
        },
    ],
    devServer: {
        port: 3334,
        reloadStrategy: 'pageReload',
    },
    rollupPlugins: {
        before: [
            replace({
                preventAssignment: true,
                values: {
                    // Replace the import path based on environment
                    '../environments/environment': isProd
                        ? '../environments/environment.prod'
                        : '../environments/environment',
                    './environments/environment': isProd
                        ? './environments/environment.prod'
                        : './environments/environment',
                },
            }),
        ],
    },
};

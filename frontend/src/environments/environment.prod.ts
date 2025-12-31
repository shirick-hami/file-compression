const API_URL = 'https://your-backend-server.com/api/v1/fc';

export const environment = {
    production: true,
    apiUrl: API_URL,
    authEnabled: true,
    logLevel: 'debug',
    pollingHealthInSeconds: 15,
    developer: 'Saptarick Mishra',
    githubUrl: 'https://github.com/shirick-hami/file-compression',
    githubRepoCloneCommand: 'git clone git@github.com:shirick-hami/file-compression.git',
    githubRepoCloneCommandHttp: 'git clone https://github.com/shirick-hami/file-compression.git',
    swaggerUrl: `${API_URL}/swagger-ui.html`,
    restDocsUrl: `${API_URL}/api-docs`
};
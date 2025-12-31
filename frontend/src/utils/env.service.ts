import {environment} from "../environments/environment";

export const env = environment;

export const getApiUrl = (): string => {
    return env.apiUrl;
};

export const isAuthEnabled = (): boolean => {
    return env.authEnabled;
};

export const getPollingHealthInSeconds = (): number => {
    return env.pollingHealthInSeconds;
};

export const getDeveloper = (): string => {
    return env.developer;
};

export const getGithubUrl = (): string => {
    return env.githubUrl;
};

export const getGithubRepoCloneCommand = (): string => {
    return env.githubRepoCloneCommand;
};

export const getGithubRepoHttpCloneCommand = (): string => {
    return env.githubRepoCloneCommandHttp;
};

export const getSwaggerUrl = (): string => {
    return env.swaggerUrl;
};

export const getRestDocsUrl = (): string => {
    return env.restDocsUrl;
};


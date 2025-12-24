type RuntimeEnv = {
  BACKEND_URL: string
  API_TIMEOUT_MILLIS: string
}

const runtimeEnv = (window as any)._env_ as RuntimeEnv | undefined

export const ENV = {
  BACKEND_URL: runtimeEnv?.BACKEND_URL ?? '',
  API_TIMEOUT_MILLIS: runtimeEnv?.API_TIMEOUT_MILLIS ?? '0'
}
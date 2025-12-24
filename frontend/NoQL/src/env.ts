// src/env.ts
type RuntimeEnv = {
  BACKEND_URL: string
  API_TIMEOUT_MILLIS: string
}

const runtimeEnv = (window as any)._env_ as RuntimeEnv | undefined

export const ENV: RuntimeEnv = runtimeEnv ?? {
  BACKEND_URL: import.meta.env.VITE_BACKEND_URL ?? '',
  API_TIMEOUT_MILLIS: import.meta.env.VITE_API_TIMEOUT_MILLIS ?? '0'
}
import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse } from 'axios'
import { log } from '../loging/logger.ts'
import { localStorageService } from '../LocalStorageService.ts'
import { authenticationApi } from './authenticationApi.ts'
import { ENV } from '@/env'


/** query parameter */
interface ApiParameter {
  name: string
  value: string | number | boolean
}

class Api {
  axiosInstance: AxiosInstance = axios.create({
    baseURL: ENV.BACKEND_URL,
    timeout: Number(ENV.API_TIMEOUT_MILLIS)
  })

  /** forbid constructor, because api is a singleton */
  private constructor() {
    log.info('BE URL is: ' + this.axiosInstance.defaults.baseURL)

    // Add a request interceptor that inserts an auth token into headers.
    this.axiosInstance.interceptors.request.use(
      (config) => {
        const token = localStorageService.getAccessToken()
        if (token) {
          config.headers['Authorization'] = `Bearer ${token}`
        }
        return config
      },
      (error) => {
        return Promise.reject(error)
      }
    )

    // Add a response interceptor to handle 401 errors and refresh the token.
    this.axiosInstance.interceptors.response.use(
      (response) => response,
      async (error) => {
        const originalRequest = error.config
        if (error.response.status === 401 && !originalRequest._retry) {
          originalRequest._retry = true
          try {
            const response = await authenticationApi.refreshToken()
            localStorageService.setAccessToken(response.data.accessToken)
            localStorageService.setRefreshToken(response.data.refreshToken)
            return this.axiosInstance(originalRequest)
          } catch (refreshError) {
            localStorageService.clearAccessToken()
            localStorageService.clearRefreshToken()
            localStorageService.clearUserId()
            window.location.href = '/login' // redirect to login page to obtain new refresh token
            return Promise.reject(refreshError)
          }
        }
        return Promise.reject(error)
      }
    )
  }

  static instance: Api | null = null

  /**
   * @return singleton instance
   */
  static getInstance(): Api {
    if (this.instance == null) {
      this.instance = new Api()
    }
    return this.instance
  }

  /**
   * GET
   * @param path in url
   * @param parameters query parameters
   */
  get(path: string, parameters: ApiParameter[] = []): Promise<AxiosResponse> {
    const requestConfig: AxiosRequestConfig = {
      url: this.createUrl(path, parameters),
      method: 'GET'
    }

    return this.axiosInstance.request(requestConfig)
  }

  /**
   * POST
   * @param path in url
   * @param data request body
   * @param parameters query parameters
   * @param headers http(s) headers
   */
  post(path: string,
       data: string | number | boolean | object | null,
       parameters: ApiParameter[] = [],
       headers: { [key: string]: string } = {}
  ): Promise<AxiosResponse> {

    const requestConfig: AxiosRequestConfig = {
      url: this.createUrl(path, parameters),
      method: 'POST',
      data: data,
      headers: headers
    }

    return this.axiosInstance.request(requestConfig)
  }

  /**
   * PUT
   * @param path in url
   * @param data request body
   * @param parameters query parameters
   */
  put(path: string,
      data: string | number | boolean | object | null,
      parameters: ApiParameter[] = []): Promise<AxiosResponse> {

    const requestConfig: AxiosRequestConfig = {
      url: this.createUrl(path, parameters),
      method: 'PUT',
      data: data
    }

    return this.axiosInstance.request(requestConfig)
  }

  /**
   * DELETE
   * @param path in url
   * @param parameters query parameters
   */
  delete(path: string, parameters: ApiParameter[] = []): Promise<AxiosResponse> {
    const requestConfig: AxiosRequestConfig = {
      url: this.createUrl(path, parameters),
      method: 'DELETE'
    }

    return this.axiosInstance.request(requestConfig)
  }

  /**
   * Create url from path and query parameters.
   * @param path path in url
   * @param parameters query parameters
   */
  private createUrl(path: string, parameters: ApiParameter[] = []): string {
    let delimiter = '?'
    for (const parameter of parameters) {
      path = path + delimiter + parameter.name + '=' + parameter.value
      delimiter = '&'
    }
    return path
  }
}

export default Api
export type { ApiParameter }

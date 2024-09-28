import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse } from 'axios'
import { log } from '../loging/logger.ts'

/** query parameter */
interface ApiParameter {
  name: string
  value: string | number | boolean
}

class Api {
  axiosInstance: AxiosInstance = axios.create({
    baseURL: import.meta.env.VITE_BACKEND_URL,
    timeout: parseInt(import.meta.env.VITE_API_TIMEOUT_MILIS),
  })

  /** forbid constructor, because api is a singleton */
  private constructor() {
    log.info("BE URL is: " + this.axiosInstance.defaults.baseURL)

    // Add a request interceptor
    this.axiosInstance.interceptors.request.use(
      (config) => {
        const token = localStorage.getItem('token')
        if (token) {
          config.headers['Authorization'] = `Bearer ${token}`
        }
        return config
      },
      (error) => {
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
      method: 'GET',
    }

    return this.axiosInstance.request(requestConfig)
  }

  /**
   * POST
   * @param path in url
   * @param data request body
   * @param parameters query parameters
   * @param headers http(s) headers
   * @param authenticate if true, then jwt is added to headers
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
      data: data,
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
      method: 'DELETE',
    }

    return this.axiosInstance.request(requestConfig)
  }

  /**
   * Create url from path and query parameters.
   * @param path path in url
   * @param parameters query parameters
   */
  createUrl(path: string, parameters: ApiParameter[] = []): string {
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

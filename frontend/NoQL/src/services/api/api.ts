import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse } from 'axios'
import { log } from '../loging/logger.ts'

// TODO: properly setup
const jwt = 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJob256aWtAZ21haWwuY29tIiwiaWF0IjoxNzI1MTE3MjQ1LCJleHAiOjE3MjUyMDM2NDV9.4oCm9owj7de-IsYqU8KJrQVaG8WYqeeWx2jAsjPJ8wxhAltW1YkMAc9cs2R2Ckhzh7v3Vg8RhRDQor8WPW7luw'

const jwtValue = 'Bearer ' + jwt

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
      headers: {
        'Authorization': jwtValue
      }
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
       headers: { [key: string]: string } = {},
       authenticate: boolean  = true
  ): Promise<AxiosResponse> {

    const requestConfig: AxiosRequestConfig = {
      url: this.createUrl(path, parameters),
      method: 'POST',
      data: data,
      headers: authenticate
        ? { ...headers, 'Authorization': jwtValue }
        : headers
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
      headers: {
        'Authorization': jwtValue
      }
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
      headers: {
        'Authorization': jwtValue
      }
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

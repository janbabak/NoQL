import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse } from 'axios'

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
  }

  static instance: Api | null = null

  /**
   * @return singleton instance
   */
  static getInstance(): Api {
    if (this.instance == null) {
      this.instance = new Api()
    }
    // TODO use log
    console.log('BE URL is: ' + this.instance.axiosInstance.defaults.baseURL)
    return this.instance
  }

  /**
   * GET
   * @param path in url
   * @param parameters query parameters
   */
  get(path: string, parameters: ApiParameter[] = []): Promise<AxiosResponse> {
    const requestConfig = {
      url: this.createUrl(path, parameters),
      method: 'GET'
    } as AxiosRequestConfig

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

    const requestConfig = {
      url: this.createUrl(path, parameters),
      method: 'POST',
      data: data,
      headers: headers,
    } as AxiosRequestConfig

    return this.axiosInstance.request(requestConfig)
  }

  /**
   * PUT
   * @param path in url
   * @param data request body
   * @param parameters query parameters
   */
  put(path: string, data: string | null, parameters: ApiParameter[] = []):
    Promise<AxiosResponse> {
    const requestConfig = {
      url: this.createUrl(path, parameters),
      method: 'PUT',
      data: data
    } as AxiosRequestConfig

    return this.axiosInstance.request(requestConfig)
  }

  /**
   * DELETE
   * @param path in url
   * @param parameters query parameters
   */
  delete(path: string, parameters: ApiParameter[] = []): Promise<AxiosResponse> {
    const requestConfig = {
      url: this.createUrl(path, parameters),
      method: 'DELETE'
    } as AxiosRequestConfig

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

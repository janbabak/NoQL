import axios, { AxiosResponse } from 'axios'
import { AuthenticationRequest, AuthenticationResponse, RegisterRequest } from '../../types/Authentication.ts'
import { localStorageService } from '../LocalStorageService.ts'

export const authenticationApi = {
  DOMAIN: '/auth',

  axiosInstance: axios.create({
    baseURL: import.meta.env.VITE_BACKEND_URL,
    timeout: parseInt(import.meta.env.VITE_API_TIMEOUT_MILIS)
  }),

  /**
   * Authenticate existing user
   * @param request data
   */
  authenticate(request: AuthenticationRequest): Promise<AxiosResponse<AuthenticationResponse>> {
    return this.axiosInstance.request({
      url: this.DOMAIN + '/authenticate',
      method: 'POST',
      data: request
    })
  },

  /**
   * Register new user
   * @param request data
   */
  register(request: RegisterRequest): Promise<AxiosResponse<AuthenticationResponse>> {
    return this.axiosInstance.request({
      url: this.DOMAIN + '/register',
      method: 'POST',
      data: request
    })
  },

  /**
   * Obtain new access token using refresh token. If refresh token is not available, reject promise.
   * @private
   */
  refreshToken(): Promise<AxiosResponse<AuthenticationResponse>> {
    const refreshToken = localStorageService.getRefreshToken()
    if (!refreshToken) {
      return Promise.reject(new Error('No refresh token available'))
    }

    return this.axiosInstance.request({
      url: this.DOMAIN + '/refreshToken',
      method: 'POST',
      data: refreshToken
    })
  },
}
import Api from './api.ts'
import { AxiosResponse } from 'axios'
import { AuthenticationRequest, AuthenticationResponse, RegisterRequest } from '../../types/Authentication.ts'

export const authenticationApi = {
  API: Api.getInstance(),
  DOMAIN: '/auth',

  /**
   * Authenticate existing user
   * @param request data
   */
  authenticate(request: AuthenticationRequest): Promise<AxiosResponse<AuthenticationResponse>> {
    return this.API.post(this.DOMAIN + '/authenticate', request, [], {}, false)
  },

  /**
   * Register new user
   * @param request data
   */
  register(request: RegisterRequest): Promise<AxiosResponse<AuthenticationResponse>> {
    return this.API.post(this.DOMAIN + '/register', request, [], {}, false)
  }
}
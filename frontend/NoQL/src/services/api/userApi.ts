import Api from './api.ts'
import { AxiosResponse } from 'axios'
import { User } from '../../types/Authentication.ts'

const userApi = {
  API: Api.getInstance(),
  DOMAIN: '/user',

  /**
   * Get user by id.
   * @param id user identifier
   */
  getById(id: string): Promise<AxiosResponse<User>> {
    return this.API.get(this.DOMAIN + '/' + id)
  }
}

export default userApi
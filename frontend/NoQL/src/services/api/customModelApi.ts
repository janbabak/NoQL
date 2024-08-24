import Api from './api.ts'
import { AxiosResponse } from 'axios'
import { CustomModel } from '../../types/CustomModel.ts'

const customModelApi = {
  API: Api.getInstance(),
  DOMAIN: '/model',

  /**
   * Get all custom models.
   */
  getAll(): Promise<AxiosResponse<CustomModel[]>> {
    return this.API.get(this.DOMAIN)
  },

  /**
   * Delete custom model by id.
   */
  delete(id: string): Promise<AxiosResponse<void>> {
    return this.API.delete(this.DOMAIN + '/' + id)
  },
}

export default customModelApi
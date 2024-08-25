import Api from './api.ts'
import { AxiosResponse } from 'axios'
import { CreateUpdateCustomModelRequest, CustomModel } from '../../types/CustomModel.ts'

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
   * Create new custom model.
   */
  create(customModel: CreateUpdateCustomModelRequest): Promise<AxiosResponse<CustomModel>> {
    return this.API.post(this.DOMAIN, customModel)
  },

  /**
   * Delete custom model by id.
   */
  delete(id: string): Promise<AxiosResponse<void>> {
    return this.API.delete(this.DOMAIN + '/' + id)
  },
}

export default customModelApi
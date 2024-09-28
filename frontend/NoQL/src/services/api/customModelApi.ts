import Api from './api.ts'
import { AxiosResponse } from 'axios'
import {
  CustomModel,
  CreateCustomModelRequest,
  UpdateCustomModelRequest,
  ModelOption
} from '../../types/CustomModel.ts'

const customModelApi = {
  API: Api.getInstance(),
  DOMAIN: '/model',

  /**
   * Get all custom models.
   */
  getAllCustomModels(userId: string | null = null): Promise<AxiosResponse<CustomModel[]>> {
    return this.API.get(this.DOMAIN, userId ? [{ name: 'userId', value: userId }] : [])
  },

  /**
   * Get all models.
   */
  getAllModels(): Promise<AxiosResponse<ModelOption[]>> {
    return this.API.get(this.DOMAIN + '/all')
  },

  /**
   * Get custom model by id.
   * @param id
   */
  getById(id: string): Promise<AxiosResponse<CustomModel>> {
    return this.API.get(this.DOMAIN + '/' + id)
  },

  /**
   * Create new custom model.
   */
  create(customModel: CreateCustomModelRequest): Promise<AxiosResponse<CustomModel>> {
    return this.API.post(this.DOMAIN, customModel)
  },

  /**
   * Update custom model.
   * @param id custom model identifier
   * @param customModel fields to update
   */
  update(id: string, customModel: UpdateCustomModelRequest): Promise<AxiosResponse<CustomModel>> {
    return this.API.put(this.DOMAIN + '/' + id, customModel)
  },

  /**
   * Delete custom model by id.
   */
  delete(id: string): Promise<AxiosResponse<void>> {
    return this.API.delete(this.DOMAIN + '/' + id)
  }
}

export default customModelApi
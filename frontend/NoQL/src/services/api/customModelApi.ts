import Api from './api.ts'
import { AxiosResponse } from 'axios'
import { CustomModel } from '../../types/CustomModel.ts'

const customModelApi = {
  API: Api.getInstance(),
  DOMAIN: '/model',

  getAll(): Promise<AxiosResponse<CustomModel[]>> {
    return this.API.get(this.DOMAIN)
  }
}

export default customModelApi
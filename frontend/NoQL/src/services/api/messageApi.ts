import Api from './api.ts'
import { AxiosResponse } from 'axios'
import { RetrievedData } from '../../types/Query.ts'

const messageApi = {
  API: Api.getInstance(),
  DOMAIN: '/message',

  /**
   * Load data of response from message.
   * @param id message identifier
   * @param page page number (default 0)
   * @param pageSize number of items per page (default 10)
   */
  getMessageData(id: string, page: number = 0, pageSize: number = 10): Promise<AxiosResponse<RetrievedData>> {
    return this.API.get(`${this.DOMAIN}/${id}/data`, [
      {
        name: 'page',
        value: page
      },
      {
        name: 'pageSize',
        value: pageSize
      }
    ])
  }
}

export default messageApi
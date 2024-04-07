import Api from './api.ts'
import { AxiosResponse } from 'axios'
import { ChatFromApi } from '../../types/Chat.ts'

const chatApi = {
  API: Api.getInstance(),
  DOMAIN: '/chat',

  createNewChat(databaseId: string): Promise<AxiosResponse<ChatFromApi>> {
    return this.API.post(this.DOMAIN, null, [
      {
        name: 'databaseId',
        value: databaseId
      }
    ])
  }
}

export default chatApi
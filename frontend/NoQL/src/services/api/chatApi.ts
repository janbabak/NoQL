import Api from './api.ts'
import { AxiosResponse } from 'axios'
import { Chat } from '../../types/Chat.ts'

const chatApi = {
  API: Api.getInstance(),
  DOMAIN: '/chat',

  getById(id: string): Promise<AxiosResponse<Chat>> {
    return this.API.get(this.DOMAIN + '/' + id)
  },

  createNewChat(databaseId: string): Promise<AxiosResponse<Chat>> {
    return this.API.post(this.DOMAIN, null, [
      {
        name: 'databaseId',
        value: databaseId
      }
    ])
  },

  deleteChat(chatId: string): Promise<AxiosResponse> {
    return this.API.delete(this.DOMAIN + '/' + chatId)
  },

  renameChat(chatId: string, newName: string): Promise<AxiosResponse> {
    return this.API.put(this.DOMAIN + '/' + chatId + '/name', null, [
      {
        name: 'name',
        value: newName
      }
    ])
  }
}

export default chatApi
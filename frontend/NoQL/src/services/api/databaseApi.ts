import type { AxiosResponse } from 'axios'
import Api from './api.ts'
import { Database } from '../../types/Database.ts'
import { QueryRequest, QueryResponse } from '../../types/Query.ts'
import { DatabaseStructure } from '../../types/DatabaseStructure.ts'
import { ChatHistoryItem } from '../../types/Chat.ts'

const databaseApi = {
  API: Api.getInstance(),
  DOMAIN: '/database',

  /**
   * Get all databases.
   */
  getAll(): Promise<AxiosResponse<Database[]>> {
    return this.API.get(this.DOMAIN)
  },

  /**
   * Get database by id.
   * @param id database identifier
   */
  getById(id: string): Promise<AxiosResponse<Database>> {
    return this.API.get(this.DOMAIN + '/' + id)
  },

  /**
   * Get database structure by id
   * @param id database identifier
   */
  getStructure(id: string): Promise<AxiosResponse<DatabaseStructure>> {
    return this.API.get(this.DOMAIN + '/' + id + '/structure')
  },

  queryChat(databaseId: string, request: QueryRequest, pageSize: number): Promise<AxiosResponse<QueryResponse>> {
    return this.API.post(
      this.DOMAIN + '/' + databaseId + '/query/chat',
      request, [
        {
          name: 'pageSize',
          value: pageSize
        }
      ]
    )
  },

  loadChatResult(databaseId: string, chatId: string, page: number, pageSize: number): Promise<AxiosResponse<QueryResponse>> {
    return this.API.post(this.DOMAIN + '/' + databaseId + '/query/load-chat-result', null,
      [
        {
          name: 'chatId',
          value: chatId
        },
        {
          name: 'pageSize',
          value: pageSize
        },
        {
          name: 'page',
          value: page
        }
      ]
    )
  },

  /**
   * Query the user's database using database query language, result is automatically paginated.
   * @param id database id
   * @param query database query in corresponding database query language
   * @param page page number (first pages is 0)
   * @param pageSize number of items in one page
   * changed
   */
  queryQueryLanguageQuery(id: string, query: string, page: number = 0, pageSize: number = 10)
    : Promise<AxiosResponse<QueryResponse>> {

    return this.API.post(
      this.DOMAIN + '/' + id + '/query/query-language',
      query,
      [
        {
          name: 'page',
          value: page
        },
        {
          name: 'pageSize',
          value: pageSize
        }
      ])
  },

  /**
   * Get chat history (chats associated to the specific database) ordered by the modification date
   * in descending order.
   * @param id database identifier
   */
  getChatHistoryByDatabaseId(id: string): Promise<AxiosResponse<ChatHistoryItem[]>> {
    return this.API.get(this.DOMAIN + '/' + id + '/chats')
  }
}

export default databaseApi
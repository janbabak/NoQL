import type { AxiosResponse } from 'axios'
import Api from './api.ts'
import { CreateDatabaseRequest, Database, UpdateDatabaseRequest } from '../../types/Database.ts'
import { ConsoleResponse, QueryRequest } from '../../types/Query.ts'
import { DatabaseStructure } from '../../types/DatabaseStructure.ts'
import { ChatHistoryItem, ChatResponse } from '../../types/Chat.ts'

const databaseApi = {
  API: Api.getInstance(),
  DOMAIN: '/database',

  /**
   * Get all databases.
   */
  getAll(userId: string | null = null): Promise<AxiosResponse<Database[]>> {
    return this.API.get(this.DOMAIN, userId ? [{ name: 'userId', value: userId }] : [])
  },

  /**
   * Get database by id.
   * @param id database identifier
   */
  getById(id: string): Promise<AxiosResponse<Database>> {
    return this.API.get(this.DOMAIN + '/' + id)
  },

  /**
   * Create new database.
   * @param database
   */
  create(database: CreateDatabaseRequest): Promise<AxiosResponse<Database>> {
    return this.API.post(this.DOMAIN, database)
  },

  /**
   * Update database.
   * @param id database identifier
   * @param database
   */
  update(id: string, database: UpdateDatabaseRequest): Promise<AxiosResponse<Database>> {
    return this.API.put(this.DOMAIN + '/' + id, database)
  },

  /**
   * Delete database by id.
   * @param id database identifier
   */
  delete(id: string): Promise<AxiosResponse<void>> {
    return this.API.delete(this.DOMAIN + '/' + id)
  },

  /**
   * Get database structure by id
   * @param id database identifier
   */
  getStructure(id: string): Promise<AxiosResponse<DatabaseStructure>> {
    return this.API.get(this.DOMAIN + '/' + id + '/structure')
  },

  queryChat(databaseId: string, request: QueryRequest, chatId: string, pageSize: number)
    : Promise<AxiosResponse<ChatResponse>> {
    return this.API.post(
      `${this.DOMAIN}/${databaseId}/chat/${chatId}/queryNew`,
      request, [
        {
          name: 'pageSize',
          value: pageSize
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
    : Promise<AxiosResponse<ConsoleResponse>> {

    return this.API.post(
      this.DOMAIN + '/' + id + '/query/queryLanguage',
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
      ],
      {
        'Content-Type': 'text/plain'
      })
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
import type { AxiosResponse } from 'axios'
import Api from './api.ts'
import { Database } from '../../types/Database.ts'
import { QueryResponse } from '../../types/QueryResponse.ts'

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
   * Query the user's database using natural language, result is automatically paginated.
   * Params:
   * @param id database id
   * @param query natural language query
   * @param pageSize size of a page of automatically paginated response
   */
  queryNaturalLanguage(id: string, query: string, pageSize: number): Promise<AxiosResponse<QueryResponse>> {
    return this.API.post(
      this.DOMAIN + '/' + id + '/query/natural-language',
      query, [
        {
          name: 'pageSize',
          value: pageSize
        }
      ])
  },

  /**
   * Query the user's database using database query language, result is automatically paginated.
   * @param id database id
   * @param query database query in corresponding database query language
   * @param page page number (first pages is 0)
   * @param pageSize number of items in one page
   * changed)
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
  }
}

export default databaseApi
export type { QueryResponse }

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
   */
  queryNaturalLanguage(id: string, query: string): Promise<AxiosResponse<QueryResponse>> {
    return this.API.post(
      this.DOMAIN + '/' + id + '/query/natural-language',
      query, [
        {
          name: 'page',
          value: 0
        },
        {
          name: 'pageSize',
          value: 10 // TODO: set default value
        }
      ])
  },

  /**
   * Query the user's database using database query language, result is automatically paginated.
   * @param id database id
   * @param query database query in corresponding database query language
   * @param page page number (first pages is 0)
   * @param pageSize number of items in one page
   * @param overrideLimit if true override the limit value no matter what (used when number of items per pages is
   * changed)
   */
  queryQueryLanguageQuery(
    id: string,
    query: string,
    page: number = 0,
    pageSize: number = 10,
    overrideLimit: boolean = false): Promise<AxiosResponse<QueryResponse>> {

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
        },
        {
          name: 'overrideLimit',
          value: overrideLimit
        }
      ])
  }
}

export default databaseApi
export type { QueryResponse }

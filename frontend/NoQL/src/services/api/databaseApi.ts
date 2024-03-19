import type { AxiosResponse } from 'axios'
import Api from './api.ts'
import { Database } from '../../types/Database.ts'
import { QueryResponse } from '../../types/QueryResponse.ts'

const databaseApi = {
  API: Api.getInstance(),
  DOMAIN: '/database',

  getAll(): Promise<AxiosResponse<Database[]>> {
    return this.API.get(this.DOMAIN)
  },

  getById(id: string): Promise<AxiosResponse<Database>> {
    return this.API.get(this.DOMAIN + '/' + id)
  },

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

  queryQueryLanguageQuery(
    id: string,
    query: string,
    page: number = 0,
    pageSize: number = 10): Promise<AxiosResponse<QueryResponse>> {

    return this.API.post(
      this.DOMAIN + '/' + id + '/query/natural-language',
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

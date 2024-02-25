import type { AxiosResponse } from 'axios'
import Api from './api.ts'
import { Database } from '../../types/Database.ts'

interface QueryResult {
  columnNames: string[]
  rows: string[][]
}

interface QueryResponse {
  query: string
  result: QueryResult
}

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
    return this.API.post(this.DOMAIN + '/' + id + '/query/natural-language', query)
  }
}

export default databaseApi
export type { QueryResponse }

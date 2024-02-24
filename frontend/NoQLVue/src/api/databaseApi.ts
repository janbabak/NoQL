import Api from '@/api/api'
import type { AxiosResponse } from 'axios'
import type { RouteParamValue } from 'vue-router'

interface Database {
  id: string
  name: string
  host: string
  port: number
  database: string
  userName: string
  password: string
  engine: string // TODO: create enum
  isSQL: boolean
}

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

  getById(id: string | RouteParamValue[]): Promise<AxiosResponse<Database>> {
    return this.API.get(this.DOMAIN + '/' + id)
  },

  queryNaturalLanguage(
    id: string | RouteParamValue[],
    query: string
  ): Promise<AxiosResponse<QueryResponse>> {
    return this.API.post(this.DOMAIN + '/' + id + '/query/natural-language', query)
  }
}

export default databaseApi
export type { Database, QueryResponse }

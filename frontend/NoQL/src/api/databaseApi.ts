import Api from '@/api/api'
import type { AxiosResponse } from 'axios'

interface Database {
  id: string,
  name: string,
  host: string,
  port: number,
  database: string,
  userName: string,
  password: string,
  engine: string, // TODO: create enum
  isSQL: boolean
}

const databaseApi = {
  API: Api.getInstance(),
  DOMAIN: '/database',

  getAll(): Promise<AxiosResponse<Database[]>> {
    return this.API.get(this.DOMAIN)
  }
}

export default databaseApi
export type { Database }
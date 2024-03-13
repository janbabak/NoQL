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

export type { Database }
interface Database {
  id: string
  name: string
  host: string
  port: number
  database: string
  userName: string
  password: string
  engine: DatabaseEngine
  isSQL: boolean
}

interface CreateDatabaseRequest {
  name: string
  host: string
  port: number
  database: string
  userName: string
  password: string
  engine: DatabaseEngine
}

enum DatabaseEngine {
  POSTGRES,
  MYSQL
}

export type { Database, CreateDatabaseRequest }
export { DatabaseEngine }
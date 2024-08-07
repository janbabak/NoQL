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
  engine: DatabaseEngine | '' // empty string is used for default value
}

interface UpdateDatabaseRequest {
  name: string
  host: string
  port: number
  database: string
  userName: string
  password: string
}

enum DatabaseEngine {
  POSTGRES,
  MYSQL
}

export { DatabaseEngine }

export type {
  Database,
  CreateDatabaseRequest,
  UpdateDatabaseRequest
}

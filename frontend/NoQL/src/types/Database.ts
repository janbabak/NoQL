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

enum DatabaseEngine {
  POSTGRES,
  MYSQL
}

export type { Database, DatabaseEngine }
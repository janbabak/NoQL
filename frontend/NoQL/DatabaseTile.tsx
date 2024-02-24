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

function DatabaseTile() {
  const database = {
    id: '4594jl34jr5l34',
    name: 'Localhost postgres',
    host: 'localhost',
    port: 5432,
    database: 'noql',
    userName: 'postgres',
    password: 'password',
    engine: 'Postgres',
    isSQL: true
  } as Database

  return (
    <>
      <h2>{database.name}</h2>
      <ul>
        <li><span>Id:</span>{database.id}</li>
        <li><span>Host:</span>{database.host}</li>
        <li><span>Port:</span>{database.port}</li>
        <li><span>Engine:</span>{database.engine}</li>
      </ul>
    </>
  )
}

export default DatabaseTile
export type { Database }

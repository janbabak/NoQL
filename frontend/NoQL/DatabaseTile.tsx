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

DatabaseTile.defaultProps = {
  database: {
    id: 'null',
    name: 'Default value',
    host: 'default',
    port: 5432,
    database: '',
    userName: '',
    password: '',
    engine: '',
    isSQL: true
  }
}

function DatabaseTile(props: { database: Database }) {
  const database = props.database as Database | null

  if (!database) {
    return (
      <div>Database not available</div>
    )
  }

  const style = {
    border: '3px solid tomato',
    borderRadius: '1rem',
    margin: '1rem',
    padding: '1rem'
  }

  return (
    <div style={style}>
      <h2>{database.name}</h2>
      <ul>
        <li><span>Id:</span>{database.id}</li>
        <li><span>Host:</span>{database.host}</li>
        <li><span>Port:</span>{database.port}</li>
        <li><span>Engine:</span>{database.engine}</li>
      </ul>
    </div>
  )
}

export default DatabaseTile
export type { Database }

import { Database } from '../../types/Database.ts'
import { Link } from 'react-router-dom'

export function DatabaseCard({ database }: { database: Database }) {
  return (
    <>
      <h3>{database.name}</h3>
      <div>{database.host}</div>
      <Link to={`database/${database.id}`}>
        <button>Query</button>
      </Link>
    </>
  )
}
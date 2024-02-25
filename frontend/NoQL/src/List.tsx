export function List() {
  const databases = [
    {
      name: 'Localhost',
      engine: 'Postgres'
    },
    {
      name: 'AWS',
      engine: 'Postgres'
    },
    {
      name: 'Localhost mysql',
      engine: 'MySQL'
    }
  ] as {
    name: string,
    engine: string
  }[]

  return (
    <ul>{
      databases.map(database => {
        return <li key={database.name}>{database.name}</li>
      })
    }</ul>
  )
}
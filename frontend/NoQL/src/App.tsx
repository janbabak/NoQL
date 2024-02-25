import { Header } from './Header.tsx'
import { Footer } from './Footer.tsx'
import DatabaseTile, { Database } from '../DatabaseTile.tsx'
import { List } from './List.tsx'
import { Button } from './Button.tsx'

function App() {

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
      <Header />
      <main>
        <a href="/example">example page</a>
        <Button />
        <DatabaseTile database={database}/>
        <DatabaseTile />
        <List />
      </main>
      <Footer />
    </>
  )
}

export default App

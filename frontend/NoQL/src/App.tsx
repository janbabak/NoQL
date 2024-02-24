import { Header } from './Header.tsx'
import { Footer } from './Footer.tsx'
import DatabaseTile from '../DatabaseTile.tsx'

function App() {
  return (
    <>
      <Header />
      <main>
        <DatabaseTile />
      </main>
      <Footer />
    </>
  )
}

export default App

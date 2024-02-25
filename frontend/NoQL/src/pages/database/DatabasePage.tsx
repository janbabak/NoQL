import { useParams } from 'react-router'

export function DatabasePage() {
  const { id } = useParams<string>()
  return (
    <>
      <h1>Database: {id}</h1>
    </>
  )
}
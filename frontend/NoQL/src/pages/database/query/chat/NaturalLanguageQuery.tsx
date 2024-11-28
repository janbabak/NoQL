interface NaturalLanguageQueryProps {
  query: string
}

/**
 * Component to display the natural language query in chat messages
 * @param query natural language query
 */
export function NaturalLanguageQuery({ query }: NaturalLanguageQueryProps) {
  return (
    <>
      <span style={{ fontWeight: 'bold' }}>You: </span>{query}
    </>
  )
}
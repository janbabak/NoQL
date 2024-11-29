interface NaturalLanguageQueryProps {
  query: string
}

/**
 * Natural language query in chat messages
 */
export function NaturalLanguageQuery({ query }: NaturalLanguageQueryProps) {
  return (
    <>
      <span style={{ fontWeight: 'bold' }}>You: </span>{query}
    </>
  )
}
interface NaturalLanguageQueryProps {
  query: string
}

/**
 * Natural language query in chat messages
 */
export function NaturalLanguageQuery({ query }: NaturalLanguageQueryProps) {
  return (
    <div>
      <span style={{ fontWeight: 'bold' }}>You: </span>{query}
    </div>
  )
}
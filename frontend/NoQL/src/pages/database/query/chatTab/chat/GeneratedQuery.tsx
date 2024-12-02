import SyntaxHighlighter from 'react-syntax-highlighter'
import { vs2015 as theme } from 'react-syntax-highlighter/dist/cjs/styles/hljs'

interface GeneratedQueryProps {
  query: string
}

/**
 * Database query - so far only SQL.
 */
export function GeneratedQuery({ query }: GeneratedQueryProps) {
  return (
    <SyntaxHighlighter
      style={theme}
      language="SQL"
      customStyle={{ borderRadius: '0.25rem', margin: '0.25rem 0 1rem 0' }}
    >
      {query}
    </SyntaxHighlighter>
  )
}
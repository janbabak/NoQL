import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter'
import { atomDark } from 'react-syntax-highlighter/dist/esm/styles/prism'
import styles from './Database.module.css'

export function GeneratedQuery({ query }: {
  query: string
}) {
  return (
    <SyntaxHighlighter
      language="sql"
      style={atomDark}
      className={styles.generatedQuery}
    >
      {query}
    </SyntaxHighlighter>
  )
}
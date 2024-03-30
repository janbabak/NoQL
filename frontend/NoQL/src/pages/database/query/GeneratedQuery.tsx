import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter'
import { atomDark } from 'react-syntax-highlighter/dist/esm/styles/prism'
import styles from '../Database.module.css'

interface Props {
  query: string
}

export function GeneratedQuery({ query }: Props) {
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
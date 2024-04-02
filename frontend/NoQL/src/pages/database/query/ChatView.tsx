import { Chat } from '../../../types/Query.ts'
import SyntaxHighlighter from 'react-syntax-highlighter'
import { vs2015 as theme } from 'react-syntax-highlighter/dist/cjs/styles/hljs'

interface ChatViewProps {
  chat: Chat
}

interface UsersQueryProps {
  query: string
}

interface ModelsResponse {
  response: string
}

function ModelsResponse({ response }: ModelsResponse) {
  return (
    <SyntaxHighlighter
      style={theme}
      language="SQL"
      customStyle={{ borderRadius: '0.25rem', margin: '0.25rem 0 1rem 0' }}
    >
      {response}
    </SyntaxHighlighter>
  )
}

function UsersQuery({ query }: UsersQueryProps) {
  return (
    <>
      <span style={{ fontWeight: 'bold' }}>You: </span>
      {query}
    </>
  )
}

export function ChatView({ chat }: ChatViewProps) {

  return (
    <div>
      {
        chat.messages.map((message: string, index: number) => {
          return (
            <div key={index}>
              {index % 2 == 0
                ? <UsersQuery query={message} />
                : <ModelsResponse response={message} />
              }
            </div>
          )
        })
      }
    </div>
  )
}
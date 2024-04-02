import { Chat } from '../../../types/Query.ts'
import { Editor } from '@monaco-editor/react'
import { editor } from 'monaco-editor'

interface ChatViewProps {
  chat: Chat
}

interface UsersQueryProps {
  query: string
}

interface ModelsResponse {
  response: string
}

function UsersQuery({ query }: UsersQueryProps) {
  return (
    <>
      <span style={{ fontWeight: 'bold' }}>You: </span>
      {query}
    </>
  )
}

function ModelsResponse({ response }: ModelsResponse) {
  const options: editor.IStandaloneEditorConstructionOptions = {
    fontSize: 16,
    fontFamily: 'monospace',
    lineHeight: 24,
    minimap: { enabled: false },
    padding: { top: 20 },
    readOnly: true,
  }

  return (
    <div style={{height: '100%'}}>
      <Editor
        height="100px"
        language="sql"
        theme="vs-dark"
        value={response}
        options={options}
      />
    </div>
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
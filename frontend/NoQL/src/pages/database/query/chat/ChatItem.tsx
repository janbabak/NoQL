import { ChatResponse } from '../../../../types/Chat.ts'
import { NaturalLanguageQuery } from './NaturalLanguageQuery.tsx'
import { GeneratedQuery } from './GeneratedQuery.tsx'
import { ResultNew } from './ResultNew.tsx'

interface ChatItemProps {
  message: ChatResponse,
}

/**
 * Component for displaying a chat item - a natural language query and its response in chat
 */
export function ChatItem({ message }: ChatItemProps) {
  return (
    <div>
      <NaturalLanguageQuery query={message.nlQuery} />

      {message.dbQuery != null && message.dbQuery != '' &&
        <GeneratedQuery query={message.dbQuery} />}

      {/*TODO: loading*/}
      {message.data != null &&
        <ResultNew message={message} loading={false} />}
    </div>
  )
}
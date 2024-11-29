import { ChatResponse } from '../../../../types/Chat.ts'
import { NaturalLanguageQuery } from './NaturalLanguageQuery.tsx'
import { GeneratedQuery } from './GeneratedQuery.tsx'
import { ResultNew } from './ResultNew.tsx'
import { Plot } from './Plot.tsx'

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

      {message.plotUrl != null &&
        <Plot plotUrl={message.plotUrl} />}

      {/*TODO: loading*/}
      {message.data != null &&
        <ResultNew message={message} />}
    </div>
  )
}
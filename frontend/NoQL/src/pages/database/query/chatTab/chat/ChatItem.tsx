import { ChatResponse } from '../../../../../types/Chat.ts'
import { NaturalLanguageQuery } from './NaturalLanguageQuery.tsx'
import { GeneratedQuery } from './GeneratedQuery.tsx'
import { ChatResultComponent } from './ChatResultComponent.tsx'
import { Plot } from './Plot.tsx'
import { ChatResponseDescription } from './ChatResponseDescription.tsx'

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

      {message.description != null && message.description != '' &&
        <ChatResponseDescription description={message.description} />}

      {message.dbQuery != null && message.dbQuery != '' &&
        <GeneratedQuery query={message.dbQuery} />}

      {message.plotUrl != null &&
        <Plot plotUrl={message.plotUrl} />}

      {message.data != null &&
        <ChatResultComponent message={message} />}
    </div>
  )
}
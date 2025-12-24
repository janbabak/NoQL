import ReactMarkdown from 'react-markdown'

interface ChatResponseDescriptionProps {
    description: string
}

/**
 * LLM response description what it did
 * @param description in markdown format
 */
export function ChatResponseDescription({ description }: ChatResponseDescriptionProps) {
    return (
        <div style={{'margin': '0.5rem 0 0.5rem 0'}}>
            <ReactMarkdown>{'**Response:** ' + description}</ReactMarkdown>
        </div>
    )
}
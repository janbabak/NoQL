interface ChatErrorProps {
    errorMessage: string
}

export function ChatError({ errorMessage }: ChatErrorProps) {
    return (
        <div style={{
            color: 'white',
            backgroundColor: 'tomato',
            borderRadius: '0.5rem',
            padding: '1rem'
        }}>
            { errorMessage }
        </div>
    )
}
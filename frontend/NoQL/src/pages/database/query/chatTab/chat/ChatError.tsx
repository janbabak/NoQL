import { Alert, AlertTitle, Box } from '@mui/material';

interface ChatErrorProps {
  title: string;
  errorMessage: string;
}

export function ChatError({ title, errorMessage }: ChatErrorProps) {
  return (
    <Box
      sx={{
        width: '100%',
        mt: 2,
        boxShadow: 3,
        borderRadius: 2,
      }}
    >
      <Alert
        severity="error"
        variant="filled"
        sx={{
          width: '100%',
          borderRadius: 2,
        }}
      >
        <AlertTitle>{title}:</AlertTitle>
        {errorMessage}
      </Alert>
    </Box>
  );
}
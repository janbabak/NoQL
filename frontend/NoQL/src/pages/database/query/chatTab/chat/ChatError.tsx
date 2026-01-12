import { Alert, AlertTitle, Box } from '@mui/material';

interface ChatErrorProps {
  title: string;  
  errorMessage: string;
}

export function ChatError({ title, errorMessage }: ChatErrorProps) {
  return (
    <Box
      sx={{
        maxWidth: 600,
        mx: 'auto',
        mt: 2,
        boxShadow: 3, // MUI shadow
        borderRadius: 2,
      }}
    >
      <Alert
        severity="error"
        variant="filled"
        sx={{
          borderRadius: 2,
        }}
      >
        <AlertTitle>{title}</AlertTitle>
        {errorMessage}
      </Alert>
    </Box>
  );
}
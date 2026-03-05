import { MailOutline } from '@mui/icons-material';
import { Typography, Container, Paper } from '@mui/material';

const VerifyEmail = () => {
  return (
    <Container maxWidth="sm" sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
      <Paper elevation={3} sx={{ padding: 4, textAlign: 'center' }}>
        <MailOutline sx={{ fontSize: 64, color: 'primary.main', mb: 2 }} />
        <Typography variant="h5" component="h2" gutterBottom>
          Controlla la tua email
        </Typography>
        <Typography variant="body1" color="textSecondary">
          Abbiamo inviato un link di verifica alla tua email. Per favore, controlla la tua casella di posta e segui le istruzioni.
        </Typography>
      </Paper>
    </Container>
  );
};

export default VerifyEmail;
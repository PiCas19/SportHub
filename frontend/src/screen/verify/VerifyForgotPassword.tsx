import {MailOutline} from '@mui/icons-material';
import {
  Typography,
  Container,
  Paper,
  TextField,
  Button,
  Box,
  CircularProgress,
} from '@mui/material';
import {useState} from "react";
import axios from "axios";
import CustomSnackbar from "../../components/CustomSnackbar.tsx";

const VerifyForgotPassword = () => {
  // Form state and feedback management
  const [email, setEmail] = useState('');
  const [isSubmitted, setIsSubmitted] = useState(false);
  const [loading, setLoading] = useState(false);
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'success',
  });

  // Handle form submission
  const handleSubmit = (e: any) => {
    e.preventDefault();
    setLoading(true);

    axios.post('http://localhost:8080/auth/forgot-password', {email})
      .then(() => {
        setIsSubmitted(true);
      })
      .catch(() => {
        showSnackbar("Qualcosa è andato storto", "error");
      })
  };

  // Utility to show snackbar
  const showSnackbar = (message: any, severity: any) => {
    setSnackbar({
      open: true,
      message,
      severity
    });
  };

  // Close snackbar
  const handleCloseSnackbar = () => {
    setSnackbar({
      ...snackbar,
      open: false
    });
  };

  return (
    <Container maxWidth="sm" sx={{display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh'}}>
      <Paper elevation={3} sx={{padding: 4, textAlign: 'center', width: '100%'}}>
        <MailOutline sx={{fontSize: 64, color: 'primary.main', mb: 2}}/>

        {/* Show form if not submitted yet */}
        {!isSubmitted ? (
          <>
            <Typography variant="h5" component="h2" gutterBottom>
              Recupera la tua password
            </Typography>
            <Typography variant="body1" color="textSecondary" sx={{mb: 3}}>
              Inserisci la tua email e ti invieremo un link per reimpostare la password.
            </Typography>

            <Box component="form" onSubmit={handleSubmit} sx={{mt: 2}}>
              <TextField
                fullWidth
                label="Email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                margin="normal"
                required
                disabled={loading}
              />

              {/* Show loader or submit button */}
              {loading ? (
                <CircularProgress sx={{mt: 3, mb: 2}}/>
              ) : (
                <Button
                  type="submit"
                  variant="contained"
                  fullWidth
                  sx={{mt: 3, mb: 2}}
                >
                  Invia link di recupero
                </Button>
              )}
            </Box>
          </>
        ) : (
          // Show confirmation message after submission
          <>
            <Typography variant="h5" component="h2" gutterBottom>
              Controlla la tua email
            </Typography>
            <Typography variant="body1" color="textSecondary">
              Abbiamo inviato un link di verifica a <strong>{email}</strong>. Per favore, controlla la tua casella di
              posta e segui le istruzioni.
            </Typography>
            {/* Button to go back to the form */}
            <Button
              variant="text"
              sx={{mt: 3}}
              onClick={() => setIsSubmitted(false)}
            >
              Torna indietro
            </Button>
          </>
        )}

        {/* Snackbar for error/success messages */}
        <CustomSnackbar
          open={snackbar.open}
          message={snackbar.message}
          severity={snackbar.severity}
          onClose={handleCloseSnackbar}
        />
      </Paper>
    </Container>
  );
};

export default VerifyForgotPassword;

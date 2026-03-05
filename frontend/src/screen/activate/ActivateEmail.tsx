import {useEffect, useState} from 'react';
import {useSearchParams} from 'react-router-dom';
import {CheckCircleOutline, ErrorOutline} from '@mui/icons-material';
import {Typography, Container, Paper, Button, CircularProgress} from '@mui/material';

// ActivateAccount component for handling account activation
const ActivateAccount = () => {
  // Get token from URL query params and manage component state
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');
  const [status, setStatus] = useState('loading');

  // Validate and activate account using token
  useEffect(() => {
    if (token) {
      fetch('http://localhost:8080/auth/activate', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({token}),
      })
        .then((response) => {
          if (response.status === 204) {
            setStatus('success');
          } else {
            setStatus('error');
          }
        })
        .catch(() => setStatus('error'));
    } else {
      setStatus('error');
    }
  }, [token]);

  // Render UI based on activation status
  return (
    <Container maxWidth="sm" sx={{display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh'}}>
      <Paper elevation={3} sx={{padding: 4, textAlign: 'center'}}>
        {status === 'loading' && <CircularProgress/>}
        {status === 'success' && (
          <>
            <CheckCircleOutline sx={{fontSize: 64, color: 'success.main', mb: 2}}/>
            <Typography variant="h5" component="h2" gutterBottom>
              Account Attivato!
            </Typography>
            <Typography variant="body1" color="textSecondary" gutterBottom>
              Il tuo account è stato attivato con successo. Ora puoi accedere.
            </Typography>
            <Button variant="contained" color="primary" href="/">
              Vai al Login
            </Button>
          </>
        )}

        {/* Error state */}
        {status === 'error' && (
          <>
            <ErrorOutline sx={{fontSize: 64, color: 'error.main', mb: 2}}/>
            <Typography variant="h5" component="h2" gutterBottom>
              Errore di Attivazione
            </Typography>
            <Typography variant="body1" color="textSecondary" gutterBottom>
              Qualcosa è andato storto. Verifica il link o contatta il supporto.
            </Typography>
          </>
        )}
      </Paper>
    </Container>
  );
};

export default ActivateAccount;
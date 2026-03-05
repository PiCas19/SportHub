import {useEffect, useState} from 'react';
import {useSearchParams} from 'react-router-dom';
import axios from 'axios';
import {CheckCircleOutline, ErrorOutline, LockOutlined} from '@mui/icons-material';
import {
  Typography,
  Container,
  Paper,
  Button,
  CircularProgress,
  TextField,
  Box,
  InputAdornment,
  IconButton
} from '@mui/material';
import {Visibility, VisibilityOff} from '@mui/icons-material';

// ResetPassword component for handling password reset functionality
const ResetPassword = () => {
  const [searchParams] = useSearchParams();
  const tokenn = searchParams.get('token');
  const [status, setStatus] = useState('loading');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  // Validate token on component mount
  useEffect(() => {
    if (tokenn) {
      axios.get('http://localhost:8080/auth/validate-token', {
        params: {
          token: tokenn
        }
      })
        .then((response) => {
          console.log(response);
          setStatus('form');
        })
        .catch((error) => {
          console.error("Errore nella validazione:", error);
          setStatus('error');
        });
    } else {
      setStatus('error');
    }
  }, [tokenn]);

  // Handle form submission to reset password
  const handleSubmit = (e: any) => {
    e.preventDefault();
    setStatus('submitting');
    fetch('http://localhost:8080/auth/reset-password', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({token: tokenn, newPassword: password}),

    })
      .then(() => {
        setStatus('success');
      })
      .catch(() => setStatus('error'));
  };

  const handleClickShowPassword = () => {
    setShowPassword(!showPassword);
  };

  const handleClickShowConfirmPassword = () => {
    setShowConfirmPassword(!showConfirmPassword);
  };

  return (
    <Container maxWidth="sm" sx={{display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh'}}>
      <Paper elevation={3} sx={{padding: 4, textAlign: 'center', width: '100%'}}>
        {(status === 'loading' || status === 'submitting') && (
          <Box sx={{display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2}}>
            <CircularProgress/>
            <Typography variant="body1">
              {status === 'loading' ? 'Verifica del token in corso...' : 'Aggiornamento password in corso...'}
            </Typography>
          </Box>
        )}

        {/* Password reset form */}
        {status === 'form' && (
          <>
            <LockOutlined sx={{fontSize: 64, color: 'primary.main', mb: 2}}/>
            <Typography variant="h5" component="h2" gutterBottom>
              Reimposta la tua password
            </Typography>
            <Typography variant="body1" color="textSecondary" gutterBottom sx={{mb: 3}}>
              Inserisci la tua nuova password per reimpostare il tuo account.
            </Typography>

            <Box component="form" onSubmit={handleSubmit}>
              <TextField
                fullWidth
                label="Nuova Password"
                type={showPassword ? 'text' : 'password'}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                margin="normal"
                required
                InputProps={{
                  endAdornment: (
                    <InputAdornment position="end">
                      <IconButton
                        aria-label="toggle password visibility"
                        onClick={handleClickShowPassword}
                        edge="end"
                      >
                        {showPassword ? <VisibilityOff/> : <Visibility/>}
                      </IconButton>
                    </InputAdornment>
                  )
                }}
              />

              <TextField
                fullWidth
                label="Conferma Password"
                type={showConfirmPassword ? 'text' : 'password'}
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                margin="normal"
                required
                InputProps={{
                  endAdornment: (
                    <InputAdornment position="end">
                      <IconButton
                        aria-label="toggle password visibility"
                        onClick={handleClickShowConfirmPassword}
                        edge="end"
                      >
                        {showConfirmPassword ? <VisibilityOff/> : <Visibility/>}
                      </IconButton>
                    </InputAdornment>
                  )
                }}
              />

              <Button
                type="submit"
                variant="contained"
                fullWidth
                sx={{mt: 3, mb: 2}}
              >
                Reimposta Password
              </Button>
            </Box>
          </>
        )}

        {/* Success state */}
        {status === 'success' && (
          <>
            <CheckCircleOutline sx={{fontSize: 64, color: 'success.main', mb: 2}}/>
            <Typography variant="h5" component="h2" gutterBottom>
              Password Reimpostata!
            </Typography>
            <Typography variant="body1" color="textSecondary" gutterBottom>
              La tua password è stata reimpostata con successo. Ora puoi accedere con la nuova password.
            </Typography>
            <Button variant="contained" color="primary" href="/" sx={{mt: 2}}>
              Vai al Login
            </Button>
          </>
        )}

        {/* Error state */}
        {status === 'error' && (
          <>
            <ErrorOutline sx={{fontSize: 64, color: 'error.main', mb: 2}}/>
            <Typography variant="h5" component="h2" gutterBottom>
              Errore di Reimpostazione
            </Typography>
            <Typography variant="body1" color="textSecondary" gutterBottom>
              Qualcosa è andato storto. Il link potrebbe essere scaduto o non valido. Richiedi un nuovo link per
              reimpostare la password.
            </Typography>
            <Button
              variant="contained"
              color="primary"
              href="/activateForgotPassword"
              sx={{mt: 2}}
            >
              Richiedi Nuovo Link
            </Button>
          </>
        )}
      </Paper>
    </Container>
  );
};

export default ResetPassword;
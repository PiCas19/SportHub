import {useEffect, useState} from 'react';
import {
  Avatar,
  Box,
  Button, CircularProgress,
  Container,
  Divider,
  Grid,
  Paper,
  TextField,
  Typography,
} from '@mui/material';
import Cookies from "js-cookie";
import CameraAltIcon from '@mui/icons-material/CameraAlt';
import PasswordIcon from '@mui/icons-material/Password';
import DirectionsRunIcon from '@mui/icons-material/DirectionsRun';
import SendIcon from '@mui/icons-material/Send';
import HeaderScreen from "../../components/HeaderScreen.tsx";
import DeleteAccount from "../../components/DeleteAccount.tsx";
import {useNavigate} from "react-router-dom";
import CustomSnackbar from '../../components/CustomSnackbar.tsx';
import axiosInstance from "../api/axiosConfig.ts";

const Settings = () => {
    const [stravaLink, setStravaLink] = useState('');
    const navigate = useNavigate();
    const [user, setUser] = useState({name: '', email: '', profilePicture: ''});
    const [passwords, setPasswords] = useState({
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    });
    const [snackbar, setSnackbar] = useState({
      open: false,
      message: '',
      severity: 'success'
    });
    const [profilePicture, setProfilePicture] = useState<File | null>(null);
    const [isUpload, setIsUpload] = useState<boolean>(false);
    const [previewUrl, setPreviewUrl] = useState('');
    const [weight, setWeight] = useState(0);
    const [height, setHeight] = useState(0);
    const [loading, setLoading] = useState<boolean>();
    const [initialMetrics, setInitialMetrics] = useState({weight: 0, height: 0});
    const [metricsModified, setMetricsModified] = useState(false);

  // Fetch initial data on component mount
  useEffect(() => {
      axiosInstance.get('api/strava/login')
        .then((response: any) => {
          setStravaLink(response.data.authorizationUrl);
        })
      axiosInstance.get('api/user/profile')
        .then((response: any) => {
          setHeight(response.data.height);
          setWeight(response.data.weight);
          setInitialMetrics({
            height: response.data.height,
            weight: response.data.weight
          });
        })
      axiosInstance.get('api/user/profile-image', {
        responseType: 'arraybuffer'
      })
        .then((response) => {
          const contentType = response.headers['content-type'];
          const imageData = response.data as any;
          const imageUrl = URL.createObjectURL(new Blob([imageData], {type: contentType}));

          setUser(prevState => ({
            ...prevState,
            profilePicture: imageUrl,
          }));
        })
        .catch((e) => {
          console.error(e)
        })
    }, []);

  // Track changes to height/weight for enabling save button
  useEffect(() => {
      if (height !== initialMetrics.height || weight !== initialMetrics.weight) {
        setMetricsModified(true);
      } else {
        setMetricsModified(false);
      }
    }, [height, weight, initialMetrics]);

  // Handle password input changes
  const handlePasswordChange = (field: string, value: string) => {
      setPasswords(prevState => ({
        ...prevState,
        [field]: value
      }));
    };

  // Handle height/weight input changes
  const handleHeight = (value: any) => {
      setHeight(value);
    }
    const handleWeight = (value: any) => {
      setWeight(value);
    }

  // Handle profile picture file selection
  const handleFileChange = (event: any) => {
      setIsUpload(false)
      const file = event.target.files[0];
      if (file) {
        setProfilePicture(file);
        const reader = new FileReader() as any;
        reader.onload = () => {
          setPreviewUrl(reader.result);
        };
        reader.readAsDataURL(file);
      }
    };

  // Upload profile picture to server
  const uploadProfilePicture = () => {
      setLoading(true);
      const formData = new FormData() as any;
      formData.append('file', profilePicture);

      axiosInstance.put('api/user/update-profile-image', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        }
      })
        .then(() => {
          setLoading(false);
          setIsUpload(true)
          showSnackbar('Immagine del profilo aggiornata con successo', 'success');
        })
        .catch(() => {
          setLoading(false);
          showSnackbar('Errore con il caricamento', 'error');
        });
    };

  // Update user password
  const updatePassword = () => {
      axiosInstance.put('auth/change-password', {
        currentPassword: passwords.currentPassword,
        newPassword: passwords.newPassword,
      })
        .then(() => {
          showSnackbar('Password aggiornata con successo', 'success');
        })
        .catch(() => {
          console.log(passwords.currentPassword + " wela" + passwords.newPassword)
          showSnackbar('Errore nel cambiare la password', 'error');
        });
    };
    const updateData = () => {
      axiosInstance.put('api/user/update-metrics', {
        weight,
        height
      })
        .then(() => {
          setInitialMetrics({weight, height});
          setMetricsModified(false);
          showSnackbar('Dati aggiornati con successo', 'success');
        })
        .catch(() => {
          showSnackbar('Errore nel aggiunta dei dati', 'error');
        });
    };

  // Redirect to Strava authorization
  const connectStrava = () => {
      if (stravaLink) {
        window.location.href = stravaLink
      }
    };

  // Show snackbar notification
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

  // Connect to Telegram bot (user)
  const connectBotTelegramUser = () => {
      axiosInstance.post('api/telegram/invite/user', {})
        .then(() => {
          console.log(Cookies.get('auth_token'));
          showSnackbar('Controlla posta elettronica per accettare invito', 'success');
        })
        .catch(() => {
          showSnackbar('Errore nel recupero del link Telegram', 'error');
        });
    }

  // Connect to Telegram bot (group)
  const connectBotTelegramGroup = () => {
      axiosInstance.post('api/telegram/invite/group', {})
        .then(() => {
          showSnackbar('Controlla posta elettronica per accettare invito', 'success');
        })
        .catch(() => {
          showSnackbar('Errore nel recupero del link Telegram', 'error');
        });
    }

    const handleAccountDeletionSuccess = () => {
      navigate('/');
    };

  // Handle account deletion success
  const handleAccountDeletionError = (error: any) => {
      console.error('Error deleting account:', error);
    };

    return (
      <Box sx={{flexGrow: 1}}>
        <HeaderScreen title="Impostazioni utente"/>
        <Container maxWidth="md" sx={{py: 4}}>
          <Paper elevation={3} sx={{p: 4}}>
            <Box sx={{mb: 6}}>
              <Typography variant="h6" component="h2" gutterBottom sx={{display: 'flex', alignItems: 'center'}}>
                <CameraAltIcon sx={{mr: 1}}/> Foto Profilo
              </Typography>
              <Divider sx={{mb: 3}}/>

              <Box sx={{display: 'flex', alignItems: 'center', flexDirection: {xs: 'column', sm: 'row'}}}>
                <Avatar
                  src={previewUrl || user.profilePicture}
                  alt={user.name}
                  sx={{
                    width: 100,
                    height: 100,
                    mb: {xs: 2, sm: 0},
                    mr: {sm: 4}
                  }}
                />
                <Box>
                  <Button
                    variant="contained"
                    component="label"
                    sx={{mr: 2}}
                  >
                    Scegli File
                    <input
                      type="file"
                      hidden
                      accept="image/png"
                      onChange={handleFileChange}
                    />
                  </Button>
                  <Button
                    variant="outlined"
                    onClick={uploadProfilePicture}
                    disabled={!profilePicture || isUpload}
                  >
                    {loading ? <CircularProgress size={24}/> : 'Carica'}
                  </Button>
                </Box>
              </Box>
            </Box>

            <Box sx={{mb: 6}}>
              <Typography variant="h6" component="h2" gutterBottom sx={{display: 'flex', alignItems: 'center'}}>
                <PasswordIcon sx={{mr: 1}}/> Cambio Password
              </Typography>
              <Divider sx={{mb: 3}}/>

              <Grid container spacing={3}>
                <Grid item xs={12}>
                  <TextField
                    fullWidth
                    type="password"
                    label="Password Attuale"
                    name="currentPassword"
                    value={passwords.currentPassword}
                    onChange={(e) => handlePasswordChange("currentPassword", e.target.value)}
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    type="password"
                    label="Nuova Password"
                    name="newPassword"
                    value={passwords.newPassword}
                    onChange={(e) => handlePasswordChange("newPassword", e.target.value)}
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    type="password"
                    label="Conferma Password"
                    name="confirmPassword"
                    value={passwords.confirmPassword}
                    onChange={(e) => handlePasswordChange("confirmPassword", e.target.value)}
                    error={passwords.newPassword !== passwords.confirmPassword && passwords.confirmPassword !== ''}
                    helperText={passwords.newPassword !== passwords.confirmPassword && passwords.confirmPassword !== '' ? 'Le password non corrispondono' : ''}
                  />
                </Grid>
                <Grid item xs={12}>
                  <Button
                    variant="contained"
                    onClick={updatePassword}
                    disabled={!passwords.currentPassword || !passwords.newPassword || !passwords.confirmPassword || (passwords.newPassword != passwords.confirmPassword)}
                  >
                    Aggiorna Password
                  </Button>
                </Grid>
              </Grid>
            </Box>

            <Box sx={{mb: 6}}>
              <Typography variant="h6" component="h2" gutterBottom sx={{display: 'flex', alignItems: 'center'}}>
                <PasswordIcon sx={{mr: 1}}/> Informazioni personali
              </Typography>
              <Divider sx={{mb: 3}}/>

              <Grid container spacing={3}>
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="Altezza in cm"
                    value={height}
                    onChange={(e) => handleHeight(e.target.value)}
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="Peso in kg"
                    value={weight}
                    onChange={(e) => handleWeight(e.target.value)}
                  />
                </Grid>
                <Grid item xs={12}>
                  <Button
                    variant="contained"
                    onClick={updateData}
                    disabled={!weight || !height || !metricsModified}
                  >
                    Salva dati
                  </Button>
                </Grid>
              </Grid>
            </Box>

            {/* Strava Connection Section */}
            <Box sx={{mb: 6}}>
              <Typography variant="h6" component="h2" gutterBottom sx={{display: 'flex', alignItems: 'center'}}>
                <DirectionsRunIcon sx={{mr: 1}}/> Connessione Strava
              </Typography>
              <Divider sx={{mb: 3}}/>

              <Box sx={{display: 'flex', flexDirection: 'column', alignItems: 'center'}}>
                <Typography variant="body1" color="textSecondary" gutterBottom>
                  Collega il tuo account Strava per sincronizzare le tue attività sportive.
                </Typography>
                <Button
                  variant="contained"
                  color="warning"
                  onClick={connectStrava}
                  disabled={!stravaLink}
                  sx={{
                    mt: 2,
                    bgcolor: '#FC4C02',
                    '&:hover': {bgcolor: '#E34402'},
                    '&.Mui-disabled': {bgcolor: '#FFD0B5'}
                  }}
                >
                  Collega Strava
                </Button>
              </Box>
            </Box>

            {/* Telegram Bot Connection Section */}
            <Box sx={{mb: 6}}>
              <Typography variant="h6" component="h2" gutterBottom sx={{display: 'flex', alignItems: 'center'}}>
                <SendIcon sx={{mr: 1}}/> Connessione bot-Telegram
              </Typography>
              <Divider sx={{mb: 3}}/>

              <Box sx={{display: 'flex', flexDirection: 'column', alignItems: 'center'}}>
                <Typography variant="body1" color="textSecondary" gutterBottom>
                  Collegati al nostro bot-telegram per ricevere tutte le notifiche
                </Typography>
                <Box sx={{display: 'flex', flexDirection: 'row', gap: 2}}>
                  <Button
                    variant="contained"
                    color="warning"
                    onClick={connectBotTelegramUser}
                    sx={{
                      paddingLeft: "20",
                      mt: 2,
                      bgcolor: '#FC4C02',
                      '&:hover': {bgcolor: '#E34402'},
                      '&.Mui-disabled': {bgcolor: '#FFD0B5'}
                    }}
                  >
                    Collega bot-Telegram-User
                  </Button>
                  <Button
                    variant="contained"
                    color="warning"
                    onClick={connectBotTelegramGroup}
                    sx={{
                      mt: 2,
                      bgcolor: '#FC4C02',
                      '&:hover': {bgcolor: '#E34402'},
                      '&.Mui-disabled': {bgcolor: '#FFD0B5'}
                    }}
                  >
                    Collega bot-Telegram-Group
                  </Button>
                </Box>
              </Box>
            </Box>

            {/* Delete Account Component */}
            <DeleteAccount
              onSuccess={handleAccountDeletionSuccess}
              onError={handleAccountDeletionError}
              showSnackbar={showSnackbar}
            />
          </Paper>

          {/* Snackbar for Notifications */}
          <CustomSnackbar
            open={snackbar.open}
            message={snackbar.message}
            severity={snackbar.severity}
            onClose={handleCloseSnackbar}
          />
        </Container>
      </Box>
    );
  }
;

export default Settings;
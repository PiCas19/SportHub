import axios from 'axios'
import {useState} from "react";
import {TextField, Button, Typography, Box, Paper, CircularProgress} from "@mui/material";
import {useNavigate, Link} from 'react-router-dom';
import CustomSnackbar from "../../components/CustomSnackbar.tsx";

function Register() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [email, setEmail] = useState("");
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'success',
  });

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

  // Handle registration form submission
  const handleRegister = () => {
    setLoading(true);
    axios.post('http://localhost:8080/auth/register', {
      username,
      password,
      email,
      firstName,
      lastName
    })
      .then(() => {
        setLoading(false);
        navigate("/verify");
      })
      .catch(() => {
        setLoading(false);
        showSnackbar("Errore durante la registrazione. Verifica i tuoi dati e riprova.", "error");
      });
  };

  // Validate form inputs
  const isFormValid =
    username.trim() !== "" &&
    password.trim() !== "" &&
    firstName.trim() !== "" &&
    lastName.trim() !== "" &&
    email.trim() !== "";

  return (
    <Box
      sx={{
        height: "100vh",
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        backgroundColor: "#f4f6f8",
      }}
    >
      <Paper
        elevation={5}
        sx={{
          padding: "40px",
          borderRadius: "12px",
          width: "350px",
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          boxShadow: "0px 4px 10px rgba(0, 0, 0, 0.1)",
        }}
      >
        <Typography variant="h5" sx={{marginBottom: "20px", fontWeight: "bold", color: "#333"}}>
          Registrazione
        </Typography>

        {/* Form Inputs */}
        <TextField
          label="Username"
          variant="outlined"
          fullWidth
          margin="normal"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
        />
        <TextField
          label="Nome"
          variant="outlined"
          fullWidth
          margin="normal"
          value={firstName}
          onChange={(e) => setFirstName(e.target.value)}
        />
        <TextField
          label="Cognome"
          variant="outlined"
          fullWidth
          margin="normal"
          value={lastName}
          onChange={(e) => setLastName(e.target.value)}
        />
        <TextField
          label="Email"
          variant="outlined"
          fullWidth
          margin="normal"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          type="email"
        />
        <TextField
          label="Password"
          type="password"
          variant="outlined"
          fullWidth
          margin="normal"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />

        {/* Register Button */}
        <Button
          variant="contained"
          color="primary"
          fullWidth
          sx={{
            marginTop: "20px",
            borderRadius: "10px",
            padding: "10px",
            fontSize: "16px",
            textTransform: "none",
            position: "relative",
          }}
          onClick={handleRegister}
          disabled={!isFormValid || loading}
        >
          {loading ? (
            <Box sx={{display: 'flex', alignItems: 'center', justifyContent: 'center'}}>
              <CircularProgress size={24} color="inherit" sx={{marginRight: '10px'}}/>
              Registrazione...
            </Box>
          ) : (
            "Registrati"
          )}
        </Button>
        <Typography variant="body2" sx={{marginTop: "15px"}}>
          <Link to="/" style={{textDecoration: "none", color: "#1976d2", fontWeight: "bold"}}>
            Indietro
          </Link>
        </Typography>
        <CustomSnackbar
          open={snackbar.open}
          message={snackbar.message}
          severity={snackbar.severity}
          onClose={handleCloseSnackbar}
        />
      </Paper>
    </Box>
  );
}

export default Register;
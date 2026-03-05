import {useState} from "react";
import {TextField, Button, Typography, Box, Paper} from "@mui/material";
import {useNavigate, Link} from 'react-router-dom';
import axios from "axios";
import Cookies from "js-cookie";
import CustomSnackbar from "../../components/CustomSnackbar.tsx";

function Login() {
  // State for form inputs
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const navigate = useNavigate();
  // State for snackbar notifications
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'success',
  });

  const handleLogin = () => {
    axios.post('http://localhost:8080/auth/login', {
      username,
      password,
    }, {})
      .then(async (response: any) => {
        const expiresIn3Seconds = new Date(new Date().getTime() + 3 * 1000);

        Cookies.set("auth_token", response.data.accessToken, {
          expires: expiresIn3Seconds,
          secure: true,
          sameSite: "Strict"
        });
        Cookies.set("refresh_token", response.data.refreshToken, {expires: 7, secure: true, sameSite: "Strict"});
        navigate("/Home");
        console.log(response.data.accessToken);
      })
      .catch(function (error) {
        showSnackbar("Errore durante l'accesso. Verifica le tue credenziali e riprova.", 'error');
        console.error("Login error:", error);
      });
  };

  // Show snackbar notification
  const showSnackbar = (message: any, severity: any) => {
    setSnackbar({
      open: true,
      message,
      severity
    });
  };

  const handleCloseSnackbar = () => {
    setSnackbar({
      ...snackbar,
      open: false
    });
  };

  const isFormValid = username.trim() !== "" && password.trim() !== "";

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
          Accedi
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

        {/* Login Button */}
        <TextField
          label="Password"
          type="password"
          variant="outlined"
          fullWidth
          margin="normal"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />

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
          }}
          onClick={handleLogin}
          disabled={!isFormValid}
        >
          Accedi
        </Button>

        {/* Navigation Links */}
        <Typography variant="body2"
                    sx={{marginTop: "15px", display: "flex", flexDirection: "column", alignItems: "center"}}>
          <Link to="/register"
                style={{paddingBottom: "10px", textDecoration: "none", color: "#1976d2", fontWeight: "bold"}}>
            Crea un nuovo account
          </Link>
          <Link to="/activateForgotPassword" style={{color: "silver", textDecoration: "underline"}}>
            Password dimenticata
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

export default Login;
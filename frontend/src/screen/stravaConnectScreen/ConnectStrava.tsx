import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import axios from "axios";
import Cookies from "js-cookie";
import { CircularProgress, Typography, Paper, Box } from "@mui/material";

function ConnectStrava() {
    // Hook per leggere i parametri dell'URL (es. ?code=abc123)
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();

    // Stato di caricamento e gestione errori
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    // Effetto eseguito al primo render per recuperare il token Strava
    useEffect(() => {
        const code = searchParams.get("code");

        // Se non c'è il codice nell'URL, mostra errore
        if (!code) {
            setError("Error: No authorization code received.");
            setLoading(false);
            return;
        }

        const fetchToken = async () => {
            try {
                // Recupera il token di autenticazione dell'utente (dal cookie)
                const token = Cookies.get("auth_token");
                if (!token) throw new Error("Authentication required. Log in.");

                // Chiamata al backend per scambiare il codice con un token Strava
                const response = await axios.post(
                  "http://localhost:8080/api/strava/token",
                  { code },
                  {
                      headers: { Authorization: `Bearer ${token}` },
                  }
                );

                // Se tutto ok, naviga alla home
                if (response.status === 200) {
                    navigate("/home");
                } else {
                    setError("Error while connecting to Strava.");
                }
            } catch (error: any) {
                console.error("Errore:", error);
                setError(error.message || "Unable to connect to Strava.");
            } finally {
                setLoading(false);
            }
        };

        fetchToken();
    }, [searchParams, navigate]);

    // UI di feedback durante la connessione o in caso di errore
    return (
      <Box sx={{ display: "flex", justifyContent: "center", alignItems: "center", height: "100vh" }}>
          <Paper sx={{ padding: 4, textAlign: "center", borderRadius: 3, boxShadow: 3 }}>
              {loading ? (
                <>
                    <Typography variant="h6">🔄 Connessione a Strava in corso...</Typography>
                    <CircularProgress sx={{ marginTop: 2 }} />
                </>
              ) : error ? (
                <Typography color="error">{error}</Typography>
              ) : (
                <Typography variant="h6">✅ Connessione completata con successo!</Typography>
              )}
          </Paper>
      </Box>
    );
}

export default ConnectStrava;

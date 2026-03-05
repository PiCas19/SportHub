import { useParams } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { Box, CircularProgress, Typography, Paper } from '@mui/material';
import { MapContainer, TileLayer, Polyline, Marker, Popup } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import axios from 'axios';

const PublicMapPage = () => {
    const { token } = useParams();
    const [mapData, setMapData] = useState<any>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

  // Fetch map data using token
  useEffect(() => {
        if (!token) return;

        axios.get(`http://localhost:8080/api/map/${token}`)
            .then((res) => {
                setMapData(res.data);
                setLoading(false);
            })
            .catch(() => {
                setError('Mappa non trovata o token non valido.');
                setLoading(false);
            });
    }, [token]);

  // Show loading spinner
  if (loading) {
        return (
            <Box
                display="flex"
                justifyContent="center"
                alignItems="center"
                height="100vh"
                sx={{ backgroundColor: "#f4f6f8" }}
            >
                <CircularProgress />
            </Box>
        );
    }

  // Show error message
  if (error) {
        return (
            <Box
                display="flex"
                justifyContent="center"
                alignItems="center"
                height="100vh"
                sx={{ backgroundColor: "#f4f6f8" }}
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
                    <Typography variant="h6" color="error" textAlign="center" sx={{ fontWeight: "bold" }}>
                        {error}
                    </Typography>
                </Paper>
            </Box>
        );
    }

  // Render map with data
  return (
        <Box
            sx={{
                minHeight: "100vh",
                display: "flex",
                justifyContent: "center",
                alignItems: "center",
                backgroundColor: "#f4f6f8",
                padding: "20px",
            }}
        >
            <Paper
                elevation={5}
                sx={{
                    padding: "30px",
                    borderRadius: "12px",
                    width: "100%",
                    maxWidth: "900px",
                    display: "flex",
                    flexDirection: "column",
                    boxShadow: "0px 4px 10px rgba(0, 0, 0, 0.1)",
                }}
            >
                <Typography variant="h5" gutterBottom textAlign="center" sx={{ fontWeight: "bold", color: "#333" }}>
                    Attività vicino a {mapData?.location || 'posizione'}
                </Typography>

              {/* Map Container */}
              <Box sx={{ mt: 2, borderRadius: "12px", overflow: "hidden" }}>
                    <MapContainer
                        center={[mapData.targetLat, mapData.targetLon]}
                        zoom={13}
                        style={{ height: '500px', width: '100%' }}
                    >
                        <TileLayer
                            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                            attribution="&copy; OpenStreetMap contributors"
                        />
                        <Marker position={[mapData.targetLat, mapData.targetLon]}>
                            <Popup>Target Location</Popup>
                        </Marker>
                        {mapData.segments.map((seg: any, idx: number) => (
                            <Polyline
                                key={idx}
                                positions={seg.polyline.map((point: number[]) => [point[0], point[1]])}
                                color={idx % 2 === 0 ? 'red' : 'blue'}
                                weight={4}
                            />
                        ))}
                    </MapContainer>
                </Box>
            </Paper>
        </Box>
    );
};

export default PublicMapPage;
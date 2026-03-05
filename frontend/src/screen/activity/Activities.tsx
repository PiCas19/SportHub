import {useState, useEffect, useCallback} from 'react';
import {
  Box,
  Container,
  Typography,
  Paper,
  List,
  ListItemText,
  ListItemAvatar,
  Avatar,
  Divider,
  Dialog,
  DialogContent,
  DialogActions,
  Button,
  Grid,
  Card,
  CardContent,
  Chip,
  IconButton,
  AppBar,
  Toolbar,
  ListItemButton,
  Fab,
  CircularProgress,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  DialogTitle,
  Slider,
  Stack,
} from '@mui/material';
import {
  DirectionsRun as RunIcon,
  DirectionsBike as BikeIcon,
  Pool as SwimIcon,
  Hiking as HikingIcon,
  AccessTime as TimeIcon,
  BarChart as StatsIcon,
  Place as LocationIcon,
  Close as CloseIcon,
  Add as AddIcon,
  Search as SearchIcon,
  FilterAlt as FilterIcon
} from '@mui/icons-material';
import axiosInstance from '../api/axiosConfig.ts';
import HeaderScreen from "../../components/HeaderScreen.tsx";
import 'leaflet/dist/leaflet.css';
import {MapContainer, Polyline, TileLayer} from "react-leaflet";
import DecodePolylineMap from '../../components/DecodePolylineMap.tsx';
import FilterSport from "../../components/filter/FilterSport.tsx";
import CustomSnackbar from "../../components/CustomSnackbar.tsx";

interface Activity {
  id: string;
  title: string;
  type: 'run' | 'ride' | 'swim' | 'walk' | 'other';
  date: string;
  duration: number;
  distance: number;
  elevationGain: number;
  averageSpeed: number;
  maxSpeed: number;
  calories: number;
  description: string | null;
  location: string;
  mapPolyline?: string;
  startDateLocal?: string;
  sportType: string;
  hasHeartRate: boolean;
  startCoordinates?: [number, number];
}

interface NewActivity {
  name: string;
  sportType: string;
  startDateLocal: string;
  elapsedTime: number;
  distance: number;
}

interface FilterParams {
  keywords?: string;
  sportType?: string;
  minDistance?: number;
  maxDistance?: number;
  startDate?: string;
  endDate?: string;
}

const Activities = () => {
  // State for activities list, selected activity, map route, and UI controls
  const [activities, setActivities] = useState<Activity[]>([]);
  const [selectedActivity, setSelectedActivity] = useState<Activity | null>(null);
  const [mapRoute, setMapRoute] = useState<[number, number][]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'success',
  });
  const [isFormOpen, setIsFormOpen] = useState<boolean>(false);
  const [dateError, setDateError] = useState<string>('');
  const [newActivity, setNewActivity] = useState<NewActivity>({
    name: '',
    sportType: 'Run',
    startDateLocal: new Date().toISOString().substring(0, 16),
    elapsedTime: 0,
    distance: 0,
  });
  const [maxDistance] = useState<number>(100);
  const [filterParams, setFilterParams] = useState<FilterParams>({
    keywords: '',
    sportType: 'all',
    minDistance: 0,
    maxDistance: 100
  });
  const [searchInput, setSearchInput] = useState<string>('');

  // Clean filter parameters by removing empty or undefined values
  const cleanFilterParams = (params: FilterParams): Partial<FilterParams> => {
    const cleaned: Partial<FilterParams> = {};
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        cleaned[key as keyof FilterParams] = value;
      }
    });
    return cleaned;
  };

  // Update map route when selected activity changes
  useEffect(() => {
    if (selectedActivity?.mapPolyline) {
      try {
        const decodedPath = DecodePolylineMap(selectedActivity.mapPolyline);
        setMapRoute(decodedPath);
      } catch (error) {
        console.error("Error decoding polyline:", error);
        setMapRoute([]);
      }
    } else {
      setMapRoute([]);
    }
  }, [selectedActivity]);

  // Debounce search input to reduce API calls
  const debouncedNameFilter = useCallback(
    (value: string) => {
      const handler = setTimeout(() => {
        setFilterParams(prev => ({
          ...prev,
          keywords: value
        }));
      }, 500);

      return () => {
        clearTimeout(handler);
      };
    },
    []
  );

  // Handle search input change
  const handleSearchChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const value = event.target.value;
    setSearchInput(value);
    debouncedNameFilter(value);
  };

  // Update filter parameters
  const handleFilterChange = (paramName: keyof FilterParams, value: string | number) => {
    if (paramName === 'keywords') {
      setSearchInput(value as string);
      debouncedNameFilter(value as string);
    } else {
      setFilterParams(prev => ({
        ...prev,
        [paramName]: value
      }));
    }
  };

  // Handle distance filter slider
  const handleDistanceChange = (_event: Event, newValue: number | number[]) => {
    const [minDistance, maxDistance] = newValue as [number, number];
    setFilterParams(prev => ({
      ...prev,
      minDistance,
      maxDistance
    }));
  };

  const handleSportChange = (newSport: string) => {
    handleFilterChange('sportType', newSport);
  };

  const mapSportTypeToBackend = (frontendType: string = ''): string => {
    const mapping: Record<string, string> = {
      'run': 'run',
      'ride': 'ride',
      'swim': 'swim',
      'walk': 'walk',
      'all': 'all'
    };
    return mapping[frontendType] || frontendType;
  };

  const fetchActivities = useCallback(() => {
    setLoading(true);

    const cleanedParams = cleanFilterParams(filterParams);
    const payload = {
      ...cleanedParams,
      sportType: mapSportTypeToBackend(cleanedParams.sportType)
    };

    axiosInstance.post("api/strava/activities/filter", payload)
      .then((response: any) => {
        const activitiesData = response.data.map((activity: any) => {
          const startCoords = activity.start_latlng?.length ?
            [activity.start_latlng[0], activity.start_latlng[1]] as [number, number] :
            undefined;

          return {
            id: activity.id.toString(),
            title: activity.name,
            type: activity.sport_type?.toLowerCase() as 'run' | 'ride' | 'swim' | 'walk' | 'other',
            date: activity.start_date,
            duration: activity.moving_time,
            distance: activity.distance,
            elevationGain: activity.total_elevation_gain,
            averageSpeed: activity.average_speed,
            maxSpeed: activity.max_speed,
            calories: activity.calories,
            description: activity.description,
            location: activity.start_latlng?.length ?
              `Lat: ${activity.start_latlng[0]}, Lng: ${activity.start_latlng[1]}` :
              'Location not available',
            sportType: activity.sport_type,
            hasHeartRate: activity.has_heartrate || false,
            startDateLocal: activity.start_date_local,
            mapPolyline: activity.map?.summary_polyline || null,
            startCoordinates: startCoords
          };
        });

        setActivities(activitiesData);
        setLoading(false);
      })
      .catch(() => {
        setLoading(false);
      });
  }, [filterParams]);

  // Fetch activities when filter params change
  useEffect(() => {
    fetchActivities();
  }, [fetchActivities]);

  // Open form to add new activity
  const handleAddActivity = () => {
    setIsFormOpen(true);
    setDateError('');
    setNewActivity(prev => ({
      ...prev,
      startDateLocal: new Date().toISOString().substring(0, 16)
    }));
  };

  const handleCloseForm = () => {
    setIsFormOpen(false);
    setDateError('');
  };

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

  const validateDate = (dateString: string): boolean => {
    const selectedDate = new Date(dateString);
    const currentDate = new Date();
    return selectedDate <= currentDate;
  };

  const handleInputChange = (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const {name, value} = event.target;

    if (name === 'startDateLocal') {
      if (!validateDate(value)) {
        setDateError('Non puoi selezionare una data futura');
      } else {
        setDateError('');
      }
    }

    setNewActivity(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSelectChange = (event: React.ChangeEvent<{ name?: string; value: unknown }>) => {
    const name = event.target.name as string;
    const value = event.target.value as string;
    setNewActivity(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmitActivity = () => {
    if (!validateDate(newActivity.startDateLocal)) {
      setDateError('Non puoi selezionare una data futura');
      return;
    }

    setLoading(true);
    axiosInstance.post('api/strava/activities', newActivity)
      .then(() => {
        showSnackbar("Attività aggiunta con successo", 'success');
        fetchActivities();
        setIsFormOpen(false);
        setNewActivity({
          name: '',
          sportType: 'Run',
          startDateLocal: new Date().toISOString().substring(0, 16),
          elapsedTime: 0,
          distance: 0,
        });
        setDateError('');
      })
      .catch(() => {
        setLoading(false);
      });
  };

  const getActivityIcon = (type: string) => {
    switch (type) {
      case 'run':
        return <RunIcon/>;
      case 'ride':
        return <BikeIcon/>;
      case 'swim':
        return <SwimIcon/>;
      case 'walk':
        return <HikingIcon/>;
      default:
        return <StatsIcon/>;
    }
  };

  // Format duration in HH:MM
  const formatDuration = (minutes: number) => {
    const hours = Math.floor(minutes / 60);
    const min = minutes % 60;
    return `${hours.toString().padStart(2, '0')}:${min.toString().padStart(2, '0')}`;
  };

  // Format distance to kilometers
  const formatDistance = (meters: number) => (meters / 1000).toFixed(2);

  // Format speed to km/h
  const formatSpeed = (mps: number) => (mps * 3.6).toFixed(1);

  const getActivityColor = (type: string) => {
    switch (type) {
      case 'run':
        return '#FF5722';
      case 'ride':
        return '#2196F3';
      case 'swim':
        return '#00BCD4';
      case 'walk':
        return '#4CAF50';
      default:
        return '#607D8B';
    }
  };

  const formatActivityType = (type: string) => {
    switch (type) {
      case 'run':
        return 'Corsa';
      case 'ride':
        return 'Bici';
      case 'swim':
        return 'Nuoto';
      case 'walk':
        return 'Camminata';
      default:
        return 'Altro';
    }
  };

  const handleOpenActivity = (activity: Activity) => {
    setSelectedActivity(activity);
  };

  const handleCloseActivity = () => {
    setSelectedActivity(null);
  };

  const getCurrentDateTimeString = () => {
    return new Date().toISOString().substring(0, 16);
  };

  return (
    <Box sx={{flexGrow: 1}}>
      <HeaderScreen title="Attività"/>
      <Container maxWidth="md" sx={{py: 4}}>
        <Paper elevation={2} sx={{p: 2, mb: 2}}>
          <Typography variant="h6" gutterBottom>
            Filtri
          </Typography>
          <Grid container spacing={2}>
            <Grid item xs={12}>
              <Box sx={{display: "flex", flexDirection: "row", justifyContent: "space-between", alignItems: 'center'}}>
                <TextField
                  fullWidth
                  label="Cerca per nome"
                  variant="outlined"
                  value={searchInput}
                  onChange={handleSearchChange}
                  placeholder="Inserisci il nome dell'attività"
                  InputProps={{
                    startAdornment: <SearchIcon/>,
                    endAdornment: searchInput && (
                      <IconButton onClick={() => setSearchInput('')} size="small">
                        <CloseIcon fontSize="small"/>
                      </IconButton>
                    )
                  }}
                />
                <FilterSport
                  sportType={filterParams.sportType || 'all'}
                  onSportChange={handleSportChange}
                />
              </Box>
            </Grid>
            <Grid item xs={12}>
              <Stack spacing={2} direction="row" alignItems="center" sx={{px: 2}}>
                <FilterIcon color="primary"/>
                <Typography variant="body2" sx={{minWidth: 100}}>
                  Distanza (km):
                </Typography>
                <Typography variant="body2" sx={{minWidth: 80}}>
                  {filterParams.minDistance} - {filterParams.maxDistance} km
                </Typography>
                <Slider
                  value={[filterParams.minDistance || 0, filterParams.maxDistance || 100]}
                  onChange={handleDistanceChange}
                  valueLabelDisplay="auto"
                  min={0}
                  max={maxDistance}
                />
              </Stack>
            </Grid>
          </Grid>
        </Paper>

        {/* Activities list */}
        <Paper elevation={2}>
          <List sx={{bgcolor: 'background.paper'}}>
            {activities.length > 0 ? (
              activities.map((activity, index) => (
                <Box key={activity.id}>
                  <ListItemButton
                    onClick={() => handleOpenActivity(activity)}
                    sx={{
                      transition: 'all 0.2s',
                      '&:hover': {bgcolor: 'rgba(0, 0, 0, 0.04)'}
                    }}
                  >
                    <ListItemAvatar>
                      <Avatar sx={{bgcolor: getActivityColor(activity.type)}}>
                        {getActivityIcon(activity.type)}
                      </Avatar>
                    </ListItemAvatar>
                    <ListItemText
                      primary={
                        <Typography variant="subtitle1" fontWeight="medium">
                          {activity.title}
                        </Typography>
                      }
                      secondary={
                        <Box component="div">
                          <Box sx={{mt: 0.5, display: 'flex', alignItems: 'center', flexWrap: 'wrap', gap: 1}}>
                            <Chip
                              size="small"
                              label={formatActivityType(activity.type)}
                              sx={{
                                bgcolor: getActivityColor(activity.type),
                                color: 'white',
                                fontWeight: 'bold'
                              }}
                            />
                            {activity.distance > 0 && (
                              <Typography variant="body2" component="span" sx={{display: 'flex', alignItems: 'center'}}>
                                <StatsIcon fontSize="small" sx={{mr: 0.5}}/>
                                {formatDistance(activity.distance)} km
                              </Typography>
                            )}
                            <Typography variant="body2" component="span" sx={{display: 'flex', alignItems: 'center'}}>
                              <TimeIcon fontSize="small" sx={{mr: 0.5}}/>
                              {formatDuration(activity.duration)}
                            </Typography>
                          </Box>
                        </Box>
                      }
                    />
                  </ListItemButton>
                  {index < activities.length - 1 && <Divider component="li"/>}
                </Box>
              ))
            ) : (
              <Box sx={{p: 3, textAlign: 'center'}}>
                <Typography variant="body1" color="text.secondary">
                  {loading ? 'Caricamento attività...' : 'Nessuna attività trovata con i filtri applicati'}
                </Typography>
                {loading && <CircularProgress sx={{mt: 2}}/>}
              </Box>
            )}
          </List>
        </Paper>

        {/* Activity details dialog */}
        {selectedActivity && (
          <Dialog
            open={Boolean(selectedActivity)}
            onClose={handleCloseActivity}
            fullWidth
            maxWidth="md"
            scroll="paper"
          >
            <AppBar position="relative" sx={{bgcolor: getActivityColor(selectedActivity.type)}}>
              <Toolbar>
                <IconButton edge="start" color="inherit" onClick={handleCloseActivity} aria-label="close">
                  <CloseIcon/>
                </IconButton>
                <Typography sx={{ml: 2, flex: 1}} variant="h6" component="div">
                  {selectedActivity.title}
                </Typography>
              </Toolbar>
            </AppBar>

            <DialogContent>
              <Grid container spacing={3}>
                <Grid item xs={12}>
                  <Box sx={{display: 'flex', alignItems: 'center', flexWrap: 'wrap', gap: 1, mb: 2}}>
                    <Chip
                      label={formatActivityType(selectedActivity.type)}
                      sx={{
                        bgcolor: getActivityColor(selectedActivity.type),
                        color: 'white',
                        fontWeight: 'bold'
                      }}
                    />
                  </Box>
                </Grid>

                {selectedActivity.mapPolyline && mapRoute.length > 0 && (
                  <Grid item xs={12}>
                    <Card>
                      <CardContent sx={{p: 0}}>
                        <Box sx={{height: 300, width: '100%', position: 'relative'}}>
                          <MapContainer
                            center={mapRoute[0] || selectedActivity.startCoordinates || [0, 0]}
                            zoom={13}
                            style={{height: '300px', width: '100%'}}
                          >
                            <TileLayer
                              url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                              attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                            />
                            <Polyline
                              positions={mapRoute}
                              color={getActivityColor(selectedActivity.type)}
                              weight={4}
                            />
                          </MapContainer>
                        </Box>
                      </CardContent>
                    </Card>
                  </Grid>
                )}

                {[
                  {label: 'Distanza', value: `${formatDistance(selectedActivity.distance)} km`},
                  {label: 'Durata', value: formatDuration(selectedActivity.duration)},
                  {label: 'Velocità Media', value: `${formatSpeed(selectedActivity.averageSpeed)} km/h`},
                  {label: 'Calorie', value: selectedActivity.calories.toString()}
                ].map((item, index) => (
                  <Grid item xs={12} sm={6} md={3} key={index}>
                    <Card>
                      <CardContent>
                        <Typography color="text.secondary" gutterBottom>
                          {item.label}
                        </Typography>
                        <Typography variant="h5" component="div">
                          {item.value}
                        </Typography>
                      </CardContent>
                    </Card>
                  </Grid>
                ))}

                <Grid item xs={12}>
                  <Card>
                    <CardContent>
                      <Typography variant="h6" gutterBottom>
                        Dettagli Aggiuntivi
                      </Typography>
                      <Grid container spacing={2}>
                        <Grid item xs={12} sm={4}>
                          <Typography color="text.secondary">Dislivello</Typography>
                          <Typography variant="body1">{selectedActivity.elevationGain} m</Typography>
                        </Grid>
                        <Grid item xs={12} sm={4}>
                          <Typography color="text.secondary">Velocità Massima</Typography>
                          <Typography variant="body1">{formatSpeed(selectedActivity.maxSpeed)} km/h</Typography>
                        </Grid>
                        <Grid item xs={12} sm={4}>
                          <Typography color="text.secondary">Luogo</Typography>
                          <Typography variant="body1" sx={{display: 'flex', alignItems: 'center'}}>
                            <LocationIcon fontSize="small" sx={{mr: 0.5}}/>
                            {selectedActivity.location}
                          </Typography>
                        </Grid>
                      </Grid>
                    </CardContent>
                  </Card>
                </Grid>

                {selectedActivity.description && (
                  <Grid item xs={12}>
                    <Card>
                      <CardContent>
                        <Typography variant="h6" gutterBottom>Note</Typography>
                        <Typography variant="body1">{selectedActivity.description}</Typography>
                      </CardContent>
                    </Card>
                  </Grid>
                )}
              </Grid>
            </DialogContent>

            <DialogActions>
              <Button onClick={handleCloseActivity}>Chiudi</Button>
            </DialogActions>
          </Dialog>
        )}

        {/* Add activity form dialog */}
        <Dialog open={isFormOpen} onClose={handleCloseForm} fullWidth maxWidth="md">
          <DialogTitle>Aggiungi Nuova Attività</DialogTitle>
          <DialogContent>
            <Grid container spacing={2} sx={{mt: 1}}>
              <Grid item xs={12}>
                <TextField
                  fullWidth
                  label="Nome Attività"
                  name="name"
                  value={newActivity.name}
                  onChange={handleInputChange}
                  required
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <FormControl fullWidth>
                  <InputLabel id="sportType-label">Sport</InputLabel>
                  <Select
                    labelId="sportType-label"
                    id="sportType"
                    name="sportType"
                    value={newActivity.sportType}
                    label="Sport"
                    onChange={(e) => handleSelectChange(e as any)}
                  >
                    {['Run', 'Ride', 'Swim', 'Walk', 'Velomobile'].map((sport) => (
                      <MenuItem key={sport} value={sport}>{formatActivityType(sport)}</MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Data e Ora"
                  name="startDateLocal"
                  type="datetime-local"
                  value={newActivity.startDateLocal}
                  onChange={handleInputChange}
                  InputLabelProps={{shrink: true}}
                  inputProps={{
                    max: getCurrentDateTimeString()
                  }}
                  error={!!dateError}
                  helperText={dateError}
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Durata (minuti)"
                  name="elapsedTime"
                  type="number"
                  value={newActivity.elapsedTime}
                  onChange={handleInputChange}
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Distanza (metri)"
                  name="distance"
                  type="number"
                  value={newActivity.distance}
                  onChange={handleInputChange}
                />
              </Grid>
            </Grid>
          </DialogContent>
          <DialogActions>
            <Button onClick={handleCloseForm}>Annulla</Button>
            <Button
              onClick={handleSubmitActivity}
              color="primary"
              variant="contained"
              disabled={loading || !!dateError || !newActivity.name}
            >
              {loading ? <CircularProgress size={24}/> : 'Salva'}
            </Button>
          </DialogActions>
        </Dialog>

        <Fab
          color="primary"
          aria-label="add"
          sx={{
            position: 'fixed',
            bottom: 24,
            right: 24,
          }}
          onClick={handleAddActivity}
          disabled={loading}
        >
          {loading ? <CircularProgress size={24} color="inherit"/> : <AddIcon/>}
        </Fab>

        <CustomSnackbar
          open={snackbar.open}
          message={snackbar.message}
          severity={snackbar.severity}
          onClose={handleCloseSnackbar}
        />
      </Container>
    </Box>
  );
};

export default Activities;
import {useEffect, useState} from 'react';
import {
  Box,
  Typography,
  Container,
  TextField,
  Button,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  ListItemSecondaryAction,
  IconButton,
  Paper,
  Divider,
  LinearProgress,
  Dialog,
  DialogContent,
  Grid,
  DialogActions,
  DialogTitle,
  FormControl,
  InputLabel,
  Select, MenuItem, CircularProgress, Fab
} from '@mui/material';
import {
  Add as AddIcon,
  Delete as DeleteIcon,
  CheckCircle as CheckCircleIcon,
} from '@mui/icons-material';
import HeaderScreen from "../../components/HeaderScreen.tsx";
import axiosInstance from "../api/axiosConfig.ts";

// Define enums for the goal types
enum GoalType {
  DISTANCE = 'DISTANCE',
  CALORIES = 'CALORIES',
  DURATION = 'DURATION',
  ACTIVITIES = 'ACTIVITIES'
}

enum Period {
  DAILY = 'DAILY',
  WEEKLY = 'WEEKLY',
  MONTHLY = 'MONTHLY',
  YEARLY = 'YEARLY'
}

enum SportType {
  RUN = 'RUN',
  SWIM = 'SWIM',
  BIKE = 'BIKE',
  WALK = 'WALK',
  OTHER = 'OTHER',
  ALL = 'ALL'
}

// Interface for the form data
interface GoalFormData {
  type: GoalType;
  sportType: SportType;
  period: Period;
  targetValue: string;
}

// Interface for the goal object
interface Goal {
  id: number;
  name: number;
  sportType: string;
  goalType: string;
  period: string;
  currentValue?: number;
  percentage?: number;
  target?: number;
  unit: string;
}

function Goals() {
  // State for loading, form, and goals
  const [loading, setLoading] = useState<boolean>(false);
  const [isFormOpen, setIsFormOpen] = useState<boolean>(false);
  const [goals, setGoals] = useState<Goal[]>([]);

  // New state for the goal form
  const [formData, setFormData] = useState<GoalFormData>({
    type: GoalType.DISTANCE,
    sportType: SportType.RUN,
    period: Period.WEEKLY,
    targetValue: ''
  });

  // Get the appropriate target value label based on goal type
  const getTargetValueLabel = () => {
    switch (formData.type) {
      case GoalType.DISTANCE:
        return 'Target (km)';
      case GoalType.CALORIES:
        return 'Target (kcal)';
      case GoalType.DURATION:
        return 'Target (minuti)';
      case GoalType.ACTIVITIES:
        return 'Target (attività)';
      default:
        return 'Target';
    }
  };

  // Get the appropriate placeholder text based on goal type
  const getTargetValuePlaceholder = () => {
    switch (formData.type) {
      case GoalType.DISTANCE:
        return 'es. 100';
      case GoalType.CALORIES:
        return 'es. 2000';
      case GoalType.DURATION:
        return 'es. 180';
      case GoalType.ACTIVITIES:
        return 'es. 5';
      default:
        return '';
    }
  };

  // Fetch goals on mount
  useEffect(() => {
    fetchGoals();
  }, []);

  // Fetch goals from API
  const fetchGoals = () => {
    setLoading(true);
    axiosInstance.get('api/goals')
      .then((response: any) => {
        const goalsWithProgress = response.data.map((goal: any) => {
          return {
            id: goal.id,
            name: goal.name,
            sportType: goal.sportType,
            goalType: goal.goalType,
            period: goal.period,
            percentage: goal.percentage,
            target: goal.target,
            currentValue: goal.current,
            unit: goal.unit
          };
        });

        setGoals(goalsWithProgress);
        setLoading(false);
      })
      .catch((error) => {
        console.error('Error fetching goals:', error);
        setLoading(false);
      });
  };

  // Handle form input changes
  const handleFormChange = (event: React.ChangeEvent<{ name?: string; value: unknown }>) => {
    const name = event.target.name as keyof GoalFormData;
    const value = event.target.value as string;

    setFormData({
      ...formData,
      [name]: value
    });
  };

  // Submit new goal
  const handleSubmitGoal = () => {
    if (!formData.targetValue) return;

    setLoading(true);
    const newGoal = {
      goalType: formData.type,
      sportType: formData.sportType,
      period: formData.period,
      targetValue: parseInt(formData.targetValue)
    };

    axiosInstance.post('api/goals', newGoal)
      .then(() => {
        setIsFormOpen(false);
        setFormData({
          type: GoalType.DISTANCE,
          sportType: SportType.RUN,
          period: Period.WEEKLY,
          targetValue: ''
        });
        fetchGoals();
      })
      .catch((error) => {
        console.error('Error creating goal:', error);
        setLoading(false);
      });
  };

  // Delete a goal
  const deleteGoal = (goalId: number) => {
    setLoading(true);
    axiosInstance.delete(`api/goals/${goalId}`)
      .then(() => {
        setGoals(goals.filter(goal => goal.id !== goalId));
        setLoading(false);
      })
      .catch((error) => {
        console.error('Error deleting goal:', error);
        setLoading(false);
      });
  };

  // Get period name in Italian
  const getPeriodName = (period: string): string => {
    switch (period) {
      case Period.DAILY:
        return 'giornaliero';
      case Period.WEEKLY:
        return 'settimanale';
      case Period.MONTHLY:
        return 'mensile';
      case Period.YEARLY:
        return 'annuo';
      default:
        return '';
    }
  };

  const handleAddActivity = () => {
    setIsFormOpen(true);
  };

  const handleCloseForm = () => {
    setIsFormOpen(false);
  };

  // Calculate overall progress across all goals
  const calculateOverallProgress = () => {
    if (goals.length === 0) return 0;

    const totalProgress = goals.reduce((sum, goal) => sum + (goal.percentage || 0), 0);
    return Math.round(totalProgress / goals.length);
  };

  return (
    <Box>
      <HeaderScreen title="I tuoi obiettivi"/>
      <Container maxWidth="md" sx={{py: 4}}>
        <Paper elevation={2} sx={{p: 3}}>
          <Typography variant="h6" component="h2" sx={{mb: 2}}>
            I tuoi obiettivi
          </Typography>

          {/* Loading State */}
          {loading ? (
            <Box sx={{display: 'flex', justifyContent: 'center', my: 4}}>
              <CircularProgress/>
            </Box>
          ) : goals.length === 0 ? (
            <Typography color="text.secondary" align="center" sx={{py: 4}}>
              Non hai ancora obiettivi. Aggiungi il tuo primo obiettivo!
            </Typography>
          ) : (
            <List>
              {goals.map((goal, index) => (
                <Box key={goal.id}>
                  {index > 0 && <Divider/>}
                  <ListItem
                    sx={{
                      borderRadius: 1,
                      my: 1,
                      flexDirection: 'column',
                      alignItems: 'flex-start'
                    }}
                  >
                    <Box sx={{display: 'flex', width: '100%', alignItems: 'center', mb: 1}}>
                      <ListItemIcon>
                        <CheckCircleIcon color={goal.percentage === 100 ? "success" : "action"}/>
                      </ListItemIcon>
                      <ListItemText
                        primary={`${goal.name} in ${goal.unit}`}
                        secondary={
                          <>
                            Obiettivo ID: {goal.id} <br/>
                            Tempo: {getPeriodName(goal.period)}
                          </>
                        }/>
                      <ListItemSecondaryAction>
                        <IconButton edge="end" onClick={() => deleteGoal(goal.id)} color="error">
                          <DeleteIcon/>
                        </IconButton>
                      </ListItemSecondaryAction>
                    </Box>

                    <Box sx={{width: '100%', px: 2}}>
                      <Box sx={{display: 'flex', justifyContent: 'space-between', mb: 0.5}}>
                        <Typography variant="body2" color="text.secondary">
                          {goal.currentValue} su {goal.target}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          {goal.percentage}%
                        </Typography>
                      </Box>
                      <LinearProgress
                        variant="determinate"
                        value={goal.percentage || 0}
                        color={goal.percentage === 100 ? "success" : "primary"}
                        sx={{height: 8, borderRadius: 4}}
                      />
                    </Box>
                  </ListItem>
                </Box>
              ))}
            </List>
          )}

          {/* Overall Progress */}
          {goals.length > 0 && (
            <Box sx={{mt: 4}}>
              <Typography variant="body2" color="text.secondary" align="center" sx={{mb: 1}}>
                {calculateOverallProgress()}% di obiettivi completati (media totale)
              </Typography>
              <LinearProgress
                variant="determinate"
                value={calculateOverallProgress()}
                color="success"
                sx={{height: 8, borderRadius: 4}}
              />
            </Box>
          )}

          <Dialog
            open={isFormOpen}
            onClose={handleCloseForm}
            fullWidth
            maxWidth="md"
          >
            <DialogTitle>Aggiungi Nuovo Obiettivo</DialogTitle>
            <DialogContent>
              <Grid container spacing={2} sx={{mt: 1}}>
                <Grid item xs={12} sm={6}>
                  <FormControl fullWidth>
                    <InputLabel id="type-label">Tipo di Obiettivo</InputLabel>
                    <Select
                      labelId="type-label"
                      id="type"
                      name="type"
                      value={formData.type}
                      onChange={(e) => handleFormChange(e)}
                      label="Tipo di Obiettivo"
                    >
                      <MenuItem value={GoalType.DISTANCE}>Distanza</MenuItem>
                      <MenuItem value={GoalType.CALORIES}>Calorie</MenuItem>
                      <MenuItem value={GoalType.DURATION}>Durata</MenuItem>
                      <MenuItem value={GoalType.ACTIVITIES}>Attività</MenuItem>
                    </Select>
                  </FormControl>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <FormControl fullWidth>
                    <InputLabel id="sportType-label">Tipo di Sport</InputLabel>
                    <Select
                      labelId="sportType-label"
                      id="sportType"
                      name="sportType"
                      value={formData.sportType}
                      onChange={() => handleFormChange}
                      label="Tipo di Sport"
                    >
                      <MenuItem value={SportType.RUN}>Corsa</MenuItem>
                      <MenuItem value={SportType.SWIM}>Nuoto</MenuItem>
                      <MenuItem value={SportType.BIKE}>Ciclismo</MenuItem>
                      <MenuItem value={SportType.WALK}>Camminata</MenuItem>
                      <MenuItem value={SportType.OTHER}>Altro</MenuItem>
                      <MenuItem value={SportType.ALL}>Tutti</MenuItem>
                    </Select>
                  </FormControl>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <FormControl fullWidth>
                    <InputLabel id="period-label">Periodo</InputLabel>
                    <Select
                      labelId="period-label"
                      id="period"
                      name="period"
                      value={formData.period}
                      onChange={() => handleFormChange}
                      label="Periodo"
                    >
                      <MenuItem value={Period.DAILY}>Giornaliero</MenuItem>
                      <MenuItem value={Period.WEEKLY}>Settimanale</MenuItem>
                      <MenuItem value={Period.MONTHLY}>Mensile</MenuItem>
                      <MenuItem value={Period.YEARLY}>Annuale</MenuItem>
                    </Select>
                  </FormControl>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label={getTargetValueLabel()}
                    placeholder={getTargetValuePlaceholder()}
                    name="targetValue"
                    value={formData.targetValue}
                    onChange={handleFormChange}
                    type="number"
                    InputProps={{inputProps: {min: 0}}}
                  />
                </Grid>
              </Grid>
            </DialogContent>
            <DialogActions>
              <Button onClick={handleCloseForm} color="primary">
                Annulla
              </Button>
              <Button
                color="primary"
                variant="contained"
                onClick={handleSubmitGoal}
                disabled={loading || !formData.targetValue}
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
        </Paper>
      </Container>
    </Box>
  );
}

export default Goals;
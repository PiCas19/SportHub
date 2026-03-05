import {
  Container,
  Typography,
  Box,
  Tabs,
  Tab,
  Grid,
  Card,
  CardContent,
  CardMedia,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Chip,
  Avatar,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  DialogActions,
  CircularProgress, Fab,
} from '@mui/material';
import EmojiEventsIcon from '@mui/icons-material/EmojiEvents';
import PersonIcon from '@mui/icons-material/Person';
import GroupIcon from '@mui/icons-material/Group';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import CancelIcon from '@mui/icons-material/Cancel';
import {useEffect, useState} from "react";
import HeaderScreen from "../../components/HeaderScreen.tsx";
import {
  Add as AddIcon,
  BarChart as StatsIcon,
  DirectionsBike as BikeIcon,
  DirectionsRun as RunIcon,
  Hiking as HikingIcon,
  Pool as SwimIcon
} from "@mui/icons-material";
import axiosInstance from "../api/axiosConfig.ts";

// Interfaces for challenge, leaderboard, and chat
interface ApiChallenge {
  id: number;
  name: string;
  maxParticipants: number;
  currentParticipants: number;
  goalType: string;
  sportType: any;
  deadline: string;
  endDate: string;
  started: boolean;
  finished: boolean;
  chatId: string;
  userRegistered: boolean;
}

interface Leaderboard {
  score: number;
  sporthubUsername: string;
  telegramUsername: string;
  rank?: number;
  isCurrentUser?: boolean;
}

interface Chat {
  id: string;
  title: string;
  chatId: string;
}

function Challenges() {
  // State for challenges, filters, and dialogs
  const [filtered, setFiltered] = useState<ApiChallenge[]>([]);
  const [tabValue, setTabValue] = useState(0);
  const [selectedChallenge, setSelectedChallenge] = useState<ApiChallenge | null>(null);
  const [leaderboardOpen, setLeaderboardOpen] = useState(false);
  const [challenges, setChallenges] = useState<ApiChallenge[]>([]);
  const [leaderboard, setLeaderboard] = useState<Leaderboard[]>([]);
  const [loading, setLoading] = useState(false);
  const [chats, setChats] = useState<Chat[]>([]);
  const [isFormOpen, setIsFormOpen] = useState<boolean>(false);
  const initialChallenge = {
    name: "",
    targetValue: "",
    goalType: "",
    sportType: "",
    maxParticipants: "",
    deadline: "",
    endDate: "",
    chatId: ""
  };
  const [newChallenge, setNewChallenge] = useState(initialChallenge);

  // Fetch chats and challenges on mount
  useEffect(() => {
    setLoading(true);

    axiosInstance.get('api/telegram/chats')
      .then((response: any) => {
        const mockGroups = response.data.chats.map((chat: any,) => ({
          id: chat.id,
          title: chat.title,
          chatId: chat.chatId
        }));
        setChats(mockGroups)
      });

    axiosInstance.get('api/competitions')
      .then((response: any) => {
        const challengeStat = response.data.map((challenge: any) => {
          return {
            id: challenge.competitionId,
            name: challenge.name,
            maxParticipants: challenge.maxParticipants,
            currentParticipants: challenge.currentParticipants,
            goalType: challenge.goalType,
            sportType: challenge.sportType,
            deadline: challenge.deadline,
            endDate: challenge.endDate,
            started: challenge.started,
            finished: challenge.finished,
            chatId: challenge.chatId,
            userRegistered: challenge.userRegistered,
          };
        })
        setLoading(false);
        setChallenges(challengeStat);
      }).catch(() => {
      setLoading(false);
    })
  }, []);

  // Filter challenges based on tab selection
  useEffect(() => {
    if (challenges) {
      getFilteredChallenges()
    }
  }, [challenges, tabValue]);

  // Handle form input changes
  const handleFormChange = (e: any) => {
    const {name, value} = e.target;
    setNewChallenge({
      ...newChallenge,
      [name]: value
    });
  };

  const handleTabChange = (_event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
  };

  const handleCloseLeaderboard = () => {
    setLeaderboardOpen(false);
  };

  const getCategoryIcon = (category: string) => {
    switch (category) {
      case 'RUN':
        return <RunIcon sx={{color: 'white !important'}}/>;
      case 'RIDE':
        return <BikeIcon sx={{color: 'white !important'}}/>;
      case 'SWIM':
        return <SwimIcon sx={{color: 'white !important'}}/>;
      case 'WALK':
        return <HikingIcon sx={{color: 'white !important'}}/>;
      default:
        return <StatsIcon sx={{color: 'white !important'}}/>;
    }
  };

  const formatActivityType = (type: string) => {
    switch (type) {
      case 'RUN':
        return 'Corsa';
      case 'RIDE':
        return 'Bici';
      case 'SWIM':
        return 'Nuoto';
      case 'WALK':
        return 'Camminata';
      default:
        return 'Altro';
    }
  };

  const getActivityColor = (type: string) => {
    switch (type) {
      case 'RUN':
        return '#FF5722';
      case 'RIDE':
        return '#2196F3';
      case 'SWIM':
        return '#00BCD4';
      case 'WALK':
        return '#4CAF50';
      default:
        return '#607D8B';
    }
  };

  const formatGoalType = (type: string) => {
    switch (type) {
      case 'DISTANCE':
        return 'Distanza';
      case 'DURATION':
        return 'Tempo';
      case 'ACTIVITIES':
        return 'Attività';
      default:
        return type;
    }
  };

  const handleSubmitActivity = () => {
    setLoading(true);

    const challengeData = {
      name: newChallenge.name,
      targetValue: newChallenge.targetValue,
      goalType: newChallenge.goalType,
      sportType: newChallenge.sportType,
      maxParticipants: newChallenge.maxParticipants,
      deadline: newChallenge.deadline,
      endDate: newChallenge.endDate,
      chatId: newChallenge.chatId
    };

    axiosInstance.post('api/competitions', challengeData)
      .then((response: any) => {
        setChallenges(prevChallenges => [...prevChallenges, response.data]);
        setLoading(false);
        handleCloseForm();
        setNewChallenge(initialChallenge);
      })
      .catch(error => {
        console.error("Errore durante l'aggiunta della challenge:", error);
        setLoading(false);
      });
  };

  const getFilteredChallenges = () => {
    if (tabValue === 0) {
      setFiltered(challenges)
    } else if (tabValue === 1) {
      setFiltered(challenges.filter(challenge => challenge.userRegistered))
    } else {
      setFiltered(challenges.filter(challenge => !challenge.userRegistered))
    }
  };

  const handleAddActivity = () => {
    setIsFormOpen(true);
  };

  const handleCloseForm = () => {
    setIsFormOpen(false);
  };

  const handleJoinChallenge = (challengeApi: ApiChallenge) => {
    setLoading(true);
    axiosInstance.post(`api/competitions/${challengeApi.id}/join`, {})
      .then(() => {
        setChallenges(challenges.map(challenge =>
          challenge.id === challengeApi.id
            ? {...challenge, userRegistered: true, currentParticipants: challenge.currentParticipants + 1}
            : challenge
        ));

        if (selectedChallenge && selectedChallenge.id === challengeApi.id) {
          setSelectedChallenge({
            ...selectedChallenge,
            userRegistered: true,
            currentParticipants: selectedChallenge.currentParticipants + 1
          });

          loadLeaderboard(challengeApi.id);
        } else {
          setSelectedChallenge({
            ...challengeApi,
            userRegistered: true,
            currentParticipants: challengeApi.currentParticipants + 1
          });
          loadLeaderboard(challengeApi.id);
        }

        setLoading(false);
      })
      .catch(error => {
        console.error("Errore durante la partecipazione alla challenge:", error);
        setLoading(false);
      });
  };

  // Leave a challenge
  const handleLeaveChallenge = (challengeId: number) => {
    setLoading(true);

    axiosInstance.post(`api/competitions/${challengeId}/leave`, {})
      .then(() => {
        setChallenges(challenges.map(challenge =>
          challenge.id === challengeId
            ? {...challenge, userRegistered: false, currentParticipants: challenge.currentParticipants - 1}
            : challenge
        ));

        if (selectedChallenge && selectedChallenge.id === challengeId) {
          setSelectedChallenge({
            ...selectedChallenge,
            userRegistered: false,
            currentParticipants: selectedChallenge.currentParticipants - 1
          });

          loadLeaderboard(challengeId);
        }

        setLoading(false);
      })
      .catch(error => {
        console.error("Errore durante l'abbandono della challenge:", error);
        setLoading(false);
      });
  };

  // Load leaderboard for a challenge
  const loadLeaderboard = (challengeId: number) => {
    setLoading(true);

    axiosInstance.get(`api/competitions/${challengeId}/leaderboard`)
      .then((response: any) => {
        const leaderboardData: Leaderboard[] = response.data.map((item: any, index: number) => ({
          score: item.score,
          sporthubUsername: item.sporthubUsername || 'User',
          telegramUsername: item.telegramUsername || '',
          rank: index + 1,
          isCurrentUser: item.sporthubUsername,
        }));

        setLeaderboard(leaderboardData);
        setLoading(false);
      })
      .catch(error => {
        console.error("Error fetching leaderboard:", error);
        setLeaderboard([]);
        setLoading(false);
      });
  };

  // Handle challenge card click
  const handleChallengeClick = (challenge: ApiChallenge) => {
    loadLeaderboard(challenge.id);
    setSelectedChallenge(challenge);
    setLeaderboardOpen(true);
  };


  return (
    <Box sx={{flexGrow: 1}}>
      <HeaderScreen title="Competizione"/>

      <Container maxWidth="lg" sx={{py: 4}}>
        <Box sx={{borderBottom: 1, borderColor: 'divider', mb: 4}}>
          <Tabs
            value={tabValue}
            onChange={handleTabChange}
            centered
            variant="fullWidth"
            sx={{mb: 2}}
          >
            <Tab label="Tutte le Challenge"/>
            <Tab
              label="Le Mie Challenge"/>
            <Tab
              label="Challenge Disponibili"/>
          </Tabs>
        </Box>

        {/* Challenge Cards */}
        <Grid container spacing={3}>
          {filtered.map((challenge, index) => (
            <Grid item xs={12} sm={6} md={4} key={index}>
              <Card
                sx={{
                  height: '100%',
                  display: 'flex',
                  flexDirection: 'column',
                  transition: 'transform 0.2s, box-shadow 0.2s',
                  '&:hover': {
                    transform: 'translateY(-5px)',
                    boxShadow: 6,
                    cursor: 'pointer'
                  }
                }}
                onClick={() => handleChallengeClick(challenge)}
              >
                <CardMedia
                  component="div"
                  sx={{
                    height: 140,
                    position: 'relative',
                    backgroundPosition: 'center 15%',
                  }}
                  image="/logo.jpeg"
                >
                  <Box
                    sx={{
                      position: 'absolute',
                      bottom: 0,
                      right: 0,
                      bgcolor: 'rgba(0,0,0,0.6)',
                      color: 'white',
                      p: 1,
                      borderTopLeftRadius: 4
                    }}
                  >
                    <Box sx={{display: 'flex', alignItems: 'center'}}>
                      <GroupIcon fontSize="small" sx={{mr: 0.5}}/>
                      <Typography
                        variant="body2">{challenge.currentParticipants}/{challenge.maxParticipants}</Typography>
                    </Box>
                  </Box>

                  <Box
                    sx={{
                      position: 'absolute',
                      top: 10,
                      left: 10,
                      borderRadius: 1
                    }}
                  >
                    <Chip
                      icon={getCategoryIcon(challenge.sportType)}
                      label={formatActivityType(challenge.sportType)}
                      sx={{color: "white", backgroundColor: getActivityColor(challenge.sportType)}}
                    />
                  </Box>

                  {challenge.userRegistered && (
                    <Box
                      sx={{
                        position: 'absolute',
                        top: 10,
                        right: 10,
                        borderRadius: 1
                      }}
                    >
                      <Chip
                        icon={<CheckCircleIcon/>}
                        label="Partecipante"
                        color="success"
                        size="small"
                      />
                    </Box>
                  )}

                  {challenge.finished && (
                    <Box
                      sx={{
                        position: 'absolute',
                        bottom: 0,
                        left: 0,
                        bgcolor: 'rgba(0,0,0,0.7)',
                        color: 'white',
                        p: 1,
                        borderTopRightRadius: 4
                      }}
                    >
                      <Typography variant="caption" sx={{fontWeight: 'bold'}}>COMPLETATA</Typography>
                    </Box>
                  )}

                  {challenge.started && !challenge.finished && (
                    <Box
                      sx={{
                        position: 'absolute',
                        bottom: 0,
                        left: 0,
                        bgcolor: 'rgba(76,175,80,0.7)',
                        color: 'white',
                        p: 1,
                        borderTopRightRadius: 4
                      }}
                    >
                      <Typography variant="caption" sx={{fontWeight: 'bold'}}>IN CORSO</Typography>
                    </Box>
                  )}

                  {!challenge.started && !challenge.finished && (
                    <Box
                      sx={{
                        position: 'absolute',
                        bottom: 0,
                        left: 0,
                        bgcolor: 'rgba(255,152,0,0.7)',
                        color: 'white',
                        p: 1,
                        borderTopRightRadius: 4
                      }}
                    >
                      <Typography variant="caption" sx={{fontWeight: 'bold'}}>IN ARRIVO</Typography>
                    </Box>
                  )}
                </CardMedia>

                <CardContent sx={{flexGrow: 1}}>
                  <Typography gutterBottom variant="h5" component="div" sx={{fontWeight: 'bold'}}>
                    {challenge.name}
                  </Typography>
                  <Typography variant="body2" color="text.secondary" sx={{mb: 2}}>
                    Tipo di challenge: {formatGoalType(challenge.goalType)}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Scadenza iscrizione: {challenge.deadline}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Fine challenge: {challenge.endDate}
                  </Typography>
                </CardContent>

                <Box sx={{p: 2, pt: 0, display: 'flex', justifyContent: 'center'}}>
                  <Button
                    variant="contained"
                    color={"secondary"}
                    startIcon={<EmojiEventsIcon/>}
                    fullWidth
                  >
                    Vedi classifica
                  </Button>
                </Box>
              </Card>
            </Grid>
          ))}
        </Grid>

        <Dialog
          open={leaderboardOpen}
          onClose={handleCloseLeaderboard}
          maxWidth="md"
          fullWidth
        >
          {selectedChallenge && (
            <>
              <DialogTitle>
                <Typography variant="h5" component="div" sx={{fontWeight: 'bold'}}>
                  {selectedChallenge.name} - Classifica
                </Typography>
              </DialogTitle>
              <DialogContent sx={{pt: 3}}>
                {loading ? (
                  <Box sx={{display: 'flex', justifyContent: 'center', my: 4}}>
                    <CircularProgress/>
                  </Box>
                ) : leaderboard.length > 0 ? (
                  <TableContainer component={Paper} sx={{mb: 2}}>
                    <Table>
                      <TableHead>
                        <TableRow sx={{bgcolor: 'grey.100'}}>
                          <TableCell align="center" width="10%"><Typography
                            variant="subtitle2">Posizione</Typography></TableCell>
                          <TableCell width="60%"><Typography variant="subtitle2">Partecipante</Typography></TableCell>
                          <TableCell align="right" width="30%"><Typography
                            variant="subtitle2">Punteggio</Typography></TableCell>
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {leaderboard.map((entry, index) => (
                          <TableRow
                            key={index}
                            sx={{
                              bgcolor: entry.isCurrentUser ? 'rgba(25, 118, 210, 0.1)' : 'transparent',
                              '&:hover': {bgcolor: 'rgba(0, 0, 0, 0.04)'}
                            }}
                          >
                            <TableCell align="center">
                              {entry.rank && entry.rank <= 3 ? (
                                <Chip
                                  label={entry.rank}
                                  color={entry.rank === 1 ? "warning" : entry.rank === 2 ? "secondary" : "default"}
                                  size="small"
                                  sx={{
                                    fontWeight: 'bold',
                                    bgcolor: entry.rank === 1 ? 'gold' : entry.rank === 2 ? 'silver' : '#cd7f32',
                                    color: 'white'
                                  }}
                                />
                              ) : (
                                entry.rank
                              )}
                            </TableCell>
                            <TableCell>
                              <Box sx={{display: 'flex', alignItems: 'center'}}>
                                <Avatar
                                  sx={{
                                    mr: 2,
                                    bgcolor: entry.isCurrentUser ? 'primary.main' : 'grey.400',
                                    width: 32,
                                    height: 32
                                  }}
                                >
                                  {(entry.sporthubUsername ? entry.sporthubUsername[0].toUpperCase() : 'U')}
                                </Avatar>
                                <Typography variant={entry.isCurrentUser ? "subtitle1" : "body1"}
                                            sx={{fontWeight: entry.isCurrentUser ? 'bold' : 'regular'}}>
                                  {entry.sporthubUsername || entry.telegramUsername || 'User'}
                                  {entry.isCurrentUser &&
                                      <Chip size="small" color="primary" label="Tu" sx={{ml: 1}}/>}
                                </Typography>
                              </Box>
                            </TableCell>
                            <TableCell align="right">
                              <Typography variant="body1" sx={{fontWeight: 'medium'}}>
                                {typeof entry.score === 'number' && !Number.isInteger(entry.score)
                                  ? entry.score.toFixed(1)
                                  : entry.score} pts
                              </Typography>
                            </TableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  </TableContainer>
                ) : (
                  <Box sx={{textAlign: 'center', my: 4}}>
                    <Typography variant="body1">
                      Nessun partecipante ha ancora registrato attività per questa challenge.
                    </Typography>
                  </Box>
                )}

                <Box sx={{display: 'flex', justifyContent: 'space-between', mt: 3, mb: 2}}>
                  {selectedChallenge.userRegistered ? (
                    <Button
                      variant="contained"
                      color="error"
                      startIcon={<CancelIcon/>}
                      onClick={() => handleLeaveChallenge(selectedChallenge.id)}
                      disabled={selectedChallenge.finished}
                    >
                      Abbandona Challenge
                    </Button>
                  ) : (
                    <Button
                      variant="contained"
                      color="primary"
                      startIcon={<PersonIcon/>}
                      onClick={() => handleJoinChallenge(selectedChallenge)}
                      disabled={selectedChallenge.started || selectedChallenge.currentParticipants >= selectedChallenge.maxParticipants}
                    >
                      Partecipa alla Challenge
                    </Button>
                  )}

                  <Button onClick={handleCloseLeaderboard} variant="outlined">
                    Chiudi
                  </Button>
                </Box>
              </DialogContent>
            </>
          )}
        </Dialog>

        {/* Add Challenge Dialog */}
        <Dialog
          open={isFormOpen}
          onClose={handleCloseForm}
          fullWidth
          maxWidth="md"
        >
          <DialogTitle>Aggiungi Nuova Challenge</DialogTitle>
          <DialogContent>
            <Box component="form" sx={{mt: 2}}>
              <Grid container spacing={2}>
                <Grid item xs={12}>
                  <TextField
                    fullWidth
                    label="Nome Challenge"
                    name="name"
                    value={newChallenge.name}
                    onChange={handleFormChange}
                    required
                  />
                </Grid>

                <Grid item xs={12} sm={6}>
                  <FormControl fullWidth>
                    <InputLabel>Tipo di Obiettivo</InputLabel>
                    <Select
                      name="goalType"
                      value={newChallenge.goalType}
                      onChange={handleFormChange}
                      label="Tipo di Obiettivo"
                    >
                      <MenuItem value="DISTANCE">Distanza</MenuItem>
                      <MenuItem value="DURATION">Tempo</MenuItem>
                      <MenuItem value="ACTIVITIES">Attivita</MenuItem>
                    </Select>
                  </FormControl>
                </Grid>

                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="Valore Target"
                    name="targetValue"
                    type="number"
                    value={newChallenge.targetValue}
                    onChange={handleFormChange}
                    required
                  />
                </Grid>

                <Grid item xs={12} sm={6}>
                  <FormControl fullWidth>
                    <InputLabel>Tipo di Sport</InputLabel>
                    <Select
                      name="sportType"
                      value={newChallenge.sportType}
                      onChange={handleFormChange}
                      label="Tipo di Sport"
                    >
                      <MenuItem value="RUN">Corsa</MenuItem>
                      <MenuItem value="RIDE">Bicicletta</MenuItem>
                      <MenuItem value="SWIM">Nuoto</MenuItem>
                      <MenuItem value="WALK">Camminata</MenuItem>
                    </Select>
                  </FormControl>
                </Grid>

                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="Numero Massimo Partecipanti"
                    name="maxParticipants"
                    type="number"
                    value={newChallenge.maxParticipants}
                    onChange={handleFormChange}
                    required
                  />
                </Grid>

                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="Scadenza Iscrizione"
                    name="deadline"
                    type="date"
                    value={newChallenge.deadline}
                    onChange={handleFormChange}
                    InputLabelProps={{
                      shrink: true,
                    }}
                    inputProps={{
                      min: new Date().toISOString().split("T")[0],
                    }}
                    required
                  />
                </Grid>

                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="Data Fine Challenge"
                    name="endDate"
                    type="date"
                    value={newChallenge.endDate}
                    onChange={handleFormChange}
                    InputLabelProps={{
                      shrink: true,
                    }}
                    inputProps={{
                      min: newChallenge.deadline || new Date().toISOString().split("T")[0],
                    }}
                    required
                  />
                </Grid>

                <Grid item xs={12}>
                  <FormControl fullWidth required>
                    <InputLabel id="chat-select-label">Chat</InputLabel>
                    <Select
                      labelId="chat-select-label"
                      name="chatId"
                      value={newChallenge.chatId}
                      onChange={handleFormChange}
                      label="Chat"
                    >
                      {chats.map(chat => (
                        <MenuItem key={chat.id} value={chat.chatId}>
                          {chat.title}
                        </MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                </Grid>
              </Grid>
            </Box>
          </DialogContent>
          <DialogActions>
            <Button onClick={handleCloseForm} color="primary">
              Annulla
            </Button>
            <Button onClick={handleSubmitActivity} color="primary" variant="contained">
              {loading ? <CircularProgress size={24}/> : 'Salva'}
            </Button>
          </DialogActions>
        </Dialog>

        {/* Add Challenge Button */}
        <Fab
          color="primary"
          sx={{
            position: 'fixed',
            bottom: 24,
            right: 24,
          }}
          onClick={handleAddActivity}
        >
          {loading ? <CircularProgress size={24} color="inherit"/> : <AddIcon/>}
        </Fab>
      </Container>
    </Box>
  );
}

export default Challenges;
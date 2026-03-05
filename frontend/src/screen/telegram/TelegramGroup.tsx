// Import MUI components and icons
import {
  Box, Button, Typography, Paper, List, ListItem, ListItemText, ListItemSecondaryAction,
  IconButton, Dialog, DialogTitle, DialogContent, DialogContentText, DialogActions,
  CircularProgress, Divider, Fab, TextField, Avatar, ListItemAvatar, Grid
} from '@mui/material';
import {Add as AddIcon, Delete, Close as CloseIcon} from '@mui/icons-material';
import {useEffect, useState} from "react";
import HeaderScreen from "../../components/HeaderScreen.tsx";
import CustomSnackbar from "../../components/CustomSnackbar.tsx";
import axiosInstance from "../api/axiosConfig.ts";

// Group type definition
type Group = {
  id: string,
  title: string,
  members: number,
  photoSmall: string,
  photoBig: string,
  chatId: string
};

function TelegramGroup() {
  // State variables
  const [loading, setLoading] = useState(false);
  const [groups, setGroups] = useState<Group[]>([]);
  const [selectedGroup, setSelectedGroup] = useState<Group>();
  const [openDialog, setOpenDialog] = useState(false);
  const [isFormOpen, setIsFormOpen] = useState<boolean>(false);
  const [photoDialogOpen, setPhotoDialogOpen] = useState(false);
  const [selectedPhoto, setSelectedPhoto] = useState(null);
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'success',
  });
  const [newGroup, setNewGroup] = useState({chatId: ''});

  // Add new group
  const handleSubmitGroup = () => {
    setLoading(true);
    axiosInstance.post('api/telegram/chats', newGroup)
      .then(async () => {
        showSnackbar("Gruppo aggiunto con successo", 'success');
        setIsFormOpen(false);
        await getChats();
        setLoading(false);
      })
      .catch(() => {
        showSnackbar("Errore nell'aggiunta del gruppo", 'error');
        setLoading(false);
      });
  };

  // Load groups
  const getChats = async () => {
    axiosInstance.get('api/telegram/chats')
      .then((response: any) => {
        const mockGroups = response.data.chats.map((chat: any) => ({
          id: chat.id,
          title: chat.title,
          members: chat.memberCount,
          photoSmall: chat.photoSmall,
          photoBig: chat.photoBig,
          chatId: chat.chatId
        }));
        setGroups(mockGroups)
      }).catch((error: any) => {
      showSnackbar('Errore nel caricamento dei gruppi: ' + error, 'error');
    });
  }

  // Load groups on mount
  useEffect(() => {
    getChats();
  }, []);

  const handleAddGroup = () => {
    setIsFormOpen(true);
  };

  const handleCloseForm = () => {
    setIsFormOpen(false);
    setNewGroup({chatId: ''});
  };

  const handleLeaveGroup = (groupId: any) => {
    axiosInstance.delete(`api/telegram/chats/${groupId}`)
      .then(() => {
        showSnackbar("Eliminazione del gruppo con successo", "success");
        setGroups(groups.filter(group => group?.id !== groupId));
        setOpenDialog(false);
      }).catch((error: any) => {
      showSnackbar('Errore nell\'eliminazione del gruppo: ' + error, 'error');
    });
  };

  // Helpers
  const openConfirmDialog = (group: any) => {
    setSelectedGroup(group);
    setOpenDialog(true);
  };

  const openPhotoDialog = (group: any, event: React.MouseEvent) => {
    event.stopPropagation();
    setSelectedPhoto(group.photoBig);
    setPhotoDialogOpen(true);
  };

  const closePhotoDialog = () => {
    setPhotoDialogOpen(false);
    setSelectedPhoto(null);
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

  const getPhotoUrl = (photoUrl: string | null) => {
    if (!photoUrl) return null;
    return photoUrl;
  };

  // UI rendering
  return (
    <Box>
      <HeaderScreen title="Gestore Gruppi Telegram"/>

      <Box sx={{maxWidth: 800, margin: '0 auto', padding: 3}}>
        <Paper sx={{padding: 3, marginTop: 3}}>
          <Box sx={{display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 2}}>
            <Typography variant="h5">I Tuoi Gruppi Telegram</Typography>
          </Box>
          <Divider sx={{marginBottom: 2}}/>

          {loading && groups.length === 0 ? (
            <Box sx={{display: 'flex', justifyContent: 'center', padding: 4}}>
              <CircularProgress/>
            </Box>
          ) : groups.length === 0 ? (
            <Typography variant="body1" align="center" sx={{padding: 4}}>
              Non sei iscritto a nessun gruppo.
            </Typography>
          ) : (
            <List>
              {groups.map((group: any) => (
                <ListItem key={group.id} divider>
                  <ListItemAvatar>
                    <Avatar
                      src={getPhotoUrl(group.photoSmall)}
                      alt={group.title}
                      onClick={(e: any) => openPhotoDialog(group, e)}
                      sx={{cursor: 'pointer'}}
                    >
                      {group.title.charAt(0)}
                    </Avatar>
                  </ListItemAvatar>
                  <ListItemText
                    primary={group.title}
                    secondary={`${group.members} membri • ID: ${group.chatId}`}
                  />
                  <ListItemSecondaryAction>
                    <IconButton
                      edge="end"
                      aria-label="delete"
                      onClick={() => openConfirmDialog(group)}
                      color="error"
                    >
                      <Delete/>
                    </IconButton>
                  </ListItemSecondaryAction>
                </ListItem>
              ))}
            </List>
          )}
        </Paper>
      </Box>

      {/* Delete Confirmation Dialog */}
      <Dialog
        open={openDialog}
        onClose={() => setOpenDialog(false)}
        // Remove any aria-hidden attributes that might be applied
        container={document.getElementById('root')}
        disablePortal={false}
        keepMounted
      >
        <DialogTitle>Conferma abbandono</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Sei sicuro di voler abbandonare il gruppo "{selectedGroup?.title}"?
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenDialog(false)} disabled={loading}>
            Annulla
          </Button>
          <Button
            onClick={() => handleLeaveGroup(selectedGroup?.id)}
            color="error"
            variant="contained"
            disabled={loading}
            startIcon={loading ? <CircularProgress size={20} color="inherit"/> : <Delete/>}
          >
            {loading ? 'Abbandono in corso...' : 'Abbandona'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Photo Dialog */}
      <Dialog
        open={photoDialogOpen}
        onClose={closePhotoDialog}
        maxWidth="md"
        // Remove any aria-hidden attributes that might be applied
        container={document.getElementById('root')}
        disablePortal={false}
        keepMounted
      >
        <DialogTitle>
          <Box display="flex" justifyContent="space-between" alignItems="center">
            <Typography variant="h6">Foto del gruppo</Typography>
            <IconButton edge="end" color="inherit" onClick={closePhotoDialog} aria-label="chiudi">
              <CloseIcon/>
            </IconButton>
          </Box>
        </DialogTitle>
        <DialogContent>
          <Box
            display="flex"
            justifyContent="center"
            alignItems="center"
            sx={{padding: 2}}
          >
            {selectedPhoto ? (
              <img
                src={getPhotoUrl(selectedPhoto)}
                alt="Foto grande del gruppo"
                style={{
                  maxWidth: '100%',
                  maxHeight: '70vh',
                  borderRadius: '4px'
                }}
              />
            ) : (
              <CircularProgress/>
            )}
          </Box>
        </DialogContent>
      </Dialog>

      {/* Add Group Dialog */}
      <Dialog
        open={isFormOpen}
        onClose={handleCloseForm}
        fullWidth
        maxWidth="md"
        // Remove any aria-hidden attributes that might be applied
        container={document.getElementById('root')}
        disablePortal={false}
        keepMounted
      >
        <DialogTitle>Aggiungi nuovo gruppo Telegram</DialogTitle>
        <DialogContent sx={{display: "flex", flexDirection: "row"}}>
          <Grid container spacing={1} sx={{mt: 1, display: "flex", flexDirection: "column"}}>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Id"
                name="chatId"
                value={newGroup.chatId}
                onChange={(e) => setNewGroup({chatId: e.target.value})}
                autoFocus
                required
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseForm} color="primary">
            Annulla
          </Button>
          <Button
            onClick={handleSubmitGroup}
            color="primary"
            variant="contained"
            disabled={loading || !newGroup.chatId.trim()}
          >
            {loading ? <CircularProgress size={24}/> : 'Salva'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Add button - fixed position */}
      <Fab
        color="primary"
        aria-label="aggiungi gruppo"
        sx={{
          position: 'fixed',
          bottom: 24,
          right: 24,
        }}
        onClick={handleAddGroup}
        disabled={loading}
        // Ensure this button is never hidden from screen readers
        tabIndex={0}
      >
        {loading ? <CircularProgress size={24} color="inherit"/> : <AddIcon/>}
      </Fab>

      {/* Snackbar for feedback */}
      <CustomSnackbar
        open={snackbar.open}
        message={snackbar.message}
        severity={snackbar.severity}
        onClose={handleCloseSnackbar}
      />
    </Box>
  );
}

export default TelegramGroup;

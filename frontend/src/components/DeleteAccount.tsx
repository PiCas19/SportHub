import {useState} from 'react';
import Cookies from 'js-cookie';
import {
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Divider,
  TextField,
  Typography
} from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import {Navigate} from 'react-router-dom';

interface DeleteAccountProps {
  onSuccess?: () => void;
  onError?: (error: any) => void;
  showSnackbar?: (message: string, severity: 'success' | 'error' | 'info' | 'warning') => void;
}

// DeleteAccount component for handling account deletion
const DeleteAccount = ({onSuccess, showSnackbar}: DeleteAccountProps) => {
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [deleteConfirmation, setDeleteConfirmation] = useState('');
  const [redirect, setRedirect] = useState(false);

  const handleOpenDeleteDialog = () => {
    setDeleteDialogOpen(true);
  };

  // Close confirmation dialog and reset input
  const handleCloseDeleteDialog = () => {
    setDeleteDialogOpen(false);
    setDeleteConfirmation('');
  };

  // Update confirmation input
  const handleDeleteConfirmationChange = (e: any) => {
    setDeleteConfirmation(e.target.value);
  };

  // Handle account deletion
  const deleteAccount = () => {
    if (deleteConfirmation !== 'ELIMINA') {
      showSnackbar?.('Inserisci "ELIMINA" per confermare l\'eliminazione dell\'account', 'error');
      return;
    }

    Cookies.remove('auth_token');

    if (onSuccess) {
      onSuccess();
    } else {
      setRedirect(true);
    }
  };

  // Redirect to home after deletion
  if (redirect) {
    return <Navigate to="/" replace/>;
  }

  return (
    <>
      {/* Delete account section */}
      <Box>
        <Typography variant="h6" component="h2" gutterBottom sx={{display: 'flex', alignItems: 'center'}}>
          <DeleteIcon sx={{mr: 1, color: 'error.main'}}/> Elimina Account
        </Typography>
        <Divider sx={{mb: 3}}/>

        <Box sx={{display: 'flex', flexDirection: 'column', alignItems: 'center'}}>
          <Typography variant="body1" color="textSecondary" gutterBottom>
            Attenzione: L'eliminazione dell'account è un'operazione irreversibile. Tutti i tuoi dati verranno cancellati
            permanentemente.
          </Typography>
          <Button
            variant="contained"
            color="error"
            startIcon={<DeleteIcon/>}
            onClick={handleOpenDeleteDialog}
            sx={{mt: 2}}
          >
            Elimina il mio account
          </Button>
        </Box>
      </Box>

      {/* Confirmation dialog */}
      <Dialog
        open={deleteDialogOpen}
        onClose={handleCloseDeleteDialog}
      >
        <DialogTitle>Conferma eliminazione account</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Stai per eliminare definitivamente il tuo account. Questa operazione non può essere annullata e tutti i tuoi
            dati verranno persi.
          </DialogContentText>
          <DialogContentText sx={{mt: 2, mb: 1, fontWeight: 'bold'}}>
            Digita "ELIMINA" per confermare:
          </DialogContentText>
          <TextField
            autoFocus
            fullWidth
            value={deleteConfirmation}
            onChange={handleDeleteConfirmationChange}
            margin="dense"
            placeholder="ELIMINA"
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDeleteDialog} color="primary">
            Annulla
          </Button>
          <Button
            onClick={deleteAccount}
            color="error"
            disabled={deleteConfirmation !== 'ELIMINA'}
          >
            Elimina definitivamente
          </Button>
        </DialogActions>
      </Dialog>
    </>
  );
};

export default DeleteAccount;
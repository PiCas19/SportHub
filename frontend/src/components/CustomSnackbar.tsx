import React from 'react';
import {Snackbar, Alert} from '@mui/material';

interface SnackbarProps {
  open: boolean;
  message: string;
  severity: any;
  onClose: () => void;
}

const CustomSnackbar: React.FC<SnackbarProps> = ({open, message, severity, onClose}) => {
  return (
    <Snackbar
      open={open}
      autoHideDuration={6000}
      onClose={onClose}
      anchorOrigin={{vertical: 'bottom', horizontal: 'center'}}
    >
      <Alert onClose={onClose} severity={severity} sx={{width: '100%'}}>
        {message}
      </Alert>
    </Snackbar>
  );
};

export default CustomSnackbar;

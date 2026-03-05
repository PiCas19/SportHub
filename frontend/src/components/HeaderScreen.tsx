import {AppBar, Toolbar, Typography} from '@mui/material';

const HeaderScreen = ({title}: any) => {
  return (
    <AppBar position="static">
      <Toolbar>
        <Typography variant="h6" component="div" sx={{display:"flex",flexGrow: 1, justifyContent:"center"}}>
          {title}
        </Typography>
      </Toolbar>
    </AppBar>
  );
};

export default HeaderScreen;
import {Drawer, List, ListItemButton, ListItemIcon, ListItemText, Typography, Box} from "@mui/material";
import {Link} from "react-router-dom";
import {Home, Person, Settings} from "@mui/icons-material";
import GroupsIcon from '@mui/icons-material/Groups';
import SportsScoreIcon from '@mui/icons-material/SportsScore';
import EmojiEventsIcon from '@mui/icons-material/EmojiEvents';

// Sidebar component for navigation
function Sidebar({open}: { open: boolean }) {
  return (
    <Drawer
      variant="permanent"
      sx={{
        display: open ? "block" : "none",
        width: 240,
        flexShrink: 0,
        "& .MuiDrawer-paper": {
          width: 240,
          backgroundColor: "#23272e",
          color: "white",
          transition: "width 0.3s",
        },
      }}
    >
      <Box sx={{display: "flex", alignItems: "center", padding: "16px", justifyContent: "center"}}>
        <Typography component={Link} to="/home" variant="h4"
                    sx={{color: "#fdd835", fontWeight: "bold"}}>SportHub</Typography>
      </Box>

      {/* Navigation links */}
      <List>
        <ListItemButton component={Link} to="/home" sx={{color: "white", "&:hover": {backgroundColor: "#3a3f4b"}}}>
          <ListItemIcon sx={{color: "white"}}>
            <Home/>
          </ListItemIcon>
          <ListItemText primary="Home"/>
        </ListItemButton>

        <ListItemButton component={Link} to="/activities"
                        sx={{color: "white", "&:hover": {backgroundColor: "#3a3f4b"}}}>
          <ListItemIcon sx={{color: "white"}}>
            <Person/>
          </ListItemIcon>
          <ListItemText primary="Activities"/>
        </ListItemButton>

        <ListItemButton component={Link} to="/goals"
                        sx={{color: "white", "&:hover": {backgroundColor: "#3a3f4b"}}}>
          <ListItemIcon sx={{color: "white"}}>
            <SportsScoreIcon/>
          </ListItemIcon>
          <ListItemText primary="Goals"/>
        </ListItemButton>

        <ListItemButton component={Link} to="/telegramGroup"
                        sx={{color: "white", "&:hover": {backgroundColor: "#3a3f4b"}}}>
          <ListItemIcon sx={{color: "white"}}>
            <GroupsIcon/>
          </ListItemIcon>
          <ListItemText primary="Telegram"/>
        </ListItemButton>

        <ListItemButton component={Link} to="/challenges"
                        sx={{color: "white", "&:hover": {backgroundColor: "#3a3f4b"}}}>
          <ListItemIcon sx={{color: "white"}}>
            <EmojiEventsIcon/>
          </ListItemIcon>
          <ListItemText primary="Challenges"/>
        </ListItemButton>

        <ListItemButton component={Link} to="/settings"
                        sx={{color: "white", "&:hover": {backgroundColor: "#3a3f4b"}}}>
          <ListItemIcon sx={{color: "white"}}>
            <Settings/>
          </ListItemIcon>
          <ListItemText primary="Impostazioni"/>
        </ListItemButton>
      </List>
    </Drawer>
  );
}

export default Sidebar;
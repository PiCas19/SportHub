import {AppBar, Toolbar, IconButton, Menu, MenuItem} from "@mui/material";
import MenuIcon from "@mui/icons-material/Menu";
import {AccountCircle} from "@mui/icons-material";
import React from "react";
import {useNavigate} from "react-router";
import Cookies from "js-cookie";

// Header component for top navigation bar
function Header({toggleSidebar}: { toggleSidebar: () => void }) {
  const navigate = useNavigate();
  const [auth] = React.useState(true);
  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);

  // Open user menu
  const handleMenu = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  // Handle logout by removing auth token and redirecting
  function logout() {
    Cookies.remove('auth_token');
    navigate('', {replace: true});
  }

  return (
    <AppBar position="fixed" sx={{backgroundColor: "#23272e", top: 0}}>
      <Toolbar sx={{display: "flex", justifyContent: "space-between"}}>
        <IconButton edge="start" color="inherit" onClick={toggleSidebar}>
          <MenuIcon/>
        </IconButton>
        {auth && (
          <div>
            <IconButton
              size="large"
              aria-label="account of current user"
              aria-controls="menu-appbar"
              aria-haspopup="true"
              onClick={handleMenu}
              color="inherit"
            >
              <AccountCircle/>
            </IconButton>
            <Menu
              id="menu-appbar"
              anchorEl={anchorEl}
              anchorOrigin={{
                vertical: 'top',
                horizontal: 'right',
              }}
              keepMounted
              transformOrigin={{
                vertical: 'top',
                horizontal: 'right',
              }}
              open={Boolean(anchorEl)}
            >
              <MenuItem onClick={logout}>Logout</MenuItem>
            </Menu>
          </div>
        )}
      </Toolbar>
    </AppBar>
  );
}

export default Header;

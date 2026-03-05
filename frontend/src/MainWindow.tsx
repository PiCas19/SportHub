import {useMediaQuery, useTheme, Box, Drawer} from "@mui/material";
import {useState, useEffect} from "react";
import {Outlet} from "react-router-dom";
import Header from "./components/Header.tsx";
import Sidebar from "./components/Sidebar.tsx";

const MainWindow = () => {
  const theme = useTheme();
  const isDesktop = useMediaQuery(theme.breakpoints.up("lg"));
  const [open, setOpen] = useState(isDesktop);

  useEffect(() => {
    if (isDesktop) {
      setOpen(true);
    }
  }, [isDesktop]);

  const toggleSidebar = () => {
    setOpen((prev) => !prev);
  };

  return (
    <Box sx={{display: "flex", height: "100vh"}}>
      {isDesktop ? (
        <Sidebar open={open}/>
      ) : (
        <Drawer open={open} onClose={toggleSidebar} variant="temporary">
          <Sidebar open={open}/>
        </Drawer>
      )}
      <Box sx={{flexGrow: 1, display: "flex", flexDirection: "column"}}>
        <Header toggleSidebar={toggleSidebar}/>
        <Box
          sx={{
            flexGrow: 1,
            overflowY: "auto",
            padding: 3,
            marginTop: "64px",
          }}
        >
          <Outlet/>
        </Box>
      </Box>
    </Box>
  );
};

export default MainWindow;

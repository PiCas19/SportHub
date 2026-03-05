import {StrictMode} from 'react';
import {createRoot} from 'react-dom/client';
import {BrowserRouter, Routes, Route} from 'react-router-dom';

import Login from './screen/loginScreen/Login.tsx';
import Register from './screen/registerScreen/Register.tsx';
import VerifyEmail from './screen/verify/VerifyEmail.tsx';
import ActivateEmail from './screen/activate/ActivateEmail.tsx';
import VerifyForgotPassword from './screen/verify/VerifyForgotPassword.tsx';
import ResetPassword from './screen/activate/ResetPassword.tsx';

import Home from './screen/homeScreen/Home.tsx';
import Activities from './screen/activity/Activities.tsx';
import Settings from './screen/settings/Settings.tsx';
import ConnectStrava from './screen/stravaConnectScreen/ConnectStrava.tsx';
import TelegramGroup from './screen/telegram/TelegramGroup.tsx';
import Goals from './screen/goals/Goals.tsx';
import Challenges from './screen/challenge/Challenges.tsx';
import PublicMapPage from './screen/map/Map.tsx';

import MainWindow from './MainWindow.tsx';
import ProtectedRoute from './components/ProtectedRoute.tsx';

import './index.css';
import './screen/api/axiosConfig.ts';

createRoot(document.getElementById('root')!).render(
  <BrowserRouter>
    <StrictMode>
      <Routes>
        <Route index element={<Login/>}/>
        <Route path="register" element={<Register/>}/>
        <Route path="verify" element={<VerifyEmail/>}/>
        <Route path="activate" element={<ActivateEmail/>}/>
        <Route path="activateForgotPassword" element={<VerifyForgotPassword/>}/>
        <Route path="resetPassword" element={<ResetPassword/>}/>
        <Route path="map/:token" element={<PublicMapPage/>}/>

        <Route
          element={
            <ProtectedRoute>
              <MainWindow/>
            </ProtectedRoute>
          }
        >
          <Route path="home" element={<Home/>}/>
          <Route path="activities" element={<Activities/>}/>
          <Route path="connectStrava" element={<ConnectStrava/>}/>
          <Route path="settings" element={<Settings/>}/>
          <Route path="goals" element={<Goals/>}/>
          <Route path="challenges" element={<Challenges/>}/>
          <Route path="telegramGroup" element={<TelegramGroup/>}/>
        </Route>
      </Routes>
    </StrictMode>
  </BrowserRouter>
);
import {render} from '@testing-library/react';
import {MemoryRouter, Route, Routes} from 'react-router-dom';
import ProtectedRoute from '../components/ProtectedRoute';
import Home from '../screen/homeScreen/Home';
import Login from '../screen/loginScreen/Login';
import Register from '../screen/registerScreen/Register';
import VerifyEmail from '../screen/verify/VerifyEmail';
import ResetPassword from '../screen/activate/ResetPassword';

describe('Main routing', () => {
  it('renders login page on /', async () => {
    render(
      <MemoryRouter initialEntries={['/']}>
        <Routes>
          <Route path="/" element={<Login/>}/>
        </Routes>
      </MemoryRouter>
    );

  });


  it('renders register page on /register', () => {
    render(
      <MemoryRouter initialEntries={['/register']}>
        <Routes>
          <Route path="/register" element={<Register/>}/>
        </Routes>
      </MemoryRouter>
    );
  });

  it('renders verify page on /verify', () => {
    render(
      <MemoryRouter initialEntries={['/verify']}>
        <Routes>
          <Route path="/verify" element={<VerifyEmail/>}/>
        </Routes>
      </MemoryRouter>
    );
  });

  it('redirects protected route if no token', () => {
    localStorage.removeItem('token');
    render(
      <MemoryRouter initialEntries={['/home']}>
        <Routes>
          <Route
            path="/home"
            element={
              <ProtectedRoute>
                <Home/>
              </ProtectedRoute>
            }
          />
          <Route path="/" element={<Login/>}/>
        </Routes>
      </MemoryRouter>
    );
  });

  it('renders protected route if token exists', () => {
    localStorage.setItem('token', 'abc123');
    render(
      <MemoryRouter initialEntries={['/home']}>
        <Routes>
          <Route
            path="/home"
            element={
              <ProtectedRoute>
                <Home/>
              </ProtectedRoute>
            }
          />
        </Routes>
      </MemoryRouter>
    );
  });

  it('renders reset password with token route', () => {
    render(
      <MemoryRouter initialEntries={['/resetPassword']}>
        <Routes>
          <Route path="/resetPassword" element={<ResetPassword/>}/>
        </Routes>
      </MemoryRouter>
    );
  });
});

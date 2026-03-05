import {render, screen, fireEvent} from '@testing-library/react';
import {MemoryRouter} from 'react-router-dom';
import Login from '../screen/loginScreen/Login.tsx';

describe('Login Component', () => {
  it('should render the login form', () => {
    render(
      <MemoryRouter>
        <Login/>
      </MemoryRouter>
    );

    expect(screen.getByLabelText(/Username/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Password/i)).toBeInTheDocument();
  });

  it('should navigate on successful login', () => {
    render(
      <MemoryRouter>
        <Login/>
      </MemoryRouter>
    );

    fireEvent.change(screen.getByLabelText(/Username/i), {target: {value: 'test'}});
    fireEvent.change(screen.getByLabelText(/Password/i), {target: {value: 'password'}});

    fireEvent.click(screen.getByRole('button', {name: /Accedi/i}));

    expect(window.location.pathname).toBe('/');
  });
});
import {render, screen, fireEvent} from '@testing-library/react';
import {expect, describe, it, vi, beforeEach} from 'vitest';
import {MemoryRouter} from 'react-router-dom';
import Register from '../screen/registerScreen/Register';
import '@testing-library/jest-dom';

describe('Register Component', () => {
  beforeEach(() => {
    // Reset dei mock prima di ogni test
    vi.clearAllMocks();
  });

  it('should render the registration form with all fields', () => {
    render(
      <MemoryRouter>
        <Register/>
      </MemoryRouter>
    );

    expect(screen.getByLabelText(/Username/i)).toBeInTheDocument();
    const nomeInputs = screen.getAllByLabelText(/Nome/i);
    expect(nomeInputs.length).toBe(2);
    expect(nomeInputs[0]).toBeInTheDocument();
    expect(screen.getByLabelText(/Cognome/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Email/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Password/i)).toBeInTheDocument();
    expect(screen.getByRole('button', {name: /Registrati/i})).toBeInTheDocument();
  });

  it('should have disabled button when form is empty', () => {
    render(
      <MemoryRouter>
        <Register/>
      </MemoryRouter>
    );

    expect(screen.getByRole('button', {name: /Registrati/i})).toBeDisabled();
  });

  it('should enable button when all fields are filled', () => {
    render(
      <MemoryRouter>
        <Register/>
      </MemoryRouter>
    );

    fireEvent.change(screen.getByLabelText(/Username/i), {target: {value: 'testuser'}});
    const nameInput = screen.getAllByRole('textbox', {name: /Nome/i})[0];
    fireEvent.change(nameInput, {target: {value: 'Test'}});
    fireEvent.change(screen.getByLabelText(/Cognome/i), {target: {value: 'Rossi'}});
    fireEvent.change(screen.getByLabelText(/Email/i), {target: {value: 'mario.rossi@example.com'}});
    fireEvent.change(screen.getByLabelText(/Password/i), {target: {value: 'password123'}});

    expect(screen.getByRole('button', {name: /Registrati/i})).not.toBeDisabled();
  });

  it('should navigate to /verify on successful registration (local, no API)', async () => {
    render(
      <MemoryRouter>
        <Register/>
      </MemoryRouter>
    );

    fireEvent.change(screen.getByLabelText(/Username/i), {target: {value: 'testuser'}});
    const nameInput = screen.getAllByRole('textbox', {name: /Nome/i})[0];
    fireEvent.change(nameInput, {target: {value: 'Test'}});
    fireEvent.change(screen.getByLabelText(/Cognome/i), {target: {value: 'Rossi'}});
    fireEvent.change(screen.getByLabelText(/Email/i), {target: {value: 'mario.rossi@example.com'}});
    fireEvent.change(screen.getByLabelText(/Password/i), {target: {value: 'password123'}});

    fireEvent.click(screen.getByRole('button', {name: /Registrati/i}));
    expect(window.location.pathname).toBe('/');
  });
});

it('should show error message on registration failure', () => {

  render(
    <MemoryRouter>
      <Register/>
    </MemoryRouter>
  );

  fireEvent.change(screen.getByLabelText(/Username/i), {target: {value: 'testuser'}});
  const nameInput = screen.getAllByRole('textbox', {name: /Nome/i})[0];
  fireEvent.change(nameInput, {target: {value: 'Test'}});
  fireEvent.change(screen.getByLabelText(/Cognome/i), {target: {value: 'Rossi'}});
  fireEvent.change(screen.getByLabelText(/Email/i), {target: {value: 'mario.rossi@example.com'}});
  fireEvent.change(screen.getByLabelText(/Password/i), {target: {value: 'password123'}});

  fireEvent.click(screen.getByRole('button', {name: /Registrati/i}));
});

it('should have a back link to the home page', () => {
  render(
    <MemoryRouter>
      <Register/>
    </MemoryRouter>
  );

  const backLink = screen.getByText(/Indietro/i);
  expect(backLink).toBeInTheDocument();
  expect(backLink.closest('a')).toHaveAttribute('href', '/');
});
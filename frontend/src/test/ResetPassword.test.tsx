import {render, screen, waitFor, fireEvent} from '@testing-library/react';
import {vi} from 'vitest';
import {MemoryRouter} from 'react-router-dom';
import ResetPassword from '../screen/activate/ResetPassword';
import axios from 'axios';

vi.mock('axios');
const mockedAxios = axios as any;

describe('ResetPassword component', () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it('renders loading initially and then shows form if token is valid', async () => {
    mockedAxios.get.mockResolvedValue({data: {valid: true}});

    render(
      <MemoryRouter initialEntries={['/reset-password?token=fake-token']}>
        <ResetPassword/>
      </MemoryRouter>
    );

    expect(screen.getByText(/Verifica del token in corso/i)).toBeInTheDocument();

    await waitFor(() =>
      expect(screen.getByText(/Reimposta la tua password/i)).toBeInTheDocument()
    );
  });

  it('shows error if token is invalid', async () => {
    mockedAxios.get.mockRejectedValue(new Error('Invalid token'));

    render(
      <MemoryRouter initialEntries={['/reset-password?token=wrong-token']}>
        <ResetPassword/>
      </MemoryRouter>
    );

    expect(screen.getByText(/Verifica del token in corso/i)).toBeInTheDocument();

    await waitFor(() =>
      expect(screen.getByText(/Errore di Reimpostazione/i)).toBeInTheDocument()
    );
  });

  it('submits new password successfully', async () => {
    mockedAxios.get.mockResolvedValue({data: {valid: true}});
    global.fetch = vi.fn().mockResolvedValue({ok: true}) as any;

    render(
      <MemoryRouter initialEntries={['/reset-password?token=abc123']}>
        <ResetPassword/>
      </MemoryRouter>
    );

    await waitFor(() =>
      expect(screen.getByText(/Reimposta la tua password/i)).toBeInTheDocument()
    );

    fireEvent.change(screen.getByLabelText(/Nuova Password/i), {target: {value: 'pass1234'}});
    fireEvent.change(screen.getByLabelText(/Conferma Password/i), {target: {value: 'pass1234'}});

    fireEvent.click(screen.getByRole('button', {name: /Reimposta Password/i}));

    await waitFor(() =>
      expect(screen.getByText(/Password Reimpostata!/i)).toBeInTheDocument()
    );
  });
  it('toggles password visibility and updates inputs', async () => {
    mockedAxios.get.mockResolvedValue({data: {valid: true}});

    render(
      <MemoryRouter initialEntries={['/reset-password?token=test']}>
        <ResetPassword/>
      </MemoryRouter>
    );

    await waitFor(() =>
      expect(screen.getByText(/Reimposta la tua password/i)).toBeInTheDocument()
    );

    const passwordField = screen.getByLabelText(/Nuova Password/i);
    const confirmPasswordField = screen.getByLabelText(/Conferma Password/i);

    fireEvent.change(passwordField, {target: {value: 'myPass123'}});
    fireEvent.change(confirmPasswordField, {target: {value: 'myPass123'}});

    expect(passwordField).toHaveValue('myPass123');
    expect(confirmPasswordField).toHaveValue('myPass123');

    const toggleButtons = screen.getAllByLabelText('toggle password visibility');
    expect(toggleButtons.length).toBe(2);

    fireEvent.click(toggleButtons[0]);
    fireEvent.click(toggleButtons[1]);
  });

});

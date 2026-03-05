import {render, screen, waitFor} from '@testing-library/react';
import {MemoryRouter} from 'react-router-dom';
import {expect, describe, it, vi, beforeEach} from 'vitest';
import ActivateAccount from '../screen/activate/ActivateEmail.tsx';

describe('ActivateAccount Component', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('should show success message on valid token', async () => {
    vi.stubGlobal('fetch', vi.fn(() =>
      Promise.resolve({status: 204})
    ));

    const token = 'fake-token';
    render(
      <MemoryRouter initialEntries={[`/activate?token=${token}`]}>
        <ActivateAccount/>
      </MemoryRouter>
    );

    expect(screen.getByRole('progressbar')).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.getByText(/Account Attivato/i)).toBeInTheDocument();
      expect(screen.getByText(/Ora puoi accedere/i)).toBeInTheDocument();
    });
  });

  it('should show error message on invalid token', async () => {
    vi.stubGlobal('fetch', vi.fn(() =>
      Promise.resolve({status: 400})
    ));

    const token = 'invalid-token';
    render(
      <MemoryRouter initialEntries={[`/activate?token=${token}`]}>
        <ActivateAccount/>
      </MemoryRouter>
    );

    expect(screen.getByRole('progressbar')).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.getByText(/Errore di Attivazione/i)).toBeInTheDocument();
      expect(screen.getByText(/Qualcosa è andato storto/i)).toBeInTheDocument();
    });
  });

  it('should show error message when token is missing', async () => {
    const fetchSpy = vi.spyOn(global, 'fetch');

    render(
      <MemoryRouter initialEntries={['/activate']}>
        <ActivateAccount/>
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText(/Errore di Attivazione/i)).toBeInTheDocument();
    });

    expect(fetchSpy).not.toHaveBeenCalled();
  });
});

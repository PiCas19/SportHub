import {render, screen, waitFor} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {MemoryRouter} from 'react-router-dom';
import Header from '../components/Header';
import {vi} from 'vitest';
import * as ReactRouter from 'react-router';

// ✅ Mock js-cookie
vi.mock('js-cookie', async () => {
  return {
    default: {
      remove: vi.fn(),
    },
  };
});

vi.mock('react-router', async () => {
  const mod = await vi.importActual<typeof ReactRouter>('react-router');
  return {
    ...mod,
    useNavigate: vi.fn(),
  };
});

const Cookies = await import('js-cookie');
const mockedNavigate = vi.fn();
(ReactRouter.useNavigate as unknown as any).mockReturnValue(mockedNavigate);

describe('Header', () => {
  it('logs out when the logout button is clicked', async () => {
    render(
      <MemoryRouter>
        <Header toggleSidebar={() => {
        }}/>
      </MemoryRouter>
    );

    userEvent.click(screen.getByRole('button', {name: /account of current user/i}));
    userEvent.click(screen.getByText(/logout/i));

    await waitFor(() => {
      expect(Cookies.default.remove).toHaveBeenCalledWith('auth_token');
      expect(mockedNavigate).toHaveBeenCalledWith('', {replace: true});
    });
  });
  it('renders the header with the correct elements', () => {
    render(
      <MemoryRouter>
        <Header toggleSidebar={() => {
        }}/>
      </MemoryRouter>
    );

    const buttons = screen.getAllByRole('button');
    expect(buttons.length).toBeGreaterThanOrEqual(2);
  });

  it('opens the menu when the account button is clicked', async () => {
    render(
      <MemoryRouter>
        <Header toggleSidebar={() => {
        }}/>
      </MemoryRouter>
    );

    // Simula il click sul pulsante account
    userEvent.click(screen.getByRole('button', {name: /account of current user/i}));

    // Verifica che il menu sia stato aperto
    await waitFor(() => expect(screen.getByText(/logout/i)).toBeInTheDocument());
  });
});

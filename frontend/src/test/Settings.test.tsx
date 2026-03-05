import {render, screen, waitFor, fireEvent} from '@testing-library/react';
import Settings from '../screen/settings/Settings';
import {vi} from 'vitest';
import axiosInstance from '../screen/api/axiosConfig';
import '@testing-library/jest-dom';
import {MemoryRouter} from 'react-router-dom';

vi.mock('../screen/api/axiosConfig');

const renderWithRouter = (ui: React.ReactElement) => {
  return render(<MemoryRouter>{ui}</MemoryRouter>);
};

describe('Settings Component', () => {
  const mockUserData = {
    name: 'Test User',
    email: 'test@example.com',
    height: 170,
    weight: 70
  };

  const mockImageResponse = new ArrayBuffer(8);
  const mockStravaLink = 'https://strava.com/auth';

  beforeEach(() => {
    // Mock all API calls that happen on component mount
    (axiosInstance.get as any)
      .mockImplementation((url: string) => {
        if (url === 'api/strava/login') {
          return Promise.resolve({data: {authorizationUrl: mockStravaLink}});
        }
        if (url === 'api/user/profile') {
          return Promise.resolve({data: mockUserData});
        }
        if (url === 'api/user/profile-image') {
          return Promise.resolve({
            data: mockImageResponse,
            headers: {'content-type': 'image/png'}
          });
        }
        return Promise.reject(new Error('Unknown URL'));
      });

    // Mock URL.createObjectURL
    global.URL.createObjectURL = vi.fn(() => 'mock-image-url');
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should render all sections correctly', async () => {
    renderWithRouter(<Settings/>);

    await waitFor(() => {
      expect(screen.getByText(/Foto Profilo/i)).toBeInTheDocument();
      expect(screen.getByText(/Cambio Password/i)).toBeInTheDocument();
      expect(screen.getByText(/Informazioni personali/i)).toBeInTheDocument();
      expect(screen.getByText(/Connessione Strava/i)).toBeInTheDocument();
      expect(screen.getByText(/Connessione bot-Telegram/i)).toBeInTheDocument();
    });
  });

  it('should handle profile picture upload', async () => {
    // Mock della chiamata PUT
    (axiosInstance.put as any).mockResolvedValueOnce({});

    const file = new File(['hello'], 'hello.png', {type: 'image/png'});
    renderWithRouter(<Settings/>);

    // Se axiosInstance.get non è importante per questo test, si può evitare
    // altrimenti mockalo:
    (axiosInstance.get as any).mockResolvedValue({data: {/* ... */}});

    // Simula selezione file
    const input = screen.getByLabelText(/scegli file/i);
    fireEvent.change(input, {target: {files: [file]}});

    // Simula clic sul bottone "Carica"
    const uploadButton = screen.getByRole('button', {name: /carica/i});
    fireEvent.click(uploadButton);

    // Aspetta che l'upload sia stato chiamato
    await waitFor(() => {
      expect(axiosInstance.put).toHaveBeenCalledWith(
        'api/user/update-profile-image',
        expect.any(FormData),
        expect.objectContaining({
          headers: expect.objectContaining({
            'Content-Type': 'multipart/form-data',
          }),
        })
      );
    });
  });

  it('should update password successfully', async () => {
    (axiosInstance.put as any).mockResolvedValueOnce({});

    renderWithRouter(<Settings/>);

    await waitFor(() => {
      expect(axiosInstance.get).toHaveBeenCalled();
    });

    fireEvent.change(screen.getByLabelText(/Password Attuale/i), {
      target: {value: 'oldPassword'}
    });

    fireEvent.change(screen.getByLabelText(/Nuova Password/i), {
      target: {value: 'newPassword'}
    });

    fireEvent.change(screen.getByLabelText(/Conferma Password/i), {
      target: {value: 'newPassword'}
    });

    fireEvent.click(screen.getByText('Aggiorna Password'));

    await waitFor(() => {
      expect(axiosInstance.put).toHaveBeenCalledWith('auth/change-password', {
        currentPassword: 'oldPassword',
        newPassword: 'newPassword'
      });
    });
  });

  it('should show error when password update fails', async () => {
    (axiosInstance.put as any).mockRejectedValueOnce(new Error('Password update failed'));

    renderWithRouter(<Settings/>);

    await waitFor(() => {
      expect(axiosInstance.get).toHaveBeenCalled();
    });

    fireEvent.change(screen.getByLabelText(/Password Attuale/i), {
      target: {value: 'wrongPassword'}
    });

    fireEvent.change(screen.getByLabelText(/Nuova Password/i), {
      target: {value: 'newPassword'}
    });

    fireEvent.change(screen.getByLabelText(/Conferma Password/i), {
      target: {value: 'newPassword'}
    });

    fireEvent.click(screen.getByText('Aggiorna Password'));

    await waitFor(() => {
      expect(screen.getByText(/Errore nel cambiare la password/i)).toBeInTheDocument();
    });
  });

  it('should connect to Strava', async () => {
    renderWithRouter(<Settings/>);

    await waitFor(() => {
      expect(axiosInstance.get).toHaveBeenCalled();
    });

    const stravaButton = screen.getByText('Collega Strava');
    expect(stravaButton).toBeEnabled();

    // Mock window.location.href
    window.location = {...window.location, href: ''};

    fireEvent.click(stravaButton);

    await waitFor(() => {
      expect(window.location.href).toBe(mockStravaLink);
    });
  });

  it('should connect Telegram bot for user', async () => {
    (axiosInstance.post as any).mockResolvedValueOnce({});

    renderWithRouter(<Settings/>);

    await waitFor(() => {
      expect(axiosInstance.get).toHaveBeenCalled();
    });

    fireEvent.click(screen.getByText('Collega bot-Telegram-User'));

    await waitFor(() => {
      expect(axiosInstance.post).toHaveBeenCalledWith('api/telegram/invite/user', {});
    });
  });

  it('should connect Telegram bot for group', async () => {
    (axiosInstance.post as any).mockResolvedValueOnce({});

    renderWithRouter(<Settings/>);

    await waitFor(() => {
      expect(axiosInstance.get).toHaveBeenCalled();
    });

    fireEvent.click(screen.getByText('Collega bot-Telegram-Group'));

    await waitFor(() => {
      expect(axiosInstance.post).toHaveBeenCalledWith('api/telegram/invite/group', {});
    });
  });

  it('should show loading state when uploading profile picture', async () => {
    (axiosInstance.put as any).mockImplementationOnce(() =>
      new Promise(resolve => setTimeout(() => resolve({}), 1000))
    );

    const file = new File(['hello'], 'hello.png', {type: 'image/png'});
    renderWithRouter(<Settings/>);

    await waitFor(() => {
      expect(axiosInstance.get).toHaveBeenCalled();
    });

    const input = screen.getByLabelText(/Scegli File/i);
    fireEvent.change(input, {target: {files: [file]}});

    fireEvent.click(screen.getByText('Carica'));

    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });
});

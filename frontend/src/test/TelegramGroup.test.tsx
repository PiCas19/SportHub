import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import TelegramGroup from '../screen/telegram/TelegramGroup';
import { vi } from 'vitest';
import axiosInstance from '../screen/api/axiosConfig';
import '@testing-library/jest-dom';

vi.mock('../screen/api/axiosConfig'); // Mocka axios

describe('TelegramGroup', () => {
  const mockGroups = [
    {
      id: '1',
      title: 'Test Group',
      members: 42,
      photoSmall: 'small.jpg',
      photoBig: 'big.jpg',
      chatId: '12345'
    }
  ];

  beforeEach(() => {
    vi.resetAllMocks();
  });

  it('mostra messaggio se nessun gruppo è presente', async () => {
    (axiosInstance.get as any).mockResolvedValueOnce({ data: { chats: [] } });
    render(<TelegramGroup />);
    await waitFor(() => {
      expect(screen.getByText(/Non sei iscritto/i)).toBeInTheDocument();
    });
  });

  it('permette di aggiungere un nuovo gruppo', async () => {
    (axiosInstance.get as any).mockResolvedValue({ data: { chats: [] } });
    (axiosInstance.post as any).mockResolvedValue({});
    render(<TelegramGroup />);

    fireEvent.click(screen.getByLabelText(/aggiungi gruppo/i));

    fireEvent.change(screen.getByLabelText(/Id/i), {
      target: { value: '67890' }
    });

    fireEvent.click(screen.getByText('Salva'));

    await waitFor(() => {
      expect(axiosInstance.post).toHaveBeenCalledWith('api/telegram/chats', {
        chatId: '67890'
      });
    });
  });

  it('gestisce errori in fase di aggiunta', async () => {
    (axiosInstance.get as any).mockResolvedValue({ data: { chats: [] } });
    (axiosInstance.post as any).mockRejectedValue(new Error('Errore'));

    render(<TelegramGroup />);
    fireEvent.click(screen.getByLabelText(/aggiungi gruppo/i));

    fireEvent.change(screen.getByLabelText(/Id/i), {
      target: { value: '67890' }
    });

    fireEvent.click(screen.getByText('Salva'));

    await waitFor(() => {
      expect(screen.getByText(/Errore nell'aggiunta/i)).toBeInTheDocument();
    });
  });

  it('apre e conferma eliminazione gruppo', async () => {
    (axiosInstance.get as any).mockResolvedValueOnce({ data: { chats: mockGroups } });
    (axiosInstance.delete as any).mockResolvedValueOnce({});

    render(<TelegramGroup />);

    await waitFor(() => {
      expect(screen.getByText('Test Group')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByLabelText('delete'));

    await waitFor(() => {
      expect(screen.getByText(/Conferma abbandono/i)).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText('Abbandona'));

    await waitFor(() => {
      expect(axiosInstance.delete).toHaveBeenCalledWith('api/telegram/chats/1');
    });
  });

  it('mostra la foto grande del gruppo quando clicchi l\'avatar', async () => {
    (axiosInstance.get as any).mockResolvedValueOnce({ data: { chats: mockGroups } });

    render(<TelegramGroup />);
    await waitFor(() => {
      expect(screen.getByText('Test Group')).toBeInTheDocument();
    });

    const avatar = screen.getByRole('img');
    fireEvent.click(avatar);

    await waitFor(() => {
      expect(screen.getByAltText(/Foto grande del gruppo/i)).toBeInTheDocument();
    });
  });
});

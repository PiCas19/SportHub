import {render, screen, waitFor, fireEvent} from '@testing-library/react';
import Challenges from '../screen/challenge/Challenges';
import {vi} from 'vitest';
import axiosInstance from '../screen/api/axiosConfig';
import '@testing-library/jest-dom';
import {MemoryRouter} from 'react-router-dom';

vi.mock('../screen/api/axiosConfig');

const renderWithRouter = (ui: React.ReactElement) => {
  return render(<MemoryRouter>{ui}</MemoryRouter>);
};

describe('Challenges Component', () => {
  const mockChallenges = [
    {
      id: 1,
      name: 'Running Challenge',
      maxParticipants: 10,
      currentParticipants: 5,
      goalType: 'DISTANCE',
      sportType: 'RUN',
      deadline: '2023-12-31',
      endDate: '2024-01-31',
      started: false,
      finished: false,
      chatId: 'chat1',
      userRegistered: false
    },
    {
      id: 2,
      name: 'Cycling Challenge',
      maxParticipants: 15,
      currentParticipants: 10,
      goalType: 'DURATION',
      sportType: 'RIDE',
      deadline: '2023-11-30',
      endDate: '2023-12-31',
      started: true,
      finished: false,
      chatId: 'chat2',
      userRegistered: true
    }
  ];

  const mockChats = [
    {id: '1', title: 'Running Group', chatId: 'chat1'},
    {id: '2', title: 'Cycling Group', chatId: 'chat2'}
  ];

  const mockLeaderboard = [
    {
      score: 100,
      sporthubUsername: 'User1',
      telegramUsername: 'user1_tg',
      rank: 1,
      isCurrentUser: false
    },
    {
      score: 80,
      sporthubUsername: 'CurrentUser',
      telegramUsername: 'currentuser_tg',
      rank: 2,
      isCurrentUser: true
    }
  ];

  beforeEach(() => {
    (axiosInstance.get as any)
      .mockImplementation((url: string) => {
        if (url === 'api/telegram/chats') {
          return Promise.resolve({data: {chats: mockChats}});
        }
        if (url === 'api/competitions') {
          return Promise.resolve({data: mockChallenges});
        }
        if (url.includes('leaderboard')) {
          return Promise.resolve({data: mockLeaderboard});
        }
        return Promise.reject(new Error('Unknown URL'));
      });

    (axiosInstance.post as any).mockImplementation((url: string) => {
      if (url.includes('join')) {
        return Promise.resolve({});
      }
      if (url === 'api/competitions') {
        return Promise.resolve({
          data: {
            id: 3,
            name: 'New Challenge',
            maxParticipants: 20,
            currentParticipants: 0,
            goalType: 'ACTIVITIES',
            sportType: 'SWIM',
            deadline: '2023-12-15',
            endDate: '2024-01-15',
            started: false,
            finished: false,
            chatId: 'chat3',
            userRegistered: false
          }
        });
      }
      return Promise.reject(new Error('Unknown URL'));
    });
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should render all tabs and challenges', async () => {
    renderWithRouter(<Challenges/>);

    await waitFor(() => {
      expect(screen.getByText('Tutte le Challenge')).toBeInTheDocument();
      expect(screen.getByText('Le Mie Challenge')).toBeInTheDocument();
      expect(screen.getByText('Challenge Disponibili')).toBeInTheDocument();
      expect(screen.getByText('Running Challenge')).toBeInTheDocument();
      expect(screen.getByText('Cycling Challenge')).toBeInTheDocument();
    });
  });

  it('should filter challenges based on tab selection', async () => {
    renderWithRouter(<Challenges/>);

    await waitFor(() => {
      expect(screen.getByText('Running Challenge')).toBeInTheDocument();
      expect(screen.getByText('Cycling Challenge')).toBeInTheDocument();
    });

    // Click on "My Challenges" tab
    fireEvent.click(screen.getByText('Le Mie Challenge'));
    await waitFor(() => {
      expect(screen.queryByText('Running Challenge')).not.toBeInTheDocument();
      expect(screen.getByText('Cycling Challenge')).toBeInTheDocument();
    });

    // Click on "Available Challenges" tab
    fireEvent.click(screen.getByText('Challenge Disponibili'));
    await waitFor(() => {
      expect(screen.getByText('Running Challenge')).toBeInTheDocument();
      expect(screen.queryByText('Cycling Challenge')).not.toBeInTheDocument();
    });
  });

  it('should open leaderboard dialog when clicking on a challenge', async () => {
    renderWithRouter(<Challenges/>);

    await waitFor(() => {
      expect(screen.getByText('Running Challenge')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText('Running Challenge'));

    await waitFor(() => {
      expect(screen.getByText('Running Challenge - Classifica')).toBeInTheDocument();
      expect(screen.getByText('User1')).toBeInTheDocument();
      expect(screen.getByText('CurrentUser')).toBeInTheDocument();
    });
  });

  it('should show loading states', async () => {
    (axiosInstance.get as any).mockImplementationOnce(() =>
      new Promise(resolve => setTimeout(() => resolve({data: mockChallenges}), 1000))
    );

    renderWithRouter(<Challenges/>);

    expect(screen.getByRole('progressbar')).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
    });
  });
});
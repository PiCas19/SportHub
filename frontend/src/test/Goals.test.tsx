import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import Goals from '../screen/goals/Goals';
import { vi } from 'vitest';
import axiosInstance from '../screen/api/axiosConfig';
import '@testing-library/jest-dom';
import { MemoryRouter } from 'react-router-dom';

vi.mock('../screen/api/axiosConfig');

const renderWithRouter = (ui: React.ReactElement) => {
  return render(<MemoryRouter>{ui}</MemoryRouter>);
};

describe('Goals Component', () => {
  const mockGoals = [
    {
      id: 1,
      name: 100,
      sportType: 'RUN',
      goalType: 'DISTANCE',
      period: 'WEEKLY',
      currentValue: 50,
      percentage: 50,
      target: 100,
      unit: 'km'
    },
    {
      id: 2,
      name: 2000,
      sportType: 'ALL',
      goalType: 'CALORIES',
      period: 'MONTHLY',
      currentValue: 1500,
      percentage: 75,
      target: 2000,
      unit: 'kcal'
    }
  ];

  beforeEach(() => {
    (axiosInstance.get as any).mockResolvedValue({ data: mockGoals });
    (axiosInstance.post as any).mockResolvedValue({});
    (axiosInstance.delete as any).mockResolvedValue({});
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should show empty state when no goals', async () => {
    (axiosInstance.get as any).mockResolvedValueOnce({ data: [] });

    renderWithRouter(<Goals />);

    await waitFor(() => {
      expect(screen.getByText('Non hai ancora obiettivi. Aggiungi il tuo primo obiettivo!')).toBeInTheDocument();
    });
  });

  it('should open and close the add goal form', async () => {
    renderWithRouter(<Goals />);

    await waitFor(() => {
      expect(screen.getByText('100 in km')).toBeInTheDocument();
    });

    // Click the add button
    fireEvent.click(screen.getByRole('button', { name: /add/i }));

    await waitFor(() => {
      expect(screen.getByText('Aggiungi Nuovo Obiettivo')).toBeInTheDocument();
    });

    // Close the form
    fireEvent.click(screen.getByText('Annulla'));
    await waitFor(() => {
      expect(screen.queryByText('Aggiungi Nuovo Obiettivo')).not.toBeInTheDocument();
    });
  });
  
  it('should add a new goal', async () => {
    renderWithRouter(<Goals />);

    await waitFor(() => {
      expect(screen.getByText('100 in km')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole('button', { name: /add/i }));

    await waitFor(() => {
      expect(screen.getByText('Aggiungi Nuovo Obiettivo')).toBeInTheDocument();
    });

    fireEvent.mouseDown(screen.getByLabelText('Tipo di Obiettivo'));
    fireEvent.click(screen.getByText('Durata'));


    fireEvent.change(screen.getByLabelText('Target (minuti)'), {
      target: { value: '60' }
    });

    fireEvent.click(screen.getByText('Salva'));

    await waitFor(() => {
      expect(axiosInstance.post).toHaveBeenCalledWith('api/goals', {
        goalType: 'DURATION',
        sportType: 'RUN',
        period: 'WEEKLY',
        targetValue: 60
      });
    });
  });


  it('should calculate overall progress correctly', async () => {
    renderWithRouter(<Goals />);

    await waitFor(() => {
      // 50% + 75% = 125% / 2 = 62.5% rounded to 63%
      expect(screen.getByText('63% di obiettivi completati (media totale)')).toBeInTheDocument();
    });
  });

  it('should handle API errors', async () => {
    (axiosInstance.get as any).mockRejectedValueOnce(new Error('API Error'));

    renderWithRouter(<Goals />);

    await waitFor(() => {
      expect(screen.queryByText('100 in km')).not.toBeInTheDocument();
    });
  });
});
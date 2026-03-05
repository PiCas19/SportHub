import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import Activities from '../screen/activity/Activities';
import axiosInstance from '../screen/api/axiosConfig';
import '@testing-library/jest-dom';

vi.mock('../screen/api/axiosConfig', () => ({
  default: {
    post: vi.fn(() => Promise.resolve({ data: [] })),
  },
}));

vi.mock('../../components/DecodePolylineMap', () => ({
  default: vi.fn().mockReturnValue([[0, 0], [1, 1]]),
}));

vi.mock('react-leaflet', () => ({
  MapContainer: ({ children }: any) => <div>{children}</div>,
  TileLayer: () => <div />,
  Polyline: () => <div />,
}));

vi.mock('../../components/HeaderScreen', () => ({
  default: () => <div>HeaderScreen Mock</div>,
}));

vi.mock('../../components/filter/FilterSport', () => ({
  default: ({ sportType, onSportChange }: any) => (
    <select
      data-testid="sport-select"
      value={sportType}
      onChange={(e) => onSportChange(e.target.value)}
    >
      <option value="all">All</option>
      <option value="run">Run</option>
      <option value="ride">Ride</option>
    </select>
  ),
}));

vi.mock('../../components/CustomSnackbar', () => ({
  default: () => <div>CustomSnackbar Mock</div>,
}));

describe('Activities Screen', () => {
  const mockActivities = [
    {
      id: '1',
      title: 'Morning Run',
      type: 'run',
      date: '2023-01-01',
      duration: 3600,
      distance: 10000,
      elevationGain: 100,
      averageSpeed: 10,
      maxSpeed: 12,
      calories: 500,
      description: 'Nice morning run',
      location: 'Park',
      mapPolyline: 'abc123',
      sportType: 'Run',
      hasHeartRate: true,
      startCoordinates: [45.0, 9.0],
    },
    {
      id: '2',
      title: 'Evening Ride',
      type: 'ride',
      date: '2023-01-02',
      duration: 7200,
      distance: 20000,
      elevationGain: 200,
      averageSpeed: 20,
      maxSpeed: 25,
      calories: 800,
      description: 'Evening bike ride',
      location: 'Hills',
      sportType: 'Ride',
      hasHeartRate: false,
    },
  ];

  beforeEach(() => {
    vi.clearAllMocks();
    (axiosInstance.post as any).mockImplementation((url:any) => {
      if (url === 'api/strava/activities/filter') {
        return Promise.resolve({ data: mockActivities });
      }
      return Promise.resolve({});
    });
  });

  it('filters activities by name', async () => {
    await act(async () => {
      render(<Activities />);
    });

    const searchInput = screen.getByLabelText('Cerca per nome');

    await act(async () => {
      fireEvent.change(searchInput, { target: { value: 'Morning' } });
    });

    await waitFor(() => {
      expect(axiosInstance.post).toHaveBeenCalledWith(
        'api/strava/activities/filter',
        expect.objectContaining({
          keywords: 'Morning',
          maxDistance: 100,
          minDistance: 0,
          sportType: "all",
        })
      );
    });
  });

  it('displays empty state when no activities are found', async () => {
    (axiosInstance.post as any).mockResolvedValueOnce({ data: [] });

    await act(async () => {
      render(<Activities />);
    });

    await waitFor(() => {
      expect(screen.getByText('Nessuna attività trovata con i filtri applicati')).toBeInTheDocument();
    });
  });

  it('handles API errors gracefully', async () => {
    (axiosInstance.post as any).mockRejectedValueOnce(new Error('API Error'));

    await act(async () => {
      render(<Activities />);
    });

    await waitFor(() => {
      expect(screen.getByText(/Nessuna attività trovata/i)).toBeInTheDocument();
    });
  });
});
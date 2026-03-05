import {render, screen, waitFor, act} from '@testing-library/react';
import {describe, it, expect, vi, beforeEach} from 'vitest';
import Home from '../screen/homeScreen/Home';
import axiosInstance from '../screen/api/axiosConfig';
import '@testing-library/jest-dom';

beforeAll(() => {
  global.ResizeObserver = class {
    observe() {
      return null;
    }

    unobserve() {
      return null;
    }

    disconnect() {
      return null;
    }
  };
});

// Mock dei componenti utilizzati in Home
vi.mock('../../components/HeaderScreen', () => ({
  default: () => <div>HeaderScreen Mock</div>,
}));

vi.mock('../../components/graph/LineChartComponent', () => ({
  default: ({title, subtitle, valueTime, valueSport}: any) => (
    <div>
      {title} - {subtitle}
      <select
        data-testid="line-time-select"
        onChange={(e) => valueTime(e.target.value)}
      >
        <option value="7d">7 Days</option>
        <option value="7m">7 Months</option>
        <option value="y">Year</option>
      </select>
      <select
        data-testid="line-sport-select"
        onChange={(e) => valueSport(e.target.value)}
      >
        <option value="all">All</option>
        <option value="run">Run</option>
        <option value="ride">Ride</option>
      </select>
    </div>
  ),
}));

vi.mock('../../components/graph/BarChartComponent', () => ({
  default: ({title, subtitle, valueTime, valueSport}: any) => (
    <div>
      {title} - {subtitle}
      <select
        data-testid="bar-time-select"
        onChange={(e) => valueTime(e.target.value)}
      >
        <option value="7d">7 Days</option>
        <option value="1m">1 Month</option>
        <option value="y">Year</option>
      </select>
      <select
        data-testid="bar-sport-select"
        onChange={(e) => valueSport(e.target.value)}
      >
        <option value="all">All</option>
        <option value="run">Run</option>
        <option value="ride">Ride</option>
      </select>
    </div>
  ),
}));

vi.mock('../../components/graph/GaugeComponent', () => ({
  default: ({title, subtitle, valueTime, valueSport}: any) => (
    <div>
      {title} - {subtitle}
      <select
        data-testid="gauge-time-select"
        onChange={(e) => valueTime(e.target.value)}
      >
        <option value="7d">7 Days</option>
        <option value="7m">7 Months</option>
        <option value="y">Year</option>
      </select>
      <select
        data-testid="gauge-sport-select"
        onChange={(e) => valueSport(e.target.value)}
      >
        <option value="all">All</option>
        <option value="run">Run</option>
        <option value="ride">Ride</option>
      </select>
    </div>
  ),
}));

// Mock JSON per le chiamate API
const mockTotalKm = {
  total_km_last_seven_day: 30,
  total_km_last_month: 90,
  total_km_year: 800,
};

const mockWeeklyPerformance = {
  Jan: {average_speed: 15, total_elevation_gain: 200},
  Feb: {average_speed: 16, total_elevation_gain: 250},
};

const mockSevenMonthsPerformance = {
  Jul: {average_speed: 14, total_elevation_gain: 300},
  Aug: {average_speed: 15, total_elevation_gain: 320},
};

const mockYearlyPerformance = {
  '2025': {average_speed: 15.5, total_elevation_gain: 3500},
};

// Mock di axiosInstance
vi.mock('../screen/api/axiosConfig', () => ({
  default: {
    get: vi.fn(),
  },
}));

describe('Home Component', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    (axiosInstance.get as any).mockImplementation((url: string) => {
      if (url === 'api/strava/total-km') {
        return Promise.resolve({data: mockTotalKm});
      }
      if (url === 'api/strava/performance/weekly') {
        return Promise.resolve({data: mockWeeklyPerformance});
      }
      if (url === 'api/strava/performance/last-7-months') {
        return Promise.resolve({data: mockSevenMonthsPerformance});
      }
      if (url === 'api/strava/performance/yearly') {
        return Promise.resolve({data: mockYearlyPerformance});
      }
      return Promise.reject(new Error('Unknown endpoint'));
    });
  });

  it('renders the Home component with HeaderScreen and charts', async () => {
    await act(async () => {
      render(<Home/>);
    });

    expect(screen.getByText('Dashboard Analisi Dati')).toBeInTheDocument();
    expect(screen.getByText('Obbiettivo')).toBeInTheDocument();
    expect(screen.getByText('Percentuale di km raggiunti')).toBeInTheDocument();
  });

  it('fetches and displays gauge data for 7 days', async () => {
    await act(async () => {
      render(<Home/>);
    });

    await waitFor(() => {
      expect(axiosInstance.get).toHaveBeenCalledWith('api/strava/total-km', {
        params: {sportType: 'all'},
      });
    });
  });

  it('fetches and displays line chart data for weekly performance', async () => {
    await act(async () => {
      render(<Home/>);
    });

    await waitFor(() => {
      expect(axiosInstance.get).toHaveBeenCalledWith('api/strava/performance/weekly', {
        params: {sportType: 'all'},
      });
    });
  });

  it('fetches and displays bar chart data for weekly performance', async () => {
    await act(async () => {
      render(<Home/>);
    });

    await waitFor(() => {
      expect(axiosInstance.get).toHaveBeenCalledWith('api/strava/performance/weekly', {
        params: {sportType: 'all'},
      });
    });
  });
});
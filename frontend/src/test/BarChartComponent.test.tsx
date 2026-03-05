import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import BarChartComponent from '../components/graph/BarChartComponent';
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

const mockData = [
  { month: "2024-01-01", elevationGain: 100, sportType: "cycling" },
  { month: "2024-02-01", elevationGain: 150, sportType: "running" },
  { month: "2024-03-01", elevationGain: 200, sportType: "cycling" },
  { month: "2024-04-01", elevationGain: 250, sportType: "running" },
  { month: "2024-05-01", elevationGain: 300, sportType: "cycling" },
  { month: "2024-06-01", elevationGain: 350, sportType: "running" },
  { month: "2024-07-01", elevationGain: 400, sportType: "cycling" },
];

const valueTimeMock = vi.fn();
const valueSportMock = vi.fn();

describe('BarChartComponent', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders BarChartComponent correctly', () => {
    render(
      <BarChartComponent
        data={mockData}
        title="Test Chart"
        subtitle="Testing BarChart"
        dataKey="elevationGain"
        valueTime={valueTimeMock}
        valueSport={valueSportMock}
      />
    );

    expect(screen.getByText('Test Chart')).toBeInTheDocument();
    expect(screen.getByText('Testing BarChart')).toBeInTheDocument();
  });

  it('renders FilterBar and FilterSport', () => {
    render(
      <BarChartComponent
        data={mockData}
        title="Test Chart"
        subtitle="Testing Filters"
        dataKey="elevationGain"
        valueTime={valueTimeMock}
        valueSport={valueSportMock}
      />
    );

    expect(screen.getByTestId('time-range-select')).toBeInTheDocument();
    expect(screen.getByTestId('sport-select')).toBeInTheDocument();
  });

  it('calls valueTime callback when time range changes', async () => {
    render(
      <BarChartComponent
        data={mockData}
        title="Test Chart"
        subtitle="Testing Callbacks"
        dataKey="elevationGain"
        valueTime={valueTimeMock}
        valueSport={valueSportMock}
      />
    );

    const timeRangeSelect = screen.getByLabelText('Periodo');
    fireEvent.mouseDown(timeRangeSelect);
    const option7m = await screen.findByText('Ultimi 7 mesi');
    fireEvent.click(option7m);
    expect(valueTimeMock).toHaveBeenCalledWith('7m');
  });

  // Test simile per valueSport
  it('calls valueSport callback when sport type changes', async () => {
    render(
      <BarChartComponent
        data={mockData}
        title="Test Chart"
        subtitle="Testing Callbacks"
        dataKey="elevationGain"
        valueTime={valueTimeMock}
        valueSport={valueSportMock}
      />
    );

    const sportTypeSelect = screen.getByLabelText('Sport');
    fireEvent.mouseDown(sportTypeSelect);

    const runningOption = await screen.findByText('Corsa');
    fireEvent.click(runningOption);

    expect(valueSportMock).toHaveBeenCalledWith('run');
  });
});
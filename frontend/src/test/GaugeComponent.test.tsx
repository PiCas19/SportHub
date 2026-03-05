import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import GaugeComponent from '../components/graph/GaugeComponent.tsx';
import '@testing-library/jest-dom';

beforeAll(() => {
  global.ResizeObserver = class {
    observe() { return null; }
    unobserve() { return null; }
    disconnect() { return null; }
  };
});

describe('GaugeComponent', () => {
  const mockValueTime = vi.fn();
  const mockValueSport = vi.fn();
  const defaultProps = {
    value: 50,
    title: 'Test Gauge',
    subtitle: 'Testing Gauge Component',
    min: 0,
    max: 100,
    valueTime: mockValueTime,
    valueSport: mockValueSport,
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders correctly with default props', () => {
    render(<GaugeComponent {...defaultProps} />);

    expect(screen.getByText('Test Gauge')).toBeInTheDocument();
    expect(screen.getByText('Testing Gauge Component')).toBeInTheDocument();
    expect(screen.getByText('50')).toBeInTheDocument();
    expect(screen.getByText('0')).toBeInTheDocument();
    expect(screen.getByText('100 km')).toBeInTheDocument();
  });

  it('displays the correct value', () => {
    render(<GaugeComponent {...defaultProps} value={75} />);
    expect(screen.getByText('75')).toBeInTheDocument();
  });

  it('displays custom min and max labels', () => {
    render(<GaugeComponent {...defaultProps} min={10} max={200} />);
    expect(screen.getByText('10')).toBeInTheDocument();
    expect(screen.getByText('200 km')).toBeInTheDocument();
  });

  it('renders the filters correctly', () => {
    render(<GaugeComponent {...defaultProps} />);
    expect(screen.getByLabelText('Periodo')).toBeInTheDocument();
    expect(screen.getByLabelText('Sport')).toBeInTheDocument();
  });

  it('calls valueTime callback when time range changes', async () => {
    render(<GaugeComponent {...defaultProps} />);

    const timeRangeSelect = screen.getByLabelText('Periodo');
    fireEvent.mouseDown(timeRangeSelect);

    const option7m = await screen.findByText('Ultimi 7 mesi');
    fireEvent.click(option7m);

    expect(mockValueTime).toHaveBeenCalledWith('7m');
  });

  it('calls valueSport callback when sport type changes', async () => {
    render(<GaugeComponent {...defaultProps} />);

    const sportTypeSelect = screen.getByLabelText('Sport');
    fireEvent.mouseDown(sportTypeSelect);

    const runningOption = await screen.findByText('Corsa');
    fireEvent.click(runningOption);

    expect(mockValueSport).toHaveBeenCalledWith('run');
  });
});
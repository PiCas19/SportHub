import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import FilterBar from '../components/filter/FilterBar';
import '@testing-library/jest-dom';

describe('FilterBar', () => {
  it('renders select with default value', () => {
    render(<FilterBar timeRange="7d" onRangeChange={vi.fn()} />);

    const select = screen.getByRole('combobox');
    expect(select).toBeInTheDocument();
    expect(select).toHaveTextContent(/ultimi 7 giorni/i);
  });

  it('calls onRangeChange when selecting a new option', () => {
    const mockChange = vi.fn();
    render(<FilterBar timeRange="7d" onRangeChange={mockChange} />);

    const select = screen.getByRole('combobox');
    fireEvent.mouseDown(select);
    const option = screen.getByText(/ultimo anno/i);
    fireEvent.click(option);
    expect(mockChange).toHaveBeenCalledWith('1y');
  });
});

import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import FilterSport from '../components/filter/FilterSport';
import '@testing-library/jest-dom';

describe('FilterSport', () => {
  it('renders select with default value', () => {
    render(<FilterSport sportType="all" onSportChange={vi.fn()} />);

    const select = screen.getByRole('combobox');
    expect(select).toBeInTheDocument();
    expect(select).toHaveTextContent(/tutti/i);
  });

  it('calls onSportChange when selecting a new option', () => {
    const mockChange = vi.fn();
    render(<FilterSport sportType="all" onSportChange={mockChange} />);

    // Apri il menu a tendina
    const select = screen.getByRole('combobox');
    fireEvent.mouseDown(select);

    // Clicca su "Nuoto"
    const option = screen.getByText(/nuoto/i);
    fireEvent.click(option);

    expect(mockChange).toHaveBeenCalledWith('swim');
  });
});

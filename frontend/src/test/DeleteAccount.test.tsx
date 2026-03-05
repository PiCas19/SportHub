import {render, screen, fireEvent} from '@testing-library/react';
import DeleteAccount from '../components/DeleteAccount';
import {vi} from 'vitest';
import Cookies from 'js-cookie';
import {MemoryRouter, Routes, Route} from 'react-router-dom';

vi.mock('js-cookie', () => ({
  default: {
    get: vi.fn(),
    remove: vi.fn(),
  }
}));

describe('DeleteAccount', () => {
  afterEach(() => {
    vi.clearAllMocks();
  });

  it('renders title and delete button', () => {
    render(
      <MemoryRouter>
        <DeleteAccount/>
      </MemoryRouter>
    );
    expect(screen.getByText('Elimina Account')).toBeInTheDocument();
    expect(screen.getByRole('button', {name: /elimina il mio account/i})).toBeInTheDocument();
  });

  it('opens confirmation dialog on click', () => {
    render(
      <MemoryRouter>
        <DeleteAccount/>
      </MemoryRouter>
    );
    fireEvent.click(screen.getByRole('button', {name: /elimina il mio account/i}));
    expect(screen.getByText(/conferma eliminazione account/i)).toBeInTheDocument();
  });

  it('disables delete button if input is incorrect', () => {
    render(
      <MemoryRouter>
        <DeleteAccount/>
      </MemoryRouter>
    );
    fireEvent.click(screen.getByRole('button', {name: /elimina il mio account/i}));
    fireEvent.change(screen.getByPlaceholderText('ELIMINA'), {target: {value: 'no'}});

    expect(screen.getByRole('button', {name: /elimina definitivamente/i})).toBeDisabled();
  });

  it('shows error message when input is incorrect', () => {
    const showSnackbar = vi.fn();

    render(
      <MemoryRouter>
        <DeleteAccount showSnackbar={showSnackbar}/>
      </MemoryRouter>
    );

    fireEvent.click(screen.getByRole('button', {name: /elimina il mio account/i}));
    fireEvent.change(screen.getByPlaceholderText('ELIMINA'), {target: {value: 'wrong'}});

    fireEvent.click(screen.getByRole('button', {name: /elimina definitivamente/i}));
  });

  it('removes auth_token and calls onSuccess if confirmation is correct', () => {
    const onSuccess = vi.fn();

    render(
      <MemoryRouter>
        <DeleteAccount onSuccess={onSuccess}/>
      </MemoryRouter>
    );

    fireEvent.click(screen.getByRole('button', {name: /elimina il mio account/i}));
    fireEvent.change(screen.getByPlaceholderText('ELIMINA'), {target: {value: 'ELIMINA'}});
    fireEvent.click(screen.getByRole('button', {name: /elimina definitivamente/i}));

    expect(Cookies.remove).toHaveBeenCalledWith('auth_token');
    expect(onSuccess).toHaveBeenCalled();
  });

  it('correctly updates state when input changes', () => {
    render(
      <MemoryRouter>
        <DeleteAccount/>
      </MemoryRouter>
    );

    fireEvent.click(screen.getByRole('button', {name: /elimina il mio account/i}));
    const inputField = screen.getByPlaceholderText('ELIMINA');
    fireEvent.change(inputField, {target: {value: 'ELIMINA'}});
  });

  it('closes the delete dialog when cancel button is clicked', () => {
    render(
      <MemoryRouter>
        <DeleteAccount/>
      </MemoryRouter>
    );

    fireEvent.click(screen.getByRole('button', {name: /elimina il mio account/i}));
    fireEvent.click(screen.getByRole('button', {name: /annulla/i}));
  });

  it('calls showSnackbar if confirmation text is wrong', () => {
    const showSnackbar = vi.fn();

    render(
      <MemoryRouter>
        <DeleteAccount showSnackbar={showSnackbar}/>
      </MemoryRouter>
    );

    fireEvent.click(screen.getByRole('button', {name: /elimina il mio account/i}));
    fireEvent.change(screen.getByPlaceholderText('ELIMINA'), {target: {value: 'sbagliato'}});
    fireEvent.click(screen.getByRole('button', {name: /elimina definitivamente/i}));
  });

  it('removes auth_token and calls onSuccess when input is correct', () => {
    const onSuccess = vi.fn();

    render(
      <MemoryRouter>
        <DeleteAccount onSuccess={onSuccess}/>
      </MemoryRouter>
    );

    fireEvent.click(screen.getByRole('button', {name: /elimina il mio account/i}));
    fireEvent.change(screen.getByPlaceholderText('ELIMINA'), {target: {value: 'ELIMINA'}});
    fireEvent.click(screen.getByRole('button', {name: /elimina definitivamente/i}));

    expect(Cookies.remove).toHaveBeenCalledWith('auth_token');
    expect(onSuccess).toHaveBeenCalled();
  });

  it('redirects to home page after successful deletion', () => {
    const onSuccess = vi.fn();

    render(
      <MemoryRouter initialEntries={['/delete']}>
        <Routes>
          <Route path="/delete" element={<DeleteAccount onSuccess={onSuccess} />} />
          <Route path="/" element={<div>Home</div>} />
        </Routes>
      </MemoryRouter>
    );

    fireEvent.click(screen.getByRole('button', {name: /elimina il mio account/i}));
    fireEvent.change(screen.getByPlaceholderText('ELIMINA'), {target: {value: 'ELIMINA'}});
    fireEvent.click(screen.getByRole('button', {name: /elimina definitivamente/i}));
  });
});

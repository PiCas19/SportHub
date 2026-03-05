import {render, screen} from '@testing-library/react';
import {MemoryRouter} from 'react-router-dom';
import ProtectedRoute from '../components/ProtectedRoute';
import {vi} from 'vitest';

vi.mock('js-cookie', () => ({
  default: {
    get: vi.fn(),
  },
}));

import Cookies from 'js-cookie';

describe('ProtectedRoute', () => {
  afterEach(() => {
    vi.clearAllMocks();
  });

  it('renders children if auth_token is present', () => {
    (Cookies.get as any).mockReturnValue('mock-token');

    render(
      <MemoryRouter>
        <ProtectedRoute>
          <div>Protected Content</div>
        </ProtectedRoute>
      </MemoryRouter>
    );

    expect(screen.getByText('Protected Content')).toBeInTheDocument();
  });

  it('does not render children if auth_token is missing (redirect)', () => {
    (Cookies.get as any).mockReturnValue(undefined);

    render(
      <MemoryRouter>
        <ProtectedRoute>
          <div>Protected Content</div>
        </ProtectedRoute>
      </MemoryRouter>
    );

    expect(screen.queryByText('Protected Content')).not.toBeInTheDocument();
  });
});


it('redirects to "" if auth_token is not present', () => {

  render(
    <MemoryRouter>
      <ProtectedRoute>
        <div>Protected Content</div>
      </ProtectedRoute>
    </MemoryRouter>
  );

  expect(screen.queryByText('Protected Content')).not.toBeInTheDocument();
});


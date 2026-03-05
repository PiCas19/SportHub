import {describe, it, expect} from 'vitest';
import {render, screen} from '@testing-library/react';
import {MemoryRouter} from 'react-router-dom';
import Sidebar from '../components/Sidebar';

describe('Sidebar', () => {
  it('renders all navigation links when open is true', () => {
    render(
      <MemoryRouter>
        <Sidebar open={true}/>
      </MemoryRouter>
    );

    expect(screen.getByText('SportHub')).toBeInTheDocument();
    expect(screen.getByText('Home')).toBeInTheDocument();
    expect(screen.getByText('Activities')).toBeInTheDocument();
    expect(screen.getByText('Goals')).toBeInTheDocument();
    expect(screen.getByText('Telegram')).toBeInTheDocument();
    expect(screen.getByText('Challenges')).toBeInTheDocument();
    expect(screen.getByText('Impostazioni')).toBeInTheDocument();
  });

  it('does not render when open is false', () => {
    const {container} = render(
      <MemoryRouter>
        <Sidebar open={false}/>
      </MemoryRouter>
    );

    // Drawer has display: none, so we expect the content not to be visible
    const drawer = container.querySelector('.MuiDrawer-root');
    expect(drawer).not.toBeVisible();
  });

  it('links direct to the correct paths', () => {
    render(
      <MemoryRouter>
        <Sidebar open={true}/>
      </MemoryRouter>
    );

    expect(screen.getByRole('link', {name: 'SportHub'})).toHaveAttribute('href', '/home');
    expect(screen.getByRole('link', {name: 'Home'})).toHaveAttribute('href', '/home');
    expect(screen.getByRole('link', {name: 'Activities'})).toHaveAttribute('href', '/activities');
    expect(screen.getByRole('link', {name: 'Goals'})).toHaveAttribute('href', '/goals');
    expect(screen.getByRole('link', {name: 'Telegram'})).toHaveAttribute('href', '/telegramGroup');
    expect(screen.getByRole('link', {name: 'Challenges'})).toHaveAttribute('href', '/challenges');
    expect(screen.getByRole('link', {name: 'Impostazioni'})).toHaveAttribute('href', '/settings');
  });
});

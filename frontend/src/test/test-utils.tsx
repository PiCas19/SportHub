import {render} from '@testing-library/react';
import {MemoryRouter} from 'react-router-dom';
import React from "react";

const renderWithRouter = (ui: React.ReactElement, {route = '/'} = {}) => {
  window.history.pushState({}, 'Test page', route);
  return render(<MemoryRouter initialEntries={[route]}>{ui}</MemoryRouter>);
};

export * from '@testing-library/react';
export {renderWithRouter};

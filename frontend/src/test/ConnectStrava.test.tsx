import { render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import ConnectStrava from "../screen/stravaConnectScreen/ConnectStrava";
import axios from "axios";
import Cookies from "js-cookie";
import { vi } from "vitest";

// Mock axios and cookies
vi.mock("axios");
vi.mock("js-cookie");

const mockAxios = axios as any;

function renderWithRouter(code?: string) {
  const url = code ? `/connect-strava?code=${code}` : "/connect-strava";
  return render(
    <MemoryRouter initialEntries={[url]}>
      <Routes>
        <Route path="/connect-strava" element={<ConnectStrava />} />
        <Route path="/home" element={<div>Home Page</div>} />
      </Routes>
    </MemoryRouter>
  );
}

describe("ConnectStrava", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("shows error if no code is provided", async () => {
    renderWithRouter();
    await waitFor(() => {
      expect(screen.getByText(/no authorization code/i)).toBeInTheDocument();
    });
  });

  it("shows error if no auth_token cookie is present", async () => {
    (Cookies.get as any).mockReturnValue(undefined);
    renderWithRouter("valid-code");
    await waitFor(() => {
      expect(screen.getByText(/authentication required/i)).toBeInTheDocument();
    });
  });

  it("shows error if axios post fails", async () => {
    (Cookies.get as any).mockReturnValue("mocked_token");
    mockAxios.post.mockRejectedValueOnce(new Error("Request failed"));
    renderWithRouter("valid-code");

    await waitFor(() => {
      expect(screen.getByText(/request failed/i)).toBeInTheDocument();
    });
  });

  it("redirects on success", async () => {
    (Cookies.get as any).mockReturnValue("mocked_token");
    mockAxios.post.mockResolvedValue({ status: 200 });

    renderWithRouter("valid-code");

    await waitFor(() => {
      expect(screen.getByText(/home page/i)).toBeInTheDocument();
    });
  });
});

import { render, screen, waitFor } from "@testing-library/react";
import PublicMapPage from "../screen/map/Map";
import { vi } from "vitest";
import axios from "axios";
import { MemoryRouter, Route, Routes } from "react-router-dom";

// Mock Leaflet
vi.mock("react-leaflet", async () => {
  const actual = await vi.importActual("react-leaflet");
  return {
    ...actual,
    MapContainer: ({ children }: any) => <div data-testid="map">{children}</div>,
    TileLayer: () => null,
    Marker: () => <div data-testid="marker" />,
    Polyline: () => <div data-testid="polyline" />,
    Popup: () => <div>Popup</div>,
  };
});

// Mock axios
vi.mock("axios");
const mockedAxios = axios as any;

const renderWithToken = (token: string) => {
  return render(
    <MemoryRouter initialEntries={[`/public/${token}`]}>
      <Routes>
        <Route path="/public/:token" element={<PublicMapPage />} />
      </Routes>
    </MemoryRouter>
  );
};

describe("PublicMapPage", () => {
  afterEach(() => {
    vi.clearAllMocks();
  });

  it("renders map on successful API call", async () => {
    mockedAxios.get.mockResolvedValueOnce({
      data: {
        targetLat: 46.01,
        targetLon: 8.96,
        location: "Lugano",
        segments: [
          { polyline: [[46.01, 8.96], [46.02, 8.97]] },
          { polyline: [[46.03, 8.98], [46.04, 8.99]] }
        ]
      }
    });

    renderWithToken("abc123");

    await waitFor(() => {
      expect(screen.getByText(/Attività vicino a Lugano/i)).toBeInTheDocument();
      expect(screen.getAllByTestId("polyline")).toHaveLength(2);
      expect(screen.getByTestId("marker")).toBeInTheDocument();
    });
  });

  it("renders error message if API fails", async () => {
    mockedAxios.get.mockRejectedValueOnce(new Error("Errore"));

    renderWithToken("invalid-token");

    await waitFor(() => {
      expect(screen.getByText(/mappa non trovata/i)).toBeInTheDocument();
    });
  });
});

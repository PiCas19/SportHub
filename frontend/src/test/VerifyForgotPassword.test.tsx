import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import VerifyForgotPassword from "../screen/verify/VerifyForgotPassword";
import axios from "axios";
import { vi } from "vitest";

// Mock axios
vi.mock("axios");
const mockAxios = axios as any;

// Mock CustomSnackbar to avoid rendering issues
vi.mock("../../components/CustomSnackbar.tsx", () => ({
  default: ({ open, message, severity }: any) => open ? <div>{`${severity}: ${message}`}</div> : null
}));

describe("VerifyForgotPassword", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders form correctly", () => {
    render(<VerifyForgotPassword />);
    expect(screen.getByText(/recupera la tua password/i)).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /invia link di recupero/i })).toBeInTheDocument();
  });

  it("shows error snackbar if request fails", async () => {
    mockAxios.post.mockRejectedValueOnce(new Error("Server error"));
    render(<VerifyForgotPassword />);

    fireEvent.change(screen.getByLabelText(/email/i), {
      target: { value: "test@example.com" }
    });
    fireEvent.click(screen.getByRole("button", { name: /invia link di recupero/i }));

    await waitFor(() => {
      expect(screen.getByText(/Qualcosa è andato storto/i)).toBeInTheDocument();
    });
  });

  it("shows success message after form submission", async () => {
    mockAxios.post.mockResolvedValueOnce({ status: 200 });
    render(<VerifyForgotPassword />);

    fireEvent.change(screen.getByLabelText(/email/i), {
      target: { value: "user@example.com" }
    });
    fireEvent.click(screen.getByRole("button", { name: /invia link di recupero/i }));

    await waitFor(() => {
      expect(screen.getByText(/controlla la tua email/i)).toBeInTheDocument();
      expect(screen.getByText(/user@example.com/i)).toBeInTheDocument();
    });
  });

  it("allows to go back from success message", async () => {
    mockAxios.post.mockResolvedValueOnce({ status: 200 });
    render(<VerifyForgotPassword />);

    fireEvent.change(screen.getByLabelText(/email/i), {
      target: { value: "user@example.com" }
    });
    fireEvent.click(screen.getByRole("button", { name: /invia link di recupero/i }));

    await waitFor(() => {
      expect(screen.getByText(/controlla la tua email/i)).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole("button", { name: /torna indietro/i }));

    expect(screen.getByText(/recupera la tua password/i)).toBeInTheDocument();
  });
});

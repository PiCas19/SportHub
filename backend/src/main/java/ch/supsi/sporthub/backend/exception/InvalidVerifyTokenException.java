package ch.supsi.sporthub.backend.exception;

public class InvalidVerifyTokenException extends RuntimeException {
  public InvalidVerifyTokenException(String message) {
    super(message);
  }
}
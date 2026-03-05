package ch.supsi.sporthub.backend.dto.request;

import lombok.Data;

/**
 * Abstract base class for all request DTOs (Data Transfer Objects).
 * <p>
 * This class can be extended by specific request classes to ensure a common
 * type hierarchy and enable shared behaviors, validations, or annotations
 * in the future.
 */
@Data
public abstract class Request {
}
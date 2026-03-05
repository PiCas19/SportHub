package ch.supsi.sporthub.backend.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Request object for submitting or updating user metrics such as weight and height.
 * <p>
 * This data may be used for calculating health indicators or personalizing the user experience.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserMetricsRequest extends Request {
    private Double weight; 
    private Double height;
}
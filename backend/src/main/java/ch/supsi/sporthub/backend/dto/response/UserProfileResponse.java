package ch.supsi.sporthub.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Data
public class UserProfileResponse extends Response {
    private double weight;
    private double height;
    private String email;
    private String firstName;
    private String lastName;
    private String username;
}
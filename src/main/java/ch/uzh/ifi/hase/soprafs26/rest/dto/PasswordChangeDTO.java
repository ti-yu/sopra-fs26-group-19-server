package ch.uzh.ifi.hase.soprafs26.rest.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Body of POST /profile/{id}/change-password.
 * Both fields are required; validation lives in UserService.changePassword.
 */
@Getter
@Setter
public class PasswordChangeDTO {
    private String oldPassword;
    private String newPassword;
}

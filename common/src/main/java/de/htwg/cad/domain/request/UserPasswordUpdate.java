package de.htwg.cad.domain.request;

import lombok.*;
import org.springframework.lang.NonNull;

import javax.validation.constraints.NotBlank;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class UserPasswordUpdate {
    @NonNull
    @NotBlank(message = "username is mandatory")
    private String username;

    @NonNull
    @NotBlank(message = "new password is mandatory")
    private String password;

    @NonNull
    @NotBlank(message = "confirm password is mandatory")
    private String passwordConfirm;
}

package de.htwg.cad.domain.request;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Data
public class UserSignUp {
    @NotBlank
    @NotNull
    @Email
    private String email;

    @NotBlank
    @NotNull
    private String password;

    @NotBlank
    @NotNull
    private String firstName;

    @NotBlank
    @NotNull
    private String lastName;

    @NotBlank
    @NotNull
    private String subModel;

    @NotBlank
    @NotNull
    private String entranceDate;

    @NotNull
    @NotEmpty
    private Set<String> roles;

    @NotNull
    private String phone;

    @NotNull
    private String avatarUrl;
}

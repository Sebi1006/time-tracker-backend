package de.htwg.cad.domain.request;

import lombok.*;
import org.springframework.lang.NonNull;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.amazonaws.services.cognitoidp.model.*;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class UserAttributesUpdate {
    @NonNull
    @NotBlank(message = "token is mandatory")
    private String token;

    @NotNull
    @NotEmpty
    private Set<AttributeType> attributes;
}

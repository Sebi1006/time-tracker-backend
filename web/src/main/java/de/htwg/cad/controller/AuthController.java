package de.htwg.cad.controller;

import com.amazonaws.services.cognitoidp.model.AdminSetUserPasswordResult;
import com.amazonaws.services.cognitoidp.model.UpdateUserAttributesResult;
import com.amazonaws.services.cognitoidp.model.UserType;
import de.htwg.cad.domain.request.*;
import de.htwg.cad.domain.response.SuccessResponse;
import de.htwg.cad.exceptions.FailedAuthenticationException;
import de.htwg.cad.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(value = "/sign-up")
    public ResponseEntity<SuccessResponse> signUp(@RequestBody @Validated UserSignUp signUp) {
        UserType result = userService.createUser(signUp);
        return new ResponseEntity<>(new SuccessResponse(result, "User account created successfully."), HttpStatus.CREATED);
    }

    @PostMapping(value = "/register-premium")
    public ResponseEntity<SuccessResponse> registerPremium(@RequestBody @Validated RegisterPremium registerPremium) {
        String result = userService.registerPremium(registerPremium);
        return new ResponseEntity<>(new SuccessResponse(result, "Tenant instance created successfully."), HttpStatus.CREATED);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<SuccessResponse> login(@RequestBody @Validated Login loginRequest) {
        return new ResponseEntity<>(userService.authenticate(loginRequest), HttpStatus.OK);
    }

    @DeleteMapping("/logout")
    public ResponseEntity<SuccessResponse> logout(@RequestHeader("Authorization") String bearerToken) {
        if (bearerToken != null && bearerToken.contains("Bearer ")) {
            String accessToken = bearerToken.replace("Bearer ", "");
            userService.logout(accessToken);
            return new ResponseEntity<>(new SuccessResponse(null, "Logged out successfully."), HttpStatus.OK);
        }

        throw new FailedAuthenticationException("Invalid password.");
    }

    @PutMapping("/change-password")
    public ResponseEntity<SuccessResponse> changePassword(@RequestBody @Validated UserPasswordUpdate userPasswordUpdate) {
        AdminSetUserPasswordResult result = userService.updateUserPassword(userPasswordUpdate);
        return new ResponseEntity<>(new SuccessResponse(result, "Updated successfully."), HttpStatus.OK);
    }

    @PutMapping("/update-attributes")
    public ResponseEntity<SuccessResponse> updateAttributes(@RequestBody @Validated UserAttributesUpdate userAttributesUpdate) {
        UpdateUserAttributesResult result = userService.updateUserAttributes(userAttributesUpdate);
        return new ResponseEntity<>(new SuccessResponse(result, "Updated successfully."), HttpStatus.OK);
    }

    @GetMapping("/get-users")
    public List<UserType> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/get-organization")
    public String getOrganization() {
        return userService.getOrganizationName();
    }
}

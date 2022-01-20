package de.htwg.cad.service.impl;

import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthResult;
import com.amazonaws.services.cognitoidp.model.AdminSetUserPasswordResult;
import com.amazonaws.services.cognitoidp.model.UpdateUserAttributesResult;
import com.amazonaws.services.cognitoidp.model.UserType;
import de.htwg.cad.TenantContext;
import de.htwg.cad.domain.TenantMaster;
import de.htwg.cad.domain.model.CognitoCredentials;
import de.htwg.cad.domain.request.*;
import de.htwg.cad.domain.response.AuthenticatedResponse;
import de.htwg.cad.domain.response.SuccessResponse;
import de.htwg.cad.exceptions.TenantNotFoundException;
import de.htwg.cad.exceptions.UserNotFoundException;
import de.htwg.cad.service.CognitoUserService;
import de.htwg.cad.service.TenantMasterService;
import de.htwg.cad.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

import static com.amazonaws.services.cognitoidp.model.ChallengeNameType.NEW_PASSWORD_REQUIRED;

@RequiredArgsConstructor
@Slf4j
@Service
public class UserServiceImpl implements UserService {
    private final TenantMasterService tenantMasterService;

    private final CognitoUserService cognitoUserService;

    /**
     * @return CognitoCredentials
     */
    private CognitoCredentials extractCognitoCredentials() {
        Optional<TenantMaster> tenantMaster = tenantMasterService.getTenantById(TenantContext.getTenantId());

        if (tenantMaster.isPresent()) {
            return CognitoCredentials.builder()
                    .clientId(tenantMaster.get().getAppClientId())
                    .clientSecret(tenantMaster.get().getAppSecretId())
                    .poolId(tenantMaster.get().getUserPoolId())
                    .build();
        } else {
            throw new TenantNotFoundException("Tenant data not found or not activated.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SuccessResponse authenticate(Login userLogin) {
        CognitoCredentials credentials = extractCognitoCredentials();
        AdminInitiateAuthResult result = cognitoUserService.initiateAuth(userLogin.getUsername(), userLogin.getPassword(), credentials)
                .orElseThrow(() -> new UserNotFoundException(String.format("User name %s not found.", userLogin.getUsername())));

        // Password change required on first login
        if (ObjectUtils.nullSafeEquals(NEW_PASSWORD_REQUIRED.name(), result.getChallengeName())) {
            return new SuccessResponse(AuthenticatedChallenge.builder()
                    .challengeType(NEW_PASSWORD_REQUIRED.name())
                    .sessionId(result.getSession())
                    .username(userLogin.getUsername())
                    .build(), "First time login - Password change required.");
        }

        return new SuccessResponse(AuthenticatedResponse.builder()
                .accessToken(result.getAuthenticationResult().getAccessToken())
                .idToken(result.getAuthenticationResult().getIdToken())
                .refreshToken(result.getAuthenticationResult().getRefreshToken())
                .username(userLogin.getUsername())
                .build(), "Login successful.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logout(@NotNull String accessToken) {
        cognitoUserService.signOut(accessToken);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserType createUser(UserSignUp signUp) {
        CognitoCredentials credentials = extractCognitoCredentials();

        return cognitoUserService.signUp(signUp, credentials);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AdminSetUserPasswordResult updateUserPassword(UserPasswordUpdate userPassword) {
        CognitoCredentials credentials = extractCognitoCredentials();

        return cognitoUserService.changeUserPassword(userPassword.getUsername(), userPassword.getPassword(), credentials.getPoolId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UpdateUserAttributesResult updateUserAttributes(UserAttributesUpdate userAttributesUpdate) {
        return cognitoUserService.updateUserAttributes(userAttributesUpdate.getToken(), userAttributesUpdate.getAttributes());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UserType> getAllUsers() {
        CognitoCredentials credentials = extractCognitoCredentials();

        return cognitoUserService.getAllUsers(credentials.getPoolId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getOrganizationName() {
        Optional<TenantMaster> tenantMaster = tenantMasterService.getTenantById(TenantContext.getTenantId());

        if (tenantMaster.isPresent()) {
            return tenantMaster.get().getOrganizationName();
        } else {
            throw new TenantNotFoundException("Tenant data not found or not activated.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String registerPremium(RegisterPremium registerPremium) {
        return cognitoUserService.registerPremium(registerPremium);
    }
}

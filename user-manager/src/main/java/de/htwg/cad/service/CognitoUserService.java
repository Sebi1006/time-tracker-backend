package de.htwg.cad.service;

import com.amazonaws.services.cognitoidp.model.*;
import de.htwg.cad.domain.model.CognitoCredentials;
import de.htwg.cad.domain.request.RegisterPremium;
import de.htwg.cad.domain.request.UserSignUp;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CognitoUserService {
    /**
     * Authenticate Cognito User
     *
     * @param username user name
     * @param password user password
     * @return Optional<AdminInitiateAuthResult>
     */
    Optional<AdminInitiateAuthResult> initiateAuth(String username, String password, CognitoCredentials credentials);

    /**
     * @param username    user name
     * @param newPassword new user password
     * @param poolId      user pool id
     * @return AdminSetUserPasswordResult
     */
    AdminSetUserPasswordResult changeUserPassword(String username, String newPassword, String poolId);

    /**
     * @param token      access token
     * @param attributes updated user attributes
     * @return UpdateUserAttributesResult
     */
    UpdateUserAttributesResult updateUserAttributes(String token, Collection<AttributeType> attributes);

    /**
     * Add a group to user
     *
     * @param username  user name
     * @param groupName group name
     */
    void addUserToGroup(String username, String groupName, String poolId);

    /**
     * Creates a new user in the specified user pool.
     *
     * @param signUp      user info
     * @param credentials aws cognito credentials
     * @return UserType
     */
    UserType signUp(UserSignUp signUp, CognitoCredentials credentials);

    /**
     * Creates a new tenant instance.
     *
     * @param registerPremium user and tenant info
     * @return String
     */
    String registerPremium(RegisterPremium registerPremium);

    /**
     * Signs out users from all devices.
     *
     * @param accessToken access token
     * @return GlobalSignOutResult
     */
    GlobalSignOutResult signOut(String accessToken);

    /**
     * @param poolId user pool id
     * @return List<UserType>
     */
    List<UserType> getAllUsers(String poolId);
}

package de.htwg.cad.service;

import com.amazonaws.services.cognitoidp.model.AdminSetUserPasswordResult;
import com.amazonaws.services.cognitoidp.model.UpdateUserAttributesResult;
import com.amazonaws.services.cognitoidp.model.UserType;
import de.htwg.cad.domain.request.*;
import de.htwg.cad.domain.response.SuccessResponse;

import javax.validation.constraints.NotNull;
import java.util.List;

public interface UserService {
    /**
     * @param userLogin user login infos
     * @return BaseResponse
     */
    SuccessResponse authenticate(Login userLogin);

    /**
     * @param accessToken user authenticate access token
     */
    void logout(@NotNull String accessToken);

    /**
     * Creates a new user in the specified user pool.
     *
     * @param signUp user info
     * @return UserType
     */
    UserType createUser(UserSignUp signUp);

    /**
     * @param userPasswordUpdate user password request
     * @return AdminSetUserPasswordResult
     */
    AdminSetUserPasswordResult updateUserPassword(UserPasswordUpdate userPasswordUpdate);

    /**
     * @param userAttributesUpdate user attributes request
     * @return UpdateUserAttributesResult
     */
    UpdateUserAttributesResult updateUserAttributes(UserAttributesUpdate userAttributesUpdate);

    /**
     * @return List<UserType>
     */
    List<UserType> getAllUsers();

    /**
     * @return String
     */
    String getOrganizationName();

    /**
     * @param registerPremium user and tenant info
     * @return String
     */
    String registerPremium(RegisterPremium registerPremium);
}

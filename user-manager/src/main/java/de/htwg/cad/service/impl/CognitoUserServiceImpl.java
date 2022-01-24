package de.htwg.cad.service.impl;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.amplify.AWSAmplify;
import com.amazonaws.services.amplify.AWSAmplifyClient;
import com.amazonaws.services.amplify.model.*;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.*;
import com.amazonaws.services.cognitoidp.model.AdminAddUserToGroupRequest;
import com.amazonaws.services.cognitoidp.model.AdminCreateUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthRequest;
import com.amazonaws.services.cognitoidp.model.AdminSetUserPasswordRequest;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.amazonaws.services.cognitoidp.model.AuthFlowType;
import com.amazonaws.services.cognitoidp.model.DeliveryMediumType;
import com.amazonaws.services.cognitoidp.model.GlobalSignOutRequest;
import com.amazonaws.services.cognitoidp.model.InvalidPasswordException;
import com.amazonaws.services.cognitoidp.model.ListUsersRequest;
import com.amazonaws.services.cognitoidp.model.MessageActionType;
import com.amazonaws.services.cognitoidp.model.NotAuthorizedException;
import com.amazonaws.services.cognitoidp.model.UpdateUserAttributesRequest;
import com.amazonaws.services.cognitoidp.model.UserNotFoundException;
import com.amazonaws.services.cognitoidp.model.UserType;
import com.amazonaws.services.cognitoidp.model.UsernameExistsException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import de.htwg.cad.domain.enums.CognitoAttributesEnum;
import de.htwg.cad.domain.model.CognitoCredentials;
import de.htwg.cad.domain.request.RegisterEnterprise;
import de.htwg.cad.domain.request.RegisterPremium;
import de.htwg.cad.domain.request.UserSignUp;
import de.htwg.cad.exceptions.FailedAuthenticationException;
import de.htwg.cad.exceptions.ServiceException;
import de.htwg.cad.service.CognitoUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserConfigType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeDataType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CreateGroupRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CreateUserPoolClientRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CreateUserPoolRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ExplicitAuthFlowsType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.PasswordPolicyType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SchemaAttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.StringAttributeConstraintsType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserPoolMfaType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserPoolPolicyType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UsernameAttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UsernameConfigurationType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.PreventUserExistenceErrorTypes;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CreateUserPoolResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CreateUserPoolClientResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RequiredArgsConstructor
@Slf4j
@Service
public class CognitoUserServiceImpl implements CognitoUserService {
    @Value("${aws.access-key}")
    private String accessKey;

    @Value("${aws.secret-key}")
    private String secretKey;

    @Value("${aws.region}")
    private String region;

    @Value("${aws.endpoint}")
    private String endpoint;

    private final AWSCognitoIdentityProvider awsCognitoIdentityProvider;
    private final CognitoIdentityProviderClient cognitoClient;

    private static final String DEFAULT_PASSWORD = "p>K28fM9";

    /**
     * {@inheritDoc}
     */
    @Override
    public String registerPremium(RegisterPremium registerPremium) {
        try {
            String tenantName = registerPremium.getOrganizationName().replaceAll("\\s+", "-").toLowerCase();
            log.info("Creating user pool with tenant name: {}", tenantName);

            PasswordPolicyType passwordPolicyType = PasswordPolicyType.builder()
                    .minimumLength(8)
                    .requireLowercase(true)
                    .requireUppercase(true)
                    .requireNumbers(true)
                    .requireSymbols(true)
                    .temporaryPasswordValidityDays(7)
                    .build();

            UserPoolPolicyType userPoolPolicyType = UserPoolPolicyType.builder()
                    .passwordPolicy(passwordPolicyType)
                    .build();

            SchemaAttributeType email = getSchemaAttributeType("email", true, "1", "256");
            SchemaAttributeType firstName = getSchemaAttributeType("first_name", false, "1", "256");
            SchemaAttributeType lastName = getSchemaAttributeType("last_name", false, "1", "256");
            SchemaAttributeType subModel = getSchemaAttributeType("sub_model", false, "1", "256");
            SchemaAttributeType entranceDate = getSchemaAttributeType("entrance_date", false, "1", "256");
            SchemaAttributeType phone = getSchemaAttributeType("phone", false, "0", "256");
            SchemaAttributeType avatarUrl = getSchemaAttributeType("avatar_url", false, "0", "256");

            List<SchemaAttributeType> schemaAttributeTypes = new LinkedList<>();
            schemaAttributeTypes.add(email);
            schemaAttributeTypes.add(firstName);
            schemaAttributeTypes.add(lastName);
            schemaAttributeTypes.add(subModel);
            schemaAttributeTypes.add(entranceDate);
            schemaAttributeTypes.add(phone);
            schemaAttributeTypes.add(avatarUrl);

            CreateUserPoolResponse userPoolResponse = cognitoClient.createUserPool(
                    CreateUserPoolRequest.builder()
                            .poolName(tenantName)
                            .policies(userPoolPolicyType)
                            .usernameAttributes(UsernameAttributeType.EMAIL)
                            .mfaConfiguration(UserPoolMfaType.OFF)
                            .usernameConfiguration(UsernameConfigurationType.builder().caseSensitive(false).build())
                            .adminCreateUserConfig(AdminCreateUserConfigType.builder().allowAdminCreateUserOnly(false).build())
                            .schema(schemaAttributeTypes)
                            .build()
            );

            String userPoolId = userPoolResponse.userPool().id();
            log.info("Created user pool with id: {}", userPoolId);

            cognitoClient.createGroup(
                    CreateGroupRequest.builder()
                            .groupName("ROLE_USER")
                            .userPoolId(userPoolId)
                            .build()
            );

            cognitoClient.createGroup(
                    CreateGroupRequest.builder()
                            .groupName("ROLE_ADMIN")
                            .userPoolId(userPoolId)
                            .build()
            );

            List<ExplicitAuthFlowsType> explicitAuthFlowsTypes = new LinkedList<>();
            explicitAuthFlowsTypes.add(ExplicitAuthFlowsType.ALLOW_ADMIN_USER_PASSWORD_AUTH);
            explicitAuthFlowsTypes.add(ExplicitAuthFlowsType.ALLOW_CUSTOM_AUTH);
            explicitAuthFlowsTypes.add(ExplicitAuthFlowsType.ALLOW_USER_PASSWORD_AUTH);
            explicitAuthFlowsTypes.add(ExplicitAuthFlowsType.ALLOW_USER_SRP_AUTH);
            explicitAuthFlowsTypes.add(ExplicitAuthFlowsType.ALLOW_REFRESH_TOKEN_AUTH);

            CreateUserPoolClientResponse userPoolClientResponse = cognitoClient.createUserPoolClient(
                    CreateUserPoolClientRequest.builder()
                            .userPoolId(userPoolId)
                            .clientName("app-client-" + tenantName)
                            .generateSecret(true)
                            .refreshTokenValidity(90)
                            .accessTokenValidity(12)
                            .idTokenValidity(12)
                            .explicitAuthFlows(explicitAuthFlowsTypes)
                            .preventUserExistenceErrors(PreventUserExistenceErrorTypes.ENABLED)
                            .build()
            );

            String appClientId = userPoolClientResponse.userPoolClient().clientId();
            String appSecretId = userPoolClientResponse.userPoolClient().clientSecret();

            AWSStaticCredentialsProvider provider = new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey));

            AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                    .withCredentials(provider)
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region))
                    .build();

            DynamoDB dynamoDB = new DynamoDB(client);

            Table table = dynamoDB.getTable("tenant_master");

            table.putItem(new Item()
                    .withPrimaryKey("id", tenantName)
                    .withString("organizationName", registerPremium.getOrganizationName())
                    .withString("userPoolId", userPoolId)
                    .withString("appClientId", appClientId)
                    .withString("appSecretId", appSecretId)
                    .withJSON("creditCardInformation", registerPremium.getCreditCardInformation().toString()));

            log.info("Put tenant information in tenant_master table successfully.");

            AdminCreateUserRequest signUpRequest = new AdminCreateUserRequest()
                    .withUserPoolId(userPoolId)
                    .withTemporaryPassword(DEFAULT_PASSWORD)
                    .withDesiredDeliveryMediums(DeliveryMediumType.EMAIL)
                    .withUsername(registerPremium.getEmail())
                    .withMessageAction(MessageActionType.SUPPRESS)
                    .withUserAttributes(
                            new AttributeType().withName("custom:first_name").withValue(registerPremium.getFirstName()),
                            new AttributeType().withName("custom:last_name").withValue(registerPremium.getLastName()),
                            new AttributeType().withName("custom:sub_model").withValue(registerPremium.getSubModel()),
                            new AttributeType().withName("custom:phone").withValue(registerPremium.getPhone()),
                            new AttributeType().withName("custom:entrance_date").withValue(registerPremium.getEntranceDate()),
                            new AttributeType().withName("custom:avatar_url").withValue(registerPremium.getAvatarUrl()),
                            new AttributeType().withName("email").withValue(registerPremium.getEmail()),
                            new AttributeType().withName("email_verified").withValue("true"));

            AdminCreateUserResult createUserResult = awsCognitoIdentityProvider.adminCreateUser(signUpRequest);
            log.info("Created admin user with id: {}", createUserResult.getUser().getUsername());

            registerPremium.getRoles().forEach(r -> addUserToGroup(registerPremium.getEmail(), r, userPoolId));
            setUserPassword(registerPremium.getEmail(), registerPremium.getPassword(), userPoolId);

            Table workTable = dynamoDB.createTable(tenantName + "-work",
                    Arrays.asList(new KeySchemaElement("id", KeyType.HASH)),
                    Arrays.asList(new AttributeDefinition("id", ScalarAttributeType.S)),
                    new ProvisionedThroughput(10L, 10L));
            workTable.waitForActive();
            log.info("Created work table successfully. Table status: {}", workTable.getDescription().getTableStatus());

            Table tagTable = dynamoDB.createTable(tenantName + "-tag",
                    Arrays.asList(new KeySchemaElement("id", KeyType.HASH)),
                    Arrays.asList(new AttributeDefinition("id", ScalarAttributeType.S)),
                    new ProvisionedThroughput(10L, 10L));
            tagTable.waitForActive();
            log.info("Created tag table successfully. Table status: {}", tagTable.getDescription().getTableStatus());

            Table projectTable = dynamoDB.createTable(tenantName + "-project",
                    Arrays.asList(new KeySchemaElement("id", KeyType.HASH)),
                    Arrays.asList(new AttributeDefinition("id", ScalarAttributeType.S)),
                    new ProvisionedThroughput(10L, 10L));
            projectTable.waitForActive();
            log.info("Created project table successfully. Table status: {}", projectTable.getDescription().getTableStatus());

            Table userWorkTable = dynamoDB.createTable(tenantName + "-user-work",
                    Arrays.asList(new KeySchemaElement("id", KeyType.HASH)),
                    Arrays.asList(new AttributeDefinition("id", ScalarAttributeType.S)),
                    new ProvisionedThroughput(10L, 10L));
            userWorkTable.waitForActive();
            log.info("Created user-work table successfully. Table status: {}", userWorkTable.getDescription().getTableStatus());

            Table projectHoursTable = dynamoDB.createTable(tenantName + "-project-hours",
                    Arrays.asList(new KeySchemaElement("id", KeyType.HASH)),
                    Arrays.asList(new AttributeDefinition("id", ScalarAttributeType.S)),
                    new ProvisionedThroughput(10L, 10L));
            projectHoursTable.waitForActive();
            log.info("Created project-hours table successfully. Table status: {}", projectHoursTable.getDescription().getTableStatus());

            AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                    .withCredentials(provider)
                    .withRegion(Regions.EU_CENTRAL_1)
                    .build();

            s3.createBucket(tenantName + "-s3-bucket");
            log.info("Created S3 bucket successfully.");

            AWSAmplify amplify = AWSAmplifyClient.builder()
                    .withCredentials(provider)
                    .withRegion(Regions.EU_CENTRAL_1)
                    .build();

            Map<String, String> environmentVariables = new HashMap<>();
            environmentVariables.put("NEXT_PUBLIC_TENANT", tenantName);

            CreateAppRequest appRequest = new CreateAppRequest()
                    .withName(tenantName + "-frontend")
                    .withRepository("https://gitlab.com/t5172/frontend")
                    .withAccessToken("glpat-aLT1jKyTeLTJNh3deCpG")
                    .withBuildSpec("amplify.yml")
                    .withIamServiceRoleArn("arn:aws:iam::179849223048:role/amplifyconsole-backend-role")
                    .withEnableBranchAutoBuild(false)
                    .withEnableAutoBranchCreation(false)
                    .withEnvironmentVariables(environmentVariables);

            CreateAppResult appResult = amplify.createApp(appRequest);
            String amplifyAppId = appResult.getApp().getAppId();
            String domain = appResult.getApp().getDefaultDomain();

            CreateBranchRequest branchRequest = new CreateBranchRequest()
                    .withAppId(amplifyAppId)
                    .withBranchName("main");

            amplify.createBranch(branchRequest);

            StartJobRequest jobRequest = new StartJobRequest()
                    .withAppId(amplifyAppId)
                    .withBranchName("main")
                    .withJobType(JobType.RELEASE);

            StartJobResult jobResult = amplify.startJob(jobRequest);

            log.info("Building the amplify app. Current status: {}", jobResult.getJobSummary().getStatus());

            while (jobResult.getJobSummary().getStatus().equals("PENDING")) {
                Thread.sleep(60 * 1000);

                GetJobRequest getJobRequest = new GetJobRequest()
                        .withJobId(jobResult.getJobSummary().getJobId())
                        .withAppId(amplifyAppId)
                        .withBranchName("main");

                GetJobResult getJobResult = amplify.getJob(getJobRequest);

                log.info("Building the amplify app. Current status: {}", getJobResult.getJob().getSummary().getStatus());

                if (getJobResult.getJob().getSummary().getStatus().equals("SUCCEED")) {
                    break;
                }
            }

            return "https://main." + domain;
        } catch (CognitoIdentityProviderException e) {
            return e.awsErrorDetails().errorMessage();
        } catch (InterruptedException e) {
            return e.getMessage();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String registerEnterprise(RegisterEnterprise registerEnterprise) {
        try {
            String tenantName = registerEnterprise.getOrganizationName().replaceAll("\\s+", "-").toLowerCase();
            log.info("Creating user pool with tenant name: {}", tenantName);

            PasswordPolicyType passwordPolicyType = PasswordPolicyType.builder()
                    .minimumLength(8)
                    .requireLowercase(true)
                    .requireUppercase(true)
                    .requireNumbers(true)
                    .requireSymbols(true)
                    .temporaryPasswordValidityDays(7)
                    .build();

            UserPoolPolicyType userPoolPolicyType = UserPoolPolicyType.builder()
                    .passwordPolicy(passwordPolicyType)
                    .build();

            SchemaAttributeType email = getSchemaAttributeType("email", true, "1", "256");
            SchemaAttributeType firstName = getSchemaAttributeType("first_name", false, "1", "256");
            SchemaAttributeType lastName = getSchemaAttributeType("last_name", false, "1", "256");
            SchemaAttributeType subModel = getSchemaAttributeType("sub_model", false, "1", "256");
            SchemaAttributeType entranceDate = getSchemaAttributeType("entrance_date", false, "1", "256");
            SchemaAttributeType phone = getSchemaAttributeType("phone", false, "0", "256");
            SchemaAttributeType avatarUrl = getSchemaAttributeType("avatar_url", false, "0", "256");

            List<SchemaAttributeType> schemaAttributeTypes = new LinkedList<>();
            schemaAttributeTypes.add(email);
            schemaAttributeTypes.add(firstName);
            schemaAttributeTypes.add(lastName);
            schemaAttributeTypes.add(subModel);
            schemaAttributeTypes.add(entranceDate);
            schemaAttributeTypes.add(phone);
            schemaAttributeTypes.add(avatarUrl);

            CreateUserPoolResponse userPoolResponse = cognitoClient.createUserPool(
                    CreateUserPoolRequest.builder()
                            .poolName(tenantName)
                            .policies(userPoolPolicyType)
                            .usernameAttributes(UsernameAttributeType.EMAIL)
                            .mfaConfiguration(UserPoolMfaType.OFF)
                            .usernameConfiguration(UsernameConfigurationType.builder().caseSensitive(false).build())
                            .adminCreateUserConfig(AdminCreateUserConfigType.builder().allowAdminCreateUserOnly(false).build())
                            .schema(schemaAttributeTypes)
                            .build()
            );

            String userPoolId = userPoolResponse.userPool().id();
            log.info("Created user pool with id: {}", userPoolId);

            cognitoClient.createGroup(
                    CreateGroupRequest.builder()
                            .groupName("ROLE_USER")
                            .userPoolId(userPoolId)
                            .build()
            );

            cognitoClient.createGroup(
                    CreateGroupRequest.builder()
                            .groupName("ROLE_ADMIN")
                            .userPoolId(userPoolId)
                            .build()
            );

            List<ExplicitAuthFlowsType> explicitAuthFlowsTypes = new LinkedList<>();
            explicitAuthFlowsTypes.add(ExplicitAuthFlowsType.ALLOW_ADMIN_USER_PASSWORD_AUTH);
            explicitAuthFlowsTypes.add(ExplicitAuthFlowsType.ALLOW_CUSTOM_AUTH);
            explicitAuthFlowsTypes.add(ExplicitAuthFlowsType.ALLOW_USER_PASSWORD_AUTH);
            explicitAuthFlowsTypes.add(ExplicitAuthFlowsType.ALLOW_USER_SRP_AUTH);
            explicitAuthFlowsTypes.add(ExplicitAuthFlowsType.ALLOW_REFRESH_TOKEN_AUTH);

            CreateUserPoolClientResponse userPoolClientResponse = cognitoClient.createUserPoolClient(
                    CreateUserPoolClientRequest.builder()
                            .userPoolId(userPoolId)
                            .clientName("app-client-" + tenantName)
                            .generateSecret(true)
                            .refreshTokenValidity(90)
                            .accessTokenValidity(12)
                            .idTokenValidity(12)
                            .explicitAuthFlows(explicitAuthFlowsTypes)
                            .preventUserExistenceErrors(PreventUserExistenceErrorTypes.ENABLED)
                            .build()
            );

            String appClientId = userPoolClientResponse.userPoolClient().clientId();
            String appSecretId = userPoolClientResponse.userPoolClient().clientSecret();

            AWSStaticCredentialsProvider provider = new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey));

            AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                    .withCredentials(provider)
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region))
                    .build();

            DynamoDB dynamoDB = new DynamoDB(client);

            Table table = dynamoDB.getTable("tenant_master");

            table.putItem(new Item()
                    .withPrimaryKey("id", tenantName)
                    .withString("organizationName", registerEnterprise.getOrganizationName())
                    .withString("userPoolId", userPoolId)
                    .withString("appClientId", appClientId)
                    .withString("appSecretId", appSecretId)
                    .withInt("numberOfUsers", registerEnterprise.getNumberOfUsers())
                    .withJSON("creditCardInformation", registerEnterprise.getCreditCardInformation().toString()));

            log.info("Put tenant information in tenant_master table successfully.");

            AdminCreateUserRequest signUpRequest = new AdminCreateUserRequest()
                    .withUserPoolId(userPoolId)
                    .withTemporaryPassword(DEFAULT_PASSWORD)
                    .withDesiredDeliveryMediums(DeliveryMediumType.EMAIL)
                    .withUsername(registerEnterprise.getEmail())
                    .withMessageAction(MessageActionType.SUPPRESS)
                    .withUserAttributes(
                            new AttributeType().withName("custom:first_name").withValue(registerEnterprise.getFirstName()),
                            new AttributeType().withName("custom:last_name").withValue(registerEnterprise.getLastName()),
                            new AttributeType().withName("custom:sub_model").withValue(registerEnterprise.getSubModel()),
                            new AttributeType().withName("custom:phone").withValue(registerEnterprise.getPhone()),
                            new AttributeType().withName("custom:entrance_date").withValue(registerEnterprise.getEntranceDate()),
                            new AttributeType().withName("custom:avatar_url").withValue(registerEnterprise.getAvatarUrl()),
                            new AttributeType().withName("email").withValue(registerEnterprise.getEmail()),
                            new AttributeType().withName("email_verified").withValue("true"));

            AdminCreateUserResult createUserResult = awsCognitoIdentityProvider.adminCreateUser(signUpRequest);
            log.info("Created admin user with id: {}", createUserResult.getUser().getUsername());

            registerEnterprise.getRoles().forEach(r -> addUserToGroup(registerEnterprise.getEmail(), r, userPoolId));
            setUserPassword(registerEnterprise.getEmail(), registerEnterprise.getPassword(), userPoolId);

            Table workTable = dynamoDB.createTable(tenantName + "-work",
                    Arrays.asList(new KeySchemaElement("id", KeyType.HASH)),
                    Arrays.asList(new AttributeDefinition("id", ScalarAttributeType.S)),
                    new ProvisionedThroughput(10L, 10L));
            workTable.waitForActive();
            log.info("Created work table successfully. Table status: {}", workTable.getDescription().getTableStatus());

            Table tagTable = dynamoDB.createTable(tenantName + "-tag",
                    Arrays.asList(new KeySchemaElement("id", KeyType.HASH)),
                    Arrays.asList(new AttributeDefinition("id", ScalarAttributeType.S)),
                    new ProvisionedThroughput(10L, 10L));
            tagTable.waitForActive();
            log.info("Created tag table successfully. Table status: {}", tagTable.getDescription().getTableStatus());

            Table projectTable = dynamoDB.createTable(tenantName + "-project",
                    Arrays.asList(new KeySchemaElement("id", KeyType.HASH)),
                    Arrays.asList(new AttributeDefinition("id", ScalarAttributeType.S)),
                    new ProvisionedThroughput(10L, 10L));
            projectTable.waitForActive();
            log.info("Created project table successfully. Table status: {}", projectTable.getDescription().getTableStatus());

            Table userWorkTable = dynamoDB.createTable(tenantName + "-user-work",
                    Arrays.asList(new KeySchemaElement("id", KeyType.HASH)),
                    Arrays.asList(new AttributeDefinition("id", ScalarAttributeType.S)),
                    new ProvisionedThroughput(10L, 10L));
            userWorkTable.waitForActive();
            log.info("Created user-work table successfully. Table status: {}", userWorkTable.getDescription().getTableStatus());

            Table projectHoursTable = dynamoDB.createTable(tenantName + "-project-hours",
                    Arrays.asList(new KeySchemaElement("id", KeyType.HASH)),
                    Arrays.asList(new AttributeDefinition("id", ScalarAttributeType.S)),
                    new ProvisionedThroughput(10L, 10L));
            projectHoursTable.waitForActive();
            log.info("Created project-hours table successfully. Table status: {}", projectHoursTable.getDescription().getTableStatus());

            AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                    .withCredentials(provider)
                    .withRegion(Regions.EU_CENTRAL_1)
                    .build();

            s3.createBucket(tenantName + "-s3-bucket");
            log.info("Created S3 bucket successfully.");

            AWSAmplify amplify = AWSAmplifyClient.builder()
                    .withCredentials(provider)
                    .withRegion(Regions.EU_CENTRAL_1)
                    .build();

            Map<String, String> environmentVariables = new HashMap<>();
            environmentVariables.put("NEXT_PUBLIC_TENANT", tenantName);

            CreateAppRequest appRequest = new CreateAppRequest()
                    .withName(tenantName + "-frontend")
                    .withRepository("https://gitlab.com/t5172/frontend")
                    .withAccessToken("glpat-aLT1jKyTeLTJNh3deCpG")
                    .withBuildSpec("amplify.yml")
                    .withIamServiceRoleArn("arn:aws:iam::179849223048:role/amplifyconsole-backend-role")
                    .withEnableBranchAutoBuild(false)
                    .withEnableAutoBranchCreation(false)
                    .withEnvironmentVariables(environmentVariables);

            CreateAppResult appResult = amplify.createApp(appRequest);
            String amplifyAppId = appResult.getApp().getAppId();
            String domain = appResult.getApp().getDefaultDomain();

            CreateBranchRequest branchRequest = new CreateBranchRequest()
                    .withAppId(amplifyAppId)
                    .withBranchName("main");

            amplify.createBranch(branchRequest);

            StartJobRequest jobRequest = new StartJobRequest()
                    .withAppId(amplifyAppId)
                    .withBranchName("main")
                    .withJobType(JobType.RELEASE);

            StartJobResult jobResult = amplify.startJob(jobRequest);

            log.info("Building the amplify app. Current status: {}", jobResult.getJobSummary().getStatus());

            while (jobResult.getJobSummary().getStatus().equals("PENDING")) {
                Thread.sleep(60 * 1000);

                GetJobRequest getJobRequest = new GetJobRequest()
                        .withJobId(jobResult.getJobSummary().getJobId())
                        .withAppId(amplifyAppId)
                        .withBranchName("main");

                GetJobResult getJobResult = amplify.getJob(getJobRequest);

                log.info("Building the amplify app. Current status: {}", getJobResult.getJob().getSummary().getStatus());

                if (getJobResult.getJob().getSummary().getStatus().equals("SUCCEED")) {
                    break;
                }
            }

            return "https://main." + domain;
        } catch (CognitoIdentityProviderException e) {
            return e.awsErrorDetails().errorMessage();
        } catch (InterruptedException e) {
            return e.getMessage();
        }
    }

    private SchemaAttributeType getSchemaAttributeType(String name, boolean required, String minLength, String maxLength) {
        return SchemaAttributeType.builder()
                .name(name)
                .attributeDataType(AttributeDataType.STRING)
                .developerOnlyAttribute(false)
                .mutable(true)
                .required(required)
                .stringAttributeConstraints(StringAttributeConstraintsType.builder().minLength(minLength).maxLength(maxLength).build())
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserType signUp(UserSignUp userSignUp, CognitoCredentials credentials) {
        try {
            final AdminCreateUserRequest signUpRequest = new AdminCreateUserRequest()
                    .withUserPoolId(credentials.getPoolId())
                    // The user's temporary password.
                    .withTemporaryPassword(DEFAULT_PASSWORD)
                    // Specify "EMAIL" if email will be used to send the welcome message
                    .withDesiredDeliveryMediums(DeliveryMediumType.EMAIL)
                    .withUsername(userSignUp.getEmail())
                    .withMessageAction(MessageActionType.SUPPRESS)
                    .withUserAttributes(
                            new AttributeType().withName("custom:first_name").withValue(userSignUp.getFirstName()),
                            new AttributeType().withName("custom:last_name").withValue(userSignUp.getLastName()),
                            new AttributeType().withName("custom:sub_model").withValue(userSignUp.getSubModel()),
                            new AttributeType().withName("custom:phone").withValue(userSignUp.getPhone()),
                            new AttributeType().withName("custom:entrance_date").withValue(userSignUp.getEntranceDate()),
                            new AttributeType().withName("custom:avatar_url").withValue(userSignUp.getAvatarUrl()),
                            new AttributeType().withName("email").withValue(userSignUp.getEmail()),
                            new AttributeType().withName("email_verified").withValue("true"));

            // create user
            AdminCreateUserResult createUserResult = awsCognitoIdentityProvider.adminCreateUser(signUpRequest);
            log.info("Created user id: {}", createUserResult.getUser().getUsername());

            // assign the roles
            userSignUp.getRoles().forEach(r -> addUserToGroup(userSignUp.getEmail(), r, credentials.getPoolId()));

            // set permanent password
            setUserPassword(userSignUp.getEmail(), userSignUp.getPassword(), credentials.getPoolId());

            return createUserResult.getUser();
        } catch (com.amazonaws.services.cognitoidp.model.UsernameExistsException e) {
            throw new UsernameExistsException("User name already exists.");
        } catch (com.amazonaws.services.cognitoidp.model.InvalidPasswordException e) {
            throw new InvalidPasswordException("Invalid password.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GlobalSignOutResult signOut(String accessToken) {
        try {
            return awsCognitoIdentityProvider.globalSignOut(new GlobalSignOutRequest().withAccessToken(accessToken));
        } catch (NotAuthorizedException e) {
            throw new FailedAuthenticationException(String.format("Logout failed: %s", e.getErrorMessage()), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addUserToGroup(String username, String groupName, String poolId) {
        try {
            // add user to group
            AdminAddUserToGroupRequest addUserToGroupRequest = new AdminAddUserToGroupRequest()
                    .withGroupName(groupName)
                    .withUserPoolId(poolId)
                    .withUsername(username);
            awsCognitoIdentityProvider.adminAddUserToGroup(addUserToGroupRequest);
        } catch (com.amazonaws.services.cognitoidp.model.InvalidPasswordException e) {
            throw new FailedAuthenticationException(String.format("Invalid parameter: %s", e.getErrorMessage()), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<AdminInitiateAuthResult> initiateAuth(String username, String password, CognitoCredentials credentials) {
        final Map<String, String> authParams = new HashMap<>();
        authParams.put(CognitoAttributesEnum.USERNAME.name(), username);
        authParams.put(CognitoAttributesEnum.PASSWORD.name(), password);
        authParams.put(CognitoAttributesEnum.SECRET_HASH.name(), calculateSecretHash(credentials.getClientId(), credentials.getClientSecret(), username));

        final AdminInitiateAuthRequest authRequest = new AdminInitiateAuthRequest()
                .withAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                .withClientId(credentials.getClientId())
                .withUserPoolId(credentials.getPoolId())
                .withAuthParameters(authParams);

        return adminInitiateAuthResult(authRequest);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AdminSetUserPasswordResult changeUserPassword(String username, String newPassword, String poolId) {
        return setUserPassword(username, newPassword, poolId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UpdateUserAttributesResult updateUserAttributes(String token, Collection<AttributeType> attributes) {
        try {
            UpdateUserAttributesRequest updateUserAttributesRequest = new UpdateUserAttributesRequest()
                    .withAccessToken(token)
                    .withUserAttributes(attributes);

            return awsCognitoIdentityProvider.updateUserAttributes(updateUserAttributesRequest);
        } catch (NotAuthorizedException e) {
            throw new NotAuthorizedException("User is not authenticated: " + e.getErrorMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UserType> getAllUsers(String poolId) {
        ListUsersRequest request = new ListUsersRequest();
        request.withUserPoolId(poolId);

        ListUsersResult result = awsCognitoIdentityProvider.listUsers(request);

        return result.getUsers();
    }

    private Optional<AdminInitiateAuthResult> adminInitiateAuthResult(AdminInitiateAuthRequest request) {
        try {
            return Optional.of(awsCognitoIdentityProvider.adminInitiateAuth(request));
        } catch (NotAuthorizedException e) {
            throw new FailedAuthenticationException(String.format("Authenticate failed: %s", e.getErrorMessage()), e);
        } catch (UserNotFoundException e) {
            String username = request.getAuthParameters().get(CognitoAttributesEnum.USERNAME.name());
            throw new de.htwg.cad.exceptions.UserNotFoundException(String.format("User name %s not found.", username), e);
        }
    }

    private AdminSetUserPasswordResult setUserPassword(String username, String password, String poolId) {
        try {
            // Sets the specified user's password in a user pool as an administrator. Works on any user.
            AdminSetUserPasswordRequest adminSetUserPasswordRequest = new AdminSetUserPasswordRequest()
                    .withUsername(username)
                    .withPassword(password)
                    .withUserPoolId(poolId)
                    .withPermanent(true);

            return awsCognitoIdentityProvider.adminSetUserPassword(adminSetUserPasswordRequest);
        } catch (com.amazonaws.services.cognitoidp.model.InvalidPasswordException e) {
            throw new FailedAuthenticationException(String.format("Invalid parameter: %s", e.getErrorMessage()), e);
        }
    }

    private String calculateSecretHash(String userPoolClientId, String userPoolClientSecret, String userName) {
        final String HMAC_SHA256_ALGORITHM = "HmacSHA256";

        SecretKeySpec signingKey = new SecretKeySpec(
                userPoolClientSecret.getBytes(StandardCharsets.UTF_8),
                HMAC_SHA256_ALGORITHM);

        try {
            Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
            mac.init(signingKey);
            mac.update(userName.getBytes(StandardCharsets.UTF_8));
            byte[] rawHmac = mac.doFinal(userPoolClientId.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception e) {
            throw new ServiceException("Error while calculating.");
        }
    }
}

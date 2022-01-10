package de.htwg.cad.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.*;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import de.htwg.cad.TenantContext;
import de.htwg.cad.domain.UserWork;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserWorkServiceImpl implements UserWorkService {
    @Value("${aws.access-key}")
    private String accessKey;

    @Value("${aws.secret-key}")
    private String secretKey;

    @Value("${aws.region}")
    private String region;

    @Value("${aws.endpoint}")
    private String endpoint;

    private DynamoDBMapper dynamoDBMapper() {
        AWSStaticCredentialsProvider provider = new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey));

        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withCredentials(provider)
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region))
                .build();

        return new DynamoDBMapper(client);
    }

    private DynamoDBMapperConfig dynamoDBMapperConfig() {
        String prefix = TenantContext.getTenantId() + "-";
        TableNameOverride tableNameOverride = new TableNameOverride("user-work").withTableNamePrefix(prefix);

        return new DynamoDBMapperConfig.Builder().withTableNameOverride(tableNameOverride).build();
    }

    @Override
    public UserWork create(UserWork userWork) {
        dynamoDBMapper().save(userWork, dynamoDBMapperConfig());
        return userWork;
    }

    @Override
    public UserWork getById(String id) {
        return dynamoDBMapper().load(UserWork.class, id, dynamoDBMapperConfig());
    }

    @Override
    public List<UserWork> getAll() {
        return dynamoDBMapper().scan(UserWork.class, new DynamoDBScanExpression(), dynamoDBMapperConfig());
    }

    @Override
    public UserWork update(UserWork userWork, String id) {
        UserWork updatedUserWork = dynamoDBMapper().load(UserWork.class, id, dynamoDBMapperConfig());
        updatedUserWork.setWorks(userWork.getWorks());
        dynamoDBMapper().save(updatedUserWork, dynamoDBMapperConfig());
        return updatedUserWork;
    }
}

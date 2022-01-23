package de.htwg.cad.domain;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@DynamoDBTable(tableName = "tenant_master")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantMaster {
    @DynamoDBHashKey(attributeName = "id")
    @DynamoDBAutoGeneratedKey
    private String id;

    @DynamoDBAttribute
    private String userPoolId;

    @DynamoDBAttribute
    private String appClientId;

    @DynamoDBAttribute
    private String appSecretId;

    @DynamoDBAttribute
    private String organizationName;

    @DynamoDBTypeConverted(converter = CreditCardInformationConverter.class)
    @DynamoDBAttribute
    private CreditCardInformation creditCardInformation;

    @DynamoDBAttribute
    private int numberOfUsers;
}

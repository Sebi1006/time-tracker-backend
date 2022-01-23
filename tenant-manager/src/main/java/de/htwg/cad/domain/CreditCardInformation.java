package de.htwg.cad.domain;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDBDocument
public class CreditCardInformation {
    @DynamoDBAttribute
    String nameOnCard;

    @DynamoDBAttribute
    String cardNumber;

    @DynamoDBAttribute
    String expirationDate;

    @DynamoDBAttribute
    String securityCode;
}

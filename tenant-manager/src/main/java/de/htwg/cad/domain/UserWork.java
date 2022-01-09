package de.htwg.cad.domain;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@DynamoDBTable(tableName = "user-work")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserWork {
    @DynamoDBHashKey(attributeName = "id")
    private String id;

    @DynamoDBTypeConvertedJson
    private Set<Work> works;
}

package de.htwg.cad.domain;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@DynamoDBTable(tableName = "project-hours")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectHours {
    @DynamoDBHashKey(attributeName = "id")
    private String id;

    @DynamoDBAttribute
    private String hours;
}

package de.htwg.cad.domain;

import lombok.*;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class WorkPackage {
    String project;

    double workingHours;

    String tag;

    String description;
}

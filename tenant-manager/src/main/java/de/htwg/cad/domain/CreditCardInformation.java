package de.htwg.cad.domain;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditCardInformation {
    String nameOnCard;

    int cardNumber;

    String expirationDate;

    int securityCode;
}

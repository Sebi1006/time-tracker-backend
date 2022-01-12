package de.htwg.cad.domain;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditCardInformation {
    String nameOnCard;

    String cardNumber;

    String expirationDate;

    int securityCode;
}

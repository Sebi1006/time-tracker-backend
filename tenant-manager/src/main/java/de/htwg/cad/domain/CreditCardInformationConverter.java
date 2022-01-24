package de.htwg.cad.domain;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;

public class CreditCardInformationConverter implements DynamoDBTypeConverter<String, CreditCardInformation> {
    @Override
    public String convert(CreditCardInformation object) {
        CreditCardInformation itemCreditCardInformation = (CreditCardInformation) object;
        String creditCardInformation = null;

        try {
            if (itemCreditCardInformation != null) {
                creditCardInformation = String.format("%s x %s x %s x %s",
                        itemCreditCardInformation.getNameOnCard(), itemCreditCardInformation.getCardNumber(),
                        itemCreditCardInformation.getExpirationDate(), itemCreditCardInformation.getSecurityCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return creditCardInformation;
    }

    @Override
    public CreditCardInformation unconvert(String s) {
        CreditCardInformation itemCreditCardInformation = new CreditCardInformation();

        try {
            if (s != null && s.length() != 0) {
                String[] data = s.split("x");
                itemCreditCardInformation.setNameOnCard(data[0].trim());
                itemCreditCardInformation.setCardNumber(data[1].trim());
                itemCreditCardInformation.setExpirationDate(data[2].trim());
                itemCreditCardInformation.setSecurityCode(data[3].trim());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return itemCreditCardInformation;
    }
}

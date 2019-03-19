package com.teste.rotinapagamento.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Gusttavo Henrique (gusttavohnssilva@gmail.com)
 * @since 19/03/19.
 */
public class AvailableLimitDTO {

    @JsonProperty("amount")
    private AmountDTO amount;

    public AvailableLimitDTO(AmountDTO amount) {
        this.amount = amount;
    }

    public AmountDTO getAmount() {
        return amount;
    }

    public void setAmount(AmountDTO amount) {
        this.amount = amount;
    }
}

package com.teste.rotinapagamento.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Gusttavo Henrique (gusttavohnssilva@gmail.com)
 * @since 19/03/19.
 */
public class AvailableLimitDTO {

    @JsonProperty("amount")
    private Double amount;

    public AvailableLimitDTO() {
    }

    @JsonIgnore
    public AvailableLimitDTO(Double amount) {
        this.amount = amount;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}

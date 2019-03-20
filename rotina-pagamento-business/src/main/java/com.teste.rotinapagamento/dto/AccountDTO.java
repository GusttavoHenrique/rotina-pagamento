package com.teste.rotinapagamento.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Gusttavo Henrique (gusttavohnssilva@gmail.com)
 * @since 19/03/19.
 */
public class AccountDTO {

    @JsonProperty("account_id")
    private Integer accountId;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty("available_credit_limit")
    private AvailableLimitDTO availableCreditLimit;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty("available_withdrawal_limit")
    private AvailableLimitDTO availableWithdrawalLimit;

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public AvailableLimitDTO getAvailableCreditLimit() {
        return availableCreditLimit;
    }

    public void setAvailableCreditLimit(AvailableLimitDTO availableCreditLimit) {
        this.availableCreditLimit = availableCreditLimit;
    }

    public AvailableLimitDTO getAvailableWithdrawalLimit() {
        return availableWithdrawalLimit;
    }

    public void setAvailableWithdrawalLimit(AvailableLimitDTO availableWithdrawalLimit) {
        this.availableWithdrawalLimit = availableWithdrawalLimit;
    }
}

package com.teste.rotinapagamento.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Gusttavo Henrique (gusttavohnssilva@gmail.com)
 * @since 19/03/19.
 */
public class AccountDTO {

    @JsonProperty("account_id")
    private Integer accountId;

    @JsonProperty("available_credit_limit")
    private AvailableLimitDTO availableCreditLimit;

    @JsonProperty("available_withdrawal_limit")
    private AvailableLimitDTO availableWithdrawalLimit;

    public AccountDTO() {
    }

    public AccountDTO(Integer accountId, AvailableLimitDTO availableCreditLimit, AvailableLimitDTO availableWithdrawalLimit) {
        this.accountId = accountId;
        this.availableCreditLimit = availableCreditLimit;
        this.availableWithdrawalLimit = availableWithdrawalLimit;
    }

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

package com.teste.rotinapagamento.util;

import com.teste.rotinapagamento.dto.AccountDTO;
import com.teste.rotinapagamento.dto.AvailableLimitDTO;

/**
 * @author Gusttavo Henrique (gusttavohnssilva@gmail.com)
 * @since 23/03/19.
 */
public class AccountBuilder {

    private Integer accountId;
    private AvailableLimitDTO availableCreditLimit;
    private AvailableLimitDTO availableWithdrawalLimit;
    private Double creditBalance;

    private void reset(){
        accountId = null;
        availableCreditLimit = null;
        availableWithdrawalLimit = null;
        creditBalance = null;
    }

    public AccountBuilder withAccountId(Integer accountId){
        this.accountId = accountId;
        return this;
    }

    public AccountBuilder withAvailableCreditLimit(Double amount){
        AvailableLimitDTO availableLimit = new AvailableLimitDTO();
        availableLimit.setAmount(amount);
        this.availableCreditLimit = availableLimit;
        return this;
    }

    public AccountBuilder withAvailableWithdrawalLimit(Double amount){
        AvailableLimitDTO availableLimit = new AvailableLimitDTO();
        availableLimit.setAmount(amount);
        this.availableWithdrawalLimit = availableLimit;
        return this;
    }

    public AccountDTO build() {
        AccountDTO account = new AccountDTO();
        account.setAccountId(accountId);
        account.setAvailableCreditLimit(availableCreditLimit);
        account.setAvailableWithdrawalLimit(availableWithdrawalLimit);
        reset();
        return account;
    }
}

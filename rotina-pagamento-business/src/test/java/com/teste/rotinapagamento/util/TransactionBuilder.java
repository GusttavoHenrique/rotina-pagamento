package com.teste.rotinapagamento.util;

import com.teste.rotinapagamento.dto.TransactionDTO;

/**
 * @author Gusttavo Henrique (gusttavohnssilva@gmail.com)
 * @since 23/03/19.
 */
public class TransactionBuilder {

    private Integer transactionId;
    private Integer accountId;
    private Integer operationTypeId;
    private Double amount;
    private Double balance;
    private Long eventDate;
    private Long dueDate;

    private void reset(){
        transactionId = null;
        accountId = null;
        operationTypeId = null;
        amount = null;
        balance = null;
        eventDate = null;
        dueDate = null;
    }

    public TransactionBuilder withTransactionId(Integer transactionId){
        this.transactionId = transactionId;
        return this;
    }

    public TransactionBuilder withAccountId(Integer accountId){
        this.accountId = accountId;
        return this;
    }

    public TransactionBuilder withOperationTypeId(Integer operationTypeId){
        this.operationTypeId = operationTypeId;
        return this;
    }

    public TransactionBuilder withAmount(Double amount){
        this.amount = amount;
        return this;
    }

    public TransactionBuilder withBalance(Double balance){
        this.balance = balance;
        return this;
    }

    public TransactionBuilder withEventDate(Long eventDate){
        this.eventDate = eventDate;
        return this;
    }

    public TransactionBuilder withDueDate(Long dueDate){
        this.dueDate = dueDate;
        return this;
    }

    public TransactionDTO build() {
        TransactionDTO transaction = new TransactionDTO();
        transaction.setAccountId(accountId);
        transaction.setTransactionId(transactionId);
        transaction.setAccountId(accountId);
        transaction.setOperationTypeId(operationTypeId);
        transaction.setAmount(amount);
        transaction.setBalance(balance);
        transaction.setEventDate(eventDate);
        transaction.setDueDate(dueDate);
        reset();
        return transaction;
    }
}

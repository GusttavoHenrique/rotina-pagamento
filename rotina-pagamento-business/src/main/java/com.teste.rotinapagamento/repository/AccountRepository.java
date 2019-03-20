package com.teste.rotinapagamento.repository;

import com.teste.rotinapagamento.auxiliar.OperationType;
import com.teste.rotinapagamento.dto.AccountDTO;
import com.teste.rotinapagamento.dto.AvailableLimitDTO;

import com.teste.rotinapagamento.dto.TransactionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gusttavo Henrique (gusttavohnssilva@gmail.com)
 * @since 19/03/19.
 */
@Repository
public class AccountRepository {

    @Autowired
    JdbcTemplate jdbcTemplate;

    /**
     * Realiza o update de contas na base de dados.
     *
     * @param accountId                      identificador da conta
     * @param availableCreditLimitAmount     limite de crédito disponível
     * @param availableWithdrawalLimitAmount limite de retirada disponível
     * @return
     */
    public AccountDTO updateAccount(Integer accountId, Double availableCreditLimitAmount, Double availableWithdrawalLimitAmount) {
        AccountDTO account = new AccountDTO();
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder();

        sql.append("UPDATE public.accounts ").append("SET ");

        boolean creditLimitNotNull = availableCreditLimitAmount != null && availableCreditLimitAmount > 0;
        if (creditLimitNotNull){
            sql.append("available_credit_limit=? ");
            params.add(availableCreditLimitAmount);
        }

        if (availableWithdrawalLimitAmount != null && availableWithdrawalLimitAmount > 0){
            sql.append(creditLimitNotNull ? "," : "").append(" available_with_drawal_limit=? ");
            params.add(availableWithdrawalLimitAmount);
        }

        sql.append("WHERE account_id=?;");
        params.add(accountId);

        try {
            account.setAccountId(accountId);
            account.setAvailableCreditLimit(new AvailableLimitDTO(availableCreditLimitAmount));
            account.setAvailableWithdrawalLimit(new AvailableLimitDTO(availableWithdrawalLimitAmount));
            jdbcTemplate.queryForObject(sql.toString(), params.toArray(), Integer.class);
        } catch (Exception e) {
            //TODO
        }

        return account;
    }

    /**
     * Abate o valor do pagamento no limite de crédito ou de retirada da conta.
     *
     * @param transaction
     */
    public void downPayment(TransactionDTO transaction, Double amount) {
        Integer accountId = transaction.getAccountId();
        if (transaction.getOperationTypeId() == OperationType.SAQUE) {
            updateAccount(accountId, null, amount);
        } else {
            updateAccount(accountId, amount, null);
        }
    }
}

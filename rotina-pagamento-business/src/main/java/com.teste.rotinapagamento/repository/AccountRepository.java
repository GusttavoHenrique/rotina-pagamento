package com.teste.rotinapagamento.repository;

import com.teste.rotinapagamento.dto.AccountDTO;
import com.teste.rotinapagamento.dto.AvailableLimitDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

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
     * @param accountId
     * @param availableCreditLimitAmount
     * @param availableWithdrawalLimitAmount
     * @return
     */
    public AccountDTO updateAccount(Integer accountId, Double availableCreditLimitAmount, Double availableWithdrawalLimitAmount) {
        AccountDTO account = new AccountDTO();
        String sql = "UPDATE public.accounts SET available_credit_limit=?, available_with_drawal_limit=? WHERE account_id=?;";

        try{
            account.setAccountId(accountId);
            account.setAvailableCreditLimit(new AvailableLimitDTO(availableCreditLimitAmount));
            account.setAvailableWithdrawalLimit(new AvailableLimitDTO(availableWithdrawalLimitAmount));
            jdbcTemplate.queryForObject(sql, new Object[]{availableCreditLimitAmount, availableWithdrawalLimitAmount, accountId}, );
        } catch (Exception e) {
            //TODO
        }

        return account;
    }
}

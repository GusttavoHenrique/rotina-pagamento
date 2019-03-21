package com.teste.rotinapagamento.repository;

import com.teste.rotinapagamento.auxiliar.OperationType;
import com.teste.rotinapagamento.dto.AccountDTO;
import com.teste.rotinapagamento.dto.AvailableLimitDTO;

import com.teste.rotinapagamento.dto.TransactionDTO;
import com.teste.rotinapagamento.exception.ResourceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
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
     * Busca na base de dados todas as contas registradas com os limites de crédito e saque atualizados.
     *
     * @return List<AccountDTO>
     */
    public List<AccountDTO> findAccounts(){
        String sql = "SELECT * FROM public.accounts";

        return jdbcTemplate.query(sql, new RowMapper<AccountDTO>() {
            @Override
            public AccountDTO mapRow(ResultSet resultSet, int i) throws SQLException {
                AccountDTO account = new AccountDTO();
                account.setAccountId(resultSet.getInt("account_id"));
                account.setAvailableCreditLimit(new AvailableLimitDTO(resultSet.getDouble("available_credit_limit")));
                account.setAvailableWithdrawalLimit(new AvailableLimitDTO(resultSet.getDouble("available_withdrawal_limit")));

                return account;
            }
        });
    }

    /**
     * Busca na base de dados uma conta que corresponda ao identificador passado.
     *
     * @param accountId identificador da conta
     * @return AccountDTO
     */
    public AccountDTO findAccount(Integer accountId){
        String sql = "SELECT * FROM public.accounts WHERE account_id=?";

        return jdbcTemplate.query(sql, new Object[] {accountId}, new ResultSetExtractor<AccountDTO>() {
            @Override
            public AccountDTO extractData(ResultSet resultSet) throws SQLException, DataAccessException {
                if(resultSet.next()){
                    AccountDTO account = new AccountDTO();
                    account.setAccountId(resultSet.getInt("account_id"));
                    account.setAvailableCreditLimit(new AvailableLimitDTO(resultSet.getDouble("available_credit_limit")));
                    account.setAvailableWithdrawalLimit(new AvailableLimitDTO(resultSet.getDouble("available_withdrawal_limit")));

                    return account;
                }

                return null;
            }
        });
    }

    /**
     * Realiza o update de contas na base de dados.
     *
     * @param accountId                      identificador da conta
     * @param availableCreditLimitAmount     limite de crédito disponível
     * @param availableWithdrawalLimitAmount limite de retirada disponível
     * @return
     */
    public AccountDTO updateAccount(Integer accountId, Double availableCreditLimitAmount, Double availableWithdrawalLimitAmount) {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder();

        sql.append("UPDATE public.accounts ").append("SET ");

        boolean creditLimitNotNull = availableCreditLimitAmount != null;
        if (creditLimitNotNull){
            sql.append("available_credit_limit=available_credit_limit + ? ");
            params.add(availableCreditLimitAmount);
        }

        if (availableWithdrawalLimitAmount != null){
            sql.append(creditLimitNotNull ? "," : "").append(" available_withdrawal_limit=available_withdrawal_limit + ? ");
            params.add(availableWithdrawalLimitAmount);
        }

        sql.append("WHERE account_id=?;");
        params.add(accountId);

        try {
            jdbcTemplate.queryForObject(sql.toString(), params.toArray(), Integer.class);
        } catch (Exception e) {
            throw new ResourceException(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro inesperado!");
        }

        return findAccount(accountId);
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

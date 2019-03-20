package com.teste.rotinapagamento.repository;

import com.teste.rotinapagamento.dto.TransactionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * @author Gusttavo Henrique (gusttavohnssilva@gmail.com)
 * @since 19/03/19.
 */
@Repository
public class TransactionRepository {

    @Autowired
    JdbcTemplate jdbcTemplate;

    public List<TransactionDTO> findTransactionsToDownPayment(Integer accountId) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT t.transaction_id, t.account_id, t.operation_type_id, t.amount, t.balance ")
                .append("FROM public.transactions t ")
                .append("JOIN public.operations_types ot ON (ot.operation_type_id = t.operation_type_id) ")
                .append("WHERE t.balance <> 0 ")
                .append(" AND t.account_id=? ")
                .append("ORDER BY ot.charge_order, t.event_date ");

        try{
            return jdbcTemplate.query(sql.toString(), new Object[]{accountId}, new RowMapper<TransactionDTO>() {
                @Override
                public TransactionDTO mapRow(ResultSet resultSet, int i) throws SQLException {
                    TransactionDTO transaction = new TransactionDTO();
                    transaction.setTransactionId(resultSet.getInt("transaction_id"));
                    transaction.setAccountId(resultSet.getInt("account_id"));
                    transaction.setOperationTypeId(resultSet.getInt("operation_type_id"));
                    transaction.setAmount(resultSet.getDouble("amount"));
                    transaction.setBalance(resultSet.getDouble("balance"));

                    return transaction;
                }
            });
        } catch (Exception e) {
            //TODO
            return null;
        }
    }

    /**
     * Insere transações na base de dados.
     *
     * @param accountId       identificador da conta
     * @param operationTypeId identificador do tipo de operação
     * @param amount          valor devido
     * @return TransactionDTO
     */
    public TransactionDTO insertTransaction(Integer accountId, Integer operationTypeId, Double amount) {
        TransactionDTO transaction = new TransactionDTO();
        String sql = "INSERT INTO public.transactions(transaction_id, account_id, operation_type_id, amount, balance, event_date, due_date) VALUES (?, ?, ?, ?, ?, ?, ?);";

        try {
            transaction.setTransactionId(getNextTransactionId());
            transaction.setAccountId(accountId);
            transaction.setOperationTypeId(operationTypeId);
            transaction.setAmount(amount);

            jdbcTemplate.queryForObject(sql, new Object[]{transaction.getTransactionId(), accountId, operationTypeId, amount, amount, new Date(), new Date()}, Integer.class);
        } catch (Exception e) {
            //TODO
        }

        return transaction;
    }

    /**
     * Atualiza o valor do balanço da transação na base de dados.
     *
     * @param transactionId
     * @param balance
     */
    public void updateBalanceByTransaction(Integer transactionId, Double balance) {
        String sql = "UPDATE public.transactions SET balance=? WHERE transaction_id=?;";

        try {
            jdbcTemplate.update(sql, new Object[]{balance, transactionId});
        } catch (Exception e) {
            //TODO
        }
    }

    /**
     * Captura o próximo indentificador disponível na tabela transactions.
     *
     * @return Integer
     */
    private Integer getNextTransactionId() {
        return jdbcTemplate.queryForObject("SELECT NEXTVAL('public.transactions_seq');", Integer.class);
    }
}

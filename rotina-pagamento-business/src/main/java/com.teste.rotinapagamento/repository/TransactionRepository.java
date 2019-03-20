package com.teste.rotinapagamento.repository;

import com.teste.rotinapagamento.dto.TransactionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author Gusttavo Henrique (gusttavohnssilva@gmail.com)
 * @since 19/03/19.
 */
@Repository
public class TransactionRepository {

    @Autowired
    JdbcTemplate jdbcTemplate;

    /**
     * Busca na base de dados uma conta que corresponda ao identificador passado.
     *
     * @param transactionId identificador da transação
     * @return TransactionDTO
     */
    public TransactionDTO findTransaction(Integer transactionId){
        String sql = "SELECT * FROM public.transactions WHERE transaction_id=?";

        return jdbcTemplate.query(sql, new Object[] {transactionId}, new ResultSetExtractor<TransactionDTO>() {
            @Override
            public TransactionDTO extractData(ResultSet resultSet) throws SQLException, DataAccessException {
                if(resultSet.next()){
                    TransactionDTO transaction = new TransactionDTO();
                    transaction.setTransactionId(resultSet.getInt("transaction_id"));
                    transaction.setAccountId(resultSet.getInt("account_id"));
                    transaction.setOperationTypeId(resultSet.getInt("operation_type_id"));
                    transaction.setAmount(resultSet.getDouble("amount"));
                    transaction.setBalance(resultSet.getDouble("balance"));

                    return transaction;
                }

                return null;
            }
        });
    }

    public List<TransactionDTO> findTransactionsToDownPayment(Integer accountId) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT t.transaction_id, t.account_id, t.operation_type_id, t.amount, t.balance ")
                .append("FROM public.transactions t ")
                .append("JOIN public.operations_types ot ON (ot.operation_type_id = t.operation_type_id) ")
                .append("WHERE t.balance <> 0 ")
                .append(" AND t.account_id=? ")
                .append("order by ot.charge_order ASC, t.event_date ASC ");

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
     * @return Integer
     */
    public Integer insertTransaction(Integer accountId, Integer operationTypeId, Double amount, Double balance) {
        Integer transactionId = null;
        String sql = "INSERT INTO public.transactions(transaction_id, account_id, operation_type_id, amount, balance, event_date, due_date) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);";

        try {
            transactionId = getNextTransactionId();
            jdbcTemplate.update(sql, new Object[]{transactionId, accountId, operationTypeId, amount, balance});
        } catch (Exception e) {
            //TODO
        }

        return transactionId;
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

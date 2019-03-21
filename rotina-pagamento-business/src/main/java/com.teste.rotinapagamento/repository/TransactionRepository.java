package com.teste.rotinapagamento.repository;

import com.teste.rotinapagamento.auxiliar.OperationType;
import com.teste.rotinapagamento.dto.TransactionDTO;
import com.teste.rotinapagamento.exception.ResourceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
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
    public TransactionDTO findTransaction(Integer transactionId, Integer accountId, Integer operationTypeId){
        StringBuilder sql = new StringBuilder("SELECT * FROM public.transactions WHERE 1=1 ");

        if(transactionId != null){
        	sql.append(" AND transaction_id=?");
        }

	    if(accountId != null){
		    sql.append(" AND account_id=?");
	    }

	    if(operationTypeId != null){
		    sql.append(" AND operation_type_id=?");
	    }

        return jdbcTemplate.query(sql.toString(), new Object[] {transactionId}, new ResultSetExtractor<TransactionDTO>() {
            @Override
            public TransactionDTO extractData(ResultSet resultSet) throws SQLException, DataAccessException {
                if(resultSet.next()){
                    TransactionDTO transaction = new TransactionDTO();
                    transaction.setTransactionId(resultSet.getInt("transaction_id"));
                    transaction.setAccountId(resultSet.getInt("account_id"));
                    transaction.setOperationTypeId(resultSet.getInt("operation_type_id"));
                    transaction.setAmount(resultSet.getDouble("amount"));
                    transaction.setBalance(resultSet.getDouble("balance"));

                    if(resultSet.getDate("event_date") != null)
                        transaction.setEventDate(resultSet.getDate("event_date").getTime());

                    if(resultSet.getDate("due_date") != null)
                        transaction.setDueDate(resultSet.getDate("due_date").getTime());

                    return transaction;
                }

                return null;
            }
        });
    }

    /**
     * Busca as transações que possuem balanços negativo para descontar pagamentos e positivos
     * para utilização no pagamento de outras transações.
     *
     * @param accountId identificador da conta
     * @return List<TransactionDTO>
     */
    public List<TransactionDTO> findTransactionsToDownPayment(Integer accountId) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT t.transaction_id, t.account_id, t.operation_type_id, t.amount, t.balance ")
                .append("FROM public.transactions t ")
                .append("JOIN public.operations_types ot ON (ot.operation_type_id = t.operation_type_id) ")
                .append("WHERE (t.balance <> 0 or (ot.operation_type_id = ? and t.balance <> 0)) ")
                .append(" AND t.account_id=? ")
                .append("order by ot.charge_order ASC, t.event_date ASC ");

        try{
            return jdbcTemplate.query(sql.toString(), new Object[]{OperationType.PAGAMENTO, accountId}, new RowMapper<TransactionDTO>() {
                @Override
                public TransactionDTO mapRow(ResultSet resultSet, int i) throws SQLException {
                    TransactionDTO transaction = new TransactionDTO();
                    transaction.setTransactionId(resultSet.getInt("transaction_id"));
                    transaction.setBalance(resultSet.getDouble("balance"));

                    return transaction;
                }
            });
        } catch (Exception e) {
            throw new ResourceException(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro inesperado!");
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
    public Integer insertTransaction(Integer accountId, Integer operationTypeId, Double amount, Double balance, Date dueDate) {
        Integer transactionId;
        String sql = "INSERT INTO public.transactions(transaction_id, account_id, operation_type_id, amount, balance, event_date, due_date) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?);";

        try {
            transactionId = getNextTransactionId();
            jdbcTemplate.update(sql, new Object[]{transactionId, accountId, operationTypeId, amount, balance, dueDate});
        } catch (Exception e) {
            throw new ResourceException(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro inesperado!");
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
        String sql = "UPDATE public.transactions SET balance=balance + ? WHERE transaction_id=?;";

        try {
            jdbcTemplate.update(sql, new Object[]{balance, transactionId});
        } catch (Exception e) {
            throw new ResourceException(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro inesperado!");
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

    /**
     * Indica se há credito credor na conta passada por parâmetro.
     *
     * @return boolean
     */
    public boolean hasCreditBalance(Integer accountId) {
        String sql = "SELECT SUM(balance) > 0 FROM public.transactions WHERE account_id=? AND operation_type_id=? GROUP BY account_id;";

        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{accountId, OperationType.PAGAMENTO}, Boolean.class);
        } catch (Exception e) {
            throw new ResourceException(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro inesperado!");
        }
    }
}

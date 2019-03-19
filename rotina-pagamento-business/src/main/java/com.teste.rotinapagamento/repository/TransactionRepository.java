package com.teste.rotinapagamento.repository;

import com.teste.rotinapagamento.dto.TransactionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Date;

/**
 * @author Gusttavo Henrique (gusttavohnssilva@gmail.com)
 * @since 19/03/19.
 */
@Repository
public class TransactionRepository {

    @Autowired
    JdbcTemplate jdbcTemplate;

    /**
     * Insere transações na base de dados.
     *
     * @param accountId identificador da conta
     * @param operationTypeId identificador do tipo de operação
     * @param amount valor devido
     * @return TransactionDTO
     */
    public TransactionDTO insertTransaction(Integer accountId, Integer operationTypeId, Double amount){
        TransactionDTO transaction = new TransactionDTO();
        String sql = "INSERT INTO public.transactions(transaction_id, account_id, operation_type_id, amount, balance, event_date, due_date) VALUES (?, ?, ?, ?, ?, ?, ?);";

        try{
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
     * Captura o próximo indentificador disponível na tabela transactions.
     *
     * @return Integer
     */
    private Integer getNextTransactionId(){
        return jdbcTemplate.queryForObject("SELECT NEXTVAL('public.transactions_seq');", Integer.class);
    }
}

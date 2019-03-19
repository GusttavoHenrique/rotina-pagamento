package com.teste.rotinapagamento.service;

import com.teste.rotinapagamento.dto.TransactionDTO;
import com.teste.rotinapagamento.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Gusttavo Henrique (gusttavohnssilva@gmail.com)
 * @since 19/03/19.
 */
@Service
@Transactional
public class TransactionService {

    @Autowired
    TransactionRepository transactionRepository;

    /**
     * Delega a operação de inserção para o método insert da classe repository.
     *
     * @param accountId identificador da conta
     * @param operationTypeId identificador do tipo de operação
     * @param amount valor devido
     * @return TransactionDTO
     */
    public TransactionDTO insertTransaction(Integer accountId, Integer operationTypeId, Double amount){
        return transactionRepository.insertTransaction(accountId, operationTypeId, amount);
    }
}

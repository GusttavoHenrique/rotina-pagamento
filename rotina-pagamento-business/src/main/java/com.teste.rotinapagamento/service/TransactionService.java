package com.teste.rotinapagamento.service;

import com.teste.rotinapagamento.auxiliar.OperationType;
import com.teste.rotinapagamento.dto.TransactionDTO;
import com.teste.rotinapagamento.repository.AccountRepository;
import com.teste.rotinapagamento.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gusttavo Henrique (gusttavohnssilva@gmail.com)
 * @since 19/03/19.
 */
@Service
@Transactional
public class TransactionService {

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    AccountRepository accountRepository;

    /**
     * Delega a operação de inserção de transações para o método insert da classe repository.
     *
     * @param transaction transação que será persistida
     * @return TransactionDTO
     */
    public TransactionDTO insertTransaction(TransactionDTO transaction) {
        if (transaction.getOperationTypeId() == OperationType.PAGAMENTO) return insertPayment(transaction);

        transaction = transactionRepository.insertTransaction(transaction.getAccountId(), transaction.getOperationTypeId(), transaction.getAmount());
        accountRepository.downPayment(transaction, transaction.getAmount());

        return transaction;
    }

    /**
     * Delega a inserção de cada um dos pagamentos da listagem recebida para o insert da classe repository.
     *
     * @param payments
     * @return List<TransactionDTO>
     */
    public List<TransactionDTO> insertPayments(List<TransactionDTO> payments) {
        List<TransactionDTO> transactions = new ArrayList<>();
        for (TransactionDTO payment : payments) {
            payment = insertPayment(payment);
            transactions.add(payment);
        }

        return transactions;
    }

    /**
     * Delega a inserção de uma transaction de pagamento para o insert da classe repository.
     *
     * @param payment transaction referente a um pagamento
     * @return TransactionDTO
     */
    private TransactionDTO insertPayment(TransactionDTO payment) {
        Double saldo = downPayment(payment);
        return transactionRepository.insertTransaction(payment.getAccountId(), OperationType.PAGAMENTO, saldo);
    }

    @Async
    protected Double downPayment(TransactionDTO payment) {
        Double paymentBalance = payment.getAmount();

        List<TransactionDTO> transactions = transactionRepository.findTransactionsToDownPayment(payment.getAccountId());
        for (TransactionDTO transaction : transactions) {
            if (paymentBalance <= 0) continue;

            Double transactionBalance = transaction.getBalance();
            accountRepository.downPayment(transaction, transactionBalance);

            transactionBalance += paymentBalance;
            transactionRepository.updateBalanceByTransaction(transaction.getTransactionId(), transactionBalance);

            paymentBalance += transaction.getBalance();
        }

        return (paymentBalance <= 0) ? 0.0 : paymentBalance;
    }
}

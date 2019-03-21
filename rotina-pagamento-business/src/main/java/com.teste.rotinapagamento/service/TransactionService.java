package com.teste.rotinapagamento.service;

import com.teste.rotinapagamento.auxiliar.OperationType;
import com.teste.rotinapagamento.dto.AccountDTO;
import com.teste.rotinapagamento.dto.TransactionDTO;
import com.teste.rotinapagamento.exception.ResourceException;
import com.teste.rotinapagamento.repository.AccountRepository;
import com.teste.rotinapagamento.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
     * Delega a inserção de transações para o método insert da classe repository.
     *
     * @param transaction transação que será persistida
     * @return TransactionDTO
     */
    public TransactionDTO insertTransaction(TransactionDTO transaction) {
        if (transaction.getOperationTypeId() == OperationType.PAGAMENTO) return insertPayment(transaction);

        transactionValidate(transaction);
        return insertCreditPurchaseOrWithdrawal(transaction);
    }

    /**
     * Delega a inserção de transações de compras ou saque para o método insert da classe repository.
     *
     * @param transaction
     * @return
     */
    private TransactionDTO insertCreditPurchaseOrWithdrawal(TransactionDTO transaction){
        Double balance = transaction.getAmount();
        Integer transactionId = transactionRepository.insertTransaction(transaction.getAccountId(), transaction.getOperationTypeId(), transaction.getAmount(), balance);
        accountRepository.downPayment(transaction, transaction.getAmount());

        return transactionRepository.findTransaction(transactionId);
    }

    /**
     * Delega a inserção de uma transaction de pagamento para o insert da classe repository.
     *
     * @param payment transaction referente a um pagamento
     * @return TransactionDTO
     */
    private TransactionDTO insertPayment(TransactionDTO payment) throws ResourceException {
        paymentValidate(payment);

        Double balance = downPayment(payment);
        Integer transactionId = transactionRepository.insertTransaction(payment.getAccountId(), OperationType.PAGAMENTO, payment.getAmount(), balance);

        return transactionRepository.findTransaction(transactionId);
    }

    /**
     * Delega a inserção de cada um dos pagamentos da listagem recebida para o insert da classe repository.
     *
     * @param payments
     * @return List<TransactionDTO>
     */
    public List<TransactionDTO> insertPayments(List<TransactionDTO> payments) throws ResourceException {
        List<TransactionDTO> transactions = new ArrayList<>();
        for (TransactionDTO payment : payments) {
            payment = insertPayment(payment);
            transactions.add(payment);
        }

        return transactions;
    }

    /**
     * Realiza o abatimento do pagamento nas transações pendentes de pagamento.
     *
     * @param payment
     * @return
     * @throws ResourceException
     */
    @Async
    protected Double downPayment(TransactionDTO payment) throws ResourceException {
        Double paymentBalance = payment.getAmount();

        List<TransactionDTO> transactions = transactionRepository.findTransactionsToDownPayment(payment.getAccountId());
        for (TransactionDTO transaction : transactions) {
            if(paymentBalance <= 0) break;

            Double transactionBalance = transaction.getBalance();
            Double downValue = 0.0;
            if(transactionBalance > 0) {
                paymentBalance += transactionBalance;
                downValue -= transactionBalance;
            } else {
                downValue = Math.abs(transactionBalance) >= Math.abs(paymentBalance) ? paymentBalance : Math.abs(transactionBalance);
            }

            accountRepository.downPayment(transaction, downValue);
            transactionRepository.updateBalanceByTransaction(transaction.getTransactionId(), downValue);

            paymentBalance -= downValue;
        }

        return paymentBalance;
    }

    /**
     * Método utilizado para realizar validações nos requests de transações de crédito ou saque.
     *
     * @param transaction transação de crédito ou saque
     */
    private void transactionValidate(TransactionDTO transaction){
        if(transaction.getAmount() >= 0)
            throw new ResourceException(HttpStatus.NOT_ACCEPTABLE, "Não é possível realizar cadastro de compras ou saques com valores nulos ou positivos.");

        AccountDTO account = accountRepository.findAccount(transaction.getAccountId());
        if(account == null || account.getAccountId() <= 0)
            throw new ResourceException(HttpStatus.NOT_ACCEPTABLE, "A operação não pode ser concluída porque a conta informada não existe.");

        if(account.getAvailableCreditLimit().getAmount() <= Math.abs(transaction.getAmount()))
            throw new ResourceException(HttpStatus.NOT_ACCEPTABLE, "A operação não pode ser concluída porque você não dispõe de limite de crédito suficiente.");

        if(account.getAvailableWithdrawalLimit().getAmount() <= Math.abs(transaction.getAmount()))
            throw new ResourceException(HttpStatus.NOT_ACCEPTABLE, "A operação não pode ser concluída porque você não dispõe de limite suficiente para saque.");
    }

    /**
     * Método utilizado para realizar validações nos requests de transações de pagamento.
     *
     * @param payment transação de pagamento
     */
    private void paymentValidate(TransactionDTO payment) {
        if (payment.getAmount() <= 0)
            throw new ResourceException(HttpStatus.NOT_ACCEPTABLE, "Não é possível realizar um pagamento com o valor nulo ou negativo.");

        AccountDTO account = accountRepository.findAccount(payment.getAccountId());
        if(account == null || account.getAccountId() <= 0)
            throw new ResourceException(HttpStatus.NOT_ACCEPTABLE, "A operação não pode ser concluída porque a conta informada não existe.");

        if(transactionRepository.hasCreditBalance(payment.getAccountId()))
            throw new ResourceException(HttpStatus.NOT_ACCEPTABLE, "Não é possível realizar um pagamento porque não há contas a pagar.");
    }

}

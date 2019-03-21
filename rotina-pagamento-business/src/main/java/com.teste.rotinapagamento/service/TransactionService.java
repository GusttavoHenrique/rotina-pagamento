package com.teste.rotinapagamento.service;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

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
		if (transaction.getOperationTypeId() == OperationType.PAGAMENTO.getId())
			return insertPayment(transaction);

		transactionValidate(transaction);
		Double newTransactionBalance = downCreditBalance(transaction);
		transaction.setBalance(newTransactionBalance);

		transaction = insertCreditPurchaseOrWithdrawal(transaction);
		return transaction;
	}

	/**
	 * Delega a inserção de transações de compras ou saque para o método insert da classe repository.
	 *
	 * @param transaction transação de compra ou saque
	 * @return TransactionDTO
	 */
	private TransactionDTO insertCreditPurchaseOrWithdrawal(TransactionDTO transaction) {
		Double balance = transaction.getAmount();
		Date dueDate = transaction.getDueDate() != null ? new Date(transaction.getDueDate()) : null;
		Integer transactionId = transactionRepository.insertTransaction(transaction.getAccountId(), transaction.getOperationTypeId(), transaction.getAmount(), balance, dueDate);
		accountRepository.updateLimitAccount(transaction, transaction.getBalance());

		return transactionRepository.findTransaction(transactionId, null, null);
	}

	/**
	 * Delega a inserção de uma transaction de pagamento para o insert da classe repository.
	 *
	 * @param payment transaction referente a um pagamento
	 * @return TransactionDTO
	 */
	private TransactionDTO insertPayment(TransactionDTO payment) throws ResourceException {
		paymentValidate(payment);

		Double balance = downPaymentInTransactions(payment);
		Integer transactionId = transactionRepository.insertTransaction(payment.getAccountId(), OperationType.PAGAMENTO.getId(), payment.getAmount(), balance, null);

		return transactionRepository.findTransaction(transactionId, null, null);
	}

	/**
	 * Delega a inserção de cada um dos pagamentos da listagem recebida para o insert da classe repository.
	 *
	 * @param payments transações de pagamento
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
	 * @param payment transação de pagamento
	 * @return Double
	 * @throws ResourceException
	 */
	private Double downPaymentInTransactions(TransactionDTO payment) throws ResourceException {
		Double paymentBalance = payment.getAmount();

		List<TransactionDTO> transactions = transactionRepository.findTransactionsToDownPayment(payment.getAccountId());
		for (TransactionDTO transaction : transactions) {
			if (paymentBalance <= 0) break;

			paymentBalance = downPaymentInTransactionBalance(transaction, paymentBalance);
		}

		return paymentBalance;
	}

	/**
	 * Realiza o abatimento do pagamento em uma transação pendente de pagamento.
	 *
	 * @param transaction transação que terá valor descontado
	 * @param paymentBalance valor do pagamento
	 * @return Double
	 */
	@Async
	protected Double downPaymentInTransactionBalance(TransactionDTO transaction, Double paymentBalance) {
		Double transactionBalance = transaction.getBalance();
		Double downValue = 0.0;
		if (transactionBalance > 0) {
			paymentBalance += transactionBalance;
			downValue -= transactionBalance;
		} else {
			downValue = Math.abs(transactionBalance) >= Math.abs(paymentBalance) ? paymentBalance : Math.abs(transactionBalance);
		}

		transactionRepository.updateBalanceByTransaction(transaction.getTransactionId(), downValue);
		accountRepository.updateLimitAccount(transaction, downValue);

		paymentBalance -= downValue;

		return paymentBalance;
	}

	/**
	 * Abate o saldo credor na trasação de compra ou saque em execução.
	 *
	 * @param transaction transação que terá valor descontado pelo saldo credor
	 * @return Double
	 */
	private Double downCreditBalance(TransactionDTO transaction) {
		TransactionDTO transactionWithCreditBalance = transactionRepository.findTransaction(null, transaction.getAccountId(), OperationType.PAGAMENTO.getId());
		Double newTransactionBalance = downTransactionBalanceInCreditBalance(transactionWithCreditBalance, transaction.getBalance());

		return newTransactionBalance;
	}

	/**
	 * Abate o balance da trasação no saldo credor da conta.
	 *
	 * @param transactionWithCreditBalance transação com o saldo credor da conta
	 * @param transactionBalance transação que terá o valor descontado
	 * @return Double
	 */
	@Async
	protected Double downTransactionBalanceInCreditBalance(TransactionDTO transactionWithCreditBalance, Double transactionBalance) {
		Double creditBalance = transactionWithCreditBalance.getBalance();
		Double downValue = Math.abs(transactionBalance) >= Math.abs(creditBalance) ? creditBalance : Math.abs(transactionBalance);

		transactionRepository.updateBalanceByTransaction(transactionWithCreditBalance.getTransactionId(), downValue);
		accountRepository.updateLimitAccount(transactionWithCreditBalance, downValue);

		transactionBalance += downValue;

		return transactionBalance;
	}

	/**
	 * Método utilizado para realizar validações nos requests de transações de crédito ou saque.
	 *
	 * @param transaction transação de crédito ou saque
	 */
	private void transactionValidate(TransactionDTO transaction) {
		if (transaction.getAmount() >= 0)
			throw new ResourceException(HttpStatus.NOT_ACCEPTABLE, "Não é possível realizar cadastro de compras ou saques com valores nulos ou positivos.");

		AccountDTO account = accountRepository.findAccount(transaction.getAccountId());
		if (account == null || account.getAccountId() <= 0)
			throw new ResourceException(HttpStatus.NOT_ACCEPTABLE, "A operação não pode ser concluída porque a conta informada não existe.");

		if (OperationType.isCompra(transaction.getOperationTypeId())
				&& account.getAvailableCreditLimit().getAmount() <= Math.abs(transaction.getAmount()))
			throw new ResourceException(HttpStatus.NOT_ACCEPTABLE, "A operação não pode ser concluída porque você não dispõe de limite de crédito suficiente.");

		if (OperationType.isSaque(transaction.getOperationTypeId())
				&& account.getAvailableWithdrawalLimit().getAmount() <= Math.abs(transaction.getAmount()))
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
		if (account == null || account.getAccountId() <= 0)
			throw new ResourceException(HttpStatus.NOT_ACCEPTABLE, "A operação não pode ser concluída porque a conta informada não existe.");

		if (transactionRepository.hasCreditBalance(payment.getAccountId()))
			throw new ResourceException(HttpStatus.NOT_ACCEPTABLE, "Não é possível realizar um pagamento porque não há contas a pagar.");
	}

}

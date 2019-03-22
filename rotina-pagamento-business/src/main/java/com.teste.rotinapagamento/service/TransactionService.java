package com.teste.rotinapagamento.service;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import com.teste.rotinapagamento.auxiliar.SourceMessage;
import com.teste.rotinapagamento.auxiliar.OperationType;
import com.teste.rotinapagamento.dto.AccountDTO;
import com.teste.rotinapagamento.dto.TransactionDTO;
import com.teste.rotinapagamento.exception.ResourceException;
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
	SourceMessage sourceMessage;

	@Autowired
	TransactionRepository transactionRepository;

	@Autowired
	AccountService accountService;

	/**
	 * Delega a inserção de transações para o método insert da classe repository.
	 *
	 * @param transaction transação que será persistida
	 * @return TransactionDTO
	 */
	public TransactionDTO insertTransaction(TransactionDTO transaction) {
		if (transaction.getOperationTypeId() == OperationType.PAGAMENTO.getId()) return insertPayment(transaction);

		transactionValidate(transaction);

		Double newTransactionBalance = downCreditBalance(transaction);
		if(newTransactionBalance != null){
			transaction.setBalance(newTransactionBalance);
		} else {
			transaction.setBalance(transaction.getAmount());
		}

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
		Double balance = transaction.getBalance();
		Date dueDate = transaction.getDueDate() != null ? new Date(transaction.getDueDate()) : null;
		Integer transactionId = transactionRepository.insertTransaction(transaction.getAccountId(), transaction.getOperationTypeId(), transaction.getAmount(), balance, dueDate);
		accountService.updateLimitAccount(transaction, balance);

		return transactionRepository.findTransaction(transactionId, null, null, null);
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

		return transactionRepository.findTransaction(transactionId, null, null, null);
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
		accountService.updateLimitAccount(transaction, downValue);

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
		TransactionDTO transactionWithCreditBalance = transactionRepository.findTransaction(null, transaction.getAccountId(), OperationType.PAGAMENTO.getId(), true);

		if(transactionWithCreditBalance == null) return null;

		Double transactionBalance = transaction.getAmount();
		Double newTransactionBalance = downTransactionBalanceInCreditBalance(transactionWithCreditBalance, transactionBalance);
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

		transactionRepository.updateBalanceByTransaction(transactionWithCreditBalance.getTransactionId(), downValue*(-1));
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
			throw new ResourceException(HttpStatus.NOT_ACCEPTABLE, sourceMessage.getMessage("transacao.cadastro.valor.nulo"));

		AccountDTO account = accountService.getAccount(transaction.getAccountId());
		if (account == null || account.getAccountId() <= 0)
			throw new ResourceException(HttpStatus.NOT_ACCEPTABLE, sourceMessage.getMessage("transacao.conta.inexistente"));

		Double totalCredit = getTotalCredit(account.getAccountId(), account.getAvailableCreditLimit().getAmount());
		if (OperationType.isCompra(transaction.getOperationTypeId()) && totalCredit < Math.abs(transaction.getAmount()))
			throw new ResourceException(HttpStatus.NOT_ACCEPTABLE, sourceMessage.getMessage("transacao.limite.credito.insuficiente"));

		totalCredit = getTotalCredit(account.getAccountId(), account.getAvailableWithdrawalLimit().getAmount());
		if (OperationType.isSaque(transaction.getOperationTypeId()) && totalCredit < Math.abs(transaction.getAmount()))
			throw new ResourceException(HttpStatus.NOT_ACCEPTABLE, sourceMessage.getMessage("transacao.limite.saque.insuficiente"));
	}

	/**
	 * Calcula o valor total de crédito para compra ou saque de uma conta.
	 *
	 * @param accountId identificador da conta
	 * @param creditLimitByOperation limite de crédito da conta para uma determinada operação
	 * @return Double
	 */
	private Double getTotalCredit(Integer accountId, Double creditLimitByOperation){
		TransactionDTO transactionWithCreditBalance = transactionRepository.findTransaction(null, accountId, OperationType.PAGAMENTO.getId(), true);
		if(transactionWithCreditBalance != null && transactionWithCreditBalance.getBalance() > 0){
			return creditLimitByOperation + transactionWithCreditBalance.getBalance();
		} else {
			return creditLimitByOperation;
		}
	}

	/**
	 * Método utilizado para realizar validações nos requests de transações de pagamento.
	 *
	 * @param payment transação de pagamento
	 */
	private void paymentValidate(TransactionDTO payment) {
		if (payment.getAmount() <= 0)
			throw new ResourceException(HttpStatus.NOT_ACCEPTABLE, sourceMessage.getMessage("transacao.pagamento.nulo"));

		AccountDTO account = accountService.getAccount(payment.getAccountId());
		if (account == null || account.getAccountId() <= 0)
			throw new ResourceException(HttpStatus.NOT_ACCEPTABLE, sourceMessage.getMessage("conta.nao.existente"));

		boolean hasNegativeBalance = transactionRepository.hasBalanceByOperation(payment.getAccountId(), OperationType.getNegativeOperations());
		if (!hasNegativeBalance)
			throw new ResourceException(HttpStatus.NOT_ACCEPTABLE, sourceMessage.getMessage("transacao.pagamento.desnecessaria"));

		boolean hasPositiveBalance = transactionRepository.hasBalanceByOperation(payment.getAccountId(), OperationType.getPositiveOperations());
		if (hasPositiveBalance)
			throw new ResourceException(HttpStatus.NOT_ACCEPTABLE, sourceMessage.getMessage("transacao.saldo.credor.existente"));
	}

}

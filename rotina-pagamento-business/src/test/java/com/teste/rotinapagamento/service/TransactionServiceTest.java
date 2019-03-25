package com.teste.rotinapagamento.service;

import com.teste.rotinapagamento.auxiliar.OperationType;
import com.teste.rotinapagamento.auxiliar.SourceMessage;
import com.teste.rotinapagamento.dto.AccountDTO;
import com.teste.rotinapagamento.dto.TransactionDTO;
import com.teste.rotinapagamento.exception.ResourceException;
import com.teste.rotinapagamento.repository.TransactionRepository;
import com.teste.rotinapagamento.util.AccountBuilder;
import com.teste.rotinapagamento.util.TransactionBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * @author Gusttavo Henrique (gusttavohnssilva@gmail.com)
 * @since 23/03/19.
 */
@RunWith(MockitoJUnitRunner.class)
public class TransactionServiceTest {

    @InjectMocks
    private TransactionService transactionService;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountService accountService;

    @Mock
    private SourceMessage sourceMessage;

    private TransactionBuilder transactionBuilder;
    private AccountBuilder accountBuilder;

    private TransactionDTO nullTransaction;
    private TransactionDTO transactionWithoutOperationTypeId;
    private TransactionDTO transactionWithoutAmount;
    private TransactionDTO transactionWithoutAccount;
    private TransactionDTO transactionWithCreditBalance;

    private TransactionDTO purchaseOrWithdrawal;
    private TransactionDTO purchaseOrWithdrawalWithPositiveAmount;
    private TransactionDTO purchaseWithoutAccountLimit;
    private TransactionDTO withdrawalWithoutAccountLimit;

    private TransactionDTO payment;
    private TransactionDTO paymentWithPositiveAmount;
    private TransactionDTO paymentWithNullAmount;
    private TransactionDTO paymentWithNegativeAmount;

    private AccountDTO account;

    @Before
    public void init() {
        when(sourceMessage.getMessage(anyString())).thenReturn("Mensagem de erro retornada!");

        transactionBuilder = new TransactionBuilder();
        accountBuilder = new AccountBuilder();

        nullTransaction = null;
        transactionWithoutOperationTypeId = transactionBuilder.build();
        transactionWithoutAmount = transactionBuilder.withOperationTypeId(1).build();
        transactionWithoutAccount = transactionBuilder.withOperationTypeId(1).withAmount(-100.00).build();
        transactionWithCreditBalance = transactionBuilder.withBalance(500.00).build();

        purchaseOrWithdrawal = transactionBuilder.withOperationTypeId(1).withAccountId(1).withAmount(-100.00).build();
        purchaseOrWithdrawalWithPositiveAmount = transactionBuilder.withOperationTypeId(1).withAccountId(1).withAmount(100.00).build();
        purchaseWithoutAccountLimit = transactionBuilder.withOperationTypeId(1).withAccountId(1).withAmount(-10000.00).build();
        withdrawalWithoutAccountLimit = transactionBuilder.withOperationTypeId(3).withAccountId(1).withAmount(-10000.00).build();

        payment = transactionBuilder.withTransactionId(5).withAccountId(1).withOperationTypeId(4).withAmount(100.00).build();
        paymentWithNullAmount = transactionBuilder.withTransactionId(3).withAccountId(1).withOperationTypeId(4).withAmount(null).build();
        paymentWithNegativeAmount = transactionBuilder.withTransactionId(4).withAccountId(1).withOperationTypeId(4).withAmount(-100.00).build();
        paymentWithPositiveAmount = transactionBuilder.withTransactionId(2).withAccountId(1).withOperationTypeId(4).withAmount(100.00).build();

        account = accountBuilder.withAccountId(purchaseOrWithdrawal.getAccountId()).
                withAvailableCreditLimit(1000.00).withAvailableWithdrawalLimit(1000.00).build();
    }

    @Test(expected = ResourceException.class)
    public void insertNullTransactionTest() {
        transactionService.insertTransaction(nullTransaction);
    }

    @Test(expected = ResourceException.class)
    public void insertTransactionWithout_operationTypeId_test() {
        transactionService.insertTransaction(transactionWithoutOperationTypeId);
    }

    @Test(expected = ResourceException.class)
    public void insertTransactionWithout_amount_test() {
        transactionService.insertTransaction(transactionWithoutAmount);
    }

    @Test(expected = ResourceException.class)
    public void insertTransactionWithout_account_test() {
        transactionService.insertTransaction(transactionWithoutAccount);
    }

    @Test(expected = ResourceException.class)
    public void insertPaymentWith_null_amountTest() {
        transactionService.insertTransaction(paymentWithNullAmount);
    }

    @Test(expected = ResourceException.class)
    public void insertPaymentWith_negative_amountTest() {
        transactionService.insertTransaction(paymentWithNegativeAmount);
    }

    @Test(expected = ResourceException.class)
    public void insertNotNecessaryPaymentTest() {
        doReturn(true)
        .when(transactionRepository)
                .hasBalanceByOperation(paymentWithPositiveAmount.getAccountId(), OperationType.getPositiveOperations());

        transactionService.insertTransaction(paymentWithPositiveAmount);
    }

    @Test
    public void insertTransaction_withPositiveAmount_test() {
        doReturn(false)
                .when(transactionRepository)
                .hasBalanceByOperation(payment.getAccountId(), OperationType.getPositiveOperations());

        doReturn(null)
                .when(transactionRepository).findTransactionsToDownPayment(payment.getAccountId());

        doReturn(payment.getTransactionId())
                .when(transactionRepository)
                .insertTransaction(payment.getAccountId(), OperationType.PAGAMENTO.getId(), payment.getAmount(), payment.getAmount(), null);

        Integer transactionId = payment.getTransactionId();
        doReturn(payment)
                .when(transactionRepository).findTransaction(transactionId, null, null, null);

        TransactionDTO transaction = transactionService.insertTransaction(payment);

        assertNotNull(transaction);
        assertEquals(payment, transaction);
    }

    @Test(expected = ResourceException.class)
    public void insertPaymentsWithNullPaymentListTest() {
        List<TransactionDTO> transactions = null;
        transactionService.insertPayments(transactions);
    }

    @Test(expected = ResourceException.class)
    public void insertPaymentsWith_empty_paymentListTest() {
        List<TransactionDTO> transactions = new ArrayList<>();
        transactionService.insertPayments(transactions);
    }

    @Test(expected = ResourceException.class)
    public void insertPurchaseOrWithdrawalWithPositiveAmountTest() {
        transactionService.insertTransaction(purchaseOrWithdrawalWithPositiveAmount);
    }

    @Test(expected = ResourceException.class)
    public void insert_purchase_withoutAccountLimitTest() {
        insertPurchaseOrWithdrawal(purchaseWithoutAccountLimit);
    }

    @Test(expected = ResourceException.class)
    public void insert_withdrawal_withoutAccountLimitTest() {
        insertPurchaseOrWithdrawal(withdrawalWithoutAccountLimit);
    }

    @Test
    public void insertPurchaseOrWithdrawalTest() {
        insertPurchaseOrWithdrawal(purchaseOrWithdrawal);
    }

    private void insertPurchaseOrWithdrawal(TransactionDTO transaction) {
        doReturn(account).when(accountService).getAccount(transaction.getAccountId());
        doReturn(transactionWithCreditBalance).when(transactionRepository)
                .findTransaction(null, account.getAccountId(), OperationType.PAGAMENTO.getId(), true);

        Date dueDate = transaction.getDueDate() != null ? new Date(transaction.getDueDate()) : null;
        doReturn(transaction.getTransactionId()).when(transactionRepository)
                .insertTransaction(transaction.getAccountId(), transaction.getOperationTypeId(), transaction.getAmount(), 0.0, dueDate);

        doReturn(transaction).when(transactionRepository)
                .findTransaction(transaction.getTransactionId(), null, null, null);

        TransactionDTO transactionResponse = transactionService.insertTransaction(transaction);
        assertNotNull(transactionResponse);
        assertEquals(transaction.getAmount(), transactionResponse.getAmount(), 0.001);

        transactionService.downCreditBalance(transaction);
        assertNotNull(transactionResponse);
        assertEquals(transaction.getBalance(), transactionResponse.getBalance(), 0.001);
    }

}

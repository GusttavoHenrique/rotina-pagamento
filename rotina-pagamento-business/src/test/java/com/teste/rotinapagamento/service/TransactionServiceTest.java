package com.teste.rotinapagamento.service;

import com.teste.rotinapagamento.auxiliar.OperationType;
import com.teste.rotinapagamento.auxiliar.SourceMessage;
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

import static org.mockito.Matchers.*;
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
    private TransactionDTO paymentWithPositiveAmount;
    private TransactionDTO paymentWithNullAmount;
    private TransactionDTO paymentWithNegativeAmount;

    @Before
    public void init() {
        when(sourceMessage.getMessage(anyString())).thenReturn("Mensagem de erro retornada!");

        transactionBuilder = new TransactionBuilder();
        accountBuilder = new AccountBuilder();

        nullTransaction = null;
        transactionWithoutOperationTypeId = transactionBuilder.build();
        transactionWithoutAmount = transactionBuilder.withOperationTypeId(1).build();
        transactionWithoutAccount = transactionBuilder.withOperationTypeId(1).withAmount(-100.00).build();

        paymentWithNullAmount = transactionBuilder.withTransactionId(3).withAccountId(1).withOperationTypeId(4).withAmount(null).build();
        paymentWithNegativeAmount = transactionBuilder.withTransactionId(4).withAccountId(1).withOperationTypeId(4).withAmount(-100.00).build();
        paymentWithPositiveAmount = transactionBuilder.withTransactionId(2).withAccountId(1).withOperationTypeId(4).withAmount(100.00).build();
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
}

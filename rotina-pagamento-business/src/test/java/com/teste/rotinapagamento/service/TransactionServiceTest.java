package com.teste.rotinapagamento.service;

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

import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

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
    private TransactionDTO transactionWithNegativeAmount;
    private TransactionDTO transactionWithPositiveAmount;

    @Before
    public void init() {
        transactionBuilder = new TransactionBuilder();
        accountBuilder = new AccountBuilder();

        nullTransaction = null;
        transactionWithoutOperationTypeId = transactionBuilder.build();
        transactionWithoutAmount = transactionBuilder.withOperationTypeId(1).build();
        transactionWithoutAccount = transactionBuilder.withOperationTypeId(1).withAmount(-100.00).build();
        transactionWithNegativeAmount = transactionBuilder.withTransactionId(1).
                withAccountId(1).withOperationTypeId(1).withAmount(-100.00).build();
        transactionWithPositiveAmount = transactionBuilder.withTransactionId(2).
                withAccountId(1).withOperationTypeId(4).withAmount(100.00).build();
    }

    @Test(expected = ResourceException.class)
    public void insertNullTransactionTest() {
        insertTransactionValidate(nullTransaction);
    }

    @Test(expected = ResourceException.class)
    public void insertTransactionWithout_operationTypeId_test() {
        insertTransactionValidate(transactionWithoutOperationTypeId);
    }

    @Test(expected = ResourceException.class)
    public void insertTransactionWithout_amount_test() {
        insertTransactionValidate(transactionWithoutAmount);
    }

    @Test(expected = ResourceException.class)
    public void insertTransactionWithout_account_test() {
        insertTransactionValidate(transactionWithoutAccount);
    }

    private void insertTransactionValidate(TransactionDTO transaction){
        when(transactionService.insertTransaction(transaction)).thenCallRealMethod();
        doReturn(anyString()).when(sourceMessage.getMessage(anyString()));
        transactionService.insertTransaction(transaction);
    }

}

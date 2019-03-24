package com.teste.rotinapagamento.service;

import com.teste.rotinapagamento.auxiliar.SourceMessage;
import com.teste.rotinapagamento.dto.AccountDTO;
import com.teste.rotinapagamento.exception.ResourceException;
import com.teste.rotinapagamento.repository.AccountRepository;
import com.teste.rotinapagamento.util.AccountBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * @author Gusttavo Henrique (gusttavohnssilva@gmail.com)
 * @since 23/03/19.
 */
@RunWith(MockitoJUnitRunner.class)
public class AccountServiceTest {

    @InjectMocks
    private AccountService accountService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private SourceMessage sourceMessage;

    private AccountBuilder accountBuilder;

    private AccountDTO nullAccount;
    private AccountDTO accountWithAvailableCredit;
    private AccountDTO accountWithAvailableWithdrawal;
    private AccountDTO accountResponse;
    private AccountDTO notExistAccount;
    private AccountDTO existingAccount;
    private AccountDTO accountWithNegativeCredit;
    private AccountDTO accountWithNegativeWithdrawal;

    @Before
    public void init() {
        when(sourceMessage.getMessage(anyString())).thenReturn("Mensagem de erro retornada!");

        accountBuilder = new AccountBuilder();

        nullAccount = null;
        accountResponse = accountBuilder.withAccountId(1).withAvailableWithdrawalLimit(1000.00).build();
        accountWithAvailableCredit = accountBuilder.withAvailableCreditLimit(2000.00).build();
        accountWithAvailableWithdrawal = accountBuilder.withAvailableWithdrawalLimit(3000.00).build();
        notExistAccount = accountBuilder.
                withAccountId(4).
                withAvailableCreditLimit(4000.00).
                withAvailableWithdrawalLimit(4000.00).
                build();
        existingAccount = accountBuilder.
                withAccountId(5).
                withAvailableCreditLimit(5000.00).
                withAvailableWithdrawalLimit(5000.00).
                build();
        accountWithNegativeCredit = accountBuilder.withAccountId(1).withAvailableCreditLimit(-2000.00).build();
        accountWithNegativeWithdrawal = accountBuilder.withAccountId(1).withAvailableWithdrawalLimit(-2000.00).build();
    }

    @Test(expected = ResourceException.class)
    public void insertAccountWithAvailableCreditAndWithdrawalLimitsNullTest() {
        accountService.insertAccount(nullAccount);
    }

    @Test
    public void insertAccountWithAvailable_credit_limitNullTest() {
        insertAccount(accountWithAvailableWithdrawal);
    }

    @Test
    public void insertAccountWithAvailable_withdrawal_limitNullTest() {
        insertAccount(accountWithAvailableCredit);
    }

    private void insertAccount(AccountDTO account){
        doReturn(accountResponse).
                when(accountRepository).
                insertAccount(anyDouble(), anyDouble());

        AccountDTO accountDTO = accountService.insertAccount(account);
        assertNotNull(accountDTO);
        assertEquals(accountDTO, accountResponse);
    }

    @Test(expected = ResourceException.class)
    public void update_null_accountTest() {
        updateAccount(null, nullAccount, null);
    }

    @Test(expected = ResourceException.class)
    public void update_inexistent_accountTest() {
        updateAccount(notExistAccount.getAccountId(), notExistAccount, null);
    }

    @Test
    public void update_existing_accountTest() {
        Integer existingAccountId = existingAccount.getAccountId();
        when(accountRepository.findAccount(anyInt())).thenReturn(existingAccount);

        doReturn(existingAccount).
                when(accountRepository).
                updateAccount(existingAccountId, existingAccount.getAvailableCreditLimit().getAmount(), existingAccount.getAvailableWithdrawalLimit().getAmount());

        AccountDTO accountDTO = accountService.updateAccount(existingAccountId, existingAccount);
        assertNotNull(accountDTO);
        assertEquals(accountDTO, existingAccount);
    }

    @Test(expected = ResourceException.class)
    public void updateWith_negativeCredit_limitAccountTest() {
        updateAccount(accountWithNegativeCredit.getAccountId(), accountWithNegativeCredit, accountResponse);
    }

    @Test(expected = ResourceException.class)
    public void updateWith_negativeWithdrawal_limitAccountTest() {
        updateAccount(accountWithNegativeWithdrawal.getAccountId(), accountWithNegativeWithdrawal, accountResponse);
    }

    private void updateAccount(Integer accountid, AccountDTO account, AccountDTO accountResponse){
        when(accountRepository.findAccount(anyInt())).thenReturn(accountResponse);
        accountService.updateAccount(accountid, account);
    }

}

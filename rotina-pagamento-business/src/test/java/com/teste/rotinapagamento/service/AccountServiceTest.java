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
    private AccountDTO inexistentAccount;
    private AccountDTO existingAccount;

    @Before
    public void init() {
        accountBuilder = new AccountBuilder();

        nullAccount = null;
        accountResponse = accountBuilder.withAccountId(1).withAvailableWithdrawalLimit(1000.00).build();
        accountWithAvailableCredit = accountBuilder.withAvailableCreditLimit(2000.00).build();
        accountWithAvailableWithdrawal = accountBuilder.withAvailableWithdrawalLimit(3000.00).build();
        inexistentAccount = accountBuilder.
                withAccountId(4).
                withAvailableCreditLimit(4000.00).
                withAvailableWithdrawalLimit(4000.00).
                build();
        existingAccount = accountBuilder.
                withAccountId(5).
                withAvailableCreditLimit(5000.00).
                withAvailableWithdrawalLimit(5000.00).
                build();

    }

    @Test(expected = ResourceException.class)
    public void insertAccountWithAvailableCreditAndWithdrawalLimitsNullTest() {
        when(accountService.insertAccount(anyObject())).thenCallRealMethod();
        doReturn(anyString()).when(sourceMessage.getMessage(anyString()));
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
        when(accountService.updateAccount(null, nullAccount)).thenCallRealMethod();
        doReturn(null).when(accountRepository.findAccount(null));
        doReturn(anyString()).when(sourceMessage.getMessage(anyString()));
        accountService.updateAccount(null, nullAccount);
    }

    @Test(expected = ResourceException.class)
    public void update_inexistent_accountTest() {
        when(accountService.updateAccount(inexistentAccount.getAccountId(), inexistentAccount)).thenCallRealMethod();
        doReturn(null).when(accountRepository.findAccount(inexistentAccount.getAccountId()));
        doReturn(anyString()).when(sourceMessage.getMessage(anyString()));
        accountService.updateAccount(inexistentAccount.getAccountId(), inexistentAccount);
    }

    @Test
    public void update_existing_accountTest() {
        Integer existingAccountId = existingAccount.getAccountId();
        when(accountService.updateAccount(existingAccountId, existingAccount)).thenCallRealMethod();

        doReturn(existingAccount).when(accountRepository.findAccount(existingAccountId));

        doReturn(existingAccount).
                when(accountRepository).
                updateAccount(existingAccountId, existingAccount.getAvailableCreditLimit().getAmount(), existingAccount.getAvailableWithdrawalLimit().getAmount());

        AccountDTO accountDTO = accountService.updateAccount(existingAccountId, existingAccount);
        assertNotNull(accountDTO);
        assertEquals(accountDTO, existingAccount);
    }

}

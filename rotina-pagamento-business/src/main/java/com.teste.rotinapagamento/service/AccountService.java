package com.teste.rotinapagamento.service;

import java.util.List;

import com.teste.rotinapagamento.auxiliar.SourceMessage;
import com.teste.rotinapagamento.auxiliar.OperationType;
import com.teste.rotinapagamento.dto.AccountDTO;
import com.teste.rotinapagamento.dto.AvailableLimitDTO;
import com.teste.rotinapagamento.dto.TransactionDTO;
import com.teste.rotinapagamento.exception.ResourceException;
import com.teste.rotinapagamento.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Gusttavo Henrique (gusttavohnssilva@gmail.com)
 * @since 19/03/19.
 */
@Transactional
@Service
public class AccountService {

    @Autowired
    private SourceMessage sourceMessage;

    @Autowired
    private AccountRepository accountRepository;

    /**
     * Delega a operação de inserção de contas para o método insert da classe repository.
     *
     * @param account conta que será persistida
     * @return AccountDTO
     */
    public AccountDTO insertAccount(AccountDTO account) {
        if(account == null || (account.getAvailableCreditLimit() == null && account.getAvailableWithdrawalLimit() == null))
            throw new ResourceException(HttpStatus.NOT_ACCEPTABLE, sourceMessage.getMessage("conta.limite.nao.informado"));

        AvailableLimitDTO availableCreditLimit = account.getAvailableCreditLimit();
        AvailableLimitDTO availableWithdrawalLimit = account.getAvailableWithdrawalLimit();

        double availableCreditLimitAmount = 0.00;
        double availableWithdrawalLimitAmount = 0.00;

        if(availableCreditLimit != null && availableCreditLimit.getAmount() != null)
             availableCreditLimitAmount = availableCreditLimit.getAmount();

        if(availableWithdrawalLimit != null && availableWithdrawalLimit.getAmount() != null)
            availableWithdrawalLimitAmount = availableWithdrawalLimit.getAmount();

        if(availableCreditLimitAmount == 0 && availableWithdrawalLimitAmount == 0)
            throw new ResourceException(HttpStatus.NOT_ACCEPTABLE, sourceMessage.getMessage("conta.limite.nao.informado"));

        return accountRepository.insertAccount(availableCreditLimitAmount, availableWithdrawalLimitAmount);
    }

    /**
     * Delega a operação de atualização de contas para o método update da classe repository.
     *
     * @param accountId  identificador da conta
     * @param account conta que será atualizada
     * @return AccountDTO
     */
    public AccountDTO updateAccount(Integer accountId, AccountDTO account) {
        if(accountId == null || account == null)
            throw new ResourceException(HttpStatus.NOT_ACCEPTABLE, sourceMessage.getMessage("conta.nao.existente"));

        AccountDTO accountDB = accountRepository.findAccount(accountId);
        if(accountDB == null)
            throw new ResourceException(HttpStatus.NOT_ACCEPTABLE, sourceMessage.getMessage("conta.nao.existente"));

        Double creditAmount = account.getAvailableCreditLimit() != null ? account.getAvailableCreditLimit().getAmount() : 0.00;
        Double withdrawalAmount = account.getAvailableWithdrawalLimit() != null ? account.getAvailableWithdrawalLimit().getAmount() : 0.00;
        Double creditAmountDB = accountDB.getAvailableCreditLimit() != null ? accountDB.getAvailableCreditLimit().getAmount() : 0.00;
        Double withdrawalAmountDB = accountDB.getAvailableWithdrawalLimit() != null ? accountDB.getAvailableWithdrawalLimit().getAmount() : 0.00;

        if(creditAmount != null && creditAmountDB != null && creditAmount < 0 && creditAmountDB < Math.abs(creditAmount))
            throw new ResourceException(HttpStatus.NOT_ACCEPTABLE, sourceMessage.getMessage("conta.limite.credito.insuficiente"));

        if(withdrawalAmount != null && withdrawalAmountDB != null && withdrawalAmount < 0 && withdrawalAmountDB < Math.abs(withdrawalAmount))
            throw new ResourceException(HttpStatus.NOT_ACCEPTABLE, sourceMessage.getMessage("conta.limite.saque.insuficiente"));

        return updateAccount(accountId, creditAmount, withdrawalAmount);
    }

    /**
     * Abate o valor do pagamento no limite de crédito ou de retirada da conta.
     *
     * @param transaction transação corrente
     */
    public void updateLimitAccount(TransactionDTO transaction, Double amount) {
        Integer accountId = transaction.getAccountId();
        if (transaction.getOperationTypeId() == OperationType.SAQUE.getId()) {
            updateAccount(accountId, null, amount);
        } else {
            updateAccount(accountId, amount, null);
        }
    }

    /**
     * Delega a operação de update de contas para o método update da classe repository.
     *
     * @param accountId                      identificador da conta
     * @param availableCreditLimitAmount     limite de crédito disponível
     * @param availableWithdrawalLimitAmount limite de retirada disponível
     * @return AccountDTO
     */
    private AccountDTO updateAccount(Integer accountId, Double availableCreditLimitAmount, Double availableWithdrawalLimitAmount){
        return accountRepository.updateAccount(accountId, availableCreditLimitAmount, availableWithdrawalLimitAmount);
    }

    /**
     * Delega a operação de busca de contas para o método find da classe repository.
     *
     * @return List<AccountDTO>
     */
    public List<AccountDTO> getAccounts(){
        return accountRepository.findAccounts();
    }

    /**
     * Delega a operação de busca de uma conta para o método find da classe repository.
     *
     * @param accountId identificador da conta
     * @return AccountDTO
     */
    public AccountDTO getAccount(Integer accountId) {
        return accountRepository.findAccount(accountId);
    }
}

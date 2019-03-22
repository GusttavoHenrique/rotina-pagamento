package com.teste.rotinapagamento.service;

import java.util.List;

import com.teste.rotinapagamento.auxiliar.OperationType;
import com.teste.rotinapagamento.dto.AccountDTO;
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
    AccountRepository accountRepository;

    /**
     * Delega a operação de inserção de contas para o método insert da classe repository.
     *
     * @param account conta que será persistida
     * @return AccountDTO
     */
    public AccountDTO insertAccount(AccountDTO account) {
        if(account.getAvailableCreditLimit() == null && account.getAvailableWithdrawalLimit() == null)
            throw new ResourceException(HttpStatus.NOT_ACCEPTABLE, "É necessário informar o(s) limite(s) de crédito e/ou saque.");

        Double availableCreditLimitAmount = account.getAvailableCreditLimit() != null ? account.getAvailableCreditLimit().getAmount() : null;
        Double availableWithdrawalLimitAmount = account.getAvailableWithdrawalLimit() != null ? account.getAvailableWithdrawalLimit().getAmount() : null;
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
        if(accountRepository.findAccount(accountId) == null)
            throw new ResourceException(HttpStatus.NOT_ACCEPTABLE, "Esta conta não existe! Não será possível atualizar os dados desta conta.");

        Double availableCreditLimitAmount = account.getAvailableCreditLimit() != null ? account.getAvailableCreditLimit().getAmount() : null;
        Double availableWithdrawalLimitAmount = account.getAvailableWithdrawalLimit() != null ? account.getAvailableWithdrawalLimit().getAmount() : null;
        return updateAccount(accountId, availableCreditLimitAmount, availableWithdrawalLimitAmount);
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

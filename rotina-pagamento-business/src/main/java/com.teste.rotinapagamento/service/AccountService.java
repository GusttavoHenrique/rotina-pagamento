package com.teste.rotinapagamento.service;

import java.util.List;

import com.teste.rotinapagamento.dto.AccountDTO;
import com.teste.rotinapagamento.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
     * Delega a operação de atualização de contas para o método update da classe repository.
     *
     * @param accountId  identificador da conta
     * @param accountDTO DTO da conta
     * @return AccountDTO
     */
    public AccountDTO updateAccount(Integer accountId, AccountDTO accountDTO) {
        Double availableCreditLimitAmount = accountDTO.getAvailableCreditLimit() != null ? accountDTO.getAvailableCreditLimit().getAmount() : null;
        Double availableWithdrawalLimitAmount = accountDTO.getAvailableWithdrawalLimit() != null ? accountDTO.getAvailableWithdrawalLimit().getAmount() : null;
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
}

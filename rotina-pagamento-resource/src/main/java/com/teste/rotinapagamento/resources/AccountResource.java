package com.teste.rotinapagamento.resources;

import java.util.List;

import com.teste.rotinapagamento.dto.AccountDTO;
import com.teste.rotinapagamento.exception.ResourceException;
import com.teste.rotinapagamento.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author Gusttavo Henrique (gusttavohnssilva@gmail.com)
 * @since 19/03/19.
 */
@RestController
@RequestMapping("/accounts")
public class AccountResource {

    @Autowired
    AccountService accountService;

    @ExceptionHandler(ResourceException.class)
    public ResponseEntity handleException(ResourceException e) {
        return ResponseEntity.status(e.getHttpStatus()).body(e.getError());
    }

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity insertAccount(@RequestBody AccountDTO accountDTO) {
        AccountDTO account = accountService.insertAccount(accountDTO);
        return ResponseEntity.status(HttpStatus.CREATED).headers(new HttpHeaders()).body(account);
    }

    @RequestMapping(value = "/{account_id}", method = RequestMethod.PATCH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity updateAccount(
            @PathVariable("account_id") Integer accountId,
            @RequestBody AccountDTO accountDTO
    ) {
        AccountDTO account = accountService.updateAccount(accountId, accountDTO);
        return ResponseEntity.status(HttpStatus.CREATED).headers(new HttpHeaders()).body(account);
    }

    @RequestMapping(value = "/limits", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity listLimits() {
        List<AccountDTO> accounts = accountService.getAccounts();
        return ResponseEntity.status(HttpStatus.OK).headers(new HttpHeaders()).body(accounts);
    }

}

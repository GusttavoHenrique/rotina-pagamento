package com.teste.rotinapagamento.resources;

import com.teste.rotinapagamento.dto.AccountDTO;
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

    @RequestMapping(value = "/{account_id}", method = RequestMethod.PATCH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity updateAccount(
            @PathVariable("account_id") Integer accountId,
            @RequestBody AccountDTO accountDTO
    ) {
        AccountDTO account = accountService.updateAccount(accountId, accountDTO);

        return ResponseEntity.status(HttpStatus.CREATED).headers(new HttpHeaders()).body(account);
    }

}

package com.teste.rotinapagamento.resources;

import com.teste.rotinapagamento.dto.TransactionDTO;
import com.teste.rotinapagamento.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Gusttavo Henrique (gusttavohnssilva@gmail.com)
 * @since 19/03/19.
 */
@RestController
@RequestMapping("/transaction")
public class TransactionResource {

    @Autowired
    TransactionService transactionService;

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity insertTransaction(
            @RequestParam("account_id") Integer accountId,
            @RequestParam("operation_type_id") Integer operationTypeId,
            @RequestParam("amount") Double amount
    ) {
        TransactionDTO transaction = transactionService.insertTransaction(accountId, operationTypeId, amount);

        return ResponseEntity.status(HttpStatus.CREATED).headers(new HttpHeaders()).body(transaction);
    }


}

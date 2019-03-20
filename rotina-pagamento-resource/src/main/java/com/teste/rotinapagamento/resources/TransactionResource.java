package com.teste.rotinapagamento.resources;

import com.teste.rotinapagamento.dto.TransactionDTO;
import com.teste.rotinapagamento.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Gusttavo Henrique (gusttavohnssilva@gmail.com)
 * @since 19/03/19.
 */
@RestController
public class TransactionResource {

    @Autowired
    TransactionService transactionService;

    @RequestMapping(value = "/transactions", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity insertTransaction(@RequestBody TransactionDTO transaction) {
        transaction = transactionService.insertTransaction(transaction);
        return ResponseEntity.status(HttpStatus.CREATED).headers(new HttpHeaders()).body(transaction);
    }

    @RequestMapping(value = "/payments", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity insertPayments(
            @RequestBody List<TransactionDTO> payments
    ) {
        payments = transactionService.insertPayments(payments);
        return ResponseEntity.status(HttpStatus.CREATED).headers(new HttpHeaders()).body(payments);
    }


}

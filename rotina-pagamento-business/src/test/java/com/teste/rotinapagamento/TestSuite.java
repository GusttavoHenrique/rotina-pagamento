package com.teste.rotinapagamento;

import com.teste.rotinapagamento.service.AccountServiceTest;
import com.teste.rotinapagamento.service.TransactionServiceTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Gusttavo Henrique (gusttavohnssilva@gmail.com)
 * @since 23/03/19.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        AccountServiceTest.class,
        TransactionServiceTest.class
})
public class TestSuite {
}

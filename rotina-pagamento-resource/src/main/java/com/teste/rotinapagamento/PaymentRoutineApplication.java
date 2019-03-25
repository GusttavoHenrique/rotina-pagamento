package com.teste.rotinapagamento;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * @author Gusttavo Henrique (gusttavohnssilva@gmail.com)
 * @since 18/03/19.
 */
@SpringBootApplication
public class PaymentRoutineApplication implements CommandLineRunner {

    @Autowired
    JdbcTemplate jdbcTemplate;

    private static final Logger log = LoggerFactory.getLogger(PaymentRoutineApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(PaymentRoutineApplication.class, args);
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();

        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Bean
    public LocalValidatorFactoryBean getValidator() {
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
        bean.setValidationMessageSource(messageSource());
        return bean;
    }

    @Override
    public void run(String... strings) throws Exception {
        log.info("Criando/atualizando sequences das tabelas.");

        jdbcTemplate.execute("CREATE SEQUENCE IF NOT EXISTS public.accounts_seq INCREMENT BY 1 " +
                "MINVALUE 1 MAXVALUE 99999999999999 START 1 CACHE 1 NO CYCLE;");

        jdbcTemplate.execute("CREATE SEQUENCE IF NOT EXISTS public.transactions_seq INCREMENT BY 1 MINVALUE 1 MAXVALUE 99999999999999 START 1 CACHE 1 NO CYCLE;");

        log.info("Sequences das tabelas criados com sucesso!");

        log.info("Criando/atualizando tabelas no banco de dados.");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS public.accounts (" +
                "    account_id integer NOT NULL, " +
                "    available_credit_limit double precision DEFAULT 0, " +
                "    available_withdrawal_limit double precision DEFAULT 0, " +
                "    CONSTRAINT account_id_pk PRIMARY KEY (account_id) );");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS public.operations_types ( " +
                "    operation_type_id integer NOT NULL, " +
                "    description character varying(50) COLLATE pg_catalog.\"default\" NOT NULL, " +
                "    charge_order integer NOT NULL, " +
                "    CONSTRAINT operations_types_pk PRIMARY KEY (operation_type_id));");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS public.transactions ( " +
                "    transaction_id integer NOT NULL, " +
                "    account_id integer NOT NULL, " +
                "    operation_type_id integer NOT NULL, " +
                "    amount double precision NOT NULL, " +
                "    balance double precision NOT NULL, " +
                "    event_date timestamp NOT NULL, " +
                "    due_date timestamp, " +
                "    CONSTRAINT transaction_id_pk PRIMARY KEY (transaction_id), " +
                "    CONSTRAINT account_id_fk FOREIGN KEY (account_id) " +
                "        REFERENCES public.accounts (account_id) MATCH SIMPLE " +
                "        ON UPDATE NO ACTION " +
                "        ON DELETE NO ACTION, " +
                "    CONSTRAINT operation_type_id_fk FOREIGN KEY (operation_type_id) " +
                "        REFERENCES public.operations_types (operation_type_id) MATCH SIMPLE " +
                "        ON UPDATE NO ACTION " +
                "        ON DELETE NO ACTION );");

        log.info("Tabelas criadas com sucesso!");

        log.info("Populando/atualizando tabela de tipos de operações.");

        jdbcTemplate.execute("INSERT INTO public.operations_types(operation_type_id, description, charge_order) VALUES (1, 'COMPRA À VISTA', 2) ON CONFLICT (operation_type_id) DO NOTHING;");
        jdbcTemplate.execute("INSERT INTO public.operations_types(operation_type_id, description, charge_order) VALUES (2, 'COMPRA PARCELADA', 1) ON CONFLICT (operation_type_id) DO NOTHING");
        jdbcTemplate.execute("INSERT INTO public.operations_types(operation_type_id, description, charge_order) VALUES (3, 'SAQUE', 0) ON CONFLICT (operation_type_id) DO NOTHING;");
        jdbcTemplate.execute("INSERT INTO public.operations_types(operation_type_id, description, charge_order) VALUES (4, 'PAGAMENTO', 0) ON CONFLICT (operation_type_id) DO NOTHING;");

        log.info("Tabela de tipos de operações populada com sucesso!");

    }
}

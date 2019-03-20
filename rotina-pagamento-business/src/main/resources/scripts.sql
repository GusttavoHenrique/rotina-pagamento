-- Table: public.accounts

-- DROP TABLE public.accounts;

CREATE TABLE public.accounts
(
    account_id integer NOT NULL,
    available_credit_limit double precision NOT NULL,
    available_with_drawal_limit double precision NOT NULL,
    CONSTRAINT account_id_pk PRIMARY KEY (account_id)
)

-----------------------------------------------------------------------------------------------

-- Table: public.operations_types

-- DROP TABLE public.operations_types;

CREATE TABLE public.operations_types
(
    operation_type_id integer NOT NULL,
    description character varying(50) COLLATE pg_catalog."default" NOT NULL,
    charge_order integer NOT NULL,
    CONSTRAINT operations_types_pk PRIMARY KEY (operation_type_id)
)

-----------------------------------------------------------------------------------------------

-- SEQUENCE: public.transactions_seq

-- DROP SEQUENCE public.transactions_seq;

CREATE SEQUENCE public.transactions_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 99999999999999
	START 1
	CACHE 1
	NO CYCLE;

-----------------------------------------------------------------------------------------------

-- Table: public.transactions

-- DROP TABLE public.transactions;

CREATE TABLE public.transactions
(
    transaction_id integer NOT NULL,
    account_id integer NOT NULL,
    operation_type_id integer NOT NULL,
    amount double precision NOT NULL,
    balance double precision NOT NULL,
    event_date timestamp NOT NULL,
    due_date timestamp NOT NULL,
    CONSTRAINT transaction_id_pk PRIMARY KEY (transaction_id),
    CONSTRAINT account_id_fk FOREIGN KEY (account_id)
        REFERENCES public.accounts (account_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT operation_type_id_fk FOREIGN KEY (operation_type_id)
        REFERENCES public.operations_types (operation_type_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)

-----------------------------------------------------------------------------------------------

-- Carregar tabela public.operations_types

INSERT INTO public.operations_types(operation_type_id, description, charge_order) VALUES (1, 'COMPRA À VISTA', 2);
INSERT INTO public.operations_types(operation_type_id, description, charge_order) VALUES (2, 'COMPRA PARCELADA', 1);
INSERT INTO public.operations_types(operation_type_id, description, charge_order) VALUES (3, 'SAQUE', 0);
INSERT INTO public.operations_types(operation_type_id, description, charge_order) VALUES (4, 'PAGAMENTO', 0);
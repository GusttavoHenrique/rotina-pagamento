# Rotina de Pagamento

Projeto para simulação de rotinas de pagamentos de uma processadora de crédito.

## Operações Disponíveis

### Cadastro de Contas:

```
POST http://localhost:8080/payment-routine/v1/accounts
```

Resposta:

```JSON
{
    "account_id": 0,
    "available_credit_limit": {
        "amount": 0
    },
    "available_withdrawal_limit": {
        "amount": 0
    }
}
```

Obs: um dos dois atributos `available_credit_limit` ou `available_withdrawal_limit` são obrigatórios no corpo da requisição. O atributo `account_id` 
é ignorado, pois é gerado pela própria aplicação.

### Atualização do Limite de Conta:

```
PATCH http://localhost:8080/payment-routine/v1/accounts/{account_id}
```

Resposta:

```JSON
{
    "account_id": 0,
    "available_credit_limit": {
        "amount": 0
    },
    "available_withdrawal_limit": {
        "amount": 0
    }
}
```

Obs: os atributos `account_id` e um dos limites (`available_credit_limit` ou `available_withdrawal_limit`) são obrigatórios no corpo da requisição.

### Consulta de Limites de Contas Cadastradas:

```
GET http://localhost:8080/payment-routine/v1/accounts/limits
```

Resposta:

```JSON
[
    {
        "account_id": 0,
        "available_credit_limit": {
            "amount": 0
        },
        "available_withdrawal_limit": {
            "amount": 0
        },
        "credit_balance": {
            "amount": 0         
        }
    }
]    
```

Obs: `credit_balance` é o saldo credor da conta. Esse saldo não é acrescentado ao limite, mas pode ser abatido nas próximas 
transações de compras ou saque. 

### Consulta de Transações

```
GET http://localhost:8080/payment-routine/v1/transactions
```

Resposta:

```JSON
[
    {
        "transaction_id": 0,
        "account_id": 0,
        "operation_type_id": 0,
        "amount": 0,
        "balance": 0,
        "event_date": 0,
        "due_date": 0
    }
]
```

Obs: o atributo `account_id` poderá ser passado como query para filtrar pelas transações de uma conta específica.

### Cadastro de Transações

```
POST http://localhost:8080/payment-routine/v1/transactions
```

Resposta:

```JSON
{
    "transaction_id": 0,
    "account_id": 0,
    "operation_type_id": 0,
    "amount": 0,
    "balance": 0,
    "event_date": 0,
    "due_date": 0
}
```

Obs: os atributos `account_id`, `operation_type_id` e `amount` são obrigatórios no corpo da requisição. O atributo `due_date` 
é opcional e todos os outros são ignorados, pois são gerados pela própria aplicação.

### Cadastro de Lista de Pagamentos

```
POST http://localhost:8080/payment-routine/v1/payments
```

Resposta:

```JSON
[
    {
        "transaction_id": 0,
        "account_id": 0,
        "operation_type_id": 0,
        "amount": 0,
        "balance": 0,
        "event_date": 0,
        "due_date": 0
    }
]
```

Obs: os atributos `account_id`, `operation_type_id` e `amount` são obrigatórios no corpo da requisição. O atributo `due_date` 
é opcional e todos os outros são ignorados, pois são gerados pela própria aplicação.

## Tipos de Operações

A tabela abaixo sugere alguns tipos de operações possíveis para realização de transações.

| operation_type_id | descrição        |
|-------------------|------------------|
| 1                 | COMPRA À VISTA   |
| 2                 | COMPRA PARCELADA |
| 3                 | SAQUE            |
| 4                 | PAGAMENTO        |

## Dependências
* Java 8
* Maven 3
* Docker
* Docker-Compose

## Executando o projeto com docker-compose

* Clonar ou baixar o projeto;
* Utilizando um terminal do sistema operacional, entrar no diretório /rotina-pagamento (diretório raiz do projeto); 
* Executar os seguintes comandos:

```SHELL
> mvn clean install -DskiTests
```

```SHELL
> docker-compose build
```

```SHELL
> docker-compose up
```

Obs.: também é possível executar o script build.sh para rodar o build da aplicação no docker. 
Lembre-se de dar permissões para o arquivo build.sh executando o comando `chmod +x build.sh`.

* Utilizar o serviço, consumindo um dos [endpoins disponíveis na aplicação](https://github.com/GusttavoHenrique/rotina-pagamento#opera%C3%A7%C3%B5es-dispon%C3%ADveis).

## Autores
* **Gusttavo Silva** - *Developer* - [gusttavohnssilva@gmail.com](mailto:gusttavohnssilva@gmail.com)
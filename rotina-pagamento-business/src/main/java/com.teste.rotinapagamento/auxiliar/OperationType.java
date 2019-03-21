package com.teste.rotinapagamento.auxiliar;

/**
 * @author Gusttavo Henrique (gusttavohnssilva@gmail.com)
 * @since 19/03/19.
 */
public enum OperationType {

    COMPRA_A_VISTA(1),
    COMPRA_PARCELADA(2),
    SAQUE(3),
    PAGAMENTO(4);

    private int id;

    OperationType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static boolean isSaque(Integer operationTypeId){
        return operationTypeId == SAQUE.id;
    }

    public static boolean isCompra(Integer operationTypeId){
        return operationTypeId == COMPRA_A_VISTA.id || operationTypeId == COMPRA_PARCELADA.id;
    }
}

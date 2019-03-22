package com.teste.rotinapagamento.auxiliar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public static List<Object> getNegativeOperations(){
        List<Object> operations = new ArrayList<>();
        operations.add(COMPRA_A_VISTA.getId());
        operations.add(COMPRA_PARCELADA.getId());
        operations.add(SAQUE.getId());
        return operations;
    }

    public static List<Object> getPositiveOperations() {
        List<Object> operations = new ArrayList<>();
        operations.add(PAGAMENTO.getId());
        return operations;
    }
}

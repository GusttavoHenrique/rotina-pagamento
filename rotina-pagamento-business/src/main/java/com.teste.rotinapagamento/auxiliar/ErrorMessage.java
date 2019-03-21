package com.teste.rotinapagamento.auxiliar;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Gusttavo Henrique (gusttavohnssilva@gmail.com)
 * @since 21/03/19.
 */
public class ErrorMessage {

    @JsonProperty("code")
    private Integer code;

    @JsonProperty("code_description")
    private String codeDescription;

    @JsonProperty("message")
    private String message;

    public ErrorMessage(Integer code, String codeDescription, String message) {
        this.code = code;
        this.codeDescription = codeDescription;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getCodeDescription() {
        return codeDescription;
    }

    public void setCodeDescription(String codeDescription) {
        this.codeDescription = codeDescription;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

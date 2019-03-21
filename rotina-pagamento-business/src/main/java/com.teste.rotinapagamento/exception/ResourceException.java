package com.teste.rotinapagamento.exception;

import com.teste.rotinapagamento.auxiliar.ErrorMessage;
import org.springframework.http.HttpStatus;

/**
 * @author Gusttavo Henrique (gusttavohnssilva@gmail.com)
 * @since 19/03/19.
 */
public class ResourceException extends RuntimeException {

	private HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

	private ErrorMessage error;

	public HttpStatus getHttpStatus() {
		return httpStatus;
	}

	public ResourceException(HttpStatus httpStatus, String message) {
		super(message);
		this.httpStatus = httpStatus;
		this.error = new ErrorMessage(httpStatus.value(), httpStatus.getReasonPhrase(), message);
	}

	public ErrorMessage getError(){
		return this.error;
	}

}

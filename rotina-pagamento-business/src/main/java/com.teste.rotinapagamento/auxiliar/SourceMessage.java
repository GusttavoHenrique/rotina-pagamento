package com.teste.rotinapagamento.auxiliar;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * @author Gusttavo Henrique (gusttavohnssilva@gmail.com)
 * @since 22/03/19.
 */
@Component
public class SourceMessage {

	@Autowired
	private MessageSource messageSource;

	public String getMessage(String id) {
		Locale locale = LocaleContextHolder.getLocale();
		return messageSource.getMessage(id, null, locale);
	}
}

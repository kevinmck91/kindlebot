package com.kevinmck91.kindlebot.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class ParseUploadFileException extends RuntimeException {
	public ParseUploadFileException(String message) {
		super(message);
	}
}

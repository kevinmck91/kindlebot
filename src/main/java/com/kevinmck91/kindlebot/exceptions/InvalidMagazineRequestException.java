package com.kevinmck91.kindlebot.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidMagazineRequestException extends RuntimeException {
	public InvalidMagazineRequestException(String message) {
		super(message);
	}
}
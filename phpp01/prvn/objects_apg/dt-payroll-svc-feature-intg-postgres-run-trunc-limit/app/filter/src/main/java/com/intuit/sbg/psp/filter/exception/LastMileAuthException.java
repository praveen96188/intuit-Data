package com.intuit.sbg.psp.filter.exception;

/**
 * @author rn5
 * Exception class to indicate Authentication Failures during LMA validation.
 *
 */
public class LastMileAuthException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public LastMileAuthException(String message, Throwable throwable) {
		super(message, throwable);
	}
}

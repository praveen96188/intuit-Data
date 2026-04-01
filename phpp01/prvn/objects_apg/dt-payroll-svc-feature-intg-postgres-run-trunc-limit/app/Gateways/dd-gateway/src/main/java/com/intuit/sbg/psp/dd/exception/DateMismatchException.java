package com.intuit.sbg.psp.dd.exception;

public class DateMismatchException extends Exception {

	   private String message = null;
	   
	    public DateMismatchException() {
	        super();
	    }
	 
	    public DateMismatchException(String message) {
	        super(message);
	        this.message = message;
	    }
	 
	    public DateMismatchException(Throwable cause) {
	        super(cause);
	    }
	 
	    @Override
	    public String toString() {
	        return message;
	    }
	 
	    @Override
	    public String getMessage() {
	        return message;
	    }

}

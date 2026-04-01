package com.intuit.sbg.psp.dd.exception;

public class LimitCheckException extends Exception {

		   private String message = null;
		   
		    public LimitCheckException() {
		        super();
		    }
		 
		    public LimitCheckException(String message) {
		        super(message);
		        this.message = message;
		    }
		 
		    public LimitCheckException(Throwable cause) {
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

package com.intuit.psp.dd.pojo;

import java.util.ArrayList;
import java.util.List;

import com.intuit.pmo.client.model.DirectDepositDate;
import com.intuit.pmo.client.model.DirectDepositDates;
import com.intuit.pmo.client.model.LimitCheckError;
import com.intuit.sbg.psp.dd.util.LimitCheckResponse;

public class LimitResponse {

	private LimitCheckResponse limitCheckResponse;
	private int errorCode;
	private List<LimitCheckError> errorMessages = new ArrayList<LimitCheckError>();
	private DirectDepositDate ddLimitCheckDate;

	public void setDDLimitCheckDate(DirectDepositDates directDepositDates){
		List<DirectDepositDate> directDepositList=directDepositDates.getDirectDepositDates();
		if(directDepositList.size() > 0) {
			ddLimitCheckDate = directDepositList.get(0);
		}
	}
	public DirectDepositDate getDDLimitCheckDate(){
		return ddLimitCheckDate;
	}

	public LimitCheckResponse getLimitCheckResponse() {
		return limitCheckResponse;
	}
	public void setLimitCheckResponse(LimitCheckResponse limitCheckResponse) {
		this.limitCheckResponse = limitCheckResponse;
	}
	public int getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
	public List<LimitCheckError> getErrorMessages() {
		return errorMessages;
	}
	public void setErrorMessages(List<LimitCheckError> errorMessages) {
		this.errorMessages = errorMessages;
	}
	
	

}

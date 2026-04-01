package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;

/**
 * Hand-written business logic
 */
public class EdiTaxFile extends BaseEdiTaxFile {

	/**
	 * Default constructor.
	 */
	public EdiTaxFile()
	{
		super();
	}

    //abstract
    public void setRecordStatus(EdiFileStatus pStatus) {
        setStatusCd(pStatus);
        setStatusEffectiveDate(PSPDate.getPSPTime());
        Application.save(this);
    }

    //abstract
    public void setAcknowledgementFile(EdiTaxFile pAckFile) {
    }

    //abstract
    public void cascadeDelete() {
    }

    public EdiTaxFile updateErrorStatus(String pFileName) {
        setFileName(pFileName);
        setStatusCd(EdiFileStatus.Error);
        setStatusEffectiveDate(PSPDate.getPSPTime());
        return Application.save(this);
    }

    public static EdiTaxFile getEdiFileByFileId(int pFileId) {
        DomainEntitySet<EdiTaxFile> ediFileSet = Application.find(EdiTaxFile.class, FileId().equalTo(pFileId));

        if (ediFileSet.isEmpty()) {
            return null;
        }
        if (ediFileSet.size() > 1) {
            throw new RuntimeException(String.format("More than one EdiTaxFile record was found for FileId %d", pFileId));
        }

        return ediFileSet.get(0);
    }

}
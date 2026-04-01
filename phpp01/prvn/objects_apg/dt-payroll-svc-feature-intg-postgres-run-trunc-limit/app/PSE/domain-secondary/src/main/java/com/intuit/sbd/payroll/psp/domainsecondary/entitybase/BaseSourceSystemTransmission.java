/**
 * --------------------------------------------------------------------------
 * Copyright (c) 2008 Intuit, Inc. All rights reserved.
 * Unauthorized reproduction is a violation of applicable law.
 * --------------------------------------------------------------------------
 *
 * --------------------------------------------------------------------------
 *
 * Author	PSP CodeGen
 * Model Version	1.0
 *
 * --------------------------------------------------------------------------
 */

package com.intuit.sbd.payroll.psp.domainsecondary.entitybase;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.TransmissionType;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission;
import com.intuit.sbd.payroll.psp.query.ScalarProperty;
import com.intuit.sbd.payroll.psp.util.StringFormatter;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.persister.entity.SingleTableEntityPersister;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;

@Entity // Annotate the class with @Entity for compile time BytecodeEnhancement for attribute lazy loading
public class BaseSourceSystemTransmission extends com.intuit.sbd.payroll.psp.DomainEntity
{
    //
    // Host
    //
    private String mHost = null;

    public void setHost(String pHost)
    {
        pHost = StringFormatter.removeNonAsciiCharacters(pHost);
        if (pHost != null && pHost.length() > 200)
        {
            String argStr = pHost.length() > 30 ? pHost.substring(0, 30) + "..." : pHost;
            throw new RuntimeException("String '" + argStr + "' is longer than allowed (200) for pHost");
        }

    	if (pHost != null && pHost.length() == 0)
    	{
    		pHost = null;
    	}

        mHost = pHost;
    }

    public String getHost()
    {
    	return mHost;
    }

    //
    // FromSourceSystem
    //
    private SourceSystemCode mFromSourceSystem = SourceSystemCode.ADE;

    public void setFromSourceSystem(SourceSystemCode pFromSourceSystem)
    {
        mFromSourceSystem = pFromSourceSystem;
    }

    public SourceSystemCode getFromSourceSystem()
    {
    	return mFromSourceSystem;
    }

    //
    // FinalizeDateTime
    //
    private SpcfCalendar mFinalizeDateTime = null;

    public void setFinalizeDateTime(SpcfCalendar pFinalizeDateTime)
    {
        mFinalizeDateTime = pFinalizeDateTime;
    }

    public SpcfCalendar getFinalizeDateTime()
    {
    	return mFinalizeDateTime;
    }

    //
    // RequestToken
    //
    private long mRequestToken = 0L;

    public void setRequestToken(long pRequestToken)
    {
        mRequestToken = pRequestToken;
    }

    public long getRequestToken()
    {
    	return mRequestToken;
    }

    //
    // ResponseToken
    //
    private long mResponseToken = 0L;

    public void setResponseToken(long pResponseToken)
    {
        mResponseToken = pResponseToken;
    }

    public long getResponseToken()
    {
    	return mResponseToken;
    }

    //
    // RequestDocument
    //
    /*
    * RequestDocument variable name is used in place of mRequestDocument to match the property name of class
    * with the mentioned property name in hibernate configuration class. This is required because RequestDocument
    * is being lazy loaded and to support lazy loading in hibernate 3.6 byte code instrumentation has been done
    * which expects class property name to be same as hibernate configuration property name.
    * */
    @Basic(fetch = FetchType.LAZY) // Set this annotation for attribute lazy loading
    private String mRequestDocument = null;

    public void setRequestDocument(String pRequestDocument)
    {
        mRequestDocument = pRequestDocument;
    }

    public String getRequestDocument()
    {
    	return mRequestDocument;
    }

    //
    // ResponseDocument
    //
    /*
     * ResponseDocument variable name is used in place of mResponseDocument to match the property name of class
     * with the mentioned property name in hibernate configuration class. This is required because ResponseDocument
     * is being lazy loaded and to support lazy loading in hibernate 3.6 byte code instrumentation has been done
     * which expects class property name to be same as hibernate configuration property name.
     * */
    @Basic(fetch = FetchType.LAZY) // Set this annotation for attribute lazy loading
    private String mResponseDocument = null;

    public void setResponseDocument(String pResponseDocument)
    {
        mResponseDocument = pResponseDocument;
    }

    public String getResponseDocument()
    {
    	return mResponseDocument;
    }

    //
    // Type
    //
    private TransmissionType mType = TransmissionType.QueryEntitlement;

    public void setType(TransmissionType pType)
    {
        mType = pType;
    }

    public TransmissionType getType()
    {
    	return mType;
    }

    //
    // InitializeDateTime
    //
    private SpcfCalendar mInitializeDateTime = null;

    public void setInitializeDateTime(SpcfCalendar pInitializeDateTime)
    {
        mInitializeDateTime = pInitializeDateTime;
    }

    public SpcfCalendar getInitializeDateTime()
    {
    	return mInitializeDateTime;
    }

    //
    // Description
    //
    private String mDescription = null;

    public void setDescription(String pDescription)
    {
        pDescription = StringFormatter.removeNonAsciiCharacters(pDescription);
        if (pDescription != null && pDescription.length() > 256)
        {
            String argStr = pDescription.length() > 30 ? pDescription.substring(0, 30) + "..." : pDescription;
            throw new RuntimeException("String '" + argStr + "' is longer than allowed (256) for pDescription");
        }

    	if (pDescription != null && pDescription.length() == 0)
    	{
    		pDescription = null;
    	}

        mDescription = pDescription;
    }

    public String getDescription()
    {
    	return mDescription;
    }

    //
    // ToSourceSystem
    //
    private SourceSystemCode mToSourceSystem = SourceSystemCode.ADE;

    public void setToSourceSystem(SourceSystemCode pToSourceSystem)
    {
        mToSourceSystem = pToSourceSystem;
    }

    public SourceSystemCode getToSourceSystem()
    {
    	return mToSourceSystem;
    }

    //
    // TransmissionIdentifier
    //
    private String mTransmissionIdentifier = null;

    public void setTransmissionIdentifier(String pTransmissionIdentifier)
    {
        pTransmissionIdentifier = StringFormatter.removeNonAsciiCharacters(pTransmissionIdentifier);
        if (pTransmissionIdentifier != null && pTransmissionIdentifier.length() > 40)
        {
            String argStr = pTransmissionIdentifier.length() > 30 ? pTransmissionIdentifier.substring(0, 30) + "..." : pTransmissionIdentifier;
            throw new RuntimeException("String '" + argStr + "' is longer than allowed (40) for pTransmissionIdentifier");
        }

    	if (pTransmissionIdentifier != null && pTransmissionIdentifier.length() == 0)
    	{
    		pTransmissionIdentifier = null;
    	}

        mTransmissionIdentifier = pTransmissionIdentifier;
    }

    public String getTransmissionIdentifier()
    {
    	return mTransmissionIdentifier;
    }

    //
    // IPAddress
    //
    private String mIPAddress = null;

    public void setIPAddress(String pIPAddress)
    {
        if (pIPAddress != null && pIPAddress.length() > 20)
        {
            String argStr = pIPAddress.length() > 30 ? pIPAddress.substring(0, 30) + "..." : pIPAddress;
            throw new RuntimeException("String '" + argStr + "' is longer than allowed (20) for pIPAddress");
        }

    	if (pIPAddress != null && pIPAddress.length() == 0)
    	{
    		pIPAddress = null;
    	}

        mIPAddress = pIPAddress;
    }

    public String getIPAddress()
    {
    	return mIPAddress;
    }

    //
    // ApplicationVersion
    //
    private String mApplicationVersion = null;

    public void setApplicationVersion(String pApplicationVersion)
    {
        if (pApplicationVersion != null && pApplicationVersion.length() > 100)
        {
            String argStr = pApplicationVersion.length() > 30 ? pApplicationVersion.substring(0, 30) + "..." : pApplicationVersion;
            throw new RuntimeException("String '" + argStr + "' is longer than allowed (100) for pApplicationVersion");
        }

    	if (pApplicationVersion != null && pApplicationVersion.length() == 0)
    	{
    		pApplicationVersion = null;
    	}

        mApplicationVersion = pApplicationVersion;
    }

    public String getApplicationVersion()
    {
    	return mApplicationVersion;
    }

    //
    // ApplicationId
    //
    private String mApplicationId = null;

    public void setApplicationId(String pApplicationId)
    {
        pApplicationId = StringFormatter.removeNonAsciiCharacters(pApplicationId);
        if (pApplicationId != null && pApplicationId.length() > 100)
        {
            String argStr = pApplicationId.length() > 30 ? pApplicationId.substring(0, 30) + "..." : pApplicationId;
            throw new RuntimeException("String '" + argStr + "' is longer than allowed (100) for pApplicationId");
        }

    	if (pApplicationId != null && pApplicationId.length() == 0)
    	{
    		pApplicationId = null;
    	}

        mApplicationId = pApplicationId;
    }

    public String getApplicationId()
    {
    	return mApplicationId;
    }

    //
    // TaxTableId
    //
    private String mTaxTableId = null;

    public void setTaxTableId(String pTaxTableId)
    {
        pTaxTableId = StringFormatter.removeNonAsciiCharacters(pTaxTableId);
        if (pTaxTableId != null && pTaxTableId.length() > 100)
        {
            String argStr = pTaxTableId.length() > 30 ? pTaxTableId.substring(0, 30) + "..." : pTaxTableId;
            throw new RuntimeException("String '" + argStr + "' is longer than allowed (100) for pTaxTableId");
        }

    	if (pTaxTableId != null && pTaxTableId.length() == 0)
    	{
    		pTaxTableId = null;
    	}

        mTaxTableId = pTaxTableId;
    }

    public String getTaxTableId()
    {
    	return mTaxTableId;
    }

    //
    // CompanyId
    //
    private String mCompanyId = null;

    public void setCompanyId(String pCompanyId)
    {
    	if (pCompanyId != null && pCompanyId.length() == 0)
    	{
    		pCompanyId = null;
    	}

        mCompanyId = pCompanyId;
    }

    public String getCompanyId()
    {
    	return mCompanyId;
    }



    // PSP query support
    public static final ScalarProperty<SourceSystemTransmission, String> Host() {return new ScalarProperty<SourceSystemTransmission, String>(null, "Host");};
    public static final ScalarProperty<SourceSystemTransmission, SourceSystemCode> FromSourceSystem() {return new ScalarProperty<SourceSystemTransmission, SourceSystemCode>(null, "FromSourceSystem");};
    public static final ScalarProperty<SourceSystemTransmission, SpcfCalendar> FinalizeDateTime() {return new ScalarProperty<SourceSystemTransmission, SpcfCalendar>(null, "FinalizeDateTime");};
    public static final ScalarProperty<SourceSystemTransmission, Long> RequestToken() {return new ScalarProperty<SourceSystemTransmission, Long>(null, "RequestToken");};
    public static final ScalarProperty<SourceSystemTransmission, Long> ResponseToken() {return new ScalarProperty<SourceSystemTransmission, Long>(null, "ResponseToken");};
    public static final ScalarProperty<SourceSystemTransmission, String> RequestDocument() {return new ScalarProperty<SourceSystemTransmission, String>(null, "RequestDocument");};
    public static final ScalarProperty<SourceSystemTransmission, String> ResponseDocument() {return new ScalarProperty<SourceSystemTransmission, String>(null, "ResponseDocument");};
    public static final ScalarProperty<SourceSystemTransmission, TransmissionType> Type() {return new ScalarProperty<SourceSystemTransmission, TransmissionType>(null, "Type");};
    public static final ScalarProperty<SourceSystemTransmission, SpcfCalendar> InitializeDateTime() {return new ScalarProperty<SourceSystemTransmission, SpcfCalendar>(null, "InitializeDateTime");};
    public static final ScalarProperty<SourceSystemTransmission, String> Description() {return new ScalarProperty<SourceSystemTransmission, String>(null, "Description");};
    public static final ScalarProperty<SourceSystemTransmission, SourceSystemCode> ToSourceSystem() {return new ScalarProperty<SourceSystemTransmission, SourceSystemCode>(null, "ToSourceSystem");};
    public static final ScalarProperty<SourceSystemTransmission, String> TransmissionIdentifier() {return new ScalarProperty<SourceSystemTransmission, String>(null, "TransmissionIdentifier");};
    public static final ScalarProperty<SourceSystemTransmission, String> IPAddress() {return new ScalarProperty<SourceSystemTransmission, String>(null, "IPAddress");};
    public static final ScalarProperty<SourceSystemTransmission, String> ApplicationVersion() {return new ScalarProperty<SourceSystemTransmission, String>(null, "ApplicationVersion");};
    public static final ScalarProperty<SourceSystemTransmission, String> ApplicationId() {return new ScalarProperty<SourceSystemTransmission, String>(null, "ApplicationId");};
    public static final ScalarProperty<SourceSystemTransmission, String> TaxTableId() {return new ScalarProperty<SourceSystemTransmission, String>(null, "TaxTableId");};
    public static final ScalarProperty<SourceSystemTransmission, String> CompanyId() {return new ScalarProperty<SourceSystemTransmission, String>(null, "CompanyId");};
}
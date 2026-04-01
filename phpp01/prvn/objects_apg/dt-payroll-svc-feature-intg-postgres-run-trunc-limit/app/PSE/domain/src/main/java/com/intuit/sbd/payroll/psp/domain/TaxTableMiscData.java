package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.HibernateException;

/**
 * Hand-written business logic
 */
public class TaxTableMiscData extends BaseTaxTableMiscData {


    private static final SpcfLogger logger = SpcfLogManager.getLogger(TaxTableMiscData.class);
	/**
	 * Default constructor.
	 */
	public TaxTableMiscData()
	{
		super();
	}

    /**
     * @See Method to delete records from the table for the sent empTax_fk.
     * @param pEmptax
     * @return
     */
    public static boolean deleteForNullRecords(EmployeeTax pEmptax) {

        try{
        Criterion<TaxTableMiscData> where =    TaxTableMiscData.EmployeeTax().equalTo(pEmptax)  ;
        DomainEntitySet<TaxTableMiscData> result =  Application.find(TaxTableMiscData.class, new Query<TaxTableMiscData>().Where(where));

        for(TaxTableMiscData miscData: result)  {
            Application.delete(TaxTableMiscData.class,miscData.getId() );
        }
        }catch(HibernateException he){
          logger.error("HibernateException: Error while deleting tax table misc records ");
            logger.error(he.getStackTrace());
            return false;
        }catch (Exception ee ){
        logger.error("Error while deleting tax table misc records ");
        logger.error(ee.getStackTrace());
        return false;
    }
        return true;
    }
}
package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.cache.NaturalKey;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.exception.ConstraintViolationException;
/**
 * Hand-written business logic
 */
public class PstubEmployeePreference extends BasePstubEmployeePreference {

    private static SpcfLogger logger = null;

    static {
        logger = Application.getLogger(PstubEmployeePreference.class);
    }

    //App names
    public static final String VMP = "VMP";

    //Preference names
    public static final String PAYSTUB_NOTIFICATION = "PaystubNotification";

    //Preference values
    public static final String ON = "On";
    public static final String OFF = "Off";

    /**
	 * Default constructor.
	 */
	public PstubEmployeePreference()
	{
		super();
	}

    /**
     * Creates a new Employee Preference
     * @param eePreference
     * @return employee preference - Can be null
     */
    public static PstubEmployeePreference createEmployeePreference(PstubEmployeePreference eePreference) {
        logger.info("Request received to create EmployeePreference");
        if(eePreference == null || eePreference.getEmployee() == null || eePreference.getAppName() == null
            || eePreference.getPreferenceName() == null) {
            logger.info("Invalid create EmployeePreference request");
            //Missing required values
            return null;
        }  else {
            Application.beginUnitOfWork();
            try {
                Application.save(eePreference);
                Application.commitUnitOfWork();
                logger.info("EmployeePreference creation successful");
                return eePreference;
            } catch (ConstraintViolationException e) {
                throw e;
            } catch(Exception e) {
                logger.info("Could not create EmployeePreference. "+e.getMessage());
                return null;
            } finally {
                Application.rollbackUnitOfWork();
            }
        }
    }

    /**
     * Returns the preference value for a given employee, app and preference name combination
     * @param pEmployee
     * @param appName
     * @param preferenceName
     * @return preference value for the given combination - Can be null
     */
    public static String getEmployeePreferenceByName(Employee pEmployee, String appName, String preferenceName) {
        logger.info("Request received to get EmployeePreference Value for Preference="+preferenceName+", for App="+appName);
        PstubEmployeePreference eePref = null;
        if(pEmployee == null || appName == null || preferenceName == null) {
            logger.info("Invalid Get EmployeePreference request");
            return null;
        } else {
            NaturalKey naturalKey = new NaturalKey(PstubEmployeePreference.class, pEmployee.getId(), appName, preferenceName);
            SpcfUniqueId primaryKey = Application.getSessionCache().getPrimaryKey(naturalKey);

            if (primaryKey != null) {
                eePref = Application.findById(PstubEmployeePreference.class, primaryKey);
            } else {
                Expression<PstubEmployeePreference> query = new Query<PstubEmployeePreference>()
                        .Where(PstubEmployeePreference.AppName().equalTo(appName)
                                                      .And(PstubEmployeePreference.Employee().equalTo(pEmployee))
                                                      .And(PstubEmployeePreference.PreferenceName().equalTo(preferenceName)));
                DomainEntitySet<PstubEmployeePreference> eePrefs = Application.find(PstubEmployeePreference.class, query);

                if (eePrefs.size() > 1) {
                    logger.info("Query did not return a unique value for get EmployeePreference by Name");
                    throw new RuntimeException("Query for employee preferences by preference name" + preferenceName + " and app name " + appName + " did not return 0 or 1 results as expected");
                }

                if (!eePrefs.isEmpty()) {
                    eePref = eePrefs.get(0);
                    Application.getSessionCache().addPrimaryKey(naturalKey, eePref.getId());
                }
            }
            if(eePref != null) {
                logger.info("Found EmployeePreference Value");
                return eePref.getPreferenceValue();
            } else {
                logger.info("Could not find EmployeePreference Value");
                return null; //Could not find a pref for the given combination
            }

        }
    }

    public static List<PstubEmployeePreference> getCompanyEmployeePreferencesByApp(String companyRealmId, SpcfUniqueId employeeId, String appName) {
        List<PstubEmployeePreference> returnList = null;
        Expression<PstubEmployeePreference> query = new Query<PstubEmployeePreference>()
                .Where(PstubEmployeePreference.Employee().Company().IAMRealmId().equalTo(companyRealmId)
                .And(PstubEmployeePreference.Employee().Id().equalTo(employeeId))
                .And(PstubEmployeePreference.AppName().equalTo(appName)));
        DomainEntitySet<PstubEmployeePreference> employeePreferences = Application.find(PstubEmployeePreference.class, query);
        if(employeePreferences == null) {
            logger.info("Could not find employee preferences for companyRealmId=" + companyRealmId + " employeeId=" + employeeId + " appName=" + appName);

        } else {
            logger.info("Found all preferences for companyRealmId=" + companyRealmId + " employeeId=" + employeeId + " appName=" + appName);
            returnList = new ArrayList<PstubEmployeePreference>(employeePreferences);
        }
        return returnList;
    }

    /**
     * Returns a list of all preferences for given employee for a given app
     * @param pEmployee
     * @param appName
     * @return List of all preferences for a given employee and app. - Can be null
     */
    public static List<PstubEmployeePreference> getEmployeePreferencesByApp(Employee pEmployee, String appName) {
        if(pEmployee == null || appName == null) {
            logger.info("Invalid get employee preferences request. Employee="+pEmployee+", appName="+appName);
            return null;
        } else {
            logger.info("Request received to get all preferences for App="+appName+", by EmployeeId="+pEmployee.getId().toString());
            Expression<PstubEmployeePreference> query = new Query<PstubEmployeePreference>()
                    .Where(PstubEmployeePreference.Employee().equalTo(pEmployee)
                    .And(PstubEmployeePreference.AppName().equalTo(appName)));
            DomainEntitySet<PstubEmployeePreference> pstubEmployeePreferenceDomainEntitySet =
                    Application.find(PstubEmployeePreference.class, query);
            if(pstubEmployeePreferenceDomainEntitySet == null) {
                logger.info("Could not find all preferences for App="+appName+", by EmployeeId="+pEmployee.getId().toString());
                return null;
            } else {
                logger.info("Found all preferences for App="+appName+", by EmployeeId="+pEmployee.getId().toString());
                List<PstubEmployeePreference> eePrefs = new ArrayList<PstubEmployeePreference>();
                eePrefs.addAll(pstubEmployeePreferenceDomainEntitySet);
                return eePrefs;
            }
        }
    }

    /**
     * Update the preference value. If there is no existing preference, it creates a new one.
     * @param updatedPreference
     * @return Updated preference or new preference (If none exists before) - Can be null
     */
    public static PstubEmployeePreference updateEmployeePreference(PstubEmployeePreference updatedPreference) {
        logger.info("Request received to update EmployeePreference");
        PstubEmployeePreference oldPreference = null;
        if(updatedPreference == null || updatedPreference.getEmployee() == null || updatedPreference.getAppName() == null
                || updatedPreference.getPreferenceName() == null) {
            //Missing required values. Cannot update without these.
            logger.info("Invalid request to update EmployeePreference");
            return null;
        } else {
            Application.beginUnitOfWork();
            Expression<PstubEmployeePreference> query = new Query<PstubEmployeePreference>()
                    .Where(PstubEmployeePreference.AppName().equalTo(updatedPreference.getAppName())
                                                  .And(PstubEmployeePreference.Employee().equalTo(updatedPreference.getEmployee()))
                                                  .And(PstubEmployeePreference.PreferenceName().equalTo(updatedPreference.getPreferenceName())));
            DomainEntitySet<PstubEmployeePreference> eePrefs = Application.find(PstubEmployeePreference.class, query);
            if (eePrefs.size() > 1) {
                logger.info("Could not find a unique value to udpate EmployeePreference");
                throw new RuntimeException("Query for employee preferences by preference name" +
                                                   updatedPreference.getPreferenceName() + " and app name " +
                                                   updatedPreference.getAppName() + " did not return 0 or 1 results as expected");
            }

            if (!eePrefs.isEmpty()) {
                oldPreference = eePrefs.get(0);
                oldPreference.setPreferenceValue(updatedPreference.getPreferenceValue());
                try {
                    oldPreference = Application.save(oldPreference);
                    Application.commitUnitOfWork();
                } catch(Exception e) {
                    logger.info("Could not update EmployeePreference due to "+e.getMessage());
                    Application.rollbackUnitOfWork();
                    return null;
                }
            } else {
                logger.info("Could not find the EmployeePreference. So create a new preference instead");
                //Did not find one. So create one.  Also, roll back the old transaction
                Application.rollbackUnitOfWork();
                oldPreference = createEmployeePreference(updatedPreference);
            }
        }
        logger.info("Update or Create EmployeePreference successful");
        return oldPreference;
    }

    /**
     * Deletes a preference
     * @param eePref
     * @return true is deletion is successful and false otherwise
     */
    public static boolean deleteEmployeePreference(PstubEmployeePreference eePref) {
        logger.info("Request received to delete EmployeePreference");
        if(eePref == null || eePref.getPreferenceName() == null
                || eePref.getAppName() == null
                || eePref.getEmployee() == null ) {
            logger.info("Invalid request to delete EmployeePreference");
            return false; //Missing required values
        } else {
            Application.beginUnitOfWork();
            try {
                Application.delete(eePref);
                Application.commitUnitOfWork();
                logger.info("Delete EmployeePreference successful");
                return true;
            } catch(Exception e) {
                logger.info("Could not delete EmployeePreference due to "+e.getMessage());
                Application.rollbackUnitOfWork();
                return false;
            }
        }
    }
}
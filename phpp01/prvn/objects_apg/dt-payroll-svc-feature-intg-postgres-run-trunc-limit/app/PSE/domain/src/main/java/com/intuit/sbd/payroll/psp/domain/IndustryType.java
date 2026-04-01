package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Query;

import java.util.*;

/**
 * Hand-written business logic
 */
public class IndustryType extends BaseIndustryType {
    static String cacheKey = "CachedIndustryTypes";

    private static SpcfLogger logger = Application.getLogger(IndustryType.class);

    public static Map<String, String> QUICKBOOKS_INDUSTRY_STANDARD_INDUSTRY_NAME_MAP = getQuickbooksIndustryStandardIndustryMap();

    public static List<String> LEGACY_INDUSTRY_NAMES = getLegacyIndustryNames();

    public static List<String> QUICKBOOKS_INDUSTRY_NAMES = getQuickbooksIndustryNames();

    /**
     * Default constructor.
     */
    public IndustryType()
    {
        super();
    }

    public static IndustryType findIndustryType(String industry) {
        String industryName = getIndustryName(industry);

        DomainEntitySet<IndustryType> industryTypes;
        if (Application.getSessionCache().isDataObjectCollectionCached(IndustryType.class, cacheKey)) {
            industryTypes = Application.getSessionCache().getDataObjectCollection(IndustryType.class, cacheKey);
        } else {
            industryTypes = Application.findObjects(IndustryType.class);
            Application.getSessionCache().addDataObjectCollection(IndustryType.class, cacheKey, industryTypes);
        }
        industryTypes = industryTypes.find(IndustryType.Industry().equalTo(industryName));
        if (industryTypes.size() > 1) {
            throw new RuntimeException("More than one IndustryType is found for Industry:" + industryName);
        }
        return industryTypes.getFirst();
    }

    public static List<String> getAllIndustryTypes(){
        String queryString = new String("Select Industry from com.intuit.sbd.payroll.psp.domain.IndustryType order by Industry");
        Query query = Application.getHibernateSession().createQuery(queryString);
        List<String> returnList = query.list();
        if(returnList != null){
            return returnList;
        }
        return new ArrayList<String>();
    }

    public static String getIndustryName(String industryName) {
        String standardIndustryName = getStandardIndustryName(industryName);

        if(StringUtils.isNotEmpty(standardIndustryName)) {
            logger.info(String.format("Found a matching StandardIndustryName=%s for a LegacyIndustryName=%s", standardIndustryName, industryName));
            return standardIndustryName;
        }

        return industryName;
    }

    public static String getStandardIndustryName(String quickbooksIndustryName) {
        return QUICKBOOKS_INDUSTRY_STANDARD_INDUSTRY_NAME_MAP.get(quickbooksIndustryName);
    }

    public static boolean isQuickbooksIndustry(String industryName) {
        return QUICKBOOKS_INDUSTRY_NAMES.contains(industryName);
    }

    public static Map<String, String> getQuickbooksIndustryStandardIndustryMap() {
        Map<String, String> quickbooksIndustryStandardIndustryMap = new HashMap<>();

        //Updated Existing PSP Industry Name
        quickbooksIndustryStandardIndustryMap.put("Accounting or Bookkeeping", "Accounting, Auditing, and Bookkeeping Services");
        quickbooksIndustryStandardIndustryMap.put("Advertising or Public Relations", "Advertising Services");
        quickbooksIndustryStandardIndustryMap.put("Agriculture, Ranching, or Farming", "Agricultural Co-operatives");
        quickbooksIndustryStandardIndustryMap.put("Art, Writing, or Photography", "Commercial Photography, Art, and Graphics");
        quickbooksIndustryStandardIndustryMap.put("Automotive Sales or Repair", "Automotive Body Repair Shops");
        quickbooksIndustryStandardIndustryMap.put("Construction General Contractor", "Construction Materials (Not Elsewhere Classified)");
        quickbooksIndustryStandardIndustryMap.put("Construction Trades (Plumber, Electrician, HVAC,...)", "Heating, Plumbing, and Air Conditioning Contractors");
        quickbooksIndustryStandardIndustryMap.put("Hair Salon, Beauty Salon, or Barber Shop", "Beauty and Barber Shops");
        quickbooksIndustryStandardIndustryMap.put("Information Technology (Computers, Software)", "Computer Maintenance, Repair and Services (Not Elsewhere Classified)");
        quickbooksIndustryStandardIndustryMap.put("Insurance Agency or Broker", "Insurance Sales, Underwriting, and Premiums");
        quickbooksIndustryStandardIndustryMap.put("Lawn Care or Landscaping", "Landscaping and Horticultural Services");
        quickbooksIndustryStandardIndustryMap.put("Legal Services", "Legal Services and Attorneys");;
        quickbooksIndustryStandardIndustryMap.put("Lodging (Hotel, Motel)", "Lodging - Hotels, Motels, Resorts, Central Reservation Services");
        quickbooksIndustryStandardIndustryMap.put("Medical, Dental, or Health Service", "Doctors and Physicians (Not Elsewhere Classified)");
        quickbooksIndustryStandardIndustryMap.put("Rental", "Recreation Services (Not Elsewhere Classified)");
        quickbooksIndustryStandardIndustryMap.put("Repair Maintenance", "Miscellaneous Repair Shops and Related Services");
        quickbooksIndustryStandardIndustryMap.put("Restaurant, Caterer, or Bar", "Eating Places and Restaurants");
        quickbooksIndustryStandardIndustryMap.put("Transportation, Trucking, or Delivery", "Taxicabs and Limousines");
        quickbooksIndustryStandardIndustryMap.put("Wholesale Distribution and Sales", "Industrial Supplies (Not Elsewhere Classified)");
        quickbooksIndustryStandardIndustryMap.put("General Product-based Business",  "Business Services (Not Elsewhere Classified)");

        //Duplicate SIC code - Potential Matching SIC code found
        quickbooksIndustryStandardIndustryMap.put("Church or Religious Organization",	"Religious Organizations");
        quickbooksIndustryStandardIndustryMap.put("Design, Architecture, Engineering",	"Architectural, Engineering, and Surveying Services");
        quickbooksIndustryStandardIndustryMap.put("Real Estate Brokerage or Developer",	"Real Estate Agents and Managers - Rentals");

        //Duplicate SIC Code - Update PSP Industry SIC code description
        quickbooksIndustryStandardIndustryMap.put("Manufacturing", "Durable Goods (Not Elsewhere Classified)");
        quickbooksIndustryStandardIndustryMap.put("Non-Profit", "Charitable and Social Service Organizations");
        quickbooksIndustryStandardIndustryMap.put("Professional Consulting", "Professional Services (Not Elsewhere Classified)");
        quickbooksIndustryStandardIndustryMap.put("Retail Shop or Online Commerce", "Miscellaneous General Merchandise");

        return quickbooksIndustryStandardIndustryMap;
    }

    public static List<String> getQuickbooksIndustryNames() {
        List<String> quickbooksIndustryNames =  new ArrayList<>();
        quickbooksIndustryNames.addAll(QUICKBOOKS_INDUSTRY_STANDARD_INDUSTRY_NAME_MAP.keySet());
        quickbooksIndustryNames.addAll(LEGACY_INDUSTRY_NAMES);
        return quickbooksIndustryNames;
    }

    public static List<String> getLegacyIndustryNames() {
        List<String> legacyIndustryNames = new ArrayList<>();
        legacyIndustryNames.add("Financial Services other than Accounting or Bookkeeping");
        legacyIndustryNames.add("Manufacturer Representative or Agent");
        legacyIndustryNames.add("Property Management or Home Association");
        legacyIndustryNames.add("Sales: Independent Agent");
        legacyIndustryNames.add("General Service-based Business");
        legacyIndustryNames.add("Other/None");
        return legacyIndustryNames;
    }

    public static String getCaseSensitiveIndustry(String caseInsensitiveIndustry) {
        if(Objects.isNull(caseInsensitiveIndustry))
            return null;
        List<String> industryNames = getAllIndustryTypes();
        for (String industryName: industryNames) {
            if(StringUtils.equalsIgnoreCase(industryName.trim(), caseInsensitiveIndustry.trim())) {
                return industryName;
            }
        }
        logger.error("Not Found a matching industry="+ caseInsensitiveIndustry);

        return null;
    }

}
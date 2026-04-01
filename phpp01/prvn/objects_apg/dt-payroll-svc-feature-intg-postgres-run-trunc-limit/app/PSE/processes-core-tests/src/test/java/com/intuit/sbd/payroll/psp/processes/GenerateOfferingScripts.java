
package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Created with IntelliJ IDEA.
 * User: jjones1
 * Date: 1/22/13
 * Time: 6:13 PM
 */

public class GenerateOfferingScripts {

    private static final String OFFERING_INSERT_SCRIPT = "INSERT INTO TEMP_PSP_OFFERING (OFFERING_SEQ, VERSION, CREATED_DATE, MODIFIED_DATE, S_K_U, NAME, DESCRIPTION, OFFERING_CODE, SERVICE_CODE, IS_APPROVED, REALM_ID, LIMIT_RULE_FK, FRAUD_RULE_FK, REPORTING_TYPE)\n" +
            "VALUES ('%s', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '%s', '%s', '%s', '%s', '%s', %s, -1, '%s', '%s', '%s')\n" +
            "/\n\n";
    private static final String OFFERING_SERVICE_CHARGE_GROUP_INSERT_SCRIPT = "INSERT INTO TEMP_PSP_OFFERING_SVCCHG_GRP (OFFERING_SVCCHG_GRP_SEQ, VERSION, CREATED_DATE, MODIFIED_DATE, NAME, DESCRIPTION, APPLIES_TO, OFFERING_FK, REALM_ID)\n"+
            "VALUES ( '%s', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '%s', '%s', '%s', '%s', -1)\n"+
            "/\n\n";
    private static final String OFFERING_SERVICE_CHARGE_INSERT_SCRIPT = "INSERT INTO TEMP_PSP_OFFERING_SVCCHG (OFFERING_SVCCHG_SEQ, VERSION, CREATED_DATE, MODIFIED_DATE, S_K_U, SKU_TYPE, IS_TIER, TIER_NUMBER, TIER_UNITS, OFFERING_SVCCHG_GRP_FK, REALM_ID) \n"+
            "VALUES ('%s' , 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '%s', '%s', %s, %s, %s, '%s', -1)\n"+
            "/\n\n";
    private static final String SERVICE_CHARGE_PRICE_INSERT_SCRIPT = "INSERT INTO TEMP_PSP_SVCCHGPRICE (SVCCHGPRICE_SEQ, VERSION, CREATED_DATE, MODIFIED_DATE, BASE_PRICE, UNIT_PRICE, EFFECTIVE_DATE, OFFERING_SERVICE_CHARGE_FK, REALM_ID) \n"+
            "VALUES ('%s', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, %s, %s, TIMESTAMP '%s', '%s', -1)\n"+
            "/\n\n";
    private static final String OFFER_SERVICE_CHARGE_ASSOC_INSERT_SCRIPT = "INSERT INTO TEMP_PSP_OFFER_SVCCHG_ASSOC (OFFER_FK, OFFERING_SERVICE_CHARGE_FK) \n"+
            "VALUES ( '%s', '%s')\n"+
            "/\n\n";
    private static final String ENTITLEMENT_OFFERING_INSERT_SCRIPT = "INSERT INTO TMP_PSP_ENTITLEMENT_CD_OF (ENTITLEMENT_CODE_OFFERING_SEQ, CREATED_DATE, MODIFIED_DATE, SERVICE_CD, EFFECTIVE_DATE, OFFERING_FK, ENTITLEMENT_CODE_FK, IS_DEFAULT, PRICE_TYPE, VERSION)\n" +
            "VALUES ('%s', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '%s', TIMESTAMP '%s', '%s', '%s', %s, '%s', 0)\n" +
            "/\n\n";
    private static final String COMMIT = "--\n"+
            "-- %s\n"+
            "--\n";

    private static final String[] SHEET_1_HEADERS = {"OFFERING_CODE", "S_K_U", "NAME", "DESCRIPTION", "IS_APPROVED", "REPORTING_TYPE", "SERVICE_CODE", "FRAUD_RULE_FK", "LIMIT_RULE_FK", "OFFERING_SEQ"};
    private static final String[] SHEET_2_HEADERS = {"OFFERING_CODE", "APPLIES_TO", "NAME", "DESCRIPTION", "OFFERING_SVCCHG_GRP_SEQ"};
    private static final String[] SHEET_3_HEADERS = {"OFFERING_CODE", "APPLIES_TO", "S_K_U", "TIER_NUMBER", "IS_TIER", "TIER_UNITS", "SKU_TYPE", "OFFERING_SVCCHG_SEQ"};
    private static final String[] SHEET_4_HEADERS = {"OFFERING_CODE", "APPLIES_TO", "S_K_U", "TIER_NUMBER","BASE_PRICE", "UNIT_PRICE","EFFECTIVE_DATE", "SVCCHGPRICE_SEQ"};
    private static final String[] SHEET_5_HEADERS = {"OFFER_CD", "OFFERING_CODE", "APPLIES_TO", "S_K_U", "TIER_NUMBER", "OFFER_SEQ"};
    private static final String[] SHEET_6_HEADERS = {"OFFERING_CODE", "ASSET_ITEM_CD", "EDITION_TYPE", "NUMBER_OF_EMPLOYEES_TYPE", "ASSET_ITEM_NUMBER", "SERVICE_CODE", "EFFECTIVE_DATE", "PRICE_TYPE", "IS_DEFAULT", "ENTITLEMENT_CODE_OFFERING_SEQ"};

    private HSSFSheet mSheet1;
    private HSSFSheet mSheet2;
    private HSSFSheet mSheet3;
    private HSSFSheet mSheet4;
    private HSSFSheet mSheet5;
    private HSSFSheet mSheet6;

    private int mSheet1RowCount = 0;
    private int mSheet2RowCount = 0;
    private int mSheet3RowCount = 0;
    private int mSheet4RowCount = 0;
    private int mSheet5RowCount = 0;
    private int mSheet6RowCount = 0;

    private HSSFWorkbook mMyWorkBook;

    private ArrayList<String> mKeyList;

    public static void main(String[] args) {
        GenerateOfferingScripts generateOfferAssoc = new GenerateOfferingScripts();
        try {
            PayrollServices.beginUnitOfWork();

            if (args.length == 0) {
                generateOfferAssoc.generateFile();
            } else {
                generateOfferAssoc.processFile(args[0]);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private void generateFile() throws Exception {
        mMyWorkBook = new HSSFWorkbook();

        mSheet1 = mMyWorkBook.createSheet("Offering");
        mSheet2 = mMyWorkBook.createSheet("OfferingServiceChargeGroup");
        mSheet3 = mMyWorkBook.createSheet("OfferingServiceCharge");
        mSheet4 = mMyWorkBook.createSheet("OfferingServiceChargePrice");
        mSheet5 = mMyWorkBook.createSheet("OfferServiceChargeAssoc");
        mSheet6 = mMyWorkBook.createSheet("EntitlementCodeOffering");

        populateOfferingRow();
        populateOfferServiceChargeAssocSheet();

        resizeColumns();

        writeWorkBook("C:/OfferingsExport" + ".xls");
    }

    private void populateOfferingRow() {
        DomainEntitySet<Offering> offerings = Application.find(Offering.class).sort(Offering.OfferingCode());
        for ( Offering offering : offerings) {
            HSSFRow myRow;
            int sheet1CellCount = 0;

            if (mSheet1RowCount == 0) {
                myRow = mSheet1.createRow(mSheet1RowCount++);
                for (String header : SHEET_1_HEADERS) {
                    myRow.createCell(sheet1CellCount++).setCellValue(header);
                }
            }

            sheet1CellCount = 0;
            myRow = mSheet1.createRow(mSheet1RowCount++);

            if (offering.getOfferingCode() != null) {
                myRow.createCell(sheet1CellCount++).setCellValue(offering.getOfferingCode().toString());
            } else {
                myRow.createCell(sheet1CellCount++).setCellValue("");
            }

            myRow.createCell(sheet1CellCount++).setCellValue(offering.getSKU());
            myRow.createCell(sheet1CellCount++).setCellValue(offering.getName());
            myRow.createCell(sheet1CellCount++).setCellValue(offering.getDescription());
            myRow.createCell(sheet1CellCount++).setCellValue(offering.getIsApproved());

            if (offering.getReportingType() != null) {
                myRow.createCell(sheet1CellCount++).setCellValue(offering.getReportingType().toString());
            } else {
                myRow.createCell(sheet1CellCount++).setCellValue("");
            }

            if (offering.getServiceCode() != null) {
                myRow.createCell(sheet1CellCount++).setCellValue(offering.getServiceCode().toString());
            } else {
                myRow.createCell(sheet1CellCount++).setCellValue("");
            }

            if (offering.getFraudRule() != null) {
                myRow.createCell(sheet1CellCount++).setCellValue(offering.getFraudRule().getId().toString());
            } else {
                myRow.createCell(sheet1CellCount++).setCellValue("");
            }

            if (offering.getLimitRule() != null) {
                myRow.createCell(sheet1CellCount++).setCellValue(offering.getLimitRule().getId().toString());
            } else {
                myRow.createCell(sheet1CellCount++).setCellValue("");
            }

            myRow.createCell(sheet1CellCount).setCellValue(offering.getId().toString());

            populateOfferingServiceChargeGroupRow(offering);
            populateEntitlementCodeOfferingRow(offering);
        }
    }

    private void populateOfferingServiceChargeGroupRow(Offering pOffering) {
        DomainEntitySet<OfferingServiceChargeGroup> offeringServiceChargeGroups = findOfferingServiceChargeGroup(pOffering)
                .sort(OfferingServiceChargeGroup.AppliesTo());
        for (OfferingServiceChargeGroup offeringServiceChargeGroup : offeringServiceChargeGroups) {
            HSSFRow myRow;
            int sheet2CellCount = 0;

            if (mSheet2RowCount == 0) {
                myRow = mSheet2.createRow(mSheet2RowCount++);
                for (String header : SHEET_2_HEADERS) {
                    myRow.createCell(sheet2CellCount++).setCellValue(header);
                }
            }

            sheet2CellCount = 0;
            myRow = mSheet2.createRow(mSheet2RowCount++);

            myRow.createCell(sheet2CellCount++).setCellValue(pOffering.getOfferingCode().toString());
            myRow.createCell(sheet2CellCount++).setCellValue(offeringServiceChargeGroup.getAppliesTo().toString());
            myRow.createCell(sheet2CellCount++).setCellValue(offeringServiceChargeGroup.getName());
            myRow.createCell(sheet2CellCount++).setCellValue(offeringServiceChargeGroup.getDescription());
            myRow.createCell(sheet2CellCount).setCellValue(offeringServiceChargeGroup.getId().toString());

            populateOfferingServiceChargeRow(pOffering, offeringServiceChargeGroup);
        }
    }

    private void populateOfferingServiceChargeRow(Offering pOffering, OfferingServiceChargeGroup pOfferingServiceChargeGroup) {
        DomainEntitySet<OfferingServiceCharge> offeringServiceCharges = findOfferingServiceCharge(pOfferingServiceChargeGroup)
                .sort(OfferingServiceCharge.SKU(), OfferingServiceCharge.TierNumber());
        for (OfferingServiceCharge offeringServiceCharge : offeringServiceCharges) {
            HSSFRow myRow;
            int sheet3CellCount = 0;

            if (mSheet3RowCount == 0) {
                myRow = mSheet3.createRow(mSheet3RowCount++);
                for (String header : SHEET_3_HEADERS) {
                    myRow.createCell(sheet3CellCount++).setCellValue(header);
                }
            }

            sheet3CellCount = 0;
            myRow = mSheet3.createRow(mSheet3RowCount++);

            myRow.createCell(sheet3CellCount++).setCellValue(pOffering.getOfferingCode().toString());
            myRow.createCell(sheet3CellCount++).setCellValue(pOfferingServiceChargeGroup.getAppliesTo().toString());
            myRow.createCell(sheet3CellCount++).setCellValue(offeringServiceCharge.getSKU());
            myRow.createCell(sheet3CellCount++).setCellValue(offeringServiceCharge.getTierNumber());
            myRow.createCell(sheet3CellCount++).setCellValue(offeringServiceCharge.getIsTier());
            myRow.createCell(sheet3CellCount++).setCellValue(offeringServiceCharge.getTierUnits());
            myRow.createCell(sheet3CellCount++).setCellValue(offeringServiceCharge.getSkuType().toString());
            myRow.createCell(sheet3CellCount).setCellValue(offeringServiceCharge.getId().toString());

            populateOfferingServiceChargePriceRow(pOffering, pOfferingServiceChargeGroup, offeringServiceCharge);
        }
    }

    private void populateOfferingServiceChargePriceRow(Offering pOffering, OfferingServiceChargeGroup pOfferingServiceChargeGroup, OfferingServiceCharge pOfferingServiceCharge) {
        DomainEntitySet<OfferingServiceChargePrice> offeringServiceChargePrices = findOfferingServiceChargePrice(pOfferingServiceCharge)
                .sort(OfferingServiceChargePrice.BasePrice(), OfferingServiceChargePrice.UnitPrice());
        for (OfferingServiceChargePrice offeringServiceChargePrice : offeringServiceChargePrices) {
            HSSFRow myRow;
            int sheet4CellCount = 0;

            if (mSheet4RowCount == 0) {
                myRow = mSheet4.createRow(mSheet4RowCount++);
                for (String header : SHEET_4_HEADERS) {
                    myRow.createCell(sheet4CellCount++).setCellValue(header);
                }
            }

            sheet4CellCount = 0;
            myRow = mSheet4.createRow(mSheet4RowCount++);

            myRow.createCell(sheet4CellCount++).setCellValue(pOffering.getOfferingCode().toString());
            myRow.createCell(sheet4CellCount++).setCellValue(pOfferingServiceChargeGroup.getAppliesTo().toString());
            myRow.createCell(sheet4CellCount++).setCellValue(pOfferingServiceCharge.getSKU());
            myRow.createCell(sheet4CellCount++).setCellValue(pOfferingServiceCharge.getTierNumber());
            myRow.createCell(sheet4CellCount++).setCellValue(offeringServiceChargePrice.getBasePrice().toString());
            myRow.createCell(sheet4CellCount++).setCellValue(offeringServiceChargePrice.getUnitPrice().toString());
            myRow.createCell(sheet4CellCount++).setCellValue(offeringServiceChargePrice.getEffectiveDate().format("yyyy-MM-dd HH:mm:ss.SS"));
            myRow.createCell(sheet4CellCount).setCellValue(offeringServiceChargePrice.getId().toString());
        }
    }

    private void populateEntitlementCodeOfferingRow(Offering pOffering) {
        DomainEntitySet<EntitlementCodeOffering> entitlementCodeOfferings = Application.find(EntitlementCodeOffering.class).find(EntitlementCodeOffering.Offering().equalTo(pOffering)).sort(EntitlementCodeOffering.EntitlementCode().AssetItemCd(),
                                                                                                                                                                                             EntitlementCodeOffering.EntitlementCode().EditionType(),
                                                                                                                                                                                             EntitlementCodeOffering.EntitlementCode().NumberOfEmployeesType(),
                                                                                                                                                                                             EntitlementCodeOffering.EntitlementCode().AssetItemNumber());
        for (EntitlementCodeOffering entitlementCodeOffering : entitlementCodeOfferings) {
            HSSFRow myRow;
            int sheet6CellCount = 0;

            if (mSheet6RowCount == 0) {
                myRow = mSheet6.createRow(mSheet6RowCount++);
                for (String header : SHEET_6_HEADERS) {
                    myRow.createCell(sheet6CellCount++).setCellValue(header);
                }
            }

            EntitlementCode entitlementCode = entitlementCodeOffering.getEntitlementCode();

            sheet6CellCount = 0;
            myRow = mSheet6.createRow(mSheet6RowCount++);

            if (pOffering.getOfferingCode() != null) {
                myRow.createCell(sheet6CellCount++).setCellValue(pOffering.getOfferingCode().toString());
            } else {
                myRow.createCell(sheet6CellCount++).setCellValue("");
            }

            myRow.createCell(sheet6CellCount++).setCellValue(entitlementCode.getAssetItemCd().toString());

            if (entitlementCode.getEditionType() != null) {
                myRow.createCell(sheet6CellCount++).setCellValue(entitlementCode.getEditionType().toString());
            } else {
                myRow.createCell(sheet6CellCount++).setCellValue("");
            }

            if (entitlementCode.getNumberOfEmployeesType() != null) {
                myRow.createCell(sheet6CellCount++).setCellValue(entitlementCode.getNumberOfEmployeesType().toString());
            } else {
                myRow.createCell(sheet6CellCount++).setCellValue("");
            }

            if (entitlementCode.getAssetItemNumber() != null) {
                myRow.createCell(sheet6CellCount++).setCellValue(entitlementCode.getAssetItemNumber());
            } else {
                myRow.createCell(sheet6CellCount++).setCellValue("");
            }

            if (entitlementCodeOffering.getServiceCd() != null) {
                myRow.createCell(sheet6CellCount++).setCellValue(entitlementCodeOffering.getServiceCd().toString());
            } else {
                myRow.createCell(sheet6CellCount++).setCellValue("");
            }

            if (entitlementCodeOffering.getEffectiveDate() != null) {
                myRow.createCell(sheet6CellCount++).setCellValue(entitlementCodeOffering.getEffectiveDate().format("yyyy-MM-dd HH:mm:ss.SS"));
            } else {
                myRow.createCell(sheet6CellCount++).setCellValue("");
            }

            if (entitlementCodeOffering.getPriceType() != null) {
                myRow.createCell(sheet6CellCount++).setCellValue(entitlementCodeOffering.getPriceType());
            } else {
                myRow.createCell(sheet6CellCount++).setCellValue("");
            }

            myRow.createCell(sheet6CellCount++).setCellValue(entitlementCodeOffering.getIsDefault());
            myRow.createCell(sheet6CellCount).setCellValue(entitlementCodeOffering.getId().toString());
        }
    }

    private void populateOfferServiceChargeAssocSheet() {
        if (mKeyList == null) {
            mKeyList = new ArrayList<String>();
        }

        DomainEntitySet<Offer> offers = Application.find(Offer.class).sort(Offer.OfferCd());
        for (Offer offer : offers) {
            HSSFRow myRow;
            int sheet5CellCount = 0;

            if (mSheet5RowCount == 0) {
                myRow = mSheet5.createRow(mSheet5RowCount++);
                for (String header : SHEET_5_HEADERS) {
                    myRow.createCell(sheet5CellCount++).setCellValue(header);
                }
            }

            DomainEntitySet<OfferingServiceCharge> offeringServiceCharges = offer.getOfferingServiceChargeCollection().sort(OfferingServiceCharge.OfferingServiceChargeGroup().Offering().OfferingCode(),
                                                                                                                            OfferingServiceCharge.OfferingServiceChargeGroup().AppliesTo(),
                                                                                                                            OfferingServiceCharge.SKU(),
                                                                                                                            OfferingServiceCharge.TierNumber());
            for (OfferingServiceCharge offeringServiceCharge : offeringServiceCharges) {

                String offerCode = offer.getOfferCd();
                String offeringCode = offeringServiceCharge.getOfferingServiceChargeGroup().getOffering().getOfferingCode().toString();
                String appliesTo = offeringServiceCharge.getOfferingServiceChargeGroup().getAppliesTo().toString();
                String sku = offeringServiceCharge.getSKU();
                String tierNumber = String.valueOf(offeringServiceCharge.getTierNumber());

                String key = String.format("%s_%s_%s_%s_%s", offerCode, offeringCode, appliesTo, sku, tierNumber);

                if (!mKeyList.contains(key)) {
                    sheet5CellCount = 0;
                    myRow = mSheet5.createRow(mSheet5RowCount++);
                    myRow.createCell(sheet5CellCount++).setCellValue(offerCode);
                    myRow.createCell(sheet5CellCount++).setCellValue(offeringCode);
                    myRow.createCell(sheet5CellCount++).setCellValue(appliesTo);
                    myRow.createCell(sheet5CellCount++).setCellValue(sku);
                    myRow.createCell(sheet5CellCount++).setCellValue(tierNumber);
                    myRow.createCell(sheet5CellCount).setCellValue(offer.getId().toString());
                    mKeyList.add(key);
                }
            }
        }
    }

    private void processFile(String pFile) throws Exception {
        InputStream inputStream = new FileInputStream(pFile);
        Workbook workbook = WorkbookFactory.create(inputStream);

        TreeMap<String, String> offeringSeqMap = processOfferingSheet(workbook);
        TreeMap<String, String> offeringServiceChargeGroupMap = processOfferingServiceChargeGroupSheet(workbook, offeringSeqMap);
        TreeMap<String, String> offeringServiceChargeMap = processOfferingServiceChargeSheet(workbook, offeringServiceChargeGroupMap);
        processOfferingServiceChargePriceSheet(workbook, offeringServiceChargeMap);
        processOfferServiceChargeAssocSheet(workbook, offeringServiceChargeMap);
        processEntitlementCodeOfferingSheet(workbook, offeringSeqMap);

    }

    private TreeMap<String, String> processOfferingSheet(Workbook pWorkbook) throws Exception{
        //Offering
        Sheet sheet = pWorkbook.getSheetAt(0);
        StringBuilder stringBuilder = new StringBuilder();
        TreeMap<String, String> sqlMap = new TreeMap<String, String>();
        TreeMap<String, String> offeringSeqMap = new TreeMap<String, String>();
        for (Row row : sheet) {
            if (row.getRowNum() == 0) {
                //Skip header record
                continue;
            }

            String offeringSeq;
            String sku = "";
            String name = "";
            String description = "";
            String offeringCode = "";
            String serviceCode = "";
            String isApproved = "";
            String limitRule = "";
            String fraudRule = "";
            String reportingType = "";

            Cell cell = row.getCell(0, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                offeringCode = OfferingCode.valueOf(getCellValue(cell)).toString();
            }

            cell = row.getCell(1, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                sku = getCellValue(cell);
            }

            cell = row.getCell(2, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                name = getCellValue(cell);
            }

            cell = row.getCell(3, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                description = getCellValue(cell);
            }

            cell = row.getCell(4, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                isApproved = Boolean.valueOf(getCellValue(cell)) ? "1" : "0";
            }

            cell = row.getCell(5, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                String value = getCellValue(cell);
                if (value != null && value.length() > 0) {
                    reportingType = ReportingType.valueOf(value).toString();
                }
            }

            cell = row.getCell(6, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                serviceCode = ServiceCode.valueOf(getCellValue(cell)).toString();
            }

            cell = row.getCell(7, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                String value = getCellValue(cell);
                if (value != null && value.length() > 0) {
                    FraudRule fr = Application.findById(FraudRule.class, SpcfUniqueId.createInstance(value));
                    fraudRule = fr.getId().toString();
                }
            }

            cell = row.getCell(8, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                String value = getCellValue(cell);
                if (value != null && value.length() > 0) {
                    LimitRule lr = Application.findById(LimitRule.class, SpcfUniqueId.createInstance(value));
                    limitRule = lr.getId().toString();
                }
            }

            cell = row.getCell(9, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                offeringSeq = getCellValue(cell);
            } else {
                offeringSeq = SpcfUniqueId.generateRandomUniqueIdString();
            }

            if (!offeringSeqMap.containsKey(offeringCode)) {
                offeringSeqMap.put(offeringCode, offeringSeq);
            } else {
                System.out.println(String.format("WARNING: Duplicate offering data. OfferingCode: %s", offeringCode ));
            }

            sqlMap.put(offeringCode, String.format(OFFERING_INSERT_SCRIPT, offeringSeq, sku, name, description, offeringCode, serviceCode, isApproved, limitRule, fraudRule, reportingType));
        }

        for (String sql : sqlMap.values()) {
            stringBuilder.append(sql);
        }

        writeFile("C:/Offering.txt", stringBuilder);

        return offeringSeqMap;
    }

    private TreeMap<String, String> processOfferingServiceChargeGroupSheet(Workbook pWorkbook, TreeMap<String, String> pOfferingSeqMap) throws Exception {
        //OfferingServiceChargeGroup
        Sheet sheet = pWorkbook.getSheetAt(1);
        StringBuilder stringBuilder = new StringBuilder();
        TreeMap<String, String> sqlMap = new TreeMap<String, String>();
        TreeMap<String, String> offeringServiceChargeGroupMap = new TreeMap<String, String>();
        for (Row row : sheet) {
            if (row.getRowNum() == 0) {
                //Skip header record
                continue;
            }

            //Key value
            String offeringCode;

            String offeringSvcChgGrpSeq;
            String name = "";
            String description = "";
            String appliesTo = "";
            String offeringFk;

            Cell cell = row.getCell(0, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                offeringCode = getCellValue(cell);
                if (pOfferingSeqMap.containsKey(offeringCode)) {
                    offeringFk = pOfferingSeqMap.get(offeringCode);
                } else {
                    throw new Exception(String.format("Unable to find offering code: %s", offeringCode));
                }
            } else {
                throw new Exception("Offering code cannot be null or empty.");
            }

            cell = row.getCell(1, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                appliesTo = getCellValue(cell);
            }

            cell = row.getCell(2, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                name = getCellValue(cell);
            }

            cell = row.getCell(3, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                description = getCellValue(cell);
            }

            cell = row.getCell(4, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                offeringSvcChgGrpSeq = getCellValue(cell);
            } else {
                offeringSvcChgGrpSeq = SpcfUniqueId.generateRandomUniqueIdString();
            }

            String key = String.format("%s_%s", offeringCode, appliesTo);
            if (!offeringServiceChargeGroupMap.containsKey(key)) {
                offeringServiceChargeGroupMap.put(key, offeringSvcChgGrpSeq);
            } else {
                System.out.println(String.format("WARNING: Duplicate offering service charge group data. OfferingCode: %s AppliesTo: %s", offeringCode, appliesTo ));
            }

            sqlMap.put(key, String.format(OFFERING_SERVICE_CHARGE_GROUP_INSERT_SCRIPT, offeringSvcChgGrpSeq, name, description, appliesTo, offeringFk));
        }

        String comment = "";
        for (String key : sqlMap.keySet()) {
            String currentCommit = key.split("_")[0];
            if (!comment.equals(currentCommit)) {
                comment = currentCommit;
                stringBuilder.append(String.format(COMMIT, comment));
            }
            stringBuilder.append(sqlMap.get(key));
        }

        writeFile("C:/OfferingServiceChargeGroup.txt", stringBuilder);

        return offeringServiceChargeGroupMap;
    }

    private TreeMap<String, String> processOfferingServiceChargeSheet(Workbook pWorkbook, TreeMap<String, String> pOfferingServiceChargeGroupMap) throws Exception {
        //OfferingServiceCharge
        Sheet sheet = pWorkbook.getSheetAt(2);
        StringBuilder stringBuilder = new StringBuilder();
        TreeMap<String, String> sqlMap = new TreeMap<String, String>();
        TreeMap<String, String> offeringServiceChargeMap = new TreeMap<String, String>();
        for (Row row : sheet) {
            if (row.getRowNum() == 0) {
                //Skip header record
                continue;
            }

            //Key values
            String offeringCode;
            String appliesTo;

            String offeringSvcChgSeq;
            String sku = "";
            String skuType = "";
            String isTier = "";
            String tierNumber = "";
            String tierUnits = "";
            String offeringSvcChgGrpFk;

            Cell cell = row.getCell(0, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                offeringCode = getCellValue(cell);
            } else {
                throw new Exception("Offering Code cannot be null or empty.");
            }

            cell = row.getCell(1, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                appliesTo = getCellValue(cell);
                String key = String.format("%s_%s", offeringCode, appliesTo);
                if (pOfferingServiceChargeGroupMap.containsKey(key)) {
                    offeringSvcChgGrpFk = pOfferingServiceChargeGroupMap.get(key);
                } else {
                    throw new Exception(String.format("Unable to find Offering Service Charge Group: %s", key));
                }
            } else {
                throw new Exception("Applies To cannot be null or empty.");
            }

            cell = row.getCell(2, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                sku = getCellValue(cell);
            }

            cell = row.getCell(3, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                tierNumber = getCellValue(cell);
            }

            cell = row.getCell(4, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                isTier = Boolean.valueOf(getCellValue(cell)) ? "1" : "0";
            }

            cell = row.getCell(5, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                tierUnits = getCellValue(cell);
            }

            cell = row.getCell(6, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                skuType = SkuType.valueOf(getCellValue(cell)).toString();
            }

            cell = row.getCell(7, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                offeringSvcChgSeq = getCellValue(cell);
            } else {
                offeringSvcChgSeq = SpcfUniqueId.generateRandomUniqueIdString();
            }

            String key = String.format("%s_%s_%s_%s", offeringCode, appliesTo, sku, tierNumber);
            if (!offeringServiceChargeMap.containsKey(key)) {
                offeringServiceChargeMap.put(key, offeringSvcChgSeq);
            } else {
                System.out.println(String.format("WARNING: Duplicate offering service charge data. OfferingCode: %s AppliesTo: %s SKU: %s Tier Number: %s", offeringCode, appliesTo, sku, tierNumber));
            }

            sqlMap.put(key, String.format(OFFERING_SERVICE_CHARGE_INSERT_SCRIPT, offeringSvcChgSeq, sku, skuType, isTier, tierNumber, tierUnits, offeringSvcChgGrpFk));
        }

        String comment = "";
        for (String key : sqlMap.keySet()) {
            String currentCommit = String.format("%s", key.split("_")[0]);
            if (!comment.equals(currentCommit)) {
                comment = currentCommit;
                stringBuilder.append(String.format(COMMIT, comment));
            }
            stringBuilder.append(sqlMap.get(key));
        }

        writeFile("C:/OfferingServiceCharge.txt", stringBuilder);

        return offeringServiceChargeMap;
    }

    private TreeMap<String, String> processOfferingServiceChargePriceSheet(Workbook pWorkbook, TreeMap<String, String> pOfferingServiceChargeMap) throws Exception {
        //OfferingServiceChargePrice
        Sheet sheet = pWorkbook.getSheetAt(3);
        TreeMap<String, String> sqlMap = new TreeMap<String, String>();
        TreeMap<String, String> serviceChargePriceMap = new TreeMap<String, String>();
        StringBuilder stringBuilder = new StringBuilder();
        for (Row row : sheet) {
            if (row.getRowNum() == 0) {
                //Skip header record
                continue;
            }

            //Key values
            String offeringCode;
            String appliesTo;
            String sku;
            String tierNumber;

            String svcChgPriceSeq;
            String basePrice = "";
            String unitPrice = "";
            String effectiveDate = "";
            String offeringSvcChgFk;

            Cell cell = row.getCell(0, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                offeringCode = getCellValue(cell);
            } else {
                throw new Exception("Offering Code To cannot be null or empty.");
            }

            cell = row.getCell(1, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                appliesTo = getCellValue(cell);
            }  else {
                throw new Exception("Applies To cannot be null or empty.");
            }

            cell = row.getCell(2, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                sku = getCellValue(cell);
            } else {
                throw new Exception("SKU cannot be null or empty.");
            }

            cell = row.getCell(3, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                tierNumber = getCellValue(cell);
                String key = String.format("%s_%s_%s_%s", offeringCode, appliesTo, sku, tierNumber);
                if (pOfferingServiceChargeMap.containsKey(key)) {
                    offeringSvcChgFk = pOfferingServiceChargeMap.get(key);
                } else {
                    throw new Exception(String.format("Unable to find Offering Service Charge: %s", key));
                }
            } else {
                throw new Exception("Tier Number cannot be null or empty.");
            }

            cell = row.getCell(4, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                basePrice = getCellValue(cell, "#.##");
            }

            cell = row.getCell(5, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                unitPrice = getCellValue(cell, "#.##");
            }

            cell = row.getCell(6, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                effectiveDate = getCellValue(cell);
            }

            cell = row.getCell(7, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                svcChgPriceSeq = getCellValue(cell);
            } else {
                svcChgPriceSeq = SpcfUniqueId.generateRandomUniqueIdString();
            }

            String key = String.format("%s_%s_%s_%s", offeringCode, appliesTo, sku, tierNumber);
            if (!serviceChargePriceMap.containsKey(key)) {
                serviceChargePriceMap.put(key, svcChgPriceSeq);
            } else {
                System.out.println(String.format("WARNING: Duplicate service charge price data. OfferingCode: %s AppliesTo: %s SKU: %s TierNumber: %s", offeringCode, appliesTo, sku, tierNumber));
            }

            sqlMap.put(key, String.format(SERVICE_CHARGE_PRICE_INSERT_SCRIPT, svcChgPriceSeq, basePrice, unitPrice, effectiveDate, offeringSvcChgFk));
        }

        String comment = "";
        for (String key : sqlMap.keySet()) {
            String currentCommit = String.format("%s", key.split("_")[0]);
            if (!comment.equals(currentCommit)) {
                comment = currentCommit;
                stringBuilder.append(String.format(COMMIT, comment));
            }
            stringBuilder.append(sqlMap.get(key));
        }

        writeFile("C:/ServiceChargePrice.txt", stringBuilder);

        return serviceChargePriceMap;
    }

    private TreeMap<String, String> processOfferServiceChargeAssocSheet(Workbook pWorkbook, TreeMap<String, String> pOfferingServiceChargeMap) throws Exception {
        //OfferServiceChargeAssoc
        Sheet sheet = pWorkbook.getSheetAt(4);
        StringBuilder stringBuilder = new StringBuilder();
        TreeMap<String, String> sqlMap = new TreeMap<String, String>();
        TreeMap<String, String> offerSeqMap = new TreeMap<String, String>();
        for (Row row : sheet) {
            if (row.getRowNum() == 0) {
                //Skip header record
                continue;
            }

            //Key values
            String offeringCode;
            String appliesTo;
            String sku;
            String tierNumber;

            String offerCode;
            String offerSeq;
            String offeringSvcChgFk;

            Cell cell = row.getCell(0, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                offerCode = getCellValue(cell);
            } else {
                throw new Exception("Offer Code cannot be null or empty.");
            }

            cell = row.getCell(1, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                offeringCode = getCellValue(cell);
            } else {
                throw new Exception("Offering Code cannot be null or empty.");
            }

            cell = row.getCell(2, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                appliesTo = getCellValue(cell);
            }  else {
                throw new Exception("Applies To cannot be null or empty.");
            }

            cell = row.getCell(3, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                sku = getCellValue(cell);
            } else {
                throw new Exception("SKU cannot be null or empty.");
            }

            cell = row.getCell(4, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                tierNumber = getCellValue(cell);

                String key = String.format("%s_%s_%s_%s", offeringCode, appliesTo, sku, tierNumber);
                if (pOfferingServiceChargeMap.containsKey(key)) {
                    offeringSvcChgFk = pOfferingServiceChargeMap.get(key);
                } else {
                    throw new Exception(String.format("Unable to find Offering Service Charge: %s", key));
                }
            } else {
                throw new Exception("Tier Number cannot be null or empty.");
            }

            cell = row.getCell(5, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                offerSeq = getCellValue(cell);
            } else {
                offerSeq = SpcfUniqueId.generateRandomUniqueIdString();
            }

            String key = String.format("%s_%s_%s_%s_%s", offerCode, offeringCode, appliesTo, sku, tierNumber);
            if (!offerSeqMap.containsKey(key)) {
                offerSeqMap.put(key, offerSeq);
            } else {
                System.out.println(String.format("WARNING: Duplicate offer service charge assoc data. OfferCode: %s OfferingCode: %s AppliesTo: %s SKU: %s TierNumber: %s", offerCode, offeringCode, appliesTo, sku, tierNumber));
            }

            sqlMap.put(key, String.format(OFFER_SERVICE_CHARGE_ASSOC_INSERT_SCRIPT, offerSeq, offeringSvcChgFk));
        }

        String comment = "";
        for (String key : sqlMap.keySet()) {
            String currentCommit = String.format("%s_%s", key.split("_")[0], key.split("_")[1]);
            if (!comment.equals(currentCommit)) {
                comment = currentCommit;
                stringBuilder.append(String.format(COMMIT, comment));
            }
            stringBuilder.append(sqlMap.get(key));
        }

        writeFile("C:/OfferServiceChargeAssoc.txt", stringBuilder);

        return offerSeqMap;
    }

    private TreeMap<String, String> processEntitlementCodeOfferingSheet(Workbook pWorkbook, TreeMap<String, String> pOfferingSeqMap) throws Exception {
        //EntitlementCodeOffering
        Sheet sheet = pWorkbook.getSheetAt(5);
        StringBuilder stringBuilder = new StringBuilder();
        TreeMap<String, String> sqlMap = new TreeMap<String, String>();
        TreeMap<String, String> entitlementCodeOfferingSeqMap = new TreeMap<String, String>();
        for (Row row : sheet) {
            if (row.getRowNum() == 0) {
                //Skip header record
                continue;
            }

            //Key values
            String offeringCode;

            AssetItemCode assetItemCode = null;
            NumberOfEmployeesType numberOfEmps = null;
            EditionType editionType = null;
            String assetItemNumber = "";

            String entitlementCodeOfferingSeq;
            String serviceCode = "";
            String effectiveDate = "";
            String priceType = "";
            String isDefault = "";
            String offeringFk;
            String entitlementCodeFk;

            Cell cell = row.getCell(0, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                offeringCode = getCellValue(cell);
                if (pOfferingSeqMap.containsKey(offeringCode)) {
                    offeringFk = pOfferingSeqMap.get(offeringCode);
                } else {
                    throw new Exception(String.format("Unable to find offering code: %s", offeringCode));
                }
            } else {
                throw new Exception("Offering code cannot be null or empty.");
            }

            cell = row.getCell(1, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                assetItemCode = AssetItemCode.valueOf(getCellValue(cell)) ;
            }

            cell = row.getCell(2, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                String value = getCellValue(cell);
                if (value != null && value.length() > 0) {
                    editionType = EditionType.valueOf(value) ;
                }
            }

            cell = row.getCell(3, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                String value = getCellValue(cell);
                if (value != null && value.length() > 0) {
                    numberOfEmps = NumberOfEmployeesType.valueOf(value);
                }
            }

            cell = row.getCell(4, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                assetItemNumber = getCellValue(cell);
            }

            cell = row.getCell(5, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                serviceCode = ServiceCode.valueOf(getCellValue(cell)).toString() ;
            }

            cell = row.getCell(6, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                effectiveDate = getCellValue(cell);
            }

            cell = row.getCell(7, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                priceType = getCellValue(cell);
            }

            cell = row.getCell(8, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                isDefault = Boolean.valueOf(getCellValue(cell)) ? "1" : "0";
            }

            cell = row.getCell(9, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                entitlementCodeOfferingSeq = getCellValue(cell);
            } else {
                entitlementCodeOfferingSeq = SpcfUniqueId.generateRandomUniqueIdString();
            }

            EntitlementCode entitlementCode = findEntitlementCode(assetItemCode, assetItemNumber, editionType, numberOfEmps, AssetTypeCode.Payroll);
            if (entitlementCode != null) {
                entitlementCodeFk = entitlementCode.getId().toString();
            } else {
                throw new Exception(String.format("Unable to find a entitlement code with the following values - Asset Item Number: %s Edition: %s and Number of Employees: %s", assetItemNumber, editionType, numberOfEmps));
            }

            String key = String.format("%s_%s_%s_%s", offeringCode, editionType, numberOfEmps, assetItemNumber);
            if (!entitlementCodeOfferingSeqMap.containsKey(key)) {
                entitlementCodeOfferingSeqMap.put(key, entitlementCodeFk);
            } else {
                System.out.println(String.format("WARNING: Duplicate entitlement code offering data. OfferingCode: %s EditionType: %s NumberOfEmps: %s AssetItemNumber: %s", offeringCode, editionType, numberOfEmps, assetItemNumber));
            }

            sqlMap.put(key, String.format(ENTITLEMENT_OFFERING_INSERT_SCRIPT, entitlementCodeOfferingSeq, serviceCode, effectiveDate, offeringFk, entitlementCodeFk, isDefault, priceType));
        }

        String comment = "";
        for (String key : sqlMap.keySet()) {
            String currentCommit = String.format("%s", key.split("_")[0]);
            if (!comment.equals(currentCommit)) {
                comment = currentCommit;
                stringBuilder.append(String.format(COMMIT, comment));
            }
            stringBuilder.append(sqlMap.get(key));
        }

        writeFile("C:/EntitlementCodeOffering.txt", stringBuilder);

        return entitlementCodeOfferingSeqMap;
    }

    private void resizeColumns() {

        mSheet1.autoSizeColumn(0);
        mSheet1.autoSizeColumn(1);
        mSheet1.autoSizeColumn(2);
        mSheet1.autoSizeColumn(3);
        mSheet1.autoSizeColumn(4);
        mSheet1.autoSizeColumn(5);
        mSheet1.autoSizeColumn(6);
        mSheet1.autoSizeColumn(7);
        mSheet1.autoSizeColumn(8);
        mSheet1.autoSizeColumn(9);

        mSheet2.autoSizeColumn(0);
        mSheet2.autoSizeColumn(1);
        mSheet2.autoSizeColumn(2);
        mSheet2.autoSizeColumn(3);
        mSheet2.autoSizeColumn(4);

        mSheet3.autoSizeColumn(0);
        mSheet3.autoSizeColumn(1);
        mSheet3.autoSizeColumn(2);
        mSheet3.autoSizeColumn(3);
        mSheet3.autoSizeColumn(4);
        mSheet3.autoSizeColumn(5);
        mSheet3.autoSizeColumn(6);
        mSheet3.autoSizeColumn(7);

        mSheet4.autoSizeColumn(0);
        mSheet4.autoSizeColumn(1);
        mSheet4.autoSizeColumn(2);
        mSheet4.autoSizeColumn(3);
        mSheet4.autoSizeColumn(4);
        mSheet4.autoSizeColumn(5);
        mSheet4.autoSizeColumn(6);
        mSheet4.autoSizeColumn(7);

        mSheet5.autoSizeColumn(0);
        mSheet5.autoSizeColumn(1);
        mSheet5.autoSizeColumn(2);
        mSheet5.autoSizeColumn(3);
        mSheet5.autoSizeColumn(4);
        mSheet5.autoSizeColumn(5);

        mSheet6.autoSizeColumn(0);
        mSheet6.autoSizeColumn(1);
        mSheet6.autoSizeColumn(2);
        mSheet6.autoSizeColumn(3);
        mSheet6.autoSizeColumn(4);
        mSheet6.autoSizeColumn(5);
        mSheet6.autoSizeColumn(6);
        mSheet6.autoSizeColumn(7);
        mSheet6.autoSizeColumn(8);
        mSheet6.autoSizeColumn(9);

    }

    private void writeWorkBook(String pFile) throws Exception{
        FileOutputStream out = new FileOutputStream(pFile);
        mMyWorkBook.write(out);
        out.close();
    }

    private void writeFile(String pFile, StringBuilder pStringBuilder) throws Exception{
        FileWriter fileWriter = new FileWriter(pFile);
        fileWriter.write(pStringBuilder.toString());
        fileWriter.flush();
        fileWriter.close();
    }

    // Finders

    private DomainEntitySet<OfferingServiceChargeGroup> findOfferingServiceChargeGroup(Offering pOffering) {
        Expression<OfferingServiceChargeGroup> query =
                new Query<OfferingServiceChargeGroup>()
                        .Where(OfferingServiceChargeGroup.Offering().equalTo(pOffering))
                        .OrderBy(OfferingServiceChargeGroup.AppliesTo());

        return Application.find(OfferingServiceChargeGroup.class, query);
    }

    private DomainEntitySet<OfferingServiceCharge> findOfferingServiceCharge(OfferingServiceChargeGroup pOfferingServiceChargeGroup) {
        Expression<OfferingServiceCharge> query =
                new Query<OfferingServiceCharge>()
                        .Where(OfferingServiceCharge.OfferingServiceChargeGroup().equalTo(pOfferingServiceChargeGroup))
                        .OrderBy(OfferingServiceCharge.SKU());

        return Application.find(OfferingServiceCharge.class, query);
    }

    private DomainEntitySet<OfferingServiceChargePrice> findOfferingServiceChargePrice(OfferingServiceCharge pOfferingServiceCharge) {
        Expression<OfferingServiceChargePrice> query =
                new Query<OfferingServiceChargePrice>()
                        .Where(OfferingServiceChargePrice.OfferingServiceCharge().equalTo(pOfferingServiceCharge))
                        .OrderBy(OfferingServiceChargePrice.BasePrice(), OfferingServiceChargePrice.UnitPrice());

        return Application.find(OfferingServiceChargePrice.class, query);
    }

    private EntitlementCode findEntitlementCode(AssetItemCode pAssertItemCode,
                                                String pAssertItemNumber,
                                                EditionType pEditionType,
                                                NumberOfEmployeesType pNumberOfEmployeesType,
                                                AssetTypeCode pAssetTypeCode) {

        Criterion<EntitlementCode> where = EntitlementCode.AssetItemNumber().equalTo(pAssertItemNumber)
                                                          .And(EntitlementCode.AssetItemCd().equalTo(pAssertItemCode)
                                                          .And(EntitlementCode.AssetTypeCd().equalTo(pAssetTypeCode)));

        if(pEditionType != null) {
            where = where.And(EntitlementCode.EditionType().equalTo(pEditionType));
        } else {
            where = where.And(EntitlementCode.EditionType().isNull());
        }

        if(pNumberOfEmployeesType != null){
            where = where.And(EntitlementCode.NumberOfEmployeesType().equalTo(pNumberOfEmployeesType));
        } else {
            where = where.And(EntitlementCode.NumberOfEmployeesType().isNull());
        }

        Expression<EntitlementCode> query =
                new Query<EntitlementCode>()
                        .Where(where)
                        .OrderBy(Entitlement.CreatedDate());

        DomainEntitySet<EntitlementCode> entitlementCodes = Application.find(EntitlementCode.class, query);

        if(entitlementCodes.size() > 1) {
            throw new RuntimeException("More than one entitlement code exists for AssetItemNumber: " + pAssertItemNumber + " Edition: " + pEditionType + " and NOE: " + pNumberOfEmployeesType);
        } else if(entitlementCodes.size() == 0) {
            return null;
        } else {
            return entitlementCodes.get(0);
        }
    }

    private String getCellValue(Cell pCell) throws Exception {
        return getCellValue(pCell, null);
    }

    private String getCellValue(Cell pCell, String pFormat) throws Exception {
        String value;

        if (pFormat == null) {
            pFormat = "#";
        }

        switch (pCell.getCellType()) {
            case STRING:
                value = pCell.getStringCellValue();
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(pCell)) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");
                    value =  simpleDateFormat.format(pCell.getDateCellValue());
                } else {
                    DecimalFormat decimalFormat = new DecimalFormat(pFormat);
                    value = decimalFormat.format(pCell.getNumericCellValue());
                }
                break;
            case BOOLEAN:
                value = String.valueOf(pCell.getBooleanCellValue());
                break;
            case FORMULA:
                value = pCell.getCellFormula();
                break;
            default:
                throw new Exception("Unknown cell type");
        }

        return value;
    }

/*    private void setCellValueAndStyle(Cell pCell, String pValue, String pFormat) {
        Workbook workbook = pCell.getRow().getSheet().getWorkbook();

        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();

        style.setDataFormat(format.getFormat(pFormat));

        pCell.setCellValue(pValue);
        pCell.setCellStyle(style);
    }

    private Map<String, String> parseFields(Object obj) {
        Map<String, String> response = new HashMap<String, String>();

        try {
            Class<?> c = obj.getClass().getSuperclass();
            Method[] methods = c.getMethods();

            for(Method method : methods){
                BeanInfo info = Introspector.getBeanInfo(c);
                PropertyDescriptor[] props = info.getPropertyDescriptors();
                for (PropertyDescriptor pd : props)
                {
                    if(method.equals(pd.getWriteMethod()) || method.equals(pd.getReadMethod()))
                    {
                        System.out.println(pd.getDisplayName());
                        System.out.println(pd.getName());
                    }
                }

*//*                try {
                    Object value = field.get(obj);
                    response.put(field.getName(), value == null ? "" : value.toString());
                } catch (Exception e) {
                    //Do nothing
                }*//*
            }
        } catch (Exception e) {
            //throw error
        }

        return response;
    }*/

}

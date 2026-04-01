package com.intuit.sbd.payroll.psp.adapters.sap.rtb;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by anandp233 on 3/3/14.
 */
public class ExcelProcessing {
    private static final SpcfLogger logger = PayrollServices.getLogger(ExcelProcessing.class);

    private Map excelKeyValueList;
    private List<String> keyList;

    public ExcelProcessing(byte[] fileBinary) throws Exception {
        processExcelFile(fileBinary);
        validatePostProcess();
    }

    private void processExcelFile(byte[] fileBinary) throws Exception {
        excelKeyValueList = new HashMap<String, List<Object>>();
        keyList = new ArrayList<String>();
        try {

            //TODO : Change below two line code if you to support the .xlsx file
/*
            //for .xlsx file
            XSSFWorkbook sheets = new XSSFWorkbook(new ByteArrayInputStream(fileBinary));
            XSSFSheet sheet = sheets.getSheetAt(0);
*/
            //for .xls file
            HSSFWorkbook sheets = new HSSFWorkbook(new ByteArrayInputStream(fileBinary));
            HSSFSheet sheet = sheets.getSheetAt(0);

            int i = 0;
            for (Row row : sheet) {
                for (int cn = 0; (cn < (row.getLastCellNum()) || (cn < keyList.size())); cn++) {
                    Object obj = null;
                    Cell cell = row.getCell(cn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    switch (cell.getCellType()) {
                        case NUMERIC:
                            obj = (long) cell.getNumericCellValue();
                            break;
                        case BOOLEAN:
                            obj = cell.getBooleanCellValue();
                            break;
                        case FORMULA:
                            try {
                                obj = cell.getStringCellValue();
                            } catch (IllegalStateException e) {
                                try {
                                    obj = (long) cell.getNumericCellValue();
                                } catch (IllegalStateException ex) {
                                    obj = cell.getBooleanCellValue();
                                }
                            }
                            break;
                        case BLANK:
                        case STRING:
                            obj = cell.getStringCellValue();
                            break;
                    }
                    if (i == 0) {
                        keyList.add(obj.toString());//adding key
                        excelKeyValueList.put(obj.toString(), new ArrayList<Object>());
                    } else {
                        ((List) excelKeyValueList.get(keyList.get(cn))).add(i - 1, obj);
                    }
                }
                i++;
            }
            //logger.info("Out put :" + excelKeyValueList.toString());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }


    /**
     * Do the basic validation for Excel processed data
     *
     * @throws RTBJobException
     */

    public void validatePostProcess() throws RuntimeException {

        if (excelKeyValueList == null || keyList == null || keyList.size() < 1 || excelKeyValueList.size() < 1) {
            throw new RuntimeException("Excel sheet is empty with no headers");
        }
        int size = ((List) excelKeyValueList.get(keyList.get(0))).size();
        if (size < 1) {
            throw new RuntimeException("Excel sheet NOT empty but no data found");
        }
        for (String key : keyList) {
            if (size != (((List) excelKeyValueList.get(key)).size())) {
                throw new RuntimeException("List of records are not equal for all columns.");
            }
        }
    }


    public Map getExcelKeyValueList() {
        return excelKeyValueList;
    }

    public List<String> getKeyList() {
        return keyList;
    }
}

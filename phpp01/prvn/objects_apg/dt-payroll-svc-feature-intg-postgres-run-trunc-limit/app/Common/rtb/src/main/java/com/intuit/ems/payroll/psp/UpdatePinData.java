package com.intuit.ems.payroll.psp;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.util.PINUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.hibernate.SQLQuery;
import org.hibernate.Query;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Todo: We should remove this class,once we update all SHA1 data in pin table.
 * Created by snannuru on 1/3/2018.
 */
public class UpdatePinData {
    private static final int HASH_ROW_LIMIT =500;
    private static final String BACKUP_QUERY_FILE_NAME="pin_modified_companies";
    private static String fileNameFirstPart;
    public static void main(String[] args) throws Exception {
        if (args.length < 2)
            throw new IllegalArgumentException("Please provide temp file path to store XLS and backup script and please provide JIRA NUMBER ");
        String tempPath = args[0];
        fileNameFirstPart=tempPath+ System.getProperty("file.separator") + BACKUP_QUERY_FILE_NAME;
        List<Object[]> companyPinList;
        String selectQueryfileName =fileNameFirstPart +"_select_query.txt";
        String dataSheetName = fileNameFirstPart+".xls";
        String sourceCompIdsSheetName = fileNameFirstPart+"_source.xls";
        String selectQueryStr = readFile(selectQueryfileName);

        FileOutputStream sourceCompOutputStream=null;
        FileOutputStream trackStatusStream=null;
        BufferedReader userInput = null;
        String userChoice ="yes";
        try {
            HSSFWorkbook workbook = new HSSFWorkbook();
            trackStatusStream = new FileOutputStream(dataSheetName);
            Set<String> sourceCompanyIds = new HashSet<String>();
            //Generate the styles
            HSSFCellStyle headerStyle=getColumnStyle(workbook);

            int sheetCount = 0;
            do {
                FileWriter fileWriter=null;
               try{
                    System.out.print("Select Query" + selectQueryStr);
                    Application.beginUnitOfWork();
                    SQLQuery selectQuery = Application.getHibernateSession().createSQLQuery(selectQueryStr);
                    selectQuery.setMaxResults(HASH_ROW_LIMIT);
                    companyPinList = selectQuery.list();

                    if (companyPinList.size() > 0) {
                        sheetCount++;
                        HSSFSheet sheet = workbook.createSheet("Company Pin Data" + sheetCount);
                        createHeaderRow(sheet,headerStyle);
                        //Create back up file.
                         fileWriter = createBackupFile(tempPath, sheetCount);
                        int rowcount = 1;
                        for (Object[] companyPIN : companyPinList) {

                           //Update  PSP_COMPANY_PIN data
                            if (!updatePin(companyPIN, sheet, rowcount,args[1])) {
                                Application.rollbackUnitOfWork();
                            }
                            sourceCompanyIds.add(companyPIN[6].toString());
                            addToBackupQuery(fileWriter, companyPIN, rowcount);
                            rowcount++;
                        }
                        System.out.println("Processed " + companyPinList.size()   + " rows and added status to xls "+ "Company Pin Data" + sheetCount);
                        Application.commitUnitOfWork();
                         userInput = new BufferedReader(new InputStreamReader(System.in));
                         System.out.print("Continue to process remaining data,please enter Yes or No ");
                         userChoice = userInput.readLine();

                    }
                    }finally {
                       if(fileWriter!=null)
                           fileWriter.close();
                   }

                } while (companyPinList.size() > 0 && userChoice.equalsIgnoreCase("yes"));
                int rowCount = 0;
                if(sourceCompanyIds.size()>0) {
                        HSSFWorkbook sourceComWorkbook = new HSSFWorkbook();
                         sourceCompOutputStream = new FileOutputStream(sourceCompIdsSheetName);
                        HSSFSheet sourceSheet = sourceComWorkbook.createSheet("Source CompanyId");
                        Row row = sourceSheet.createRow(rowCount++);
                        Cell cell = row.createCell(0);
                        cell.setCellValue("Source Company Id");
                        for (String sourceCompanyId : sourceCompanyIds) {
                            Row datarow = sourceSheet.createRow(rowCount++);
                            Cell dataCell = datarow.createCell(0);
                            dataCell.setCellValue(sourceCompanyId);

                        }

                        sourceComWorkbook.write(sourceCompOutputStream);
                }
                workbook.write(trackStatusStream);
            sourceCompOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }finally {

            if(sourceCompOutputStream!=null)
                sourceCompOutputStream.close();

            if(trackStatusStream!=null)
                trackStatusStream.close();

            if(userInput!=null){
                userInput.close();
            }
        }
    }

    private static HSSFCellStyle getColumnStyle(HSSFWorkbook sourceCompWorkbook) {

        HSSFCellStyle cellStyle = sourceCompWorkbook.createCellStyle();

         /* Create HSSFFont object from the workbook */
        HSSFFont boldFont=sourceCompWorkbook.createFont();
        boldFont.setBold(true);
        cellStyle.setFont(boldFont);

       return cellStyle;
    }

    private static void addToBackupQuery(FileWriter fileWriter,Object[] companyPINData, int rowCount) throws Exception {

        String modifiedId = companyPINData[2] != null ? companyPINData[2]
                .toString() : "";
        String pinValue = companyPINData[4] != null ? companyPINData[4]
                .toString() : "";
        String hashType = companyPINData[5] != null ? companyPINData[5]
                .toString() : "";
        String modifiedDate = companyPINData[3] != null ? companyPINData[3]
                .toString() : "";

        String updatesql = "update PSPADM.PSPADM.PSP_COMPANY_PIN  set MODIFIER_ID='"
                + modifiedId + "', P_I_N_VALUE='" + pinValue + "',HASH_TYPE='"
                + hashType + "',MODIFIED_DATE='" + modifiedDate
                + "' where COMPANY_PIN_SEQ='" + companyPINData[0] + "' ;\n";

        String comment = "/* row count" + rowCount + " */\n";
        fileWriter.write(comment);
        fileWriter.write(updatesql);
        fileWriter.flush();

    }

    private static FileWriter createBackupFile(String tempPath, int sheetCount)
            throws Exception {
        String backupFileName = fileNameFirstPart+"_"+sheetCount+".sql";
        File backupQuery = new File(backupFileName);
        backupQuery.createNewFile();
        FileWriter backUpfw = new FileWriter(backupQuery.getAbsoluteFile(), true);
        return backUpfw;
    }

    private static void createHeaderRow(HSSFSheet sheet, HSSFCellStyle style) {
        Row headerRow = sheet.createRow(0);

        Cell cell= headerRow.createCell(0);
        cell.setCellValue("COMPANY_PIN_SEQ");
        cell.setCellStyle(style);
         cell=headerRow.createCell(1);
        cell.setCellValue("MODIFIER_ID");
        cell.setCellStyle(style);
        cell=headerRow.createCell(2);
        cell.setCellValue("MODIFIED_DATE");
        cell.setCellStyle(style);
        cell=headerRow.createCell(3);
        cell.setCellValue("P_I_N_VALUE");
        cell.setCellStyle(style);
        cell=headerRow.createCell(4);
        cell.setCellValue("STATUS");
        cell.setCellStyle(style);
    }

    private static boolean updatePin(Object[] companyPINData, HSSFSheet sheet,
                                     int rowCount,String modifierId) {
        Row row = sheet.createRow(rowCount);
        System.out.println(companyPINData[0].toString());
        row.createCell(0).setCellValue(companyPINData[0].toString());
        row.createCell(1).setCellValue(companyPINData[2]!=null?companyPINData[2].toString():"");
        row.createCell(2).setCellValue(companyPINData[3] != null ? companyPINData[3].toString() : "");
        row.createCell(3).setCellValue(companyPINData[4]!=null?companyPINData[4].toString():"");
        Cell cell = row.createCell(4);
        cell.setCellValue("STARTED");

        UUID uuid = UUID.randomUUID();
        long longUuid = ByteBuffer.wrap(uuid.toString().getBytes()).getLong();
        String collectedStr = Long.toString(longUuid, Character.MAX_RADIX)
                .substring(0, 8);
        String updateSql = "update PSPADM.PSP_COMPANY_PIN  set MODIFIER_ID=:modifierId, P_I_N_VALUE=:pinValue,HASH_TYPE='SHA512',MODIFIED_DATE=CURRENT_TIMESTAMP where COMPANY_PIN_SEQ=:companyPinSeq";
        SQLQuery query = Application.getHibernateSession().createSQLQuery(updateSql);
        query.setString("modifierId",modifierId);
        query.setString("pinValue",PINUtils.encrypt(collectedStr));
        query.setString("companyPinSeq",companyPINData[0].toString());
        if (query.executeUpdate() > 0) {
            cell.setCellValue("COMPLETED");
            return true;
        }

        return false;
    }

    //Read the select query to collect company sequence numbers
    private static String readFile(String fileName) throws Exception {
        byte[] encoded = Files.readAllBytes(Paths.get(fileName));
        return new String(encoded, StandardCharsets.UTF_8);

    }

}

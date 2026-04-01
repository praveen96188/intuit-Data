package com.intuit.sbd.payroll.psp.batchjobs.ATFDataExtract;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.utils.ATFDataExtractCompare;
import com.intuit.sbd.payroll.psp.batchjobs.utils.CompareResults;
import com.intuit.sbd.payroll.psp.domain.*;
import intuit.osp.common.utils.FileUtils;
import org.junit.platform.commons.util.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * User: ihannur
 * Date: 8/20/12
 * Time: 10:08 AM
 */
public class ATFDataExtractTestsUtil {

    public static void runExtractAndValidateFilesForAnnualData(int pYear, ATFDataFile... atfDateFiles) throws Exception {
        runExtractAndValidateFiles(ATFDataExtractRunType.AnnualData, pYear, 0, atfDateFiles);
    }

    public static void runExtractAndValidateFilesForQuarterlyData(int pYear, int pQuarter, ATFDataFile... atfDateFiles) throws Exception {
        runExtractAndValidateFiles(ATFDataExtractRunType.QuarterlyData, pYear, pQuarter, atfDateFiles);
    }

    public static void runExtractAndValidateFilesForUpdatedData(ATFDataFile... atfDateFiles) throws Exception {
        runExtractAndValidateFiles(ATFDataExtractRunType.UpdatedData, 0, 0, atfDateFiles);        
    }

    public static void runExtractAndValidateFilesForUpdatedDGData(ATFDataFile... atfDateFiles) throws Exception {
        runExtractAndValidateFilesDG(ATFDataExtractRunType.UpdatedData, 0, 0, atfDateFiles);
    }
    
    public static void runExtractAndValidateFiles(ATFDataExtractRunType pExtractType, int pYear, int pQuarter, ATFDataFile... atfDateFiles) throws Exception {
        if(ATFDataExtractRunType.UpdatedData == pExtractType) {
            BatchJobManager.runJob(BatchJobType.ATFDataExtract, pExtractType.toString());   
        } else if(ATFDataExtractRunType.AnnualData == pExtractType) {
            BatchJobManager.runJob(BatchJobType.ATFDataExtract, pExtractType.toString(), String.valueOf(pYear));
        } else {
            BatchJobManager.runJob(BatchJobType.ATFDataExtract, pExtractType.toString(), String.valueOf(pYear), String.valueOf(pQuarter));
        }

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<ATFDataExtractBatch> atfDataExtractBatches = PayrollServices.entityFinder.find(ATFDataExtractBatch.class);
        atfDataExtractBatches = atfDataExtractBatches.sort(ATFDataExtractBatch.BatchId().Descending());
        ATFDataExtractBatch extractBatch = atfDataExtractBatches.get(0);
        assertEquals("Extract Batch Status", ATFDataExtractBatchStatus.Completed, extractBatch.getBatchStatus());

        for (ATFDataExtractFile extractFile : extractBatch.getATFDataExtractFileCollection()) {
            assertEquals("Extract File Status", ATFDataExtractFileStatus.Extracted, extractFile.getFileStatus());
        }

        for (ATFDataFile atfDateFile : atfDateFiles) {
            ATFDataExtractFile atfDataExtract = null;
            DomainEntitySet<ATFDataExtractFile> extractFiles =
                    Application.find(ATFDataExtractFile.class,
                                     ATFDataExtractFile.ATFDataExtractBatch().equalTo(extractBatch)
                                                       .And(ATFDataExtractFile.FileType().equalTo(atfDateFile.extractFileType)));
            if (extractFiles != null) {
                atfDataExtract = extractFiles.get(0);
            }

            validateFile(
                    Application.findFileOnClassPath(atfDateFile.expectedFile),
                    atfDataExtract);

        }
        PayrollServices.rollbackUnitOfWork();

    }

    public static void runExtractAndValidateFilesDG(ATFDataExtractRunType pExtractType, int pYear, int pQuarter, ATFDataFile... atfDateFiles) throws Exception {
        if(ATFDataExtractRunType.UpdatedData == pExtractType) {
            BatchJobManager.runJob(BatchJobType.ATFDataExtract, pExtractType.toString());
        } else if(ATFDataExtractRunType.AnnualData == pExtractType) {
            BatchJobManager.runJob(BatchJobType.ATFDataExtract, pExtractType.toString(), String.valueOf(pYear));
        } else {
            BatchJobManager.runJob(BatchJobType.ATFDataExtract, pExtractType.toString(), String.valueOf(pYear), String.valueOf(pQuarter));
        }

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<ATFDataExtractBatch> atfDataExtractBatches = PayrollServices.entityFinder.find(ATFDataExtractBatch.class);
        atfDataExtractBatches = atfDataExtractBatches.sort(ATFDataExtractBatch.BatchId().Descending());
        ATFDataExtractBatch extractBatch = atfDataExtractBatches.get(0);
        assertEquals("Extract Batch Status", ATFDataExtractBatchStatus.Completed, extractBatch.getBatchStatus());

        for (ATFDataExtractFile extractFile : extractBatch.getATFDataExtractFileCollection()) {
            assertEquals("Extract File Status", ATFDataExtractFileStatus.Extracted, extractFile.getFileStatus());
        }

        for (ATFDataFile atfDateFile : atfDateFiles) {
            ATFDataExtractFile atfDataExtract = null;
            DomainEntitySet<ATFDataExtractFile> extractFiles =
                    Application.find(ATFDataExtractFile.class,
                            ATFDataExtractFile.ATFDataExtractBatch().equalTo(extractBatch)
                                    .And(ATFDataExtractFile.FileType().equalTo(atfDateFile.extractFileType)));
            if (extractFiles != null) {
                atfDataExtract = extractFiles.get(0);
            }

            validateFileDG(
                    Application.findFileOnClassPath(atfDateFile.expectedFile),
                    atfDataExtract);

        }
        PayrollServices.rollbackUnitOfWork();

    }

    private static void validateFile(String pExpectedFileName, ATFDataExtractFile pExtractFile) throws Exception {
        String createdFileName = extractFile(pExtractFile);

        BufferedReader expectedReader = new BufferedReader(new FileReader(pExpectedFileName));
        BufferedReader compareReader = new BufferedReader(new FileReader(createdFileName));

        String inputLine;
        List<String> lineList = new ArrayList<>();
        while ((inputLine = expectedReader.readLine()) != null) {
            lineList.add(inputLine);
        }

        List<String> lineListActual = new ArrayList<>();
        while ((inputLine = compareReader.readLine()) != null) {
            lineListActual.add(inputLine);
        }

        expectedReader.close();
        compareReader.close();

        Collections.sort(lineList);
        Collections.sort(lineListActual);

        String outputFile = pExpectedFileName + "_out";
        String outputFile_actual = createdFileName + "_out";

        FileWriter fileWriter = new FileWriter(outputFile);
        PrintWriter out = new PrintWriter(fileWriter);
        for (String outputLine : lineList) {
            out.println(outputLine);
        }
        out.flush();
        out.close();
        fileWriter.close();

        FileWriter fileWriter_actual = new FileWriter(outputFile_actual);
        PrintWriter out_actual = new PrintWriter(fileWriter_actual);
        for (String outputLine : lineListActual) {
            out_actual.println(outputLine);
        }
        out_actual.flush();
        out_actual.close();
        fileWriter_actual.close();

        BufferedReader expectedReader_actual = new BufferedReader(new FileReader(outputFile));
        BufferedReader compareReader_actual = new BufferedReader(new FileReader(outputFile_actual));

        ATFDataExtractCompare compare = new ATFDataExtractCompare();
        CompareResults compareResults = compare.compareATFDataExtractFile(expectedReader_actual, compareReader_actual);

        String expectedFileStr = org.apache.commons.io.FileUtils.readFileToString(new File(outputFile));
        System.out.println("Expected file: " + expectedFileStr);
        System.out.println("Expected file size: " + expectedFileStr.length());

        String createdFileStr = org.apache.commons.io.FileUtils.readFileToString(new File(outputFile_actual));
        System.out.println("Generated file: " + createdFileStr);
        System.out.println("Generated file size: " + createdFileStr.length());

        if (!compareResults.getStatus()) {
            System.out.println(compareResults.toString());
        }
        assertEquals("File " + createdFileName + " matches expected file " + pExpectedFileName, true, compareResults.getStatus());
    }

    private static void validateFileDG(String pExpectedFileName, ATFDataExtractFile pExtractFile) throws Exception {
        String createdFileName = extractFile(pExtractFile);

        BufferedReader expectedReader = new BufferedReader(new FileReader(pExpectedFileName));
        BufferedReader compareReader = new BufferedReader(new FileReader(createdFileName));

        ATFDataExtractCompare compare = new ATFDataExtractCompare();
        CompareResults compareResults = compare.compareATFDataExtractFile(expectedReader, compareReader);

        if (!compareResults.getStatus()) {
            System.out.println(compareResults.toString());
            System.out.println("Actual file:");
            System.out.println(org.apache.commons.io.FileUtils.readFileToString(new File(createdFileName)));
        }
        assertEquals("File " + createdFileName + " matches expected file " + pExpectedFileName, false, compareResults.getStatus());
    }

    private static String extractFile(ATFDataExtractFile pExtractFile) throws Exception {
        return FileUtils.gUnZip(pExtractFile.getFileName());
    }

    public static class ATFDataFile {
        public ATFDataExtractFileType extractFileType;
        public String expectedFile;

        public ATFDataFile(ATFDataExtractFileType pATFDataExtractFileType, String pExpectedFile) {
            this.extractFileType = pATFDataExtractFileType;
            this.expectedFile = pExpectedFile;
        }
    }
}

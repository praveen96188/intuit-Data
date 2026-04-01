package com.intuit.ems.payroll.psp.csv;

import au.com.bytecode.opencsv.CSVWriter;
import com.intuit.ems.payroll.psp.csv.model.CSVFile;
import com.intuit.ems.payroll.psp.csv.model.FileRecord;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.collections4.CollectionUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

//TODO: use nio
public class CSVFileWriter {

    private CSVFile csvFile;
    private CSVWriter csvWriter;
    private static final SpcfLogger LOGGER = Application.getLogger(CSVFileWriter.class);

    public CSVFileWriter(CSVFile csvFile) {
        this.csvFile = csvFile;
    }

    public void write() throws IOException {
        LOGGER.info("Writing to the file:Start");

        FileRecord fileRecord = csvFile.getFileRecord();
        try {
            csvWriter = openWriter(csvFile.getFileName());
            writeHeader(fileRecord.getHeader());
            writeData(fileRecord.getData());
        } finally {
            closeWriter(csvWriter);
        }
        LOGGER.info("Writing to the file:Done");
    }

    private void writeHeader(List<String> headerList) {
        LOGGER.info("Write Header:Start");
        String[] header = new String[headerList.size()];
        for(int index = 0; index <= headerList.size() -1 ; index++) {
            header[index] = headerList.get(index);
        }
        csvWriter.writeNext(header);
        LOGGER.info("Write Header:Done");
    }

    private void writeData(List<List<String>> data) {
    	if(CollectionUtils.isEmpty(data) || CollectionUtils.isEmpty(data.get(0))) {
            LOGGER.info("No data. Exiting the writer.");
            return;
        }
        LOGGER.info("Write Data:Start");
        int count = 0;
        LOGGER.info("Total records Count=" + data.size());
        for(List<String> row : data) {
            String[] column = row.stream().toArray(String[]::new);
            csvWriter.writeNext(column);
            if (++count % 500 == 0) {
                LOGGER.info("Completed Output Records=" + count);
            }
        }
        LOGGER.info("Completed Output Records=" + count);
        LOGGER.info("Write Data:Done");
    }

    private CSVWriter openWriter(String inputFile) throws IOException {
        File file = new File(inputFile);
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, true));
        return new CSVWriter(bufferedWriter);
    }

    private void closeWriter(CSVWriter csvWriter) throws IOException {
        try {
            csvWriter.flush();
            csvWriter.close();
        } catch(Exception e) {
            LOGGER.warn("Error closing the writer", e);
        }
    }
}

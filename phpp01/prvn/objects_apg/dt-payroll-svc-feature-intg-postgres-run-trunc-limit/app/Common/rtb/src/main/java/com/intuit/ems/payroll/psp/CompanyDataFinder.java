package com.intuit.ems.payroll.psp;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

//TODO : Implement batch reading and processing.
public class CompanyDataFinder {

    private String inputFile;
    private String outputFile;
    private boolean writeHeader = true;

    private static final int BATCH_SIZE = 900;

    public CompanyDataFinder(String inputFile, String outputFile, boolean writeHeader) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.writeHeader = writeHeader;
    }

    public static void main(String[] args) throws Exception {
        CompanyDataFinder companyDataFinder = new CompanyDataFinder(args[0], args[1], true);
        companyDataFinder.process();
    }

    private void process() throws Exception {
        System.out.println("********* START ***********");
        List<String> einList = readInputData();
        List<String> encryptedEINList = getEncryptedEIN(einList);
        Set<CompanyData> companyDataSet = getCompanyData(encryptedEINList);
        writeOutputData(companyDataSet);
        System.out.println("********* DONE ***********");
    }

    private void writeOutputData(Set<CompanyData> companyDataSet) throws IOException {
        System.out.println("Writing to the file:Start");
        CSVWriter csvWriter = null;
        int count = 0;
        try {
            csvWriter = openWriter(outputFile);
            String[] outputContent = new String[4];
            if (writeHeader)
                writeHeader(csvWriter);
            for(CompanyData companyData : companyDataSet) {
                outputContent[0] = companyData.getCompanySeq();
                outputContent[1] = companyData.getPsid();
                outputContent[2] = companyData.getEin();
                outputContent[3] = companyData.getLegalName();
                csvWriter.writeNext(outputContent);
                count++;
                if (count % 500 == 0) {
                    System.out.println("Completed Output Records=" + count);
                }
            }
            System.out.println("Completed Output Records=" + count);
        } finally {
            closeWriter(csvWriter);
        }
        System.out.println("Writing to the file:Done");
    }

    private List<String> readInputData() throws Exception {
        System.out.println("Reading the file:Start");
        List<String> einList = new ArrayList<>();
        CSVReader csvReader = null;
        String[] lineContents = null;
        try {
            csvReader = openReader(inputFile);
            while ((lineContents = csvReader.readNext()) != null) {
                if (StringUtils.isNotBlank(lineContents[0])) {
                    einList.add(lineContents[0]);
                }
            }
        } finally {
            closeReader(csvReader);
        }
        System.out.println("Reading the file:Done");
        System.out.println("Records to process=" + einList.size());
        return einList;
    }

    private List<String> getEncryptedEIN(List<String> einList) {
        System.out.println("Encrypting the EINs:Start");
        List<String> encryptedEINList = new ArrayList<>();
        for(String ein : einList) {
            try {
                List<String> einEncList = EncryptionUtils.deterministicEncryptWithAllKeys(Company.FedTaxIdKeyName, ein);
                encryptedEINList.addAll(einEncList);
            } catch (Exception e) {
                System.out.println("Failed to get encrypted EIN=" + ein);
                e.printStackTrace();
            }
        }
        System.out.println("Encrypting the EINs:Done");
        return encryptedEINList;
    }

    private Set<CompanyData> getCompanyData(List<String> encryptedEINList) {
        System.out.println("Query Company Data:Start");
        Set<CompanyData> companyDataSet = new LinkedHashSet<>();
        try {
            Application.beginUnitOfWork();
            List<List<String>> partitionedList = ListUtils.partition(encryptedEINList, BATCH_SIZE);
            for (List<String> partition : partitionedList) {
                DomainEntitySet<Company> companySet = Application.find(Company.class, Company.FedTaxIdEnc().in(partition));
                for (Company company : companySet) {
                    CompanyData companyData = new CompanyData(company.getId().toString(), company.getSourceCompanyId(), company.getFedTaxId(), company.getLegalName());
                    companyDataSet.add(companyData);
                }
            }
        } finally {
            Application.rollbackUnitOfWork();
        }
        System.out.println("Total Records Found=" + companyDataSet.size());
        System.out.println("Query Company Data:Done");
        return companyDataSet;
    }

    private void writeHeader(CSVWriter csvWriter) {
        System.out.println("Write Header:Start");
        String[] header = new String[4];
        header[0] = "COMPANY_SEQ";
        header[1] = "PSID";
        header[2] = "EIN";
        header[3] = "LEGAL_NAME";
        csvWriter.writeNext(header);
        System.out.println("Write Header:Done");
    }

    private CSVWriter openWriter(String inputFile) throws IOException {
        File file = new File(inputFile);
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, true));
        return new CSVWriter(bufferedWriter);
    }

    private void closeWriter(CSVWriter csvWriter) throws IOException {
        csvWriter.flush();
        csvWriter.close();
    }

    private CSVReader openReader(String outputFile) throws IOException {
        Path path = Paths.get(outputFile);
        BufferedReader bufferedReader = Files.newBufferedReader(path);
        return new CSVReader(bufferedReader);
    }

    private void closeReader(CSVReader csvReader) throws IOException {
        csvReader.close();
    }

    private class CompanyData {

        private String companySeq;
        private String psid;
        private String ein;
        private String legalName;

        public CompanyData(String companySeq, String psid, String ein, String legalName) {
            this.companySeq = companySeq;
            this.psid = psid;
            this.ein = ein;
            this.legalName = legalName;
        }

        public String getCompanySeq() {
            return companySeq;
        }

        public String getEin() {
            return ein;
        }

        public String getLegalName() {
            return legalName;
        }

        public String getPsid() {
            return psid;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CompanyData that = (CompanyData) o;
            return Objects.equals(companySeq, that.companySeq) && Objects.equals(psid, that.psid) && Objects.equals(ein, that.ein) && Objects.equals(legalName, that.legalName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(companySeq, psid, ein, legalName);
        }
    }
}

package com.intuit.sbd.payroll.psp.batchjobs.reports;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.opencsv.bean.*;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author kmuthurangam
 *
 * CsvBeanReportEnricher helps enrich the reports where both source and target are csv files. It seralises each record into a Bean, enriches the record, deserailizes and write it back
 * into the target csv file.
 *
 */
public class CsvBeanReportEnricher<T> implements ReportEnricher {

    private static final SpcfLogger logger = Application.getLogger(CsvBeanReportEnricher.class);

    private Path sourcePath;
    private Path targetPath;
    private String[] columnHeaderNames;
    private MappingStrategy columnNameMappingStrategy;
    private Class beanType;
    private int batchSize;
    private RecordEnricher recordEnricher;

    private Reader reader;
    private Writer writer;

    private StatefulBeanToCsv statefulBeanToCsv;

    private CsvBeanReportEnricher(Path sourcePath, Path targetPath, String[] columnHeaderNames, MappingStrategy columnNameMappingStrategy, Class beanType, int batchSize, RecordEnricher recordEnricher) throws IOException {
        this.columnHeaderNames = columnHeaderNames;
        this.columnNameMappingStrategy = columnNameMappingStrategy;
        this.beanType = beanType;
        this.recordEnricher = recordEnricher;
        this.sourcePath = sourcePath;
        this.targetPath = targetPath;
        this.batchSize = batchSize;

        this.validate();
        this.initialize();
    }

    private void validate() {
        Objects.requireNonNull(sourcePath, "sourcePath cannot be null");
        Objects.requireNonNull(targetPath, "targetPath cannot be null");
        Objects.requireNonNull(columnHeaderNames, "columnHeaderNames cannot be null");
        Objects.requireNonNull(columnNameMappingStrategy, "columnNameMappingStrategy cannot be null");
        Objects.requireNonNull(beanType, "beanType cannot be null");
        Objects.requireNonNull(recordEnricher, "recordEnricher cannot be null");
    }

    private void initialize() throws IOException {
        openReader();
        openWriter();
    }

    @Override
    public void openReader() throws IOException {
        reader = new BufferedReader(new InputStreamReader(new FileInputStream(sourcePath.toFile()), StandardCharsets.UTF_8));
    }

    private MappingStrategy getColumnNameMappingStrategy() {
        return columnNameMappingStrategy;
    }

    public Writer getWriter() {
        return writer;
    }

    public String[] getColumnHeaderNames() {
        return columnHeaderNames;
    }

    private Class getBeanType() {
        return beanType;
    }

    public RecordEnricher getRecordEnricher() {
        return recordEnricher;
    }

    @Override
    public int getBatchSize() {
        return this.batchSize;
    }

    @Override
    public void enrichRecords(List t) throws IOException {
        getRecordEnricher().enrichRecords(t);
    }

    @Override
    public void writeObject(Object o) throws Exception {
        statefulBeanToCsv.write(o);
    }

    @Override
    public void writeObjects(List t) throws Exception {
        statefulBeanToCsv.write(t);
    }

    @Override
    public Iterator<T> iterator() throws IOException {
        CsvToBean csvToBean = new CsvToBeanBuilder(reader)
                .withSkipLines(1) // Ignore the header fields
                .withMappingStrategy(getColumnNameMappingStrategy())
                .withIgnoreEmptyLine(true)
                .withType(getBeanType()).build();
        return csvToBean.iterator();
    }

    @Override
    public void enrichReport() throws Exception {
        writeHeaders();
        ReportEnricher.super.enrichReport();
    }

    @Override
    public void closeReader() throws IOException {
        if (Objects.nonNull(reader)) {
            reader.close();
        }
    }

    @Override
    public void openWriter() throws IOException {
        boolean deleted =  Files.deleteIfExists(targetPath);
        if(deleted) {
            logger.warn(String.format("Enriched file %s already exists, deleted for re-enriching", targetPath));
        }
        writer = Files.newBufferedWriter(targetPath, StandardOpenOption.CREATE_NEW, StandardOpenOption.APPEND);
        statefulBeanToCsv = createStatefulBeanToCsv();
    }

    @Override
    public void closeWriter() throws IOException {
        if (Objects.nonNull(writer)) {
            writer.close();
        }
    }

    @Override
    public SpcfLogger getLogger() {
        return logger;
    }

    private void writeHeaders() throws IOException {
        getWriter().append(Arrays.stream(getColumnHeaderNames()).collect(Collectors.joining(",")));
        getWriter().append(System.lineSeparator());
    }

    private StatefulBeanToCsv createStatefulBeanToCsv() {
        return new StatefulBeanToCsvBuilder(writer)
                .withMappingStrategy(getColumnNameMappingStrategy())
                .withApplyQuotesToAll(false).build();
    }

    public static class Builder<T> {

        private Path sourcePath;
        private Path targetPath;
        private String[] columnHeaderNames;
        private MappingStrategy columnNameMappingStrategy;
        private Class beanType;
        private int batchSize;
        private RecordEnricher recordEnricher;

        public Builder sourcePath(Path sourcePath) {
            this.sourcePath = sourcePath;
            return this;
        }

        public Builder targetPath(Path targetPath) {
            this.targetPath = targetPath;
            return this;
        }

        public Builder columnHeaderNames(String[] columnHeaderNames) {
            this.columnHeaderNames = columnHeaderNames;
            return this;
        }

        public Builder columnNameMappingStrategy(MappingStrategy columnNameMappingStrategy) {
            this.columnNameMappingStrategy = columnNameMappingStrategy;
            return this;
        }

        public Builder beanType(Class beanType) {
            this.beanType = beanType;
            return this;
        }

        public Builder batchSize(int batchSize) {
            this.batchSize = batchSize;
            return this;
        }

        public Builder recordEnricher(RecordEnricher recordEnricher) {
            this.recordEnricher = recordEnricher;
            return this;
        }

        public CsvBeanReportEnricher<T> build() throws IOException {
            return new CsvBeanReportEnricher(sourcePath, targetPath, columnHeaderNames, columnNameMappingStrategy, beanType, batchSize, recordEnricher);
        }
    }
}

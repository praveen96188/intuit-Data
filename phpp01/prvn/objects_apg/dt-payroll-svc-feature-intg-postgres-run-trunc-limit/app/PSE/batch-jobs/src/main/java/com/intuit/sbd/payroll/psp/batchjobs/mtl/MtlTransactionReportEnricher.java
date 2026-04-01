package com.intuit.sbd.payroll.psp.batchjobs.mtl;


import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.batchjobs.reports.CsvBeanReportEnricher;
import com.intuit.sbd.payroll.psp.batchjobs.reports.ReportEnricher;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.MappingStrategy;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author kmuthurangam
 *
 * MtlTransactionReportEnricher enriches all records in the MTL Transaction Report with sensitive PII and missing critical information. Currently it supports csv file as both source and target.
 * It works in batch size and supports upto match size of {@code MtlTransactionRecord.MAX_BATCH_SIZE} due to database IN criteria limitation
 *
 */
public class MtlTransactionReportEnricher {

    private static final SpcfLogger logger = Application.getLogger(MtlTransactionReportEnricher.class);

    public static final int MAX_BATCH_SIZE = 1000;

    private ReportEnricher<MtlTransactionRecord> reportEnricher;

    public MtlTransactionReportEnricher(Path sourcePath, Path targetPath) throws IOException {
        reportEnricher = createReportEnricher(sourcePath, targetPath);
    }

    private ReportEnricher<MtlTransactionRecord> createReportEnricher(Path sourcePath, Path targetPath) throws IOException {
        return new CsvBeanReportEnricher.Builder<MtlTransactionRecord>()
                .sourcePath(sourcePath)
                .targetPath(targetPath)
                .columnHeaderNames(getMtlTransactionReportHeaders())
                .columnNameMappingStrategy(getColumnNameMappingStrategy())
                .beanType(MtlTransactionRecord.class)
                .batchSize(getBatchSize())
                .recordEnricher(new MtlTransactionRecordEnricher()).build();
    }

    public void enrichReport() throws Exception {
        reportEnricher.enrichReport();
    }

    public void close() throws Exception {
        reportEnricher.close();
    }

    private MappingStrategy getColumnNameMappingStrategy() {
        ColumnPositionMappingStrategy<MtlTransactionRecord> mappingStrategy = new ColumnPositionMappingStrategy<>();
        mappingStrategy.setType(MtlTransactionRecord.class);
        mappingStrategy.setColumnMapping(getPositionalColumnMapping());
        return mappingStrategy;
    }

    private int getBatchSize() {
        int batchSize = MtlTransactionReportUtils.getMtlBatchSize();
        if(batchSize > MAX_BATCH_SIZE) {
            batchSize = MAX_BATCH_SIZE;
            logger.warn(String.format("Defaulting to max batch size of 1000 as provided batch size of %d cannot be supported", batchSize));
        }
        return batchSize;
    }

    private String[] getMtlTransactionReportHeaders() {
        return MtlTransactionReportUtils.getMtlTransactionReportHeaders();
    }

    public String[] getPositionalColumnMapping() {
        return MtlTransactionReportUtils.getMtlBeanColumMapping();
    }

}

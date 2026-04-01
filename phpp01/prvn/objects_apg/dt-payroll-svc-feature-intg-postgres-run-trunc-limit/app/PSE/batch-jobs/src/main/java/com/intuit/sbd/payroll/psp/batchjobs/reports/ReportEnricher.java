package com.intuit.sbd.payroll.psp.batchjobs.reports;

import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * @author kmuthurangam
 *
 * ReportEnricher intends to enrich the reports where source and target could be different datasources like csv, database. Few examples below
 * <li>Source - csv, Target - Database</li>
 * <li>Source - kafka, Target - csv</li>
 *
 */
public interface ReportEnricher<T> extends AutoCloseable {

    void openReader() throws IOException;

    Iterator<T> iterator() throws IOException;

    default void enrichReport() throws Exception {
        StopWatch stopWatch = new StopWatch();
        try {
            stopWatch.start();
            int processedRecords = 0;
            Iterator<T> iterator = iterator();
            List<T> records = new ArrayList<>(getBatchSize());
            while (iterator.hasNext()) {
                T type = iterator.next();

                records.add(type);

                processedRecords++;
                if ((records.size() % getBatchSize()) == 0) {
                    enrichAndWriteObjects(records);
                    getLogger().info( "Total number of records processed="+processedRecords +" in "+ stopWatch.getElapsedTimeString());
                    records.clear();
                }
            }

            if (records.size()!=0) {
                enrichAndWriteObjects(records);
            }

            getLogger().info("Total number of records processed=" + processedRecords + " in " + stopWatch.getElapsedTimeString());
        } finally {
            if(Objects.nonNull(stopWatch)) {
                stopWatch.stop();
            }
            close();
        }
    }

    default void enrichAndWriteObjects(List<T> t) throws Exception {
        enrichRecords(t);
        writeObjects(t);
    }

    default void enrichAndWriteObject(T t) throws Exception {
        enrichRecord(t);
        writeObject(t);
    }

    default int getBatchSize() {
        return 100;
    }

    default void enrichRecord(T t) throws IOException {

    }

    default void enrichRecords(List<T> t) throws IOException {

    }

    void closeReader() throws IOException;

    void openWriter() throws IOException;

    void writeObject(T t) throws Exception;

    void writeObjects(List<T> t) throws Exception;

    void closeWriter() throws IOException;

    @Override
    default void close() throws Exception {
        closeReader();
        closeWriter();
    }

    SpcfLogger getLogger();
}

package com.intuit.sbd.payroll.psp.batchjobs.mtl;

import com.intuit.sbd.payroll.psp.Application;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.MappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import lombok.SneakyThrows;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MtlTransactionReportFileUtils {

    private static final String baseMTLFolder = "mtl";

    public static Path getAbsolutePath(String fileName) {
        return Paths.get(Application.findFileOnClassPath(baseMTLFolder) + File.separator + fileName);
    }

    @SneakyThrows
    public void createReport(Path path, List<MtlTransactionRecord> mtlTransactionRecords) {
        Files.deleteIfExists(path);

        //Open Writer in append mode
        Writer writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.APPEND);

        //Write Headers and Records
        writeMtlHeadersAndRecords(writer, mtlTransactionRecords);

        //Close Writer
        if (Objects.nonNull(writer)) {
            writer.close();
        }
    }


    private void writeMtlHeadersAndRecords(@NotNull Writer writer, @NotNull List<MtlTransactionRecord> allRawMTLTransactionRecords) throws Exception {
        StatefulBeanToCsv statefulBeanToCsv = createStatefulBeanToCsv(writer);
        writer.append(Arrays.stream(getColumnHeaderNames()).collect(Collectors.joining(",")));
        writer.append(System.lineSeparator());
        statefulBeanToCsv.write(allRawMTLTransactionRecords);
    }

    public String[] getColumnHeaderNames() {
        return MtlTransactionReportUtils.getMtlTransactionReportHeaders();
    }

    private StatefulBeanToCsv createStatefulBeanToCsv(Writer writer) {
        return new StatefulBeanToCsvBuilder(writer)
                .withMappingStrategy(getColumnNameMappingStrategy())
                .withApplyQuotesToAll(false).build();
    }

    private MappingStrategy getColumnNameMappingStrategy() {
        ColumnPositionMappingStrategy<MtlTransactionRecord> mappingStrategy = new ColumnPositionMappingStrategy<>();
        mappingStrategy.setType(MtlTransactionRecord.class);
        mappingStrategy.setColumnMapping(getPositionalColumnMapping());
        return mappingStrategy;
    }

    public String[] getPositionalColumnMapping() {
        return MtlTransactionReportUtils.getMtlBeanColumMapping();
    }
}

package com.intuit.sbd.payroll.psp.batchjobs.sendCustomEmail;

import com.intuit.sbd.payroll.psp.batchjobs.sendCustomEmail.models.inputFileModel.LegacyToSymphonyInputFileModel;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.FileReader;
import java.util.List;

@Slf4j
public class L2SMInputFileDeserializer implements InputFileDeserializer {

    @Override
    public List<LegacyToSymphonyInputFileModel>  deserialize(String filePath) {
        try {
            log.info("job=SendCustomEmailsProcessor, Action=L2SMInputFileDeserializer, Method=deserialize, Status=Start");
            List<LegacyToSymphonyInputFileModel> list = new CsvToBeanBuilder<LegacyToSymphonyInputFileModel>(new FileReader(filePath))
                    .withType(LegacyToSymphonyInputFileModel.class).build().parse();
            log.info("job=SendCustomEmailsProcessor, Action=L2SMInputFileDeserializer, Method=deserialize, Status=Complete");
            return list;
        } catch (Exception e) {
            log.error("job=SendCustomEmailsProcessor, Action=L2SMInputFileDeserializer, Method=deserialize, Status=Error");
            throw new RuntimeException(e);
        }


    }
}

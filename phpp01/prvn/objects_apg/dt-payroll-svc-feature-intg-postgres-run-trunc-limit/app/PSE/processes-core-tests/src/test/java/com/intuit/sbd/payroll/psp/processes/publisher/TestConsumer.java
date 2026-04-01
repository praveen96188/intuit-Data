package com.intuit.sbd.payroll.psp.processes.publisher;

import com.intuit.eventbus.exceptions.FormatException;
import com.intuit.eventbus.utils.Result;
import com.intuit.payroll.api.employee.model.EmployeeCDM;
import com.intuit.payroll.api.shared.model.AddressCDM;
import com.intuit.payroll.api.shared.model.AddressCDMImpl;
import com.intuit.payroll.api.shared.model.AddressSubCDM;
import com.intuit.sbd.payroll.psp.domain.Address;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.mapper.jackson.CustomObjectMapperResolver;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.kafka.core.ConsumerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import static org.junit.Assert.*;

public class TestConsumer {

    private Consumer<String, Result<FormatException, String>> consumer;


    public TestConsumer(String topic, boolean shouldReadLatest){
        initConsumer(topic, shouldReadLatest);
    }

    public void seekLatest(){
        consumer.seekToEnd(new ArrayList<>());
        consumer.poll(Duration.ofSeconds(60));
        consumer.commitSync();
    }

    public ConsumerRecords getMessages(){
        return consumer.poll(Duration.ofSeconds(60));
    }


    private void initConsumer(String topic, boolean shouldReadLatest){
        consumer = createConsumer();
        consumer.subscribe(Arrays.asList(topic));
        if(shouldReadLatest){
            seekLatest();
        }
    }

    private Consumer<String, Result<FormatException, String>> createConsumer() {
        ConsumerFactory<String, Result<FormatException, String>> consumerFactory = PayrollApplicationBeanFactory.getBean(ConsumerFactory.class);
        return consumerFactory.createConsumer();
    }

}

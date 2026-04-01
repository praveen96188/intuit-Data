package com.intuit.sbd.payroll.psp.entity.publisher;

import com.intuit.sbd.payroll.psp.context.threading.ChildThreadRequestContextHelper;
import com.intuit.sbd.payroll.psp.entity.EntityEventContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class EntityEventPublisherExecutor {

    private final int THREAD_POOL_SIZE = 10;

    private ExecutorService executor;

    @PostConstruct
    public void init() {
        executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    //Todo: graceful shutdown of executor
    public void publishEvent(EntityEventContext entityEventContext) {
        ChildThreadRequestContextHelper childThreadRequestContextHelper = new ChildThreadRequestContextHelper();
        childThreadRequestContextHelper.loadThreadLocals();
        executor.submit(new PublisherService(entityEventContext, childThreadRequestContextHelper));
    }
}

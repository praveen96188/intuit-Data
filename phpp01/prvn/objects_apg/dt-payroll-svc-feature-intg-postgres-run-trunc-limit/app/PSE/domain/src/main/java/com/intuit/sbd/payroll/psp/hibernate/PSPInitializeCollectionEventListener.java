package com.intuit.sbd.payroll.psp.hibernate;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManagerHelper;
import com.intuit.sbd.payroll.psp.context.model.RequestContext;
import com.intuit.sbd.payroll.psp.filter.PartitionedParentTenantInfo;
import com.intuit.sbd.payroll.psp.filter.TenantInfo;
import com.intuit.sbd.payroll.psp.filter.constants.PartitionedTablesDetails;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlagLazyLoader;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.BatchFetchQueue;
import org.hibernate.engine.spi.CollectionEntry;
import org.hibernate.engine.spi.PersistenceContext;
import org.hibernate.event.internal.DefaultInitializeCollectionEventListener;
import org.hibernate.event.spi.InitializeCollectionEvent;

import java.lang.reflect.Field;
import java.util.*;

/**
 * TODO: This code uses Reflection to get Private Member Variables of Hibernate Classes. We need to check the functionality of this code before performing any Hibernate Upgrade
 * This code is used to reset Company Filter when we are seeing Hibernate Batching with multiple companies
 */
@Slf4j
public class PSPInitializeCollectionEventListener extends DefaultInitializeCollectionEventListener {

    private PartitionedTablesDetails partitionedTablesDetails;
    private List<String> partitionedAndChildClassesSimpleNameList;

    public PSPInitializeCollectionEventListener() {
        partitionedTablesDetails = PayrollApplicationBeanFactory.getBean(PartitionedTablesDetails.class);
        partitionedAndChildClassesSimpleNameList = partitionedTablesDetails.getPartitionedAndChildClassesSimpleNameList();
    }
    public void onInitializeCollection(InitializeCollectionEvent event) throws HibernateException {
        RequestContext requestContext = null;
        try {
            requestContext = handleBatchingInternal(event);
            super.onInitializeCollection(event);
        } finally {
            setRequestContextAgain(requestContext);
        }
    }

    private RequestContext handleBatchingInternal(InitializeCollectionEvent event) {
        try {
            boolean enableAspect = FeatureFlagLazyLoader.getInstance().getFeatureFlagValue(FeatureFlags.Key.ENABLE_BATCHING_ASPECT);
            if (!enableAspect) {
                return null;
            }

            if(Hibernate.isInitialized(event.getCollection())) {
                return null;
            }

            RequestContext requestContext = PSPRequestContextManagerHelper.getPSPRequestContextManager().getRequestContext();
            boolean isRequestContextSet = isRequestContextSet(requestContext);
            if (!isRequestContextSet) {
                return null;
            }

            ////Example: com.intuit.sbd.payroll.psp.domain.PayrollRun -> will return PayrollRun
            String className = event.getAffectedOwnerEntityName();
            className = className.substring(className.lastIndexOf(".") + 1);

            String collectionClass = getCollectionClass(event);

            //Checking if this collection is a partitioned or child table collection
            if(!partitionedAndChildClassesSimpleNameList.contains(collectionClass)) {
                return null;
            }

            PersistenceContext persistenceContext = Application.getHibernatePersistentContext();

            if (Objects.isNull(persistenceContext)) {
                return null;
            }
            return checkBatchQueue(persistenceContext, className, collectionClass, requestContext, event);
        } catch (Exception e) {
            log.error("Exception occurred during handleBatchingInternal", e);
        }
        return null;
    }

    private String getCollectionClass(InitializeCollectionEvent event) {
        //Example: com.intuit.sbd.payroll.psp.domain.PayrollRun.FinancialTransactionSet -> will return FinancialTransaction
        String collectionClass = event.getCollection().getRole();
        collectionClass = collectionClass.substring(collectionClass.lastIndexOf(".") + 1);
        collectionClass = collectionClass.substring(0, collectionClass.length() - 3);
        return collectionClass;
    }


    private boolean isRequestContextSet(RequestContext requestContext) {
        if (Objects.isNull(requestContext) ||
                Objects.isNull(requestContext.getCompanyInfo()) ||
                Objects.isNull(requestContext.getCompanyInfo().getCompanySequence())) {
            return false;
        }
        return true;
    }

    private RequestContext checkBatchQueue(PersistenceContext persistenceContext, String className, String collectionClass, RequestContext requestContext, InitializeCollectionEvent event) {
        try {
            BatchFetchQueue batchFetchQueue = persistenceContext.getBatchFetchQueue();
            Field privateField = BatchFetchQueue.class.getDeclaredField("batchLoadableCollections");
            privateField.setAccessible(true);
            Map<String, LinkedHashMap<CollectionEntry, PersistentCollection>> batchLoadableCollections =
                    (Map<String, LinkedHashMap<CollectionEntry, PersistentCollection>>) privateField.get(batchFetchQueue);

            if(Objects.isNull(batchLoadableCollections)) {
                return null;
            }

            //This will return the Fully Qualified Name of the Set. Example: com.intuit.sbd.payroll.psp.domain.PayrollRun.FinancialTransactionSet
            String batchLoadableCollection = event.getCollection().getRole();
            LinkedHashMap<CollectionEntry, PersistentCollection> map = batchLoadableCollections.get(batchLoadableCollection);

            if(Objects.isNull(map) || map.size() < 2) {
                return null;
            }

            Set<String> companyIds = new HashSet<>();
            for (Map.Entry<CollectionEntry, PersistentCollection> me : map.entrySet()) {
                String companyId = null;
                final PersistentCollection collection = me.getValue();

                if (collection.getOwner() instanceof TenantInfo) {
                    TenantInfo tenantInfo = (TenantInfo) collection.getOwner();
                    companyId = tenantInfo.getTenantId().toString();
                    companyIds.add(companyId);
                }

                if (collection.getOwner() instanceof PartitionedParentTenantInfo) {
                    PartitionedParentTenantInfo partitionedParentTenantInfo = (PartitionedParentTenantInfo) collection.getOwner();
                    companyId = partitionedParentTenantInfo.getTenantId().toString();
                    companyIds.add(companyId);
                }

                if (companyIds.size() > 1) {
                    PSPRequestContextManagerHelper.getPSPRequestContextManager().clearRequestContext();
                    log.info("Event=DifferentCompanyEntitiesFoundWhileLoadingSet Count={} RequestContextSet={}, RequestContext={}, ParentClass={}, CollectionClass={}",
                            companyIds.size(), true, requestContext, className, collectionClass);
                    return requestContext;
                }
            }

        } catch (Exception e) {
            log.error("Exception occurred during checkBatchQueue", e);
        }
        return null;
    }

    private void setRequestContextAgain(RequestContext requestContext) {
        try {
            if(Objects.nonNull(requestContext)) {
                PSPRequestContextManagerHelper.getPSPRequestContextManager().setRequestContext(requestContext);
            }
        } catch (Exception e) {
            log.error("Exception occurred during setRequestContextAgain", e);
        }
    }
}

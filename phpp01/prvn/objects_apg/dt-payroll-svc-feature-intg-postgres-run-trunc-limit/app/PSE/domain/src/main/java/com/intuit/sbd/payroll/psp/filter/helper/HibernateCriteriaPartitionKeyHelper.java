package com.intuit.sbd.payroll.psp.filter.helper;

import com.intuit.sbd.payroll.psp.filter.constants.PartitionedTablesDetails;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.criterion.Restrictions;
import org.hibernate.internal.CriteriaImpl;
import org.hibernate.sql.JoinType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class is used to add Partition Key to Criteria Queries.
 */
@Slf4j
@Component
public class HibernateCriteriaPartitionKeyHelper {
    private PartitionedTablesDetails partitionedTablesDetails;

    @Autowired
    public HibernateCriteriaPartitionKeyHelper(PartitionedTablesDetails partitionedTablesDetails) {
        this.partitionedTablesDetails = partitionedTablesDetails;
    }

    public Criteria addRestrictionsToHibernateCriteria(Criteria hibernateCriteria, SpcfUniqueId companySequence) {
        try {
            List<String> addedTables = new ArrayList<>();
            hibernateCriteria = addRestrictionsIfSubcriteriaExists(hibernateCriteria, companySequence, addedTables);
            hibernateCriteria = addRestrictionsOnFetchModes(hibernateCriteria, companySequence, addedTables);

        } catch (Exception e) {
            log.error("Exception occurred during addRestrictionsToHibernateCriteria", e);
        }
        return hibernateCriteria;
    }

    /**
     * This method will add Partition Key if the Hibernate Criteria already have any restriction on the Partitioned Table,
     * basically the partitioned table is present as a Subcriteria
     */
    private Criteria addRestrictionsIfSubcriteriaExists(Criteria hibernateCriteria, SpcfUniqueId companySequence, List<String> addedTables) {
        CriteriaImpl criteriaImpl = (CriteriaImpl) hibernateCriteria;
        Iterator<CriteriaImpl.Subcriteria> subcriteriaIterator = criteriaImpl.iterateSubcriteria();
        while (subcriteriaIterator.hasNext()) {
            CriteriaImpl.Subcriteria subcriteria = subcriteriaIterator.next();
            String path = subcriteria.getPath();
            String className = path.substring(path.lastIndexOf('.') + 1);
            if (partitionedTablesDetails.getPartitionedAndChildClassesSimpleNameList().contains(className)) {
                String propertyName = subcriteria.getAlias() + ".Company.Id";
                hibernateCriteria.add(Restrictions.eq(propertyName, companySequence));
                addedTables.add(className);
            }
        }
        return hibernateCriteria;
    }

    /**
     * This method will add Partition Key if the Hibernate Criteria have the partitioned table only as a Join in FetchModes,
     * This case can occur if we are EagerLoading a Partitioned Table.
     * Here, we create an Alias for the table and then add the restriction.
     */
    private Criteria addRestrictionsOnFetchModes(Criteria hibernateCriteria, SpcfUniqueId companySequence, List<String> addedTables) {
        CriteriaImpl criteriaImpl = (CriteriaImpl) hibernateCriteria;
        for(String className : partitionedTablesDetails.getPartitionedAndChildClassesSimpleNameList()) {
            String fetchModePath = "this." + className;
            FetchMode fetchMode = criteriaImpl.getFetchMode(fetchModePath);
            if (!addedTables.contains(className) && FetchMode.JOIN.equals(fetchMode)) {
                hibernateCriteria.createCriteria(className, className, JoinType.LEFT_OUTER_JOIN, Restrictions.eq("Company.Id", companySequence));
            }
        }
        return hibernateCriteria;
    }
}

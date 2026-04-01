package com.intuit.sbd.payroll.psp.domainsecondary;

import com.amazonaws.util.CollectionUtils;
import com.intuit.sbd.payroll.psp.domainsecondary.entitybase.BaseHcm401kPolicy;
import com.intuit.sbd.payroll.psp.query.Criterion;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Hand-written business logic
 */
public class Hcm401kPolicy extends BaseHcm401kPolicy {

	/**
	 * Default constructor.
	 */
	public Hcm401kPolicy()
	{
		super();
	}

	private static Set<Hcm401kPolicy> policies;

	private static void initializePoliciesIfEmpty(){

		if(CollectionUtils.isNullOrEmpty(policies) || policies.size() < 4){
			DeductionItemProvider deductionItemProvider = DeductionItemProvider.Guideline;
			Set<Hcm401kPolicy> hcm401kPolicies = new HashSet<>();

			Criterion<Hcm401kPolicy> policyWhere = (Hcm401kPolicy.DeductionItemProvider().equalTo(deductionItemProvider));
			hcm401kPolicies.addAll(ApplicationSecondary.find(Hcm401kPolicy.class, policyWhere));
			policies = hcm401kPolicies;
		}
	}

	public static Hcm401kPolicy getHcm401kPolicyByDeductionItemAndProvider(DeductionItemPolicy deductionItemPolicy, DeductionItemProvider deductionItemProvider){
		initializePoliciesIfEmpty();

		if (CollectionUtils.isNullOrEmpty(policies)) return null;

		Optional<Hcm401kPolicy> first = policies.stream().filter(hcm401kPolicy -> hcm401kPolicy.getDeductionItemPolicy().in(deductionItemPolicy)
				&& hcm401kPolicy.getDeductionItemProvider() == deductionItemProvider).findFirst();
		return first.isPresent() ? first.get() : null;
	}

}
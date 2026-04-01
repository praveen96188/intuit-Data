package com.intuit.sbd.payroll.psp.domain;

import com.google.gson.JsonObject;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.hibernate.EntityChangeListener;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.springframework.util.CollectionUtils;

import javax.persistence.Entity;
import java.util.List;

/**
 * Hand-written business logic
 */
@Entity // Annotate the class with @Entity for compile time BytecodeEnhancement for attribute lazy loading
public class EntityUpdate extends BaseEntityUpdate  implements  EntityChangeListener{
    private boolean isDuplicate=false;

	/**
	 * Default constructor.
	 */
	public EntityUpdate()
	{
		super();
	}

	@Override
	public JsonObject getChangedAttribute() {
		return null;
	}

	@Override
	public String getuniqueId() {
		return this.getId().toString();
	}

	@Override
	public String getEntitiesName() {
		return "EntityUpdate";
	}

	@Override
	public Long getEntityVersion() {
		return this.getVersion();
	}
	@Override
	public void isDuplicate(boolean duplicate) {
		 this.isDuplicate=duplicate;
	}

	@Override
	public boolean getDuplicate() {
		return isDuplicate;
	}

	public static List<EntityUpdate> findEntitiesUsing(List<SpcfUniqueId> entityIds) {
		return Application.executeQuery(EntityUpdate.class, EntityUpdate.Id().in(entityIds));
	}

	public static List<EntityUpdate> findEntitiesUsing(SpcfCalendar startTime, SpcfCalendar endTime, List<Status> statuses,
														List<String> entitiesName, int batchSize, int maxRetryCount) {
		Criterion<EntityUpdate> criterion = EntityUpdate.Status().in(statuses)
				.And(EntityUpdate.CreatedDate().between(startTime, endTime))
				.And(RetryCount().between(0, maxRetryCount))
				.And(EntityName().in(entitiesName));
		Expression<EntityUpdate> expression = new Query<EntityUpdate>()
				.Where(criterion).OrderBy(EntityUpdate.CreatedDate()).LimitResults(0, batchSize);
		return Application.executeQuery(EntityUpdate.class, expression);
	}

}
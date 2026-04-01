package com.intuit.ems.payroll.psp.jss.monitoring.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 
 * @author kmuthurangam
 *
 */
public class BatchJob {

	private String name;

	private Date nextSplunkSearchTime;

	private List<BatchJob> dependsOn;

	public BatchJob() {
		super();
	}

	public BatchJob(String name) {
		super();
		this.name = name;
	}

	public BatchJob(String name, List<BatchJob> dependsOn) {
		super();
		this.name = name;
		this.dependsOn = dependsOn;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getNextSplunkSearchTime() {
		return nextSplunkSearchTime;
	}

	public void setNextSplunkSearchTime(Date nextSplunkSearchTime) {
		this.nextSplunkSearchTime = nextSplunkSearchTime;
	}

	public List<BatchJob> getDependsOn() {
		return dependsOn;
	}

	public void setDependsOn(List<BatchJob> depdensOn) {
		this.dependsOn = depdensOn;
	}

	public void addDepdensOn(BatchJob batchJob) {
		if (this.dependsOn == null) {
			this.dependsOn = new ArrayList<BatchJob>();
		}
		this.dependsOn.add(batchJob);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(name);
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BatchJob other = (BatchJob) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}

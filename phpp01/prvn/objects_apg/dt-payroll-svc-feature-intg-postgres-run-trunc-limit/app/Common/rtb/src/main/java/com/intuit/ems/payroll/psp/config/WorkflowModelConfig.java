package com.intuit.ems.payroll.psp.config;

import com.intuit.ems.payroll.psp.workflowfinder.model.WorkflowModel;
import com.intuit.sbg.psp.spring.YamlPropertySourceFactory;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/**
 * @author rn5
 *
 */
@Configuration
@Data
@ConfigurationProperties(prefix = "rtb.workflow-finder")
@PropertySource(value = "classpath:rtb.yaml", factory = YamlPropertySourceFactory.class)
public class WorkflowModelConfig {

	private Map<String, List<WorkflowModel>> modules;

	@PostConstruct
	public void validate() {
		modules.forEach((module, workflowModels) -> {
			workflowModels.forEach(workflowModel -> {
				Validate.isTrue(CollectionUtils.isNotEmpty(workflowModel.getClassAnnotation())
						|| CollectionUtils.isNotEmpty(workflowModel.getMethodAnnotation()));
			});
		});
	}
}

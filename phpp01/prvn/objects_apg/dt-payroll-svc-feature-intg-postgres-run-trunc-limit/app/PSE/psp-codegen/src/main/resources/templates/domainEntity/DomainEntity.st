package com.intuit.sbd.payroll.psp.domain;

$if(bedlDataEntity.hasClob)$
import javax.persistence.Entity;
$endif$


/**
 * Hand-written business logic
 */

$if(bedlDataEntity.hasClob)$
@Entity // Annotate the class with @Entity for compile time BytecodeEnhancement for attribute lazy loading
$endif$

public class $bedlDataEntity.className$ extends Base$bedlDataEntity.className$ {

	/**
	 * Default constructor.
	 */
	public $bedlDataEntity.className$()
	{
		super();
	}

}

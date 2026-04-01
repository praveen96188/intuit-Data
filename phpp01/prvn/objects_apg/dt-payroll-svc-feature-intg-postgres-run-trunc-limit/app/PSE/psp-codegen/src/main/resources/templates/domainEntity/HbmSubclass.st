<?xml version="1.0" encoding="utf-8" ?>
<!DOCTYPE hibernate-mapping PUBLIC "-//Hibernate/Hibernate Mapping DTD 3.0//EN" "http://www.hibernate.org/dtd/hibernate-mapping-3.0.dtd">
<hibernate-mapping package="com.intuit.sbd.payroll.psp.domain" default-cascade="merge" auto-import="false" default-access="com.intuit.sbd.payroll.psp.hibernate.PSPMixedAccessor">
	<joined-subclass name="com.intuit.sbd.payroll.psp.domain.$bedlDataEntity.ClassName$" entity-name="com.intuit.sbd.payroll.psp.domain.$bedlDataEntity.ClassName$" extends="com.intuit.sbd.payroll.psp.domain.$bedlDataEntity.BaseClassName$" table="PSP_$bedlDataEntity.PspTableName$" >
		<key column="$bedlDataEntity.keyPropertyName$" />

		<!-- $bedlDataEntity.ClassName$ Declared Properties -->
		$bedlDataEntity.ScalarProperties:HbmScalarProperty(); separator="\n\n"$
        $bedlDataEntity.ComplexProperties:HbmComplexProperty(); separator="\n\n"$
        $bedlDataEntity.OneToOneProperties:HbmOneToOneProperty(); separator="\n\n"$
        $bedlDataEntity.ManyToOneProperties:HbmManyToOneProperty(); separator="\n\n"$
        $bedlDataEntity.OneToManyProperties:HbmOneToManyProperty(); separator="\n\n"$
        $bedlDataEntity.ManyToManyProperties:HbmManyToManyProperty(); separator="\n\n"$
	</joined-subclass>
	$bedlDataEntity.FilterDefinitions:FilterDefinition(); separator="\n\n"$
</hibernate-mapping>

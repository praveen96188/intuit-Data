<?xml version="1.0" encoding="utf-8" ?>
<!DOCTYPE hibernate-mapping PUBLIC "-//Hibernate/Hibernate Mapping DTD 3.0//EN" "http://www.hibernate.org/dtd/hibernate-mapping-3.0.dtd">
<hibernate-mapping package="com.intuit.sbd.payroll.psp.domain" default-cascade="merge" auto-import="false" default-access="com.intuit.sbd.payroll.psp.hibernate.PSPMixedAccessor">
	<class name="com.intuit.sbd.payroll.psp.domain.$bedlDataEntity.ClassName$" entity-name="com.intuit.sbd.payroll.psp.domain.$bedlDataEntity.ClassName$" table="PSP_$bedlDataEntity.PspTableName$"$if(bedlDataEntity.isDynamicUpdate)$ dynamic-update="true"$endif$>
	    $if(!bedlDataEntity.isDataObject)$
		<id name="Id" column="$bedlDataEntity.keyPropertyName$" type="com.intuit.sbd.payroll.psp.hibernate.SpcfUniqueIdUserType" access="property">
			<generator class="assigned"/>
		</id>
		$else$
		$if(bedlDataEntity.KeyProperty.isEnumeration)$
        <id name="$bedlDataEntity.KeyProperty.propertyName$" column="$bedlDataEntity.KeyProperty.FirstPspColumnName$" >
           <type name="com.intuit.sbd.payroll.psp.hibernate.SpcfStringEnumUserType">
		        <param name="enumClassname">com.intuit.sbd.payroll.psp.domain.$bedlDataEntity.KeyProperty.PropertyType$</param>
           </type>
        </id>
        $else$
        <id name="$bedlDataEntity.KeyProperty.propertyName$" column="$bedlDataEntity.KeyProperty.FirstPspColumnName$" type="$bedlDataEntity.KeyProperty.PropertyHbmType$" />
		$endif$
		$endif$

		<!-- $bedlDataEntity.ClassName$ Version Property -->
		<version name="Version" type="long" column="VERSION" unsaved-value="negative" />

		<!-- $bedlDataEntity.ClassName$ Admin Properties -->
	    $if(!bedlDataEntity.isDataObject)$
		<property name="CreatorId" type="string" column="CREATOR_ID" not-null="false" />
		<property name="CreatedDate" type="com.intuit.sbd.payroll.psp.hibernate.SpcfCalendarUserType" column="CREATED_DATE" not-null="true" />
		<property name="ModifierId" type="string" column="MODIFIER_ID" not-null="false" />
		<property name="ModifiedDate" type="com.intuit.sbd.payroll.psp.hibernate.SpcfCalendarUserType" column="MODIFIED_DATE" not-null="true"/>
		<property name="TempId" type="string" column="$bedlDataEntity.keyPropertyName$" update="false" insert="false" access="property"/>
        $endif$
        <property name="RealmId" type="long" column="REALM_ID" not-null="true" insert="true" update="false" access="property"/>

		<!-- $bedlDataEntity.ClassName$ Declared Properties -->
		$bedlDataEntity.ScalarProperties:HbmScalarProperty(); separator="\n\n"$
        $bedlDataEntity.ComplexProperties:HbmComplexProperty(); separator="\n\n"$
        $bedlDataEntity.OneToOneProperties:HbmOneToOneProperty(); separator="\n\n"$
        $bedlDataEntity.ManyToOneProperties:HbmManyToOneProperty(); separator="\n\n"$
        $bedlDataEntity.OneToManyProperties:HbmOneToManyProperty(); separator="\n\n"$
        $bedlDataEntity.ManyToManyProperties:HbmManyToManyProperty(); separator="\n\n"$
        $bedlDataEntity.Filters:Filter(); separator="\n\n"$
	</class>
	$bedlDataEntity.FilterDefinitions:FilterDefinition(); separator="\n\n"$
</hibernate-mapping>

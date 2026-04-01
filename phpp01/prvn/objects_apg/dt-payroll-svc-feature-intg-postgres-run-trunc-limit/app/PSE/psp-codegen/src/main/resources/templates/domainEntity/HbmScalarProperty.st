$if(!it.IsKeyProperty)$
$if(it.isEnumeration)$
<property name="$it.propertyName$" column="$it.FirstPspColumnName$" $if(it.optimisticLockingDisabled)$ optimistic-lock="false"$endif$>
	<type name="com.intuit.sbd.payroll.psp.hibernate.SpcfStringEnumUserType">
		<param name="enumClassname">com.intuit.sbd.payroll.psp.domain.$it.propertyType$</param>
	</type>
</property>
$else$
<property name="$it.propertyName$" type="$it.PropertyHbmType$" column="$it.FirstPspColumnName$"$if(it.optimisticLockingDisabled)$ optimistic-lock="false"$endif$ $if(it.isClob)$lazy="true"$endif$/>
$endif$
$endif$


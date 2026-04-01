$if(it.isOwner)$
<many-to-one name="$it.PropertyName$" class="com.intuit.sbd.payroll.psp.domain.$it.propertyType$" column="$it.FirstPspColumnName$" unique="true" $if(it.isNotNullable)$ not-null="$it.isNotNullable$" $endif$ />
$else$
<set name="$it.propertyName$Set" inverse="true" access="property" batch-size="100">
	<key column="$it.InverseColumnName$" $if(it.isNotNullable)$ not-null="$it.isNotNullable$" $endif$/>
	<one-to-many class="com.intuit.sbd.payroll.psp.domain.$it.PropertyType$" />
	$it.Filters:Filter(); separator="\n\n"$
</set>
$endif$
<set name="$it.propertyName$Set" inverse="true" $if(it.isComposition)$ cascade="all-delete-orphan" $endif$ access="property" batch-size="$it.batchSizeForOneToMany$">
	<key column="$it.JoinColumnName$" $if(it.isNotNullable)$ not-null="$it.isNotNullable$" $endif$/>
	<one-to-many class="com.intuit.sbd.payroll.psp.domain.$it.PropertyType$" />
	$it.Filters:Filter(); separator="\n\n"$
</set>
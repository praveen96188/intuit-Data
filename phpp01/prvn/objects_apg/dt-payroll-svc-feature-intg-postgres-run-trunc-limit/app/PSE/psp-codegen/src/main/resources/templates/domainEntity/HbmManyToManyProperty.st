$if(it.isOwner)$
<set name="$it.propertyName$Set" table="$it.JoinTableName$" access="property">
	<key column="$it.InverseJoinColumnName$" />
	<many-to-many column="$it.JoinColumnName$" class="com.intuit.sbd.payroll.psp.domain.$it.propertyType$" />
</set>
$else$
<set name="$it.propertyName$Set" table="$it.JoinTableName$" access="property">
	<key column="$it.JoinColumnName$" />
	<many-to-many column="$it.InverseJoinColumnName$" class="com.intuit.sbd.payroll.psp.domain.$it.propertyType$" />
</set>
$endif$
//
// $it.propertyName$
//
protected DomainEntitySet<$it.propertyType$> m$it.propertyName$Set = new DomainEntitySet<$it.propertyType$>();
$if(it.isAccessorOverride)$private DomainEntitySet<$it.propertyType$> mFiltered$it.propertyName$Set = null;$endif$

@SuppressWarnings("unchecked")
public DomainEntitySet<$it.propertyType$> get$it.propertyName$Collection()
{
	$if(it.isAccessorOverride)$
	if (mFiltered$it.propertyName$Set != null) {
	    return mFiltered$it.propertyName$Set;
	}
    if ($if(it.hasChangeTrackedProperty)$!m$it.memoryChangeTrackedProperty$Changed && $endif$!Hibernate.isInitialized(m$it.propertyName$Set.toNative())) {
        mFiltered$it.propertyName$Set = Application.find($it.propertyType$.class, $it.propertyType$.$it.owner.className$().equalTo(($it.owner.className$) this).And($it.accessorOverride$));
        return mFiltered$it.propertyName$Set;
    }
    $endif$
	return m$it.propertyName$Set;
}

@SuppressWarnings("unchecked")
public void add$it.propertyName$($it.propertyType$ p$it.propertyName$)
{
	get$it.propertyName$Collection().add(p$it.propertyName$);
}

@SuppressWarnings("unchecked")
public void remove$it.propertyName$($it.propertyType$ p$it.propertyName$)
{
	get$it.propertyName$Collection().remove(p$it.propertyName$);
}


/**
 * Package-protected setter for $it.propertyName$.
 * Only for O/R-mapper use.
 * @param p$it.propertyName$ $it.propertyName$.
*/
@SuppressWarnings("unchecked")
void set$it.propertyName$Set(Set<$it.propertyType$> p$it.propertyName$)
{
	m$it.propertyName$Set =  new DomainEntitySet<$it.propertyType$>(p$it.propertyName$);
}

/**
 * Package-Protected getter for $it.propertyName$.
 * Only for internal and O/R-mapper use.
 * @return $it.propertyName$.
*/
@SuppressWarnings("unchecked")
Set<$it.propertyType$> get$it.propertyName$Set()
{
	return m$it.propertyName$Set.toNative();
}

@SuppressWarnings("UnusedDeclaration")
public void reinitialize$it.propertyName$Set() {
    m$it.propertyName$Set = new DomainEntitySet<$it.propertyType$>();
}
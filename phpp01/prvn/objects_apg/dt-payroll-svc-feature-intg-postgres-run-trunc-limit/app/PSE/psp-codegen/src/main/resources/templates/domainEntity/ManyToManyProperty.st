//
// $it.propertyName$
//
protected DomainEntitySet<$it.propertyType$> m$it.propertyName$Set = new DomainEntitySet<$it.propertyType$>();

@SuppressWarnings("unchecked")
public DomainEntitySet<$it.propertyType$> get$it.propertyName$Collection()
{
	return m$it.propertyName$Set;
}

@SuppressWarnings("unchecked")
public void add$it.propertyName$($it.propertyType$ p$it.propertyName$)
{
	m$it.propertyName$Set.add(p$it.propertyName$);
}

@SuppressWarnings("unchecked")
public void remove$it.propertyName$($it.propertyType$ p$it.propertyName$)
{
	m$it.propertyName$Set.remove(p$it.propertyName$);
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


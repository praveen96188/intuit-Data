//
// $it.propertyName$
//
$if(it.isOwner)$
private $it.propertyType$ m$it.propertyName$ = null;

public void set$it.propertyName$($it.propertyType$ p$it.propertyName$)
{
	m$it.propertyName$ = p$it.propertyName$;
}

public $it.propertyType$ get$it.propertyName$()
{
	return m$it.propertyName$;
}
$else$
protected DomainEntitySet<$it.propertyType$> m$it.propertyName$Set = new DomainEntitySet<$it.propertyType$>();

@SuppressWarnings("unchecked")
public DomainEntitySet<$it.propertyType$> get$it.propertyName$Collection()
{
	return m$it.propertyName$Set;
}

@SuppressWarnings("UnusedDeclaration")
public void reinitialize$it.propertyName$Set() {
    m$it.propertyName$Set = new DomainEntitySet<$it.propertyType$>();
}

public void set$it.propertyName$($it.propertyType$ p$it.propertyName$)
{
    m$it.propertyName$Set.add(p$it.propertyName$);
}

public $it.propertyType$ get$it.propertyName$()
{
    if (m$it.propertyName$Set.size() == 0) {
       return null;
    }
    else {
	   return m$it.propertyName$Set.get(0);
	}
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
$endif$
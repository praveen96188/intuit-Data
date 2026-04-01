//
// $it.propertyName$
//
$if(it.isClob)$
@Basic(fetch = FetchType.LAZY)
private String m$it.propertyName$ = $it.propertyDefaultValue$;
$else$
private $it.propertyType$ m$it.propertyName$ = $it.propertyDefaultValue$;
$endif$

$if(it.inMemoryChangeTracked)$
private boolean m$it.propertyName$Changed;
$endif$


public void set$it.propertyName$($if(it.isClob)$ String $else$ $it.propertyType$ $endif$ p$it.propertyName$)
{
    $if(it.hasMaxLength)$
    if (p$it.propertyName$ != null && p$it.propertyName$.length() > $it.maxLength$)
    {
        String argStr = p$it.propertyName$.length() > 30 ? p$it.propertyName$.substring(0, 30) + "..." : p$it.propertyName$;
        throw new RuntimeException("String '" + argStr + "' is longer than allowed ($it.maxLength$) for p$it.propertyName$");
    }

    $endif$
	$if(it.isString)$
	if (p$it.propertyName$ != null && p$it.propertyName$.length() == 0)
	{
		p$it.propertyName$ = null;
	}

    $endif$
    $if(it.inMemoryChangeTracked)$
    if (m$it.propertyName$ != null)
    {
        m$it.propertyName$Changed = true;
    }
    $endif$

    m$it.propertyName$ = p$it.propertyName$;

}


public $if(it.isClob)$ String $else$ $it.propertyType$ $endif$ get$it.propertyName$()
{
	return m$it.propertyName$;
}
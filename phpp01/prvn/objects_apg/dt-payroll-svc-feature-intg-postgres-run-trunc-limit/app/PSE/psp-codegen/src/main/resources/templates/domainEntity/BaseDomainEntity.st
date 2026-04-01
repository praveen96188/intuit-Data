/**
 * --------------------------------------------------------------------------
 * Copyright (c) 2008 Intuit, Inc. All rights reserved.
 * Unauthorized reproduction is a violation of applicable law.
 * --------------------------------------------------------------------------
 *
 * --------------------------------------------------------------------------
 *
 * Author	PSP CodeGen
 * Model Version	$modelVersion$
 *
 * --------------------------------------------------------------------------
 */

package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.spc.foundations.portability.SpcfFactory;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.query.*;

import org.hibernate.internal.SessionFactoryImpl;
import java.util.Set;
import org.hibernate.Hibernate;
import org.hibernate.persister.entity.SingleTableEntityPersister;

$if(bedlDataEntity.hasClob)$
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;

@Entity // Annotate the class with @Entity for compile time BytecodeEnhancement for attribute lazy loading
$endif$

public class Base$bedlDataEntity.className$ extends com.intuit.sbd.payroll.psp.$bedlDataEntity.baseClassNameIncludingDomainEntity$ $if(bedlDataEntity.isTenantInfoRequired)$ implements com.intuit.sbd.payroll.psp.filter.TenantInfo $endif$ $if(bedlDataEntity.isPartitionedParentTenantInfoRequired)$ implements com.intuit.sbd.payroll.psp.filter.PartitionedParentTenantInfo $endif$
{
    $bedlDataEntity.ScalarProperties:ScalarProperty(); separator="\n\n"$
    $bedlDataEntity.ComplexProperties:ComplexProperty(); separator="\n\n"$
    $bedlDataEntity.OneToOneProperties:OneToOneProperty(); separator="\n\n"$
    $bedlDataEntity.ManyToOneProperties:ManyToOneProperty(); separator="\n\n"$
    $bedlDataEntity.OneToManyProperties:OneToManyProperty(); separator="\n\n"$
    $bedlDataEntity.ManyToManyProperties:ManyToManyProperty(); separator="\n\n"$

    $if(bedlDataEntity.hasFilteredCollections)$
    @Override
    public void onRefresh() {
        $bedlDataEntity.filteredCollections:{k|mFiltered$k$Set = null;}; separator="\n"$
    }
    $endif$

    $if(bedlDataEntity.isDataObject)$
	@Override
	public boolean equals(Object obj)
	{
		if (obj == null || !(obj instanceof $bedlDataEntity.className$))
		{
			return false;
		}
		else if (this == obj)
		{
			return true;
		}
		else
		{
			return get$bedlDataEntity.KeyProperty.PropertyName$() == null ? false	: get$bedlDataEntity.KeyProperty.PropertyName$().equals((($bedlDataEntity.className$)obj).get$bedlDataEntity.KeyProperty.PropertyName$());
		}
	}

	@Override
	public int hashCode()
	{
		return get$bedlDataEntity.KeyProperty.PropertyName$() == null ? super.hashCode() : get$bedlDataEntity.KeyProperty.PropertyName$().hashCode();

	}
    $endif$

    // PSP query support
    $bedlDataEntity.ScalarProperties:{k|public static final ScalarProperty<$bedlDataEntity.className$, $if(k.isClob)$ String $else$ $k.PropertyReferenceType$ $endif$> $k.PropertyName$() {return new ScalarProperty<$bedlDataEntity.className$,$if(k.isClob)$ String $else$ $k.PropertyReferenceType$ $endif$>(null, "$k.PropertyName$");};}; separator="\n"$
    $bedlDataEntity.OneToOneProperties:{k|public static final $k.PropertyReferenceType$Expression<$bedlDataEntity.className$> $k.PropertyName$() {return new $k.PropertyReferenceType$Expression<$bedlDataEntity.className$>(null, "$k.PropertyName$", $k.isNotOwner$);};}; separator="\n"$
    $bedlDataEntity.ComplexProperties:{k|public static final $k.PropertyReferenceType$Expression<$bedlDataEntity.className$> $k.PropertyName$() {return new $k.PropertyReferenceType$Expression<$bedlDataEntity.className$>(null, "$k.PropertyName$");};}; separator="\n"$
    $bedlDataEntity.ManyToOneProperties:{k|public static final $k.PropertyReferenceType$Expression<$bedlDataEntity.className$> $k.PropertyName$() {return new $k.PropertyReferenceType$Expression<$bedlDataEntity.className$>(null, "$k.PropertyName$");};}; separator="\n"$
    $bedlDataEntity.OneToManyProperties:{k|public static final $k.PropertyReferenceType$DomainEntitySetProperty<$bedlDataEntity.className$, $k.PropertyReferenceType$> $k.PropertyName$Set() {return new $k.PropertyReferenceType$DomainEntitySetProperty<$bedlDataEntity.className$, $k.PropertyReferenceType$>(null, "$k.PropertyName$Set", "$k.InversePropertyName$");};}; separator="\n"$
    $bedlDataEntity.ManyToManyProperties:{k|public static final DomainEntitySetProperty<$bedlDataEntity.className$, $k.PropertyReferenceType$> $k.PropertyName$Set() {return new DomainEntitySetProperty<$bedlDataEntity.className$, $k.PropertyReferenceType$>(null, "$k.PropertyName$Set");};}; separator="\n"$
}

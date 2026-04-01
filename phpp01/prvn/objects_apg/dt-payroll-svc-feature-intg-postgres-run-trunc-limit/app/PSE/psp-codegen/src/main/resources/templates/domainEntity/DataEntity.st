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

import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import java.util.Set;

public class $bedlDataEntity.className$ implements java.io.Serializable
{
	/**
     * Compiler generated UID.
     */
    private static final long serialVersionUID = -847113724770446714L;

    /*** Properties ***/
    $bedlDataEntity.ScalarProperties:ScalarProperty(); separator="\n\n"$

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
		    $bedlDataEntity.className$ dt = ($bedlDataEntity.className$)obj;
		    $bedlDataEntity.ScalarProperties:{k|
		      $k.PropertyType$ p$k.PropertyName$Prop = dt.get$k.PropertyName$();
		      $if(k.isPrimitive)$
		      if (p$k.PropertyName$Prop != m$k.PropertyName$)
		      {
		         return false;
		      }
		      $else$
		      if (p$k.PropertyName$Prop == null && m$k.PropertyName$ != null)
			  {
				return false;
			  }
			  else if (p$k.PropertyName$Prop != null)
			  {
				if (m$k.PropertyName$ == null)
				{
					return false;
				}
				else if (!p$k.PropertyName$Prop.equals(m$k.PropertyName$))
				{
					return false;
				}
			  }
		      $endif$
		    }; separator="\n"$

		     return true;
		}
	}

	@Override
	public int hashCode()
	{
		int m$bedlDataEntity.className$Hash = 7;
		int m$bedlDataEntity.className$HashValue = 0;
		
        $bedlDataEntity.ScalarProperties:{k|
          $if(k.isLong)$
          m$bedlDataEntity.className$HashValue += (int)(m$k.PropertyName$ ^ (m$k.PropertyName$ >>> 32));
          $endif$
          $if(k.isDouble)$
          long p$k.PropertyName$ = Double.doubleToLongBits(m$k.PropertyName$);
          m$bedlDataEntity.className$HashValue += (int)(p$k.PropertyName$ ^ (p$k.PropertyName$ >>> 32));
          $endif$
          $if(k.isBoolean)$
          m$bedlDataEntity.className$HashValue += (m$k.PropertyName$) ? 1 : 0;
          $endif$
          $if(k.isInteger)$
          m$bedlDataEntity.className$HashValue += mm$bedlDataEntity.className$;
          $endif$
          $if(!k.isPrimitive)$
          m$bedlDataEntity.className$HashValue += (m$k.PropertyName$ == null) ? 0 : m$k.PropertyName$.hashCode();
          $endif$
        }; separator="\n"$

		m$bedlDataEntity.className$Hash = m$bedlDataEntity.className$Hash * 31 + m$bedlDataEntity.className$HashValue;

		return m$bedlDataEntity.className$Hash;
    }
    
}
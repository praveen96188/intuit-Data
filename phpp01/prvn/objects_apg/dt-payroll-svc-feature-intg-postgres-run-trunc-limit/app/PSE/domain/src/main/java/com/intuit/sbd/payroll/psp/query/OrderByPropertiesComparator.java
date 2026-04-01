package com.intuit.sbd.payroll.psp.query;

import com.intuit.sbd.payroll.psp.Application;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.metadata.ClassMetadata;

import java.util.Comparator;
import java.util.Set;
import java.util.LinkedHashSet;

/**
 * Created by IntelliJ IDEA.
 * User: achaves
 * Date: Dec 10, 2008
 * Time: 8:48:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class OrderByPropertiesComparator<T> implements Comparator<T> {
    Set<SortableProperty<T, ?>> properties = new LinkedHashSet<SortableProperty<T, ?>>();

    public OrderByPropertiesComparator(Query pQuery) {
        properties.addAll(pQuery.getOrderByProperties());
    }

    public OrderByPropertiesComparator(SortableProperty<T, ?>... pProperties) {
        for (SortableProperty<T, ?> sortableProperty : pProperties) {
            properties.add(sortableProperty);
        }
    }

    public int compare(T o1, T o2) {
        ClassMetadata classMetadata = Application.getHibernateClassMetadata(Application.getActualObject(o1).getClass());
        for (SortableProperty sp : properties) {
            Property p = (Property) sp;
            Comparable left = p.getPropertyValue(classMetadata, o1);
            Comparable right = p.getPropertyValue(classMetadata, o2);

            // Enumerations should order by value to maintain backwards compatibility with unit tests ...
            if (left instanceof Enum) {
                left = left.toString();
            }
            if (right instanceof Enum) {
                right = right.toString();
            }

            int comparisonResult = compare(left, right, sp.isDescending());
            if (comparisonResult != 0) {
                return comparisonResult;
            }
        }

        return compare((Comparable) classMetadata.getIdentifier(o1, (SharedSessionContractImplementor) null), (Comparable) classMetadata.getIdentifier(o2, (SharedSessionContractImplementor) null), false);
    }

    private int compare(Comparable left, Comparable right, boolean isDescending) {
        int comparisonResult = 0;
        if (left == null) {
            if (right != null) {
                comparisonResult = -1;
            }
        }
        else {
            if (right == null) {
                comparisonResult = 1;
            }
            else {
                comparisonResult = left.compareTo(right);
            }
        }

        return isDescending ? comparisonResult * -1 : comparisonResult;
    }
}


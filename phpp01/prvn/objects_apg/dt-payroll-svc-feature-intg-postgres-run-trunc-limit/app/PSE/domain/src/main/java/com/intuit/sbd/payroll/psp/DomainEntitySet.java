package com.intuit.sbd.payroll.psp;

import com.intuit.sbd.payroll.psp.query.*;

import java.util.*;

public class DomainEntitySet<T extends DataObject> implements Set<T> {
    private Set<T> underlyingSet;
    private List<T> underlyingList = null;

    public DomainEntitySet() {
        underlyingSet = new LinkedHashSet();
    }

    public DomainEntitySet(Set<T> entities) {
        underlyingSet = entities;
    }

    public DomainEntitySet(Comparator<T> comparator, Set<T> entities) {
        underlyingSet = new TreeSet<T>(comparator);
        underlyingSet.addAll(entities);
    }

    public DomainEntitySet<T> find(Criterion pCriterion) {
        DomainEntitySet<T> result = new DomainEntitySet<T>();

        EvaluateVisitor evaluator = new EvaluateVisitor(pCriterion);

        for (T element : this) {
            if (evaluator.visit(element)) {
                result.add(element);
            }
        }

        return result;
    }

    /**
     * Find the first matching element
     * @param pCriterion
     * @return first element matching the supplied criterion or null if no element matches
     */
    public T findEntity(Criterion pCriterion) {
        DomainEntitySet<T> result = new DomainEntitySet<T>();

        EvaluateVisitor evaluator = new EvaluateVisitor(pCriterion);

        for (T element : this) {
            if (evaluator.visit(element)) {
                result.add(element);
            }
        }

        if (result.size() == 1)
            return result.get(0);

        if (result.size() == 0)
            return null;

        throw new RuntimeException("multiple matching elements (" + result.size() + ") found");
    }

    public DomainEntitySet<T> find(Query pQuery) {
        // TreeSet will order according to order by columns
        TreeSet<T> result = new TreeSet<T>(new OrderByPropertiesComparator(pQuery));

        EvaluateVisitor evaluator = new EvaluateVisitor(pQuery);

        for (T element : this) {
            if (evaluator.visit(element)) {
                result.add(element);
            }
        }

        return new DomainEntitySet<T>(result);
    }

    public DomainEntitySet<T> sort(SortableProperty<T, ?>... pSortableProperties) {
        return new DomainEntitySet<T>(new OrderByPropertiesComparator(pSortableProperties), this);
    }

    public int size() {
        return underlyingSet.size();
    }

    public boolean isEmpty() {
        return underlyingSet.isEmpty();
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }

    public boolean contains(Object o) {
        return underlyingSet.contains(o);
    }

    public Iterator<T> iterator() {
        return underlyingSet.iterator();
    }

    public Object[] toArray() {
        return underlyingSet.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return underlyingSet.toArray(a);
    }

    public boolean add(T o) {
        if (underlyingList != null) {
            underlyingList.add(o);
        }
        return underlyingSet.add(o);
    }

    public boolean remove(Object o) {
        if (underlyingList != null) {
            underlyingList.remove(o);
        }

        return underlyingSet.remove(o);
    }

    public T remove(int index) {
        T o = get(index);
        if (remove(o)) {
            return o;
        }
        else {
            return null;
        }
    }


    public boolean containsAll(Collection<?> c) {
        return underlyingSet.containsAll(c);
    }

    public boolean addAll(Collection<? extends T> c) {
        if (underlyingList != null) {
            underlyingList.addAll(c);
        }

        return underlyingSet.addAll(c);
    }

    public boolean removeAll(Collection<?> c) {
        if (underlyingList != null) {
            underlyingList.removeAll(c);
        }

        return underlyingSet.removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        if (underlyingList != null) {
            underlyingList.retainAll(c);
        }

        return underlyingSet.retainAll(c);
    }

    public void clear() {
        if (underlyingList != null) {
            underlyingList.clear();
        }

        underlyingSet.clear();
    }

    public T get(int index) {
        if (index == 0) {
            return iterator().next();
        }
        else {
            if (underlyingList == null) {
                underlyingList = new ArrayList<T>(underlyingSet);
            }

            return underlyingList.get(index);
        }
    }

    public T getFirst() {
        return size() > 0 ? get(0) : null;
    }

    public Set<T> toNative() {
        return underlyingSet;
    }

    public String toString() {
        String maxToStringSize = System.getProperty("DomainEntitySet.toString.maxSize");
        int maxVisibleSize = maxToStringSize != null ? Integer.parseInt(maxToStringSize) : 5;
        if (size() <= maxVisibleSize) {
            return Arrays.toString(toArray());
        }

        return super.toString();
    }
}
package com.intuit.sbd.payroll.psp.query;

import com.intuit.sbd.payroll.psp.Application;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.metadata.ClassMetadata;

import java.util.*;

/**
 * GetProperty - Scalar, DataEntity (1:1), DataEntityCollection (1:m, m:m)
 */
public class Property<Q, V> extends Expression<Q> {
    public Property(String propertyName) {
        this(null, propertyName);
    }

    public Property(DomainEntityProperty pParentDomainEntityExpression, String pPropertyName) {
        propertyName = pPropertyName;
        parentDomainEntityExpression = pParentDomainEntityExpression;
    }

    @Override
    protected <T> T accept(ExpressionVisitor<?, T> visitor) {
        return visitor.visitPropertyExpression(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (getParentDomainObjectExpression() != null) {
            sb.append(getParentDomainObjectExpression().toString());
            sb.append(".");
        }
        sb.append(getPropertyName());

        return sb.toString();
    }

    public String getPropertyName() {
        return propertyName;
    }

    public String getPropertyQueryPath() {
        return getPropertyName();
    }

    @Override
    public DomainEntityProperty getParentDomainObjectExpression() {
        return parentDomainEntityExpression;
    }

    Comparable getPropertyValue(ClassMetadata classMetadata, Object pPersistentObject) {
        Object value = pPersistentObject;

        for (int i = getPropertyPathList().size() - 1; i >= 0; i--) {
            value = Application.getActualObject(value);
            if (value == null) {
                return null;
            }
            if (!classMetadata.getEntityName().equals(value.getClass().getName())) {
                if (value instanceof Collection) {
                    throw new RuntimeException("Implicit joins do not support evaluate visitor.  Use exists instead.");
                }
                classMetadata = Application.getHibernateClassMetadata(value.getClass());
            }
            Property p = (Property) getPropertyPathList().get(i);
            String propertyName = p.getPropertyName();

            if (classMetadata.getIdentifierPropertyName().equals(propertyName)) {
                value = classMetadata.getIdentifier(value, (SharedSessionContractImplementor) null);
            }
            else {
                if (p instanceof DomainEntityProperty &&
                        ((DomainEntityProperty) p).isImplementedAsCollection()) {
                   Set collectionValue = (Set) classMetadata.getPropertyValue(value, propertyName);
                    if (collectionValue.size() == 0) {
                        value = null;
                    }
                    else {
                        value = collectionValue.iterator().next();
                    }
                }
                else {
                   value = classMetadata.getPropertyValue(value, propertyName);
                }
            }
        }

        return (Comparable) value;
    }

    String getAlias(HashMap<String, String> pPropertyPathToAliasMap, String rootAlias) {
        if (getPropertyPathList().size() == 1) {
            return rootAlias;
        }

       String alias = "";
       for (int i = getPropertyPathList().size() - 1; i > 0; i--) {
           String propertyName = ((Property) getPropertyPathList().get(i)).getPropertyName();
           String propertyPath = propertyName;
           if (alias.length() > 0) {
               propertyPath = alias + "." + propertyPath;
           }

           if (!pPropertyPathToAliasMap.containsKey(propertyPath)) {
               pPropertyPathToAliasMap.put(propertyPath, "t" + pPropertyPathToAliasMap.size());
           }
           alias = pPropertyPathToAliasMap.get(propertyPath);
       }

       return alias;
    }

    List<Property<Q, ?>> getPropertyPathList() {
        if (propertyPathList == null) {
            propertyPathList = new ArrayList<Property<Q, ?>>();

            Property propertyExpr = this;
            propertyPathList.add(propertyExpr);

            while (propertyExpr.getParentDomainObjectExpression() != null) {
                propertyExpr = propertyExpr.getParentDomainObjectExpression();
                propertyPathList.add(propertyExpr);
            }
        }
        return propertyPathList;
    }

    String getPropertyPath() {
        if (propertyPath == null) {
            propertyPath = getPropertyName();
            for (int i = 1; i < getPropertyPathList().size(); i++) {
                propertyPath = ((Property) getPropertyPathList().get(i)).getPropertyName() + "." + propertyPath;
            }
        }
        return propertyPath;
    }

    @Override
    public String getParentProperty() {
        DomainEntityProperty property = getParentDomainObjectExpression();
        if (property == null) {
            return null;
        } else {
            if (property.getParentDomainObjectExpression() != null) {
                throw new RuntimeException("Collections cannot be eagerly filtered by joining to a third table");
            }
            return property.getPropertyName();
        }
    }

    public String getAssociationPath(HashMap<String, String> pPropertyPathToAliasMap, String rootAlias) {
        if(getPropertyPathList().size() == 1){
            return getPropertyName();
        }
        return getAlias(pPropertyPathToAliasMap, rootAlias) + "." + getPropertyName();
    }


    private DomainEntityProperty parentDomainEntityExpression;
    private String propertyName;
    private List<Property<Q, ?>> propertyPathList;
    private String propertyPath;
}

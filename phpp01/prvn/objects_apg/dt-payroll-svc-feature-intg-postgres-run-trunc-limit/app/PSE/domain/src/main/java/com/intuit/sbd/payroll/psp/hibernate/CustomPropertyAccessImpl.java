package com.intuit.sbd.payroll.psp.hibernate;

import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.property.access.spi.Getter;
import org.hibernate.property.access.spi.GetterMethodImpl;
import org.hibernate.property.access.spi.PropertyAccess;
import org.hibernate.property.access.spi.PropertyAccessStrategy;
import org.hibernate.property.access.spi.Setter;
import org.hibernate.property.access.spi.SetterFieldImpl;

/**
 * This class defines the Getter and Setter strategy for Entities
 *
 * @author cbhat
 *
 */
public class CustomPropertyAccessImpl implements PropertyAccess {
	private final PropertyAccessStrategy strategy;

	private final Getter getter;
	private final Setter setter;

	private final String PROPERTY_PREFIX = "m";

	public CustomPropertyAccessImpl(
			PropertyAccessStrategy strategy,
			Class clazz,
			String propertyName) {
		this.strategy = strategy;

		// For getting fields use getter method.
		String fieldNameForGet = propertyName;
		// We are setting fields directly using reflection and not using setter method.
		// Hence need to append 'm' to the propertyName.
		String fieldNameForSet = PROPERTY_PREFIX + propertyName;

		// getter using method
		this.getter = new GetterMethodImpl( clazz, fieldNameForGet,
				ReflectHelper.findGetterMethod( clazz, fieldNameForGet) );
		// setter using field
		this.setter = new SetterFieldImpl(clazz, fieldNameForSet,
				ReflectHelper.findField(clazz, fieldNameForSet));
	}

	@Override
	public PropertyAccessStrategy getPropertyAccessStrategy() {
		return strategy;
	}

	@Override
	public Getter getGetter() {
		return getter;
	}

	@Override
	public Setter getSetter() {
		return setter;
	}
}

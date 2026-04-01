	/**
	 * $it.propertyName$ getter.
	 */
	private static final $it.propertyName$Getter s$it.propertyName$Getter = new $it.propertyName$Getter();

	/**
	 * $it.propertyName$ setter.
	 */
	private static final $it.propertyName$Setter s$it.propertyName$Setter = new $it.propertyName$Setter();

	/**
	 * Getter class to get non-portable type from portable type.
	 */
	public static final class $it.propertyName$Getter implements Getter
	{
		/**
		 * Generated ID used for serialization.
		 */
		private static final long serialVersionUID = -7419532343666521290L;

		/**
		 * Package-protected default constructor.
		 */
		$it.propertyName$Getter()
		{
			super();
		}

		/**
		 * @see org.hibernate.property.Getter#getMethod()
		 */
		public final Method getMethod()
		{
			return null;
		}

		/**
		 * @see org.hibernate.property.Getter#getMethodName()
		 */
		public final String getMethodName()
		{
			return null;
		}

		/**
		 * @see org.hibernate.property.Getter#get(java.lang.Object)
		 */
		public final Object get(Object target) throws HibernateException
		{
			$bedlDataEntity.className$ parent = ($bedlDataEntity.className$)target;

			com.intuit.sbd.payroll.psp.DomainEntitySet<com.intuit.sbd.payroll.psp.domain.$it.propertyType$> someSet = (com.intuit.sbd.payroll.psp.DomainEntitySet<com.intuit.sbd.payroll.psp.domain.$it.propertyType$>)(parent.get$it.propertyName$Collection());

			return someSet == null ? null : someSet.toNative();
		}

		/**
		 * @see org.hibernate.property.Getter#getForInsert(java.lang.Object, java.util.Map, org.hibernate.engine.SessionImplementor)
		 */
		@SuppressWarnings("unchecked")
		public final Object getForInsert(Object target, Map mergeMap, SessionImplementor session)
		{
			return get(target);
		}

		/**
		 * @see org.hibernate.property.Getter#getReturnType()
		 */
		@SuppressWarnings("unchecked")
		public final Class getReturnType()
		{
			return Set.class;
		}
	}

	/**
	 * Setter class to set portable type from non-portable type.
	 */
	public static final class $it.propertyName$Setter implements Setter
	{
		/**
		 * Generated ID used for serialization.
		 */
		private static final long serialVersionUID = 25965206073940214L;

		/**
		 * Package-protected default constructor.
		 */
		$it.propertyName$Setter()
		{
			super();
		}

		/**
		 * @see org.hibernate.property.Setter#getMethod()
		 */
		public final Method getMethod()
		{
			return null;
		}

		/**
		 * @see org.hibernate.property.Setter#getMethodName()
		 */
		public final String getMethodName()
		{
			return null;
		}

		/**
		 * @see org.hibernate.property.Setter#set(java.lang.Object, java.lang.Object, org.hibernate.engine.SessionFactoryImplementor)
		 */
		@SuppressWarnings("unchecked")
		public final void set(Object target, Object value, SessionFactoryImplementor session)
		{
			$bedlDataEntity.className$ parent = ($bedlDataEntity.className$)target;

			parent.set$it.propertyName$Set(new com.intuit.sbd.payroll.psp.DomainEntitySet<com.intuit.sbd.payroll.psp.domain.$it.propertyType$>((Set<com.intuit.sbd.payroll.psp.domain.$it.propertyType$>)value));
		}
	}

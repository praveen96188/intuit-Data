<?xml version='1.0' encoding='utf-8'?>
<!DOCTYPE hibernate-configuration PUBLIC
  "-//Hibernate/Hibernate Configuration DTD//EN"
  "http://www.hibernate.org/dtd/hibernate-configuration-3.0.dtd">
<hibernate-configuration>
  <session-factory>
    <property name="connection.url">jdbc:oracle:thin:@localhost:1521:XE</property>

    <property name="connection.driver_class">oracle.jdbc.OracleDriver</property>

    <property name="connection.username">psp_local</property>
    <property name="connection.password">psp_local</property>

    <property name="hibernate.cache.provider_class">org.hibernate.cache.EhCacheProvider</property>

    $bedlDataEntitys: {bedlDataEntity| <mapping resource="resources/com/intuit/sbd/payroll/psp/domain/$bedlDataEntity.ClassName$.hbm.xml"/>};separator="\n"$

    $bedlDataObjects: {bedlDataObject| <mapping resource="resources/com/intuit/sbd/payroll/psp/domain/$bedlDataObject.ClassName$.hbm.xml"/>};separator="\n"$

      <!-- DB schema will be updated if needed -->
    <!-- <property name="hbm2ddl.auto">update</property> -->
  </session-factory>
</hibernate-configuration>

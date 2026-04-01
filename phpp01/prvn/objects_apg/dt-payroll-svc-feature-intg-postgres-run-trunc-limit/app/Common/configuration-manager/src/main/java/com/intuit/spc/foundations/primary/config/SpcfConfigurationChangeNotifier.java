package com.intuit.spc.foundations.primary.config;

import com.intuit.spc.foundations.portability.SpcfArgumentNullException;
import com.intuit.spc.foundations.portability.SpcfFactory;
import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.collections.ISpcfIterator;
import com.intuit.spc.foundations.portability.collections.SpcfHashMap;
import com.intuit.spc.foundations.portability.collections.SpcfList;
import com.intuit.spc.foundations.portability.reflect.SpcfClass;
import com.intuit.spc.foundations.primary.config.util.SpcfConfigurationComparator;


/**
 * This Notifier is for notifying Changes in the Configuration to 
 * listeners.
 * Note that it is not the responsibility of the Configuration Change Notifier to 
 * actively monitor persistent configuration storages and determine when configurations 
 * have changed. 
 * <p>
 * The Configuration Change Notifier detects a configuration change only if one of the {@link SpcfConfigurationManager#reload(String)},
 * {@link SpcfConfigurationManager#reload()} and 
 * {@link SpcfConfigurationManager#storeConfiguration(String, ISpcfConfiguration)} methods of CMS are called, either by the client application or 
 * some other external component. Configuration Change Notifier may also be explicitly notified whenever a change in the individual configuration entries.
 * </p>
 * 
 * <p>
 * Notifier may also get notified explicitly outside of Configuration Manager. This explicit notification is useful
 * when a custom configuration wants to notify the listeners of any changes in the individual configuration entries.  
 * </p>
 * <p>
 * For more info go to:
 * <a
 * href="http://sdswiki.intuit.com/index.php/SPC_Foundations_Configuration_Management_System_2.0#Implementing_Lazy_Load.2FSave">SPCF
 * Configuration Management Lazy Load/Save Users Guide </a>
 * </p>
 * @author barunachalam
 */
public class SpcfConfigurationChangeNotifier {

	/**
	 * Factory Instance
	 */
    private static final SpcfFactory Factory = SpcfFactory.getInstance();
    
    /**
     * Listeners
     */
    private static final SpcfHashMap<String, SpcfList<ISpcfConfigurationChangeListener>> Listeners = Factory.<String, SpcfList<ISpcfConfigurationChangeListener>>createHashMap();
    
    /**
     * Adds a listener that listens to configuration changes. Listeners will only be notified if configurations for
     * the specified moduleId change. Adding the same listener instance twice will cause the instance to be notified
     * twice during a configuration change.
     * <p>
     * Note that it is not the responsibility of the Notifier to actively monitor persistent configuration storages and
     * determine when configurations have changed. The Notifier will detect a configuration change only if one of the {@link SpcfConfigurationManager#reload(String)},
     * {@link SpcfConfigurationManager#reload()} and 
     * {@link SpcfConfigurationManager#storeConfiguration(String, ISpcfConfiguration)} methods of CMS are called,
     * either by the client application or some other external component.
     * Notifier may also get notified explicitly outside of Configuration Manager. This explicit notification is useful
     * when a custom configuration wants to notify the listeners of any changes in the individual configuration entries.  
     * </p>
     * @param moduleId A string representing the id of the module, cannot be null.
     * @param listener The listener, cannot be null.
     * @throws SpcfArgumentNullException if moduleId or listener is null.
     * @throws SpcfMetaConfigEntryMissingException If the moduleId does not exist in the meta configuration file.
     */
    public static void addListener(String moduleId, ISpcfConfigurationChangeListener listener)
    {      
        //SpcfParamValidator.checkIsNotNull(moduleId, "moduleId");
        SpcfParamValidator.checkIsNotNull(listener, "listener");
        checkModuleIdExists(moduleId);
        
        synchronized (Listeners)
        {
            SpcfList<ISpcfConfigurationChangeListener> list = Listeners.getItem(moduleId);
            if (list == null)
            {
                list = Factory.<ISpcfConfigurationChangeListener>createArrayList();
                Listeners.add(moduleId, list);
            }
            list.add(listener);
        }
    }


    /**
     * Removes listener for moduleId if it is found. The Notifier relies on the equals method (Object.equals in java and
     * System.Object.Equals in C#) to determine whether or not two listeners are equal.
     * 
     * @param moduleId A string representing the id of the module, cannot be null.
     * @param listener The listener, cannot be null.
     * @throws SpcfArgumentNullException if moduleId or listener is null.
     * @throws SpcfMetaConfigEntryMissingException If the moduleId does not exist in the meta configuration file.
     */
    public static void removeListener(String moduleId, ISpcfConfigurationChangeListener listener)
    {
        //SpcfParamValidator.checkIsNotNull(moduleId, "moduleId");
        SpcfParamValidator.checkIsNotNull(listener, "listener");
        checkModuleIdExists(moduleId);
        synchronized (Listeners)
        {
            SpcfList<ISpcfConfigurationChangeListener> obj = Listeners.getItem(moduleId);
            if (obj != null)
            {
                SpcfList<ISpcfConfigurationChangeListener> list = obj;
                list.remove(listener);
            }
        }
    }
    
    /**
     * Removes all listeners for moduleId.
     * 
     * @param moduleId A string representing the id of the module, cannot be null.
     * @throws SpcfMetaConfigEntryMissingException If the moduleId does not exist in the meta configuration file.
     * @throws SpcfArgumentNullException if moduleId is null.
     */
    public static void removeListeners(String moduleId)
    {
        checkModuleIdExists(moduleId);
        Listeners.remove(moduleId);        
    }
    

    /**
     * To notify Configuration bulk change for a module to all the listeners.
     * @param moduleId ID of the module for which the configuration is changed
     * @param oldConfig Configuration before the change
     * @param newConfig Configuration after the change
     * @throws SpcfConfigChangeNotificationException if at least one configuration change listener throws an unexpected
     *             exception. In this case, the listener will be removed from the Notifier.
     * @throws SpcfArgumentNullException if moduleId is null.
     */
    public static void notifyConfigurationListeners(String moduleId,  ISpcfImmutableConfiguration oldConfig, ISpcfImmutableConfiguration newConfig)
    {
        SpcfParamValidator.checkIsNotNull(moduleId, "moduleId");
        if (SpcfConfigurationComparator.areEqual(oldConfig, newConfig))
        {
            return;
        }
        SpcfList<ISpcfConfigurationChangeListener> obj = Listeners.getItem(moduleId);
        if (obj != null)
        {
            synchronized (Listeners)
            {
                SpcfList<ISpcfConfigurationChangeListener> list = obj;
                for (ISpcfIterator<ISpcfConfigurationChangeListener> it = list.getIterator(); it.hasNext();)
                {
                    ISpcfConfigurationChangeListener listener = it.next();
                    try
                    {
                        // Listeners should not throw exceptions, so if a runtime exception is thrown,
                        // we'll remove it.
                        listener.onChange(moduleId, newConfig);
                    }
                    catch (Exception e)
                    {
                        list.remove(listener);
                        throw new SpcfConfigChangeNotificationException(SpcfClass.classNameForObject(listener),
                                moduleId, "A configuration change listener failed and has been removed from Notifier", e);
                    }
                } // end of for
            }
        }
    }
    

    /**
     * To notify Configuration individual value change in a module to all the listeners. This will not be called
     * by Configuration Manager. Those who wants to notify any changes in the configuration values shall
     * call this API explicitly.
     * @param moduleId ID of the module in which a configuration value is changed
     * @param configurationKey Configuration Key
     * @param newValue Configuration value
     * @throws SpcfConfigChangeNotificationException if at least one configuration change listener throws an unexpected
     *             exception. In this case, the listener will be removed from the Notifier.
     * @throws SpcfArgumentNullException if moduleId is null.             
     */
    public static void notifyConfigurationListeners(String moduleId, String configurationKey, Object newValue )
    {    
        SpcfParamValidator.checkIsNotNull(moduleId, "moduleId");
        SpcfList<ISpcfConfigurationChangeListener> obj = Listeners.getItem(moduleId);
        if (obj != null)
        {
            synchronized (Listeners)
            {
                SpcfList<ISpcfConfigurationChangeListener> list = obj;
                for (ISpcfIterator<ISpcfConfigurationChangeListener> it = list.getIterator(); it.hasNext();)
                {
                    ISpcfConfigurationChangeListener listener = it.next();
                    try
                    {
                        // Listeners should not throw exceptions, so if a runtime exception is thrown,
                        // we'll remove it.
                        listener.onChange(moduleId, configurationKey, newValue);
                    }
                    catch (Exception e)
                    {
                        list.remove(listener);
                        throw new SpcfConfigChangeNotificationException(SpcfClass.classNameForObject(listener),
                                moduleId, "A configuration change listener failed and has been removed from Notifier", e);
                    }
                } // end of for
            }
        }
    }
    
    /**
     * To check whether a module ID exists or not
     * @param moduleId Module ID
     */
    private static void checkModuleIdExists(String moduleId)
    {        
        if (!SpcfConfigurationManager.getInstance().hasModuleId(moduleId))
        {
            throw new SpcfMetaConfigEntryMissingException(moduleId, "Missing provider for module '" + moduleId + "'.");
        }        
    }
}

package com.intuit.spc.foundations.portability;

/**
 * An enumeration of Architectures to catagorize exceptions by architectures
 */
public class SpcfArchitectureModuleEnum extends SpcfIdentifierEnum {

	/**
	 * The class constructor.
	 * @param id The ID of the static instance.
	 */
	private SpcfArchitectureModuleEnum (int id)
	{
		super(id);
	}

	//WHEN YOU ADD A NEW MODULE TO YOUR ARCHITECTURE
	// - Add a unique entry at the bottom of the list (bump the number)
	// - Create a new XXXModuleException that returns the new entry
	/**
	 * Portability
	 */
	public static final SpcfArchitectureModuleEnum Portability       = new SpcfArchitectureModuleEnum (1);
	
	/**
	 * Primary
	 */
	public static final SpcfArchitectureModuleEnum Primary           = new SpcfArchitectureModuleEnum (2);
	
	/**
	 * ExceptionHandling
	 */
	public static final SpcfArchitectureModuleEnum ExceptionHandling = new SpcfArchitectureModuleEnum (3);
	
	/**
	 * Config
	 */
	public static final SpcfArchitectureModuleEnum Config            = new SpcfArchitectureModuleEnum (4);
	
	/**
	 * Logging
	 */
	public static final SpcfArchitectureModuleEnum Logging           = new SpcfArchitectureModuleEnum (5);
	
	/**
	 * XML
	 */
	public static final SpcfArchitectureModuleEnum Xml               = new SpcfArchitectureModuleEnum (6);
	
	/**
	 * Transactions
	 */
	public static final SpcfArchitectureModuleEnum Transactions      = new SpcfArchitectureModuleEnum (7);
	
	/**
	 * Net
	 */
	public static final SpcfArchitectureModuleEnum Net               = new SpcfArchitectureModuleEnum (8);
	
	/**
	 * Component
	 */
	public static final SpcfArchitectureModuleEnum Component         = new SpcfArchitectureModuleEnum (9);
    
    /**
     * DataAccess
     */
    public static final SpcfArchitectureModuleEnum DataAccess        = new SpcfArchitectureModuleEnum (10);

    /**
     * Micro-Orchestrations
     */
    public static final SpcfArchitectureModuleEnum MicroOrchestration = new SpcfArchitectureModuleEnum (11);

    
    /**
     * VersionManager
     */
    public static final SpcfArchitectureModuleEnum VersionManager = new SpcfArchitectureModuleEnum (12);

	//If you add a new module above, go add it the Java side now before you forget!
}

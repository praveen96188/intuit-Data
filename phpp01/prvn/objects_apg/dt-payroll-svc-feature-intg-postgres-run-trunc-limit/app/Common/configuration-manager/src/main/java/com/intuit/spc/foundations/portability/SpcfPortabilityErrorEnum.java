package com.intuit.spc.foundations.portability;

/**
 * The enumeration of errors for the portability module
 */
public class SpcfPortabilityErrorEnum extends SpcfIdentifierEnum 
{
	/**
	 * the one and only public constructor
	 * @param id the id of the static instance
	 */
	private SpcfPortabilityErrorEnum (int id)
	{
		super(id);
	}

	//WHEN YOU ADD A NEW EXCEPTION TO YOUR MODULE
	// - Add a unique entry at the bottom of the list (bump the number)
	// - Create a new XXXException that returns the new entry
	/**
	 * ArgumentNull
	 */
	public static final SpcfPortabilityErrorEnum ArgumentNull           = new SpcfPortabilityErrorEnum (1001);
	
	/**
	 * ArgumentOutOfRange
	 */
	public static final SpcfPortabilityErrorEnum ArgumentOutOfRange     = new SpcfPortabilityErrorEnum (1002);
	
	/**
	 * ClassCast
	 */
	public static final SpcfPortabilityErrorEnum ClassCast              = new SpcfPortabilityErrorEnum (1003);
	
	/**
	 * IllegalArgument
	 */
	public static final SpcfPortabilityErrorEnum IllegalArgument        = new SpcfPortabilityErrorEnum (1004);
	
	/**
	 * IndexOutOfBounds
	 */
	public static final SpcfPortabilityErrorEnum IndexOutOfBounds       = new SpcfPortabilityErrorEnum (1005);
	
	/**
	 * InvalidFactoryState
	 */
	public static final SpcfPortabilityErrorEnum InvalidFactoryState    = new SpcfPortabilityErrorEnum (1006);
	
	/**
	 * NullPointer
	 */
	public static final SpcfPortabilityErrorEnum NullPointer            = new SpcfPortabilityErrorEnum (1007);
	
	/**
	 * NumberFormat
	 */
	public static final SpcfPortabilityErrorEnum NumberFormat           = new SpcfPortabilityErrorEnum (1008);
	
	/**
	 * Security
	 */
	public static final SpcfPortabilityErrorEnum Security               = new SpcfPortabilityErrorEnum (1009);
	
	/**
	 * UnsupportedOperation
	 */
	public static final SpcfPortabilityErrorEnum UnsupportedOperation   = new SpcfPortabilityErrorEnum (1010);
	
	/**
	 * Format
	 */
	public static final SpcfPortabilityErrorEnum Format                 = new SpcfPortabilityErrorEnum (1011);
	
	/**
	 * NoSuchElement
	 */
	public static final SpcfPortabilityErrorEnum NoSuchElement          = new SpcfPortabilityErrorEnum (1012);
	
	/**
	 * ClassMethodInvoke
	 */
	public static final SpcfPortabilityErrorEnum ClassMethodInvoke      = new SpcfPortabilityErrorEnum (1013);
	
	/**
	 * ConcurrentModification
	 */
	public static final SpcfPortabilityErrorEnum ConcurrentModification = new SpcfPortabilityErrorEnum (1014);
	
	/**
	 * UnsupportedEncoding
	 */
	public static final SpcfPortabilityErrorEnum UnsupportedEncoding    = new SpcfPortabilityErrorEnum (1015);
	
	/**
	 * FileNotFound
	 */
	public static final SpcfPortabilityErrorEnum FileNotFound           = new SpcfPortabilityErrorEnum (1016);
	
	/**
	 * FileAlreadyExists
	 */
	public static final SpcfPortabilityErrorEnum FileAlreadyExists      = new SpcfPortabilityErrorEnum (1017);
	
	/**
	 * DirNotFound
	 */
	public static final SpcfPortabilityErrorEnum DirNotFound            = new SpcfPortabilityErrorEnum (1018);
	
	/**
	 * DirAlreadyExists
	 */
	public static final SpcfPortabilityErrorEnum DirAlreadyExists       = new SpcfPortabilityErrorEnum (1019);
	
	/**
	 * IO
	 */
	public static final SpcfPortabilityErrorEnum Io                     = new SpcfPortabilityErrorEnum (1020);
    
	/**
	 * IllegalState
	 */
	public static final SpcfPortabilityErrorEnum IllegalState           = new SpcfPortabilityErrorEnum (1021);
    
	/**
	 * ClassCreateInstance
	 */
	public static final SpcfPortabilityErrorEnum ClassCreateInstance    = new SpcfPortabilityErrorEnum (1022);
    
	/**
	 * ClassNotFound
	 */
	public static final SpcfPortabilityErrorEnum ClassNotFound          = new SpcfPortabilityErrorEnum (1023);
    
	/**
	 * InvalidOperation
	 */
	public static final SpcfPortabilityErrorEnum InvalidOperation       = new SpcfPortabilityErrorEnum (1024);
    
	/**
	 * InvalidRegex
	 */
	public static final SpcfPortabilityErrorEnum InvalidRegex           = new SpcfPortabilityErrorEnum (1025);
    
	/**
	 * Zip
	 */
	public static final SpcfPortabilityErrorEnum Zip                    = new SpcfPortabilityErrorEnum (1026);
    
	/**
	 * MalformedUrl
	 */
	public static final SpcfPortabilityErrorEnum MalformedUrl           = new SpcfPortabilityErrorEnum (1027);
    
	/**
	 * NotImplemented
	 */
	public static final SpcfPortabilityErrorEnum NotImplemented         = new SpcfPortabilityErrorEnum (1028);    
    
	/**
	 * ThreadStateError
	 */
	public static final SpcfPortabilityErrorEnum ThreadStateError       = new SpcfPortabilityErrorEnum (1029);
    
	/**
	 * ThreadInterruptedError
	 */
	public static final SpcfPortabilityErrorEnum ThreadInterruptedError = new SpcfPortabilityErrorEnum (1030);
    
	/**
	 * EOF
	 */
	public static final SpcfPortabilityErrorEnum Eof                    = new SpcfPortabilityErrorEnum (1031);
    
	/**
	 * FileLock
	 */
	public static final SpcfPortabilityErrorEnum FileLock               = new SpcfPortabilityErrorEnum (1032);
    
	/**
	 * SpcfSecurityLoggingConfigurationException
	 */
	public static final SpcfPortabilityErrorEnum SpcfSecurityLoggingConfigurationException = new SpcfPortabilityErrorEnum (1033);

	/**
	 * NoSuchMethod
	 */
	public static final SpcfPortabilityErrorEnum NoSuchMethod = new SpcfPortabilityErrorEnum (1034);

	/**
	 * IllegalAccess
	 */
	public static final SpcfPortabilityErrorEnum IllegalAccess = new SpcfPortabilityErrorEnum (1035);       
	
	/**
	 * InvocationTarget
	 */
	public static final SpcfPortabilityErrorEnum InvocationTarget = new SpcfPortabilityErrorEnum (1036);       

	/** 
	 * Instantiation
	 */
	public static final SpcfPortabilityErrorEnum Instantiation = new SpcfPortabilityErrorEnum (1037);
	
	/** 
	 * NoSuchField
	 */
	public static final SpcfPortabilityErrorEnum NoSuchField = new SpcfPortabilityErrorEnum (1038);
	
	/** 
	 * MissingResource
	 */
	public static final SpcfPortabilityErrorEnum MissingResource = new SpcfPortabilityErrorEnum (1039);
    
     /** 
     * CryptoError
     */
    public static SpcfPortabilityErrorEnum CryptoError = new SpcfPortabilityErrorEnum (1040);
    
    /** 
     * AlreadyInitialized
     */
    public static SpcfPortabilityErrorEnum AlreadyInitialized = new SpcfPortabilityErrorEnum (1041);
    
    /** 
     * NotInitialized
     */
    public static SpcfPortabilityErrorEnum NotInitialized = new SpcfPortabilityErrorEnum (1042);
	
    /** 
     * NoSuchAnnotation
     */
    public static SpcfPortabilityErrorEnum NoSuchAnnotation = new SpcfPortabilityErrorEnum (1043);
    
    /** 
     * XmlBinder
     */
    public static SpcfPortabilityErrorEnum XmlBinder = new SpcfPortabilityErrorEnum (1044);

    /** 
     * InvalidTypeString
     */
    public static SpcfPortabilityErrorEnum InvalidTypeString = new SpcfPortabilityErrorEnum (1045);
    
    /**
     * AssemblyNotFound
     */
    public static SpcfPortabilityErrorEnum AssemblyNotFound = new SpcfPortabilityErrorEnum(1046);
 
	//If you add a new one here, go add it the .NET side now before you forget!
    
    
    
    
    // Security/Crypto Exceptions from 5000+ range

    // Password Derived Key Errors.
    
    /**
     * Password Invalid error.
     */
    public static SpcfPortabilityErrorEnum PASSWORD_INVALID = new SpcfPortabilityErrorEnum (5000);
    
    /**
     * Salt Invalid error.
     */
    public static SpcfPortabilityErrorEnum SALT_INVALID = new SpcfPortabilityErrorEnum (5001);
    
    /**
     * Iteration Count Invalid error.
     */
    public static SpcfPortabilityErrorEnum ITERATION_COUNT_INVALID = new SpcfPortabilityErrorEnum (5002);
    
    /**
     * Key Size Invalid error.
     */
    public static SpcfPortabilityErrorEnum KEY_SIZE_INVALID = new SpcfPortabilityErrorEnum (5003);
    
    /**
     * Derived Key error.
     */
    public static SpcfPortabilityErrorEnum DERIVED_KEY_ERROR = new SpcfPortabilityErrorEnum (5004);
    
    /**
     * Password Null error.
     */
    public static SpcfPortabilityErrorEnum PASSWORD_NULL = new SpcfPortabilityErrorEnum (5005);
    
    
    // Password Quality Validation Errors.
    
    /**
     * Password does not contain the minimum lower case chars necessary for validation.
     */
    public static SpcfPortabilityErrorEnum MIN_LOWER_CASE_CHARS_ERROR = new SpcfPortabilityErrorEnum (5100);
    
    /**
     * Password does not contain the minimum upper case chars necessary for validation.
     */
    public static SpcfPortabilityErrorEnum MIN_UPPER_CASE_CHARS_ERROR = new SpcfPortabilityErrorEnum (5101);
    
    /**
     * Password does not contain the minimum letter chars necessary for validation.
     */
    public static SpcfPortabilityErrorEnum MIN_LETTER_CHARS_ERROR = new SpcfPortabilityErrorEnum (5102);
    
    /**
     * Password does not contain the minimum numeric chars necessary for validation.
     */
    public static SpcfPortabilityErrorEnum MIN_NUMERIC_CHARS_ERROR = new SpcfPortabilityErrorEnum (5103);
    
    /**
     * Password does not contain the minimum symbol chars necessary for validation.
     */
    public static SpcfPortabilityErrorEnum MIN_SYMBOL_CHARS_ERROR = new SpcfPortabilityErrorEnum (5104);
    
    /**
     * Password does not contain the minimum length necessary for validation.
     */
    public static SpcfPortabilityErrorEnum MIN_LENGTH_ERROR = new SpcfPortabilityErrorEnum (5105);
    
    /**
     * Password contains more than the maximum sequence of chars allowed.
     */
    public static SpcfPortabilityErrorEnum MAX_SEQUENCE_CHARS_ERROR = new SpcfPortabilityErrorEnum (5106);
    
    /**
     * Password contains more than the maximum repeated consecutive chars allowed.
     */
    public static SpcfPortabilityErrorEnum MAX_REPEATED_CONSECUTIVE_CHARS_ERROR = new SpcfPortabilityErrorEnum (5107);
    
    /**
     * Password contains text which is part of a black list regular expression list.
     */
    public static SpcfPortabilityErrorEnum BLACKLIST_MATCH_ERROR = new SpcfPortabilityErrorEnum (5108);
    
    /**
     * Password contains less than six chars.
     */
    public static SpcfPortabilityErrorEnum LENGTH_LESS_THAN_SIX_CHARS_ERROR = new SpcfPortabilityErrorEnum (5109);
    
    /**
     * Password quality spec is null.
     */
    public static SpcfPortabilityErrorEnum PASSWORD_QUALITY_SPEC_NULL = new SpcfPortabilityErrorEnum (5110);
    
    /**
     * Password contains symbols that are not part of the defined symbol set.
     */
    public static SpcfPortabilityErrorEnum SYMBOL_CHARS_ERROR = new SpcfPortabilityErrorEnum (5111);


    // Crypto Errors
    
    /**
     * Crypto Provider error
     * This error basically indicates that the crpto provider could not be initialized. It could be due to required parameters which have not been initialized.
     * This error also could mean that the crypto provider is not supported in the current platform.
     */
    public static SpcfPortabilityErrorEnum CRYPTO_PROVIDER_ERROR = new SpcfPortabilityErrorEnum (5200);
    
    /**
     * Encryption error
     * This error indicates that there was an error while encrypting data.
     */
    public static SpcfPortabilityErrorEnum ENCRYPTION_ERROR = new SpcfPortabilityErrorEnum (5201);

    /**
     * Key Null error
     * This error indicates that the key was null.
     */
    public static SpcfPortabilityErrorEnum KEY_NULL_ERROR = new SpcfPortabilityErrorEnum (5202);
    
    /**
     * Crypto System error
     */
    public static SpcfPortabilityErrorEnum CRYPTO_SYSTEM_ERROR = new SpcfPortabilityErrorEnum (5203);

    /**
     * Key Name Null error
     * This error means that the key name was not set or is not available.
     */
    public static SpcfPortabilityErrorEnum KEY_NAME_NULL_ERROR = new SpcfPortabilityErrorEnum (5204);
    
    /**
     * Decryption error
     * This error indicates that there was an error while decrypting data.
     */
    public static SpcfPortabilityErrorEnum DECRYPTION_ERROR = new SpcfPortabilityErrorEnum (5205);
    
    /**
     * Key Null error
     * This error indicates that the key was null.
     */
    public static SpcfPortabilityErrorEnum KEY_PROVIDER_NULL_ERROR = new SpcfPortabilityErrorEnum (5206);
    
    /**
     * Key Null error
     * This error indicates that the key was null.
     */
    public static SpcfPortabilityErrorEnum CRYPTO_PROVIDER_NULL_ERROR = new SpcfPortabilityErrorEnum (5207);
    
    /**
     * Algorithm/Cipher error.
     * This error indicates that the algorithm/cipher supplied is not supported for the current request.
     */
    public static SpcfPortabilityErrorEnum ALGORITHM_ERROR = new SpcfPortabilityErrorEnum(5208);
    
    /**
     * Cipher Mode error.
     * This error indicates that the cipher mode supplied is not supported for the current request.
     */
    public static SpcfPortabilityErrorEnum CIPHER_MODE_ERROR = new SpcfPortabilityErrorEnum(5209);
    
    /**
     * Padding error.
     * This error indicates that the padding supplied is not supported for the current request.
     */
    public static SpcfPortabilityErrorEnum PADDING_ERROR = new SpcfPortabilityErrorEnum(5210);

    /**
     * Key error.
     * This error indicates that the key was invalid or not in the proper format.
     */
    public static SpcfPortabilityErrorEnum KEY_ERROR = new SpcfPortabilityErrorEnum(5211);
    
    /**
     * Signature error.
     * This error indicates that there was an error creating signature.
     */
    public static SpcfPortabilityErrorEnum SIGNATURE_ERROR = new SpcfPortabilityErrorEnum(5212);

    /**
     * Hash Algorithm error.
     * This error means that the hash algorithm supplied is not supported in the current context.
     */
    public static SpcfPortabilityErrorEnum HASH_ALGORITHM_ERROR = new SpcfPortabilityErrorEnum(5213);
    // Security Exceptions
}

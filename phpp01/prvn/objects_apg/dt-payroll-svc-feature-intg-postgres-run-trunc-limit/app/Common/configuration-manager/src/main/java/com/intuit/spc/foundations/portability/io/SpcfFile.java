package com.intuit.spc.foundations.portability.io;

import com.intuit.spc.foundations.portability.*;
import com.intuit.spc.foundations.portability.text.*;

/**
 * SpcfFile represents a file on the file system. 
 */
public abstract class SpcfFile extends SpcfFileSystemEntry
{
	/**
	 * Creates an SpcfFile object that points to the specified path.
	 * @param path location of the file
	 * @return SpcfFile object created through the factory
	 */
	public static SpcfFile createInstance(String path)
	{
		return SpcfFactory.getInstance().createFile(path);
	}
	
	
	/**
	 * Creates an SpcfFile object that points to the file specified 
	 * by name under the specified path.
	 * @param path location of the file
	 * @param name file under the path
	 * @return SpcfFile object created through the factory
	 */
	public static SpcfFile createInstance(String path, String name)
	{
		// Construct a total path from the path and name: 
		String separator = SpcfSystem.getFileSeparator();
		String totalPath = path;
		if (!totalPath.endsWith(separator)) totalPath += separator;
		if (name.contains(separator)) name = name.substring(name.lastIndexOf(separator)); // Name starts with last separator
		totalPath += name;
		
		// Return the directory object:
		return SpcfFactory.getInstance().createFile(totalPath);
	}
	
	
	/**
	 * Does the file exist and can the file be read?
	 * @return true if it can be read, false otherwise 
	 * @throws SpcfSecurityException if a security exception occurs
	 */
	public abstract boolean canRead();
	
	
	/**
	 * Does the file exist and can the file be read?
	 * @param path file to be checked
	 * @return true if it can be read, false otherwise 
	 * @throws SpcfSecurityException if a security exception occurs
	 */
	public static boolean canRead(String path)
	{
		return createInstance(path).canRead();
	}
	
	
	/**
	 * Does the file exist and can the file be written to?
	 * @return true if it can be written to, false otherwise 
	 * @throws SpcfSecurityException if a security exception occurs
	 */
	public abstract boolean canWrite();
	

	/**
	 * Does the file exist and can the file be written to?
	 * @param path file to be checked
	 * @return true if it can be written to, false otherwise 
	 * @throws SpcfSecurityException if a security exception occurs
	 */
	public static boolean canWrite(String path)
	{
		return createInstance(path).canWrite();
	}


	/**
	 * Copies the source file to the target location.
	 * @param sourcePath the name of the file to copy
	 * @param targetPath the name of the file to copy to
	 * @return true if the copy succeeded, false otherwise
	 * @throws SpcfSecurityException if a security exception occurs.
	 * @throws SpcfIOException if any other I/O exception occurs.
	 */
	public static boolean copy(String sourcePath, String targetPath)
	{
		return createInstance(sourcePath).copy(targetPath);
	}

	
	/**
	 * Creates a file on the file system with default access permissions defined. 
	 * Note: Do not confuse this with createInstance().
	 * @param path file to create
	 * @return SpcfFile that corresponds to the newly created file, if it could be created.
	 * @throws SpcfSecurityException if the file cannot be created because of a security issue
	 * @throws SpcfIOException if an I/O error occurred
	 */
	public static SpcfFile createFile(String path)
	{
		SpcfFile file = createInstance(path); 
		file.createFile();
		return file;
	}

	
	/**
	 * Creates a file on the file system with default access permissions defined.
	 * Note: Do not confuse this with createInstance().
	 * @throws SpcfSecurityException if the file cannot be created
	 * @throws SpcfIOException if an I/O error occurred
	 */
	public abstract void createFile();

	
	/**
	 * Deletes the file.
	 * An exception is not thrown if the specified file does not exist. 
	 * @param path file to delete
	 * @return true if the file exists, could be deleted, and was deleted; false otherwise
	 * @throws SpcfSecurityException if a security exception occurs
	 * @throws SpcfIOException if an I/O error occurred
	 */
	public static boolean delete(String path)
	{
		return createInstance(path).delete();
	}
	

	/**
	 * Does the file exist?
	 * @param path file to check
	 * @return true if it exists, false otherwise
	 */
	public static boolean exists(String path)
	{
		return createInstance(path).exists();
	}

	
	/**
	 * If this is a file, returns the extension of the file, including the '.'
	 * @return file extension with the leading period, empty string if not applicable
	 */
	public String getFileExtension()
	{
		String s = getPath();
		int lastDot = s.lastIndexOf('.');
		if (lastDot < 0) return "";
		return s.substring(lastDot+1,s.length());
	}


	/**
	 * Gets the length of the file in bytes, if this is an existing file.
	 * Returns 0 if it is not a file, it doesn't exist, or an exception occurs.
	 * @return length in bytes as a long
	 * @throws SpcfSecurityException if a security exception occurs
	 * @throws SpcfIOException if an I/O exception occurs
	 */
	public abstract long getLength();

	
	/**
	 * Gets the length of the file in bytes, if path points to a file.
	 * @param path file to check
	 * @return length in bytes as a long
	 */
	public static long getLength(String path)
	{
		return createInstance(path).getLength();
	}


	/**
	 * Moves/renames the file from oldPath to newPath. 
	 * @param oldPath old path of file
	 * @param newPath new path of file 
	 * @return true if the move succeeded, false otherwise
	 * @throws SpcfFileAlreadyExistsException if the target file already exists
	 * @throws SpcfSecurityException if a security exception is encountered
	 */
	public static boolean move(String oldPath, String newPath)
	{
		return createInstance(oldPath).move(newPath);
	}

		
	/**
	 * Opens a file for binary reading. Seeking is enabled but writing is disabled.
	 * @param path location of the file
	 * @param fileLock how the file should be locked
	 * @return binary stream
	 * @throws SpcfArgumentNullException if path is null
	 * @throws SpcfArgumentOutOfRangeException is the path is ""
	 * @throws SpcfFileNotFoundException if the file doesn't exist. 
	 * @throws SpcfSecurityException if a security or access permission issue is encountered.
	 * @throws SpcfFileLockException if an exception occurs because of a locking issue
	 * @throws SpcfIOException if an IO exception occurs.
	 */
	public static SpcfStream openForBinaryReading(String path, SpcfFileLockEnum fileLock)
	{
		SpcfParamValidator.checkIsNotNullOrEmptyString(path, "Path");
		return createInstance(path).openForBinaryReading(fileLock);
	}
	

	/**
	 * Opens a file for binary reading. Seeking is enabled but writing is disabled.
	 * @param fileLock how the file should be locked
	 * @return binary stream
	 * @throws SpcfFileNotFoundException if the file doesn't exist. 
	 * @throws SpcfSecurityException if a security or access permission issue is encountered.
	 * @throws SpcfFileLockException if an exception occurs because of a locking issue
	 * @throws SpcfIOException if an IO exception occurs.
	 */
	public abstract SpcfStream openForBinaryReading(SpcfFileLockEnum fileLock);
	

	/**
	 * Opens a file for binary reading. Seeking is enabled but writing is disabled. No locking.
	 * @param path location of the file
	 * @return binary stream
	 * @throws SpcfArgumentNullException if path is null
	 * @throws SpcfArgumentOutOfRangeException is the path is ""
	 * @throws SpcfFileNotFoundException if the file doesn't exist. 
	 * @throws SpcfSecurityException if a security or access permission issue is encountered.
	 * @throws SpcfFileLockException if an exception occurs because of a locking issue
	 * @throws SpcfIOException if an IO exception occurs.
	 */
	public static SpcfStream openForBinaryReading(String path)
	{
		SpcfParamValidator.checkIsNotNullOrEmptyString(path, "Path");
		return createInstance(path).openForBinaryReading();
	}
	

	/**
	 * Opens a file for binary reading. Seeking is enabled but writing is disabled. No locking.
	 * @return binary stream
	 * @throws SpcfFileNotFoundException if the file doesn't exist. 
	 * @throws SpcfSecurityException if a security or access permission issue is encountered.
	 * @throws SpcfFileLockException if an exception occurs because of a locking issue
	 * @throws SpcfIOException if an IO exception occurs.
	 */
	public SpcfStream openForBinaryReading()
	{
		return openForBinaryReading(SpcfFileLockEnum.None);
	}
	
	
	/**
	 * Opens a file for binary writing. Seeking and reading are disabled.
	 * If the file does not exist, it will be created automatically.
	 * If the file does exist, it will be truncated.
	 * @param path location of the file
	 * @param fileLock how the file should be locked
	 * @return binary stream
	 * @throws SpcfArgumentNullException if path or encoding is null
	 * @throws SpcfArgumentOutOfRangeException is the path is ""
	 * @throws SpcfFileNotFoundException if the given file object does not denote an existing, writable regular file and a new regular file of that name cannot be created, or if some other error occurs while opening or creating the file 
	 * @throws SpcfSecurityException if a security or access permission issue is encountered.
	 * @throws SpcfFileLockException if an exception occurs because of a locking issue
	 * @throws SpcfIOException if an IO exception occurs.
	 */
	public static SpcfStream openForBinaryWriting(String path, SpcfFileLockEnum fileLock)
	{
		SpcfParamValidator.checkIsNotNullOrEmptyString(path, "Path");
		return createInstance(path).openForBinaryWriting(fileLock);
	}
	

	/**
	 * Opens a file for binary writing. Seeking and reading are disabled.
	 * If the file does not exist, it will be created automatically.
	 * If the file does exist, it will be truncated.
	 * @param fileLock how the file should be locked
	 * @return binary stream
	 * @throws SpcfFileNotFoundException if the given file object does not denote an existing, writable regular file and a new regular file of that name cannot be created, or if some other error occurs while opening or creating the file 
	 * @throws SpcfSecurityException if a security or access permission issue is encountered.
	 * @throws SpcfFileLockException if an exception occurs because of a locking issue
	 * @throws SpcfIOException if an IO exception occurs.
	 */
	public abstract SpcfStream openForBinaryWriting(SpcfFileLockEnum fileLock);
	

	/**
	 * Opens a file for binary writing. Seeking and reading are disabled. No locking.
	 * If the file does not exist, it will be created automatically.
	 * If the file does exist, it will be truncated.
	 * @param path location of the file
	 * @return binary stream
	 * @throws SpcfArgumentNullException if path is null
	 * @throws SpcfArgumentOutOfRangeException is the path is ""
	 * @throws SpcfFileNotFoundException if the given file object does not denote an existing, writable regular file and a new regular file of that name cannot be created, or if some other error occurs while opening or creating the file 
	 * @throws SpcfSecurityException if a security or access permission issue is encountered.
	 * @throws SpcfFileLockException if an exception occurs because of a locking issue
	 * @throws SpcfIOException if an IO exception occurs.
	 */
	public static SpcfStream openForBinaryWriting(String path)
	{
		SpcfParamValidator.checkIsNotNullOrEmptyString(path, "Path");
		return createInstance(path).openForBinaryWriting();
	}
	

	/**
	 * Opens a file for binary writing. Seeking and reading are disabled. No locking.
	 * If the file does not exist, it will be created automatically.
	 * If the file does exist, it will be truncated.
	 * @return binary stream
	 * @throws SpcfFileNotFoundException if the given file object does not denote an existing, writable regular file and a new regular file of that name cannot be created, or if some other error occurs while opening or creating the file 
	 * @throws SpcfSecurityException if a security or access permission issue is encountered.
	 * @throws SpcfFileLockException if an exception occurs because of a locking issue
	 * @throws SpcfIOException if an IO exception occurs.
	 */
	public SpcfStream openForBinaryWriting()
	{
		return openForBinaryWriting(SpcfFileLockEnum.None);
	}
	
	
	/**
	 * Opens a file for binary read/write random access. Reading, writing, and seeking are all enabled.
	 * If the file does not exist, it will be created automatically.
	 * @param path location of the file
	 * @param fileLock how the file should be locked
	 * @return binary stream
	 * @throws SpcfArgumentNullException if path or encoding is null
	 * @throws SpcfArgumentOutOfRangeException is the path is ""
	 * @throws SpcfFileNotFoundException if the given file object does not denote an existing, writable regular file and a new regular file of that name cannot be created, or if some other error occurs while opening or creating the file 
	 * @throws SpcfSecurityException if a security or access permission issue is encountered.
	 * @throws SpcfFileLockException if an exception occurs because of a locking issue
	 * @throws SpcfIOException if an IO exception occurs.
	 */
	public static SpcfStream openForBinaryRandomAccess(String path, SpcfFileLockEnum fileLock)
	{
		SpcfParamValidator.checkIsNotNullOrEmptyString(path, "Path");
		return createInstance(path).openForBinaryRandomAccess(fileLock);
	}

	
	/**
	 * Opens a file for binary read/write random access. Reading, writing, and seeking are all enabled.
	 * If the file does not exist, it will be created automatically.
	 * @param fileLock how the file should be locked
	 * @return binary stream
	 * @throws SpcfFileNotFoundException if the given file object does not denote an existing, writable regular file and a new regular file of that name cannot be created, or if some other error occurs while opening or creating the file 
	 * @throws SpcfSecurityException if a security or access permission issue is encountered.
	 * @throws SpcfFileLockException if an exception occurs because of a locking issue
	 * @throws SpcfIOException if an IO exception occurs.
	 */
	public abstract SpcfStream openForBinaryRandomAccess(SpcfFileLockEnum fileLock);
	
	
	/**
	 * Opens a file for binary read/write random access. Reading, writing, and seeking are all enabled. No locking.
	 * If the file does not exist, it will be created automatically.
	 * @param path location of the file
	 * @return binary stream
	 * @throws SpcfArgumentNullException if path is null
	 * @throws SpcfArgumentOutOfRangeException is the path is ""
	 * @throws SpcfFileNotFoundException if the given file object does not denote an existing, writable regular file and a new regular file of that name cannot be created, or if some other error occurs while opening or creating the file 
	 * @throws SpcfSecurityException if a security or access permission issue is encountered.
	 * @throws SpcfFileLockException if an exception occurs because of a locking issue
	 * @throws SpcfIOException if an IO exception occurs.
	 */
	public static SpcfStream openForBinaryRandomAccess(String path)
	{
		SpcfParamValidator.checkIsNotNullOrEmptyString(path, "Path");
		return createInstance(path).openForBinaryRandomAccess();
	}

	
	/**
	 * Opens a file for binary read/write random access. Reading, writing, and seeking are all enabled. No locking.
	 * If the file does not exist, it will be created automatically.
	 * @return binary stream
	 * @throws SpcfFileNotFoundException if the given file object does not denote an existing, writable regular file and a new regular file of that name cannot be created, or if some other error occurs while opening or creating the file 
	 * @throws SpcfSecurityException if a security or access permission issue is encountered.
	 * @throws SpcfFileLockException if an exception occurs because of a locking issue
	 * @throws SpcfIOException if an IO exception occurs.
	 */
	public SpcfStream openForBinaryRandomAccess()
	{
		return openForBinaryRandomAccess(SpcfFileLockEnum.None);
	}
	
	
	/**
	 * Opens a file for text reading.
	 * @param path location of the file
	 * @param fileLock how the file should be locked
	 * @param encoding how the text characters are encoded as bytes in the file
	 * @return text reader
	 * @throws SpcfArgumentNullException if path or encoding is null
	 * @throws SpcfArgumentOutOfRangeException is the path is ""
	 * @throws SpcfFileNotFoundException if the file doesn't exist. 
	 * @throws SpcfIllegalArgumentException if the encoding is not supported
	 * @throws SpcfSecurityException if a security or access permission issue is encountered.
	 * @throws SpcfUnsupportedEncodingException if the selected encoding is not supported
	 * @throws SpcfFileLockException if an exception occurs because of a locking issue
	 * @throws SpcfIOException if an IO exception occurs.
	 */
	public static SpcfReader openForTextReading(String path, SpcfFileLockEnum fileLock, SpcfEncoding encoding)
	{
		SpcfParamValidator.checkIsNotNullOrEmptyString(path, "Path");
		SpcfParamValidator.checkIsNotNull(encoding, "Encoding");
		return createInstance(path).openForTextReading(fileLock, encoding);
	}
	
	
	/**
	 * Opens a file for text reading.
	 * @param fileLock how the file should be locked
	 * @param encoding how the text characters are encoded as bytes in the file
	 * @return text reader
	 * @throws SpcfFileNotFoundException if the file doesn't exist. 
	 * @throws SpcfIllegalArgumentException if the encoding is not supported
	 * @throws SpcfSecurityException if a security or access permission issue is encountered.
	 * @throws SpcfUnsupportedEncodingException if the selected encoding is not supported
	 * @throws SpcfFileLockException if an exception occurs because of a locking issue
	 * @throws SpcfIOException if an IO exception occurs.
	 */
	public abstract SpcfReader openForTextReading(SpcfFileLockEnum fileLock, SpcfEncoding encoding);
	
	

	/**
	 * Opens a file for text reading. No locking. Encoding is UTF_8.
	 * @param path location of the file
	 * @return text reader
	 * @throws SpcfArgumentNullException if path is null
	 * @throws SpcfArgumentOutOfRangeException is the path is ""
	 * @throws SpcfFileNotFoundException if the file doesn't exist. 
	 * @throws SpcfSecurityException if a security or access permission issue is encountered.
	 * @throws SpcfFileLockException if an exception occurs because of a locking issue
	 * @throws SpcfIOException if an IO exception occurs.
	 */
	public static SpcfReader openForTextReading(String path)
	{
		SpcfParamValidator.checkIsNotNullOrEmptyString(path, "Path");
		return createInstance(path).openForTextReading();
	}
	
	
	/**
	 * Opens a file for text reading. No locking. Encoding is UTF_8.
	 * @return text reader
	 * @throws SpcfFileNotFoundException if the file doesn't exist. 
	 * @throws SpcfSecurityException if a security or access permission issue is encountered.
	 * @throws SpcfFileLockException if an exception occurs because of a locking issue
	 * @throws SpcfIOException if an IO exception occurs.
	 */
	public SpcfReader openForTextReading()
	{
		return openForTextReading(SpcfFileLockEnum.None, SpcfEncoding.Utf8);
	}
	
	
	/**
	 * Opens the file for text appending.
	 * If the file does not exist, it will be created automatically.
	 * @param path location of the file
	 * @param fileLock how the file should be locked
	 * @param encoding how the text characters are encoded as bytes in the file
	 * @return text writer
	 * @throws SpcfArgumentNullException if path or encoding is null
	 * @throws SpcfArgumentOutOfRangeException is the path is ""
	 * @throws SpcfIllegalArgumentException if the encoding is not supported
	 * @throws SpcfSecurityException if a security or access permission issue is encountered.
	 * @throws SpcfUnsupportedEncodingException if the selected encoding is not supported
	 * @throws SpcfFileLockException if an exception occurs because of a locking issue
	 * @throws SpcfIOException if an IO exception occurs.
	 */
	public static SpcfWriter openForTextAppending(String path, SpcfFileLockEnum fileLock, SpcfEncoding encoding)
	{
		SpcfParamValidator.checkIsNotNullOrEmptyString(path, "Path");
		SpcfParamValidator.checkIsNotNull(encoding, "Encoding");
		return createInstance(path).openForTextAppending(fileLock, encoding);
	}

	
	/**
	 * Opens the file for text appending.
	 * If the file does not exist, it will be created automatically.
	 * @param fileLock how the file should be locked
	 * @param encoding how the text characters are encoded as bytes in the file
	 * @return text writer
	 * @throws SpcfArgumentNullException if encoding is null
	 * @throws SpcfIllegalArgumentException if the encoding is not supported
	 * @throws SpcfSecurityException if a security or access permission issue is encountered.
	 * @throws SpcfUnsupportedEncodingException if the selected encoding is not supported
	 * @throws SpcfFileLockException if an exception occurs because of a locking issue
	 * @throws SpcfIOException if an IO exception occurs.
	 */
	public abstract SpcfWriter openForTextAppending(SpcfFileLockEnum fileLock, SpcfEncoding encoding);
	

	/**
	 * Opens the file for text appending. No locking. Encoding is UTF_8.
	 * If the file does not exist, it will be created automatically.
	 * @param path location of the file
	 * @return text writer
	 * @throws SpcfArgumentNullException if path is null
	 * @throws SpcfArgumentOutOfRangeException is the path is ""
	 * @throws SpcfSecurityException if a security or access permission issue is encountered.
	 * @throws SpcfFileLockException if an exception occurs because of a locking issue
	 * @throws SpcfIOException if an IO exception occurs.
	 */
	public static SpcfWriter openForTextAppending(String path)
	{
		SpcfParamValidator.checkIsNotNullOrEmptyString(path, "Path");
		return createInstance(path).openForTextAppending();
	}

	
	/**
	 * Opens the file for text appending. No locking. Encoding is UTF_8.
	 * If the file does not exist, it will be created automatically.
	 * @return text writer
	 * @throws SpcfSecurityException if a security or access permission issue is encountered.
	 * @throws SpcfFileLockException if an exception occurs because of a locking issue
	 * @throws SpcfIOException if an IO exception occurs.
	 */
	public SpcfWriter openForTextAppending()
	{
		return openForTextAppending(SpcfFileLockEnum.None, SpcfEncoding.Utf8);
	}
	

	/**
	 * Opens the file for text writing.
	 * If the file does not exist, it will be created automatically.
	 * If the file does exist, it will be truncated.
	 * @param path location of the file
	 * @param fileLock how the file should be locked
	 * @param encoding how the text characters are encoded as bytes in the file
	 * @return text writer
	 * @throws SpcfArgumentNullException if path or encoding is null
	 * @throws SpcfArgumentOutOfRangeException is the path is ""
	 * @throws SpcfIllegalArgumentException if the encoding is not supported
	 * @throws SpcfSecurityException if a security or access permission issue is encountered.
	 * @throws SpcfUnsupportedEncodingException if the selected encoding is not supported
	 * @throws SpcfFileLockException if an exception occurs because of a locking issue
	 * @throws SpcfIOException if an IO exception occurs.
	 */
	public static SpcfWriter openForTextWriting(String path, SpcfFileLockEnum fileLock, SpcfEncoding encoding)
	{
		SpcfParamValidator.checkIsNotNullOrEmptyString(path, "Path");
		SpcfParamValidator.checkIsNotNull(encoding, "Encoding");
		return createInstance(path).openForTextWriting(fileLock, encoding);
	}

	
	/**
	 * Opens the file for text writing.
	 * If the file does not exist, it will be created automatically.
	 * If the file does exist, it will be truncated.
	 * @param fileLock how the file should be locked
	 * @param encoding how the text characters are encoded as bytes in the file
	 * @return text writer
	 * @throws SpcfArgumentNullException if encoding is null
	 * @throws SpcfIllegalArgumentException if the encoding is not supported
	 * @throws SpcfSecurityException if a security or access permission issue is encountered.
	 * @throws SpcfUnsupportedEncodingException if the selected encoding is not supported
	 * @throws SpcfFileLockException if an exception occurs because of a locking issue
	 * @throws SpcfIOException if an IO exception occurs.
	 */
	public abstract SpcfWriter openForTextWriting(SpcfFileLockEnum fileLock, SpcfEncoding encoding);
	
		
	/**
	 * Opens the file for text writing. No locking. Encoding is UTF_8.
	 * If the file does not exist, it will be created automatically.
	 * If the file does exist, it will be truncated.
	 * @param path location of the file
	 * @return text writer
	 * @throws SpcfArgumentNullException if path is null
	 * @throws SpcfArgumentOutOfRangeException is the path is ""
	 * @throws SpcfSecurityException if a security or access permission issue is encountered.
	 * @throws SpcfFileLockException if an exception occurs because of a locking issue
	 * @throws SpcfIOException if an IO exception occurs.
	 */
	public static SpcfWriter openForTextWriting(String path)
	{
		SpcfParamValidator.checkIsNotNullOrEmptyString(path, "Path");
		return createInstance(path).openForTextWriting();
	}

	
	/**
	 * Opens the file for text writing. No locking. Encoding is UTF_8.
	 * If the file does not exist, it will be created automatically.
	 * If the file does exist, it will be truncated.
	 * @return text writer
	 * @throws SpcfSecurityException if a security or access permission issue is encountered.
	 * @throws SpcfFileLockException if an exception occurs because of a locking issue
	 * @throws SpcfIOException if an IO exception occurs.
	 */
	public SpcfWriter openForTextWriting()
	{
		return openForTextWriting(SpcfFileLockEnum.None, SpcfEncoding.Utf8);
	}
}

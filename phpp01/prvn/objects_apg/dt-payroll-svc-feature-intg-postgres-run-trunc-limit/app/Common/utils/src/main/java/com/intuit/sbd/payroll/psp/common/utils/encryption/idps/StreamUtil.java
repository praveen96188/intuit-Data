package com.intuit.sbd.payroll.psp.common.utils.encryption.idps;

import java.io.*;

import java.nio.ByteBuffer;
import java.util.Properties;
import java.util.UUID;

import com.intuit.idps.IdpsClient;
import com.intuit.idps.domain.item.Key;
import com.intuit.idps.service.IdpsException;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.spc.foundations.primary.config.ISpcfImmutableConfiguration;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.fileencryption.*;

import com.intuit.idps.service.StreamingCryptoService;

import java.nio.channels.ReadableByteChannel;
import java.security.NoSuchAlgorithmException;

import org.apache.tools.ant.taskdefs.Exec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *@author snasim
 * Utility Class for File Stream
 */
public class StreamUtil {

	private static Logger logger = LoggerFactory.getLogger("StreamUtil");

	private static IdpsClient idpsClient ;

	private static String keyName = "PSP/" + "TestJob" + "AES256_GCM";


	/**
	 * Copies all bytes remaining on the input stream to the output stream
	 * using a small 512 byte buffer.
	 * 
	 * @param input	The input stream to copy from.
	 * @param out	The output stream to copy to.
	 * 
	 * @return The number of bytes copied.
	 */
	public static int copyBytes (InputStream input, FileOutputStream out) {
		byte[] buffer = new byte[512];
		int length = 0;
		
		try {
			int read = input.read(buffer);
			while (read >= 0) {
				length += read;
				out.write(buffer, 0, read);
				read = input.read(buffer);
			}
		} catch (IOException ex) {
			throw new RuntimeException("StreamUtil.copyBytes() was unable to copy all bytes from the input to the output stream: " + ex);  //$NON-NLS-L$ 
		}
			
		return length;
	}

	/**
	 * Copies all bytes remaining on the input stream to byte[] in encrypted format
	 *
	 * @param is The input stream to copy from.
	 * @return copied byte[]
	 *
	 */
	public static int copyEncryptedBytes(InputStream is,File output,Key key) {
	//	byte[] ret;
		int length = 0;

		try (IDPSFileOutputStream os = new IDPSFileOutputStream(output,key)) {
			length = StreamUtil.copyBytes(is, os);
			is.close();
		} catch (IOException ioex) {
			throw new RuntimeException("Exception occured at StreamUtil.copyBytes() ", ioex);
		}
		return length;
	}
	public static int copyDecryptedBytes(File encFile,File output,Key key) {
		//	byte[] ret;
		int length = 0;

		try {
			IDPSFileInputStream is = new IDPSFileInputStream( encFile, key);
			try (FileOutputStream os = new FileOutputStream(output)) {
				length = StreamUtil.copyBytes(is, os);
				is.close();
			} catch (IOException ioex) {
				throw new RuntimeException("Exception occured at StreamUtil.copyBytes() ", ioex);
			}
		}catch(Exception e){

		}
		return length;
	}

	/**
	 * Returns decypted stream from encrypted stream
	 * @param inputStream
	 * @return
	 */
	public static InputStream getDecryptedStream(final IDPSFileInputStream inputStream) {
		try {
		//	inputStream.mark(0);
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int readLength = 0;
			while ((readLength = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, readLength);
			}
		//	inputStream.reset();
			outputStream.flush();
			return new ByteArrayInputStream(outputStream.toByteArray());
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	/**
	 * Encypted stream is decrypted and return as string
	 * @param inputStream
	 * @return
	 */
	public static String inputStreamToString(final IDPSFileInputStream inputStream) {
		try {
			Writer writer5 = new StringWriter();
			Reader reader = new BufferedReader(new InputStreamReader(getDecryptedStream(inputStream), "UTF-8"));
			int readLength = 0;
			char[] buffer = new char[1024];
			while ((readLength = reader.read(buffer)) != -1) {
				writer5.write(buffer, 0, readLength);
			}
			return writer5.toString();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	static {
		ISpcfImmutableConfiguration config = ConfigurationManager.getNonProxiedConfiguration("PSP-Keys");
		String apiKeyId = config.getString("psp_idps_api_key_id");
		String apiSecretKey = config.getString("psp_idps_api_secret_key");
		String apiPolicy = config.getString("psp_idps_api_policy");
		String accessType = config.getString("psp_idps_access_type");
		String endpoint = config.getString("psp_idps_endpoint");
		keyName = config.getString("psp_idps_batchjobs_keyname");
		Properties idpsProperties = new Properties();
		idpsProperties.setProperty("endpoint", endpoint);
		if(!apiPolicy.isEmpty()){
			//logger.info("apiPolicy : " + apiPolicy);
			idpsProperties.setProperty("policy_id", apiPolicy);
			if (StringUtils.isNotBlank(accessType)) {
				idpsProperties.setProperty("access_type", accessType);
			}
		} else {
			idpsProperties.setProperty("api_key_id", apiKeyId);
			idpsProperties.setProperty("api_secret_key", apiSecretKey);
		}

		try {
			idpsClient = IdpsClient.Factory.newInstance(idpsProperties);
			idpsClient.setCryptoLocation(IdpsClient.CryptoLocation.LOCAL);
		} catch (IdpsException | IOException e) {
			//logger.info("Exception : "+e.getMessage());
			e.printStackTrace();
		} catch (Exception exp){
		//	logger.info("Exception : "+exp.getMessage());
			exp.printStackTrace();
		}
	}

	/**
	 * Encypts given file and returns encrypted file
	 * @param key
	 * @param input
	 * @param output
	 * @return
	 * @throws IdpsException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws NoSuchAlgorithmException
	 */
	public static int streamEncryptFileSingleThread(Key key,File input,File output) throws IdpsException, IOException, InterruptedException, NoSuchAlgorithmException {

		// set up the input channel around the byte array input stream
		logger.info("streamEncryptFile:" + input.getName() + "outputFile:" + output.getName());
		FileInputStream in = new FileInputStream(input);

		// for encryption the output buffer needs to be slightly larger than the input buffer
		byte[] outbuffer = new byte[8*1024];
		FileOutputStream fos = new FileOutputStream(output);


		StreamingCryptoService stream = null;

		int totalBytes = 0;
		try {
			// the key handle

			int chunkSize = 4096;
			// initialize the streaming encryption context
			stream = StreamingCryptoService.Factory.streamEncryptInit(key,chunkSize, in.getChannel());

			// get the output channel
			ReadableByteChannel streamOut = stream.getOutputChannel();
			int nBytes;
			// loop until we've encrypted all the bytes we intend to
			while ((nBytes = stream.streamEncryptNext()) != -1) {


				// read the encrypted bytes from the stream's output channel
				int readBytes = streamOut.read(ByteBuffer.wrap(outbuffer, 0, nBytes));

				// make sure the number of bytes encrypted are the number of bytes read
				assert nBytes == readBytes;

				// do whatever you want with outbuffer, in this example we write to a file
				fos.write(outbuffer, 0, readBytes);

				// book-keeping
				totalBytes += nBytes;


			}
			fos.flush();

		}
		finally {
			// close out everything
			if (in != null)
				in.close();
			if (fos != null)
				fos.close();

			// close the stream
			stream.streamClose();
		}
		return totalBytes;
	}

	/**
	 * decrypt input encypted file and creats decrypted file
	 * @param key
	 * @param encFile
	 * @param decFile
	 * @throws IdpsException
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws InterruptedException
	 */
	public static void streamDecryptFileSingleThread(Key key,File encFile,File decFile) throws IdpsException, IOException, NoSuchAlgorithmException, InterruptedException {

		// set up the input channel over the file
		logger.info("streamDecryptFile:" + encFile.getName() + "outputFile:" + decFile.getName());
		FileInputStream cipherFis = new FileInputStream(encFile);
		ReadableByteChannel inchannel = cipherFis.getChannel();
		FileOutputStream fos = new FileOutputStream(decFile);
		// set up the output buffer and the byte array output stream
		byte[] outbuffer = new byte[8*1024];
		//ByteArrayOutputStream baos = new ByteArrayOutputStream();

		StreamingCryptoService stream = null;
		int totalDecryptedBytes = 0;
		int nBytes;

		try {
			// set the key handle
			//Key key = idpsClient.newKeyHandleLatest("StreamingKey");

			// initialize the streaming decryption context
			stream = StreamingCryptoService.Factory.streamDecryptInit(key, inchannel);

			// get the output readable channel
			ReadableByteChannel streamOut = stream.getOutputChannel();

			// loop until we've read and decrypted all the available bytes in the file
			while ((nBytes = stream.streamDecryptNext()) != -1) {

				// read from the output channel
				int readBytes = streamOut.read(ByteBuffer.wrap(outbuffer, 0, nBytes));

				// make sure we've read the amount of bytes we're supposed to
				assert nBytes == readBytes;

				// do anything with the bytes, in this case we write to a byte array output stream
				//baos.write(outbuffer, 0, readBytes);
				fos.write(outbuffer, 0, readBytes);
				// book-keeping
				totalDecryptedBytes += nBytes;

				// print out every 1 MB
				if ((totalDecryptedBytes / (1024*1024)) != ((totalDecryptedBytes-nBytes) / (1024*1024))) {
					logger.info("Stream-DECRYPT: " + (totalDecryptedBytes/(1024*1024)) + "MB");
				}
			}
			fos.flush();
			logger.info("Finished Stream-DECRYPT: finished. Wrote " + totalDecryptedBytes + " bytes");
		}
		catch (Exception e) {
			// in case of error - close the stream
			e.printStackTrace();
			if (stream != null) {
				stream.streamClose();
				stream = null;
			}
			throw e;
		}
		finally {
			// close streams
			if (cipherFis != null)
				cipherFis.close();
			if (fos != null)
				fos.close();

			// close the encryption stream
			if (stream != null)
				stream.streamClose();
		}
	}

	/**
	 * Given a encrypted file it creates a temporary decypted file for email attachment
	 * @param encFile
	 * @return
	 */
	public static String createDecryptedFileForEmail(String encFile){
		String emailFilePath = "";
		try{
			String tempDir = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_temp", "");
			File tempEmailFile;
			File encryptedFile = new File(encFile);
			String fileName = encryptedFile.getName();
			tempEmailFile = new File(tempDir, fileName);
			if (!tempEmailFile.getParentFile().exists()) {
				boolean created = tempEmailFile.getParentFile().mkdirs();

				if (!created) {
					logger.error("Unable to create directory for email files.");
					return emailFilePath;
				}

			}
			if (tempEmailFile.exists()) {
				tempEmailFile.delete();
			}
			//
			Key key  = IDPSFileStreamManager.newKeyHandleLatest();
			StreamUtil.streamDecryptFileSingleThread(key,encryptedFile, tempEmailFile);
			emailFilePath = tempEmailFile.getPath();

		}catch(Exception e){
			logger.error("Failed to create decrypted files for email.");
		}
		logger.info("email file path:" + emailFilePath);
		return emailFilePath;
	}

	/**
	 * copy encrypted file to given output file
	 * @param encFile
	 * @param output
	 * @param key
	 * @return
	 */
	public static int copyEncryptedFile(File encFile,File output,Key key) {
		//	byte[] ret;
		int length = 0;

		try {
			IDPSFileInputStream is = new IDPSFileInputStream( encFile, key);
			try (IDPSFileOutputStream os = new IDPSFileOutputStream(output,key)) {
				length = StreamUtil.copyBytes(is, os);
				is.close();
			} catch (IOException ioex) {
				throw new RuntimeException("Exception occured at StreamUtil.copyBytes() ", ioex);
			}
		}catch(Exception e){

		}
		return length;
	}

	/**
	 *  Checks whether file is IDPS encrypted or not
	 * @param fileName
	 * @return
	 */
	public static boolean isFileIDPSEncrypted(File fileName)
	{
		InputStream sred = null;
		boolean ret = false;
		try {
			sred = new FileInputStream(fileName);
			ret = isStreamIDPSEncrypted(sred);
			//sred.close();
			logger.info("isFileIDPSEncrypted:" + fileName + ":value:" + ret );
		}
		catch(Exception e)
		{
			ret = false;
			logger.error("isFileIDPSEncrypted Failed to openfile :" + fileName);
			throw new RuntimeException(e);
		}
		finally{
			if(sred != null){
				try {
					sred.close();
				}catch(Exception e){
					logger.error("Failed toclose file:" + fileName);
				}
			}
		}
		return ret;

	}

	/**
	 * Checks whether given file name is IDPS encrypted
	 * @param fileName
	 * @return
	 */
	public static boolean isFileIDPSEncrypted(String fileName)
	{
		InputStream sred = null;
		boolean ret = false;
		try {
			sred = new FileInputStream(new File(fileName));
			ret = isStreamIDPSEncrypted(sred);
			//sred.close();
			logger.info("isFileIDPSEncrypted:" + fileName + ":value:" + ret );
		}
		catch(Exception e)
		{
			ret = false;
			logger.error("isFileIDPSEncrypted Failed to openfile :" + fileName);
			throw new RuntimeException(e);
		}
		finally{
			if(sred != null){
				try {
					sred.close();
				}catch(Exception e){
					logger.error("Failed toclose file:" + fileName);
				}
			}
		}
		return ret;

	}

	/**
	 * Checks whether input stream is IDPS encrypted
	 * @param inp
	 * @return
	 */
	public static boolean isStreamIDPSEncrypted(InputStream inp ){
		boolean ret = false;
		try{
			byte[] bytes = new byte[2];

			//pInputStream.mark(1024); //Adding buffer
			int length = inp.read(bytes, 0, 2);
			ret = idpsClient.isIdpsCiphertext(bytes);
		}catch(IllegalArgumentException e)
		{
			ret = false;
			logger.error("Failed to get isStreamIDPSEncrypted" + ret);
			throw e;
		}
		catch(Exception e)
		{
			ret = false;
			logger.error("Failed to get isStreamIDPSEncrypted" + ret);
			throw new RuntimeException(e);
		}
		return ret;
	}

	/**
	 * @param val
	 * @param allowedRegEx
	 * @return
	 */
	protected static String format(String val, String allowedRegEx) {
		val = val.toUpperCase().trim();
		val = val.replaceAll(" +", " ");
		char[] charNotAllowed = val.replaceAll(allowedRegEx, "").toCharArray();
		for (char ch : charNotAllowed) {
			val = val.replace(ch, '~');
		}
		return val.replaceAll("~", "");
	}

	/**
	 * @param pFile
	 * @param key
	 * @return
	 */
	private static byte[] readFile(File pFile,Key key) {
		BufferedInputStream inputStream = null;
		byte[] buffer = new byte[0];

		try {
			buffer = new byte[(int) pFile.length()];
			inputStream = new BufferedInputStream(new IDPSFileInputStream(pFile,key));
			inputStream.read(buffer);
		} catch (IOException ioe) {
			throw new RuntimeException("Failed to read file: " + pFile.getName(), ioe);
		}catch (Exception ex){

		}
		finally {
			if (inputStream != null) {
				try { inputStream.close(); }
				catch (Throwable t) {}
			}
		}

		return buffer;
	}

	/**
	 * @param fileName
	 * @return
	 */
	public static String FileWithoutExt(String fileName){
		String fileNameWithOutExt = FilenameUtils.removeExtension(fileName);
		return fileNameWithOutExt;
	}

	/**
	 * @param pFile
	 * @return
	 */
	private static byte[] readFile(File pFile) {
		BufferedInputStream inputStream = null;
		byte[] buffer = new byte[0];
		try {
			buffer = new byte[(int) pFile.length()];
			inputStream = new BufferedInputStream(new FileInputStream(pFile));
			inputStream.read(buffer);
		} catch (IOException ioe) {
			throw new RuntimeException("Failed to read RAFEnrollmentFile: " + pFile.getName(), ioe);
		} finally {
			if (inputStream != null) {
				try { inputStream.close(); }
				catch (Throwable t) {}
			}
		}

		return buffer;
	}

	/*public static void main(String[] args) {


		try {
			Key key = idpsClient.newKeyHandleLatest(keyName);
		try {

			File f = new File("WT10731.RPT");
			if(isFileIDPSEncrypted(f)) {
				IDPSFileInputStream sec = new IDPSFileInputStream(f, key);
				String str = inputStreamToString(sec);
				logger.info(str);
				sec.close();
			}
			else{
				byte[] b = readFile(f);
				String s = new String(b);
				System.out.println(s);
			}

		}
		catch(Exception e)
		{
			logger.info(e.getMessage());
		}


		} catch (Exception ex) {

		}

	}*/
}


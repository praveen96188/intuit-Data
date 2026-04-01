package com.intuit.sbg.psp.filter.service;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intuit.platform.components.lastmile.Constants.SignAlgorithm;
import com.intuit.platform.components.lastmile.FileKeyStoreAccessor;
import com.intuit.platform.components.lastmile.RSAKeyLoader;
import com.intuit.platform.components.lastmile.ReadOnlyClaimsSet;
import com.intuit.platform.components.lastmile.exceptions.AudienceIncorrectException;
import com.intuit.platform.components.lastmile.exceptions.ExpirationException;
import com.intuit.platform.components.lastmile.exceptions.KeyLoadException;
import com.intuit.platform.components.lastmile.exceptions.KeyNotFoundException;
import com.intuit.platform.components.lastmile.exceptions.RequiredFieldsException;
import com.intuit.platform.components.lastmile.exceptions.SignatureInvalidException;
import com.intuit.platform.components.lastmile.exceptions.TokenParseException;
import com.intuit.platform.components.lastmile.exceptions.UnsupportedAlgorithmException;
import com.intuit.platform.components.lastmile.exceptions.VerifierNotInitializedException;
import com.intuit.platform.components.lastmile.recipient.DefaultVerifier;
import com.intuit.platform.components.lastmile.recipient.JWTVerifierCache;
import com.intuit.platform.components.lastmile.recipient.RSATokenParser;
import com.intuit.platform.components.lastmile.recipient.TokenParserComposite;
import com.intuit.platform.components.lastmile.recipient.Verifier;
import com.intuit.sbg.psp.filter.exception.LastMileAuthException;

/**
 * @author rn5 
 * 
 * Service Layer for LMA. Any requests to the application should
 * have authentication tokens and requests should come only through gateways.
 * 
 */
public class LastMileAuthenticationService {

	private TokenParserComposite tokenParserComposite = null;
	private Verifier verifier = null;
	private final Logger logger = LoggerFactory.getLogger(LastMileAuthenticationService.class);

	public LastMileAuthenticationService(String keystorePath, String keystorePassword, Set<String> targetAudienceSet) {
		logger.info("Configuring last mile authentication. keystorePath={}", keystorePath);
		RSAKeyLoader rsaKeyLoader;
		try {
			rsaKeyLoader = new RSAKeyLoader(new FileKeyStoreAccessor(keystorePath, keystorePassword));
			rsaKeyLoader.loadAllPublicKeys();
		} catch (KeyLoadException e) {
			throw new LastMileAuthException("Failed to initialize LMA. Error while loading key.", e);
		}
		JWTVerifierCache jwtVerifierCache = new JWTVerifierCache();
		try {
			jwtVerifierCache.setRsaKeyLoader(rsaKeyLoader);
		} catch (KeyNotFoundException e) {
			throw new LastMileAuthException("Failed to initialize LMA. LMA Key not found", e);
		}

		RSATokenParser rsaTokenParser = new RSATokenParser();
		rsaTokenParser.setJwtVerifierCache(jwtVerifierCache);

		tokenParserComposite = new TokenParserComposite();
		tokenParserComposite.addTokenParser(SignAlgorithm.RS256, rsaTokenParser);

		verifier = new DefaultVerifier();
		verifier.setTargetAudience(targetAudienceSet);
	}

	public void authenticate(String signedClaim) {
		ReadOnlyClaimsSet readOnlyClaimsSet = null;
		try {
			readOnlyClaimsSet = tokenParserComposite.parse(signedClaim);
			verifier.verifyAll(readOnlyClaimsSet);
		} catch (VerifierNotInitializedException | SignatureInvalidException | TokenParseException
				| UnsupportedAlgorithmException | RequiredFieldsException | ExpirationException
				| AudienceIncorrectException e) {
			throw new LastMileAuthException("Last Mile Authentication failed. Audience = " + readOnlyClaimsSet.getAudience(), e);
		}
	}

}

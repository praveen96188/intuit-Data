package com.intuit.sbg.psp.filter.helper;

import java.net.InetAddress;
import java.util.*;

import org.apache.commons.collections4.SetValuedMap;
import com.intuit.sbg.psp.filter.service.constants.Constants;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.commons.lang3.StringUtils;


/**
 * @author rn5
 * Helper for Last Mile Auth. Helps to deal with Audience & ignore Urls.
 */
public class LastMileAuthHelper {

	/**
	 * Get set of host URLs/patterns. 
	 * @param hostURL
	 * @return set of host URLs/patterns
	 */
	public Set<String> getUrlPattternSet(String hostURL) {
		Set<String> hostURLSet = new HashSet<>();
		StringTokenizer tokenizer = new StringTokenizer(hostURL, Constants.AUDIENCE_DELIMITER);
		while (tokenizer.hasMoreTokens()) {
			hostURLSet.add(tokenizer.nextToken().trim());
		}
		return hostURLSet;
	}

	/**
	 * Get map of httpMethod and host URLs/patterns.
	 * @param ignoreUrlEntries
	 * @return map of httpMethod and host URLs/patterns.
	 */
	public SetValuedMap<String,String> getIgnoreUrlPattternMap(String ignoreUrlEntries) {
		Set<String> hostURLSet = getUrlPattternSet(ignoreUrlEntries);
		SetValuedMap<String, String> result = new HashSetValuedHashMap();
		for (String entry: hostURLSet) {
			entry = entry.trim();
			if (!StringUtils.isEmpty(entry)) {
				String[] splitString = entry.split(Constants.IGNORE_ENTRY_DELIMITER);
				if (splitString.length == 1 && !entry.contains(Constants.IGNORE_ENTRY_DELIMITER)) {
					result.put(Constants.COMMON_HTTP_METHOD, entry);
				} else if (splitString.length == 2) {
					if (StringUtils.isEmpty(splitString[0].trim()) || StringUtils.isEmpty(splitString[1].trim()))
						continue;
					result.put(splitString[0].trim(), splitString[1].trim());
				}
			}
		}
		return result;
	}

	/**
	 * Check if we need to bypass filter operation.
	 * @param uri
	 * @param ignorePathSet
	 * @return true if we need to bypass filter operation.
	 */
	public boolean isFilterSkipRequired(String hostInfo, String pathInfo, Set<String> ignorePathSet) {
		boolean skipFilter = ignorePathSet.stream().anyMatch(ignorePath -> pathInfo.contains(ignorePath));
		if (skipFilter)
			return true;

		try {
			InetAddress host = InetAddress.getLocalHost();
			if (hostInfo.toLowerCase().contains(host.getHostName().toLowerCase())) {
				skipFilter = true;
			}
		} catch (Exception e) {
			skipFilter = false;
		}
		return skipFilter;
	}
}

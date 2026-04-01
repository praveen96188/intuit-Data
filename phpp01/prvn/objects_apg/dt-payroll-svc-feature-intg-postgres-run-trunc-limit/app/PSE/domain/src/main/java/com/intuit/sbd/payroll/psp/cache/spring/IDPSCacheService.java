package com.intuit.sbd.payroll.psp.cache.spring;

import com.intuit.idps.domain.item.Key;
import com.intuit.idps.domain.item.ListedVersion;
import com.intuit.idps.domain.item.VersionIterator;
import com.intuit.idps.service.IdpsException;
import com.intuit.idps.service.rest.IdpsCommunicationException;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.configuration.IDPSManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
@Slf4j
public class IDPSCacheService {

    private final CacheManager cacheManager;
    private static final String CACHE_NAME = "keysCache";
    private static final int CACHE_REFRESH_FIXED_DELAY = 120000;
    private static final int CACHE_REFRESH_INITIAL_DELAY = 480000;

    @Autowired
    public IDPSCacheService(@Qualifier("localCacheManager") CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    //ConcurrentHashMapCache
    @Cacheable(value = CACHE_NAME, key = "#keyName", sync = true)
    public List<Key> getKeys(String keyName) throws IdpsCommunicationException, IdpsException {
        return getKeysFromIDPS(keyName);
    }

    public List<Key> getKeysFromIDPS(String keyName) throws IdpsCommunicationException, IdpsException {
        log.info("Fetching the Keys from IDPS for keyName={}", keyName);
        List<Key> keyList = new ArrayList<>();
        Key latestKey = IDPSManager.getIdpsClient().newKeyHandleLatest(keyName);
        VersionIterator listedVersions = latestKey.listAllVersions(Boolean.TRUE);
        for (ListedVersion listedVersion : listedVersions) {
            Key key = IDPSManager.getIdpsClient().newKeyHandle(keyName, listedVersion.getVersion());
            keyList.add(key);
        }
        return keyList;
    }

    //ToDo Periodic Refresh of all keys irrespective of access
    @Scheduled(fixedDelay = CACHE_REFRESH_FIXED_DELAY, initialDelay = CACHE_REFRESH_INITIAL_DELAY)
    public void refreshKeys() throws IdpsCommunicationException, IdpsException {
        log.info("Refreshing the Keys Cache");
        StopWatch timer = new StopWatch();
        timer.start();
        Map<String, List<Key>> keysCache = (Map<String, List<Key>>) cacheManager.getCache(CACHE_NAME).getNativeCache();
        log.info("Current cachesize={}", keysCache.size());
        if (!keysCache.isEmpty()) {
            for (String keyName : keysCache.keySet()) {
                keysCache.put(keyName, getKeysFromIDPS(keyName));
                log.info("Refreshing the key={}", keyName);
            }
        }
        timer.stop();
        log.info("Time taken to refresh the cache cacheRefreshTime={} ms", timer.getTotalTimeMillis());
    }

    // Added to support Unit Tests, evicts the cache for a given key version
    public void evictVersion(String keyName, int version) {
        Cache keysCache = this.cacheManager.getCache(CACHE_NAME);
        List<Key> keys = (List<Key>) keysCache.get(keyName).get();
        keys.remove(version - 1);
        keysCache.put(keyName, keys);
    }

    //evicts the cache for a given key
    public void evict(String keyName) {
        log.info("Evicting the cache for key={}", keyName);
        this.cacheManager.getCache(CACHE_NAME).evictIfPresent(keyName);
    }

}

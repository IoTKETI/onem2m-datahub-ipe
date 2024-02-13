package kr.re.keti.sc.ipe.interworking.sementic.service;

import kr.re.keti.sc.ipe.datahub.vo.NgsiLdCollectRequestVO;
import kr.re.keti.sc.ipe.interworking.sementic.vo.SementicDescriptorCacheVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class SementicInterworkingCacheManager {

    private Map<String, NgsiLdCollectRequestVO> datahubEntityCache;
    private Map<String, SementicDescriptorCacheVO> onem2mSementicDescriptorCache;

    public SementicInterworkingCacheManager() {
        this.datahubEntityCache = new ConcurrentHashMap<>();
        this.onem2mSementicDescriptorCache = new ConcurrentHashMap<>();
    }

    public void addDatahubEntityCache(String entityId, NgsiLdCollectRequestVO commonEntityFullVO) {
        this.datahubEntityCache.put(entityId, commonEntityFullVO);
    }

    public NgsiLdCollectRequestVO getDatahubEntityCache(String entityId) {
        return this.datahubEntityCache.get(entityId);
    }

    public void addSementicDescriptorCache(String m2mUri, SementicDescriptorCacheVO sementicDescriptorCacheVO) {
        log.info("addSementicDescriptorCache containerUri={}, sementicDescriptor={}", m2mUri, sementicDescriptorCacheVO.getSementicDescriptor());
        this.onem2mSementicDescriptorCache.put(m2mUri, sementicDescriptorCacheVO);
    }

    public SementicDescriptorCacheVO getSementicDescriptorCacheByContainerUri(String containerUri) {
        return onem2mSementicDescriptorCache.get(containerUri);
    }
}

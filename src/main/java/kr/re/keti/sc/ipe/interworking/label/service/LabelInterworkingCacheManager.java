package kr.re.keti.sc.ipe.interworking.label.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import kr.re.keti.sc.ipe.common.code.IpeCode;
import kr.re.keti.sc.ipe.datahub.vo.NgsiLdCollectRequestVO;
import kr.re.keti.sc.ipe.interworking.label.vo.ContainerMetadataBaseVO;

@Component
public class LabelInterworkingCacheManager {

    private Map<String, NgsiLdCollectRequestVO> datahubEntityCache;
    private Map<String, ContainerMetadataBaseVO> containerMetadataCache;

    public LabelInterworkingCacheManager() {
        this.datahubEntityCache = new ConcurrentHashMap<>();
        this.containerMetadataCache = new ConcurrentHashMap<>();
    }

    public void addDatahubEntityCache(String entityId, NgsiLdCollectRequestVO entityVO) {
        this.datahubEntityCache.put(entityId, entityVO);
    }

    public NgsiLdCollectRequestVO getDatahubEntityCache(String entityId) {
        return this.datahubEntityCache.get(entityId);
    }

    public void addContainerMetadataCache(ContainerMetadataBaseVO containerMetadataBaseVO) {
        this.containerMetadataCache.put(containerMetadataBaseVO.getSubUri(), containerMetadataBaseVO);
    }

    public List<ContainerMetadataBaseVO> getContainerMetadataByResourceId(String onem2mResourceId) {
        return getContainerMetadataByResourceId(onem2mResourceId, null);
    }

    public List<ContainerMetadataBaseVO> getContainerMetadataByResourceId(String onem2mResourceId, IpeCode.IwkedDirection iwkedDirection) {

        List<ContainerMetadataBaseVO> containerMetadataBaseVOs = new ArrayList<>();

        for(ContainerMetadataBaseVO containerMetadataBaseVO : containerMetadataCache.values()) {

            if(iwkedDirection != null && !containerMetadataBaseVO.getLabelVO().getIwkedDirection().contains(iwkedDirection)) {
                continue;
            }

            if(onem2mResourceId.equals(containerMetadataBaseVO.getSubUri())) {
                containerMetadataBaseVOs.add(containerMetadataBaseVO);
                continue;
            }

            List<String> relatedResource = containerMetadataBaseVO.getLabelVO().getIwkedRelatedResources();
            if(relatedResource != null) {
                if(relatedResource.contains(onem2mResourceId)) {
                    containerMetadataBaseVOs.add(containerMetadataBaseVO);
                    continue;
                }
            }
        }
        if(containerMetadataBaseVOs.size() > 0) {
            return containerMetadataBaseVOs;
        } else {
            return null;
        }
    }
}

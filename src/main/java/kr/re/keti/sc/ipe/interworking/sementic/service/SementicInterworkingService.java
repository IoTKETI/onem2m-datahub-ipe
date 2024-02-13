package kr.re.keti.sc.ipe.interworking.sementic.service;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import kr.re.keti.sc.ipe.common.code.Constants;
import kr.re.keti.sc.ipe.common.code.IpeCode;
import kr.re.keti.sc.ipe.datahub.service.DataHubApiService;
import kr.re.keti.sc.ipe.datahub.vo.AttributeVO;
import kr.re.keti.sc.ipe.datahub.vo.NgsiLdCollectRequestVO;
import kr.re.keti.sc.ipe.datahub.vo.GeoPropertyVO;
import kr.re.keti.sc.ipe.interworking.sementic.vo.NgsiLdContainerUriVO;
import kr.re.keti.sc.ipe.interworking.sementic.vo.SementicDescriptorCacheVO;
import kr.re.keti.sc.ipe.interworking.sementic.vo.sementicdescriptor.*;
import kr.re.keti.sc.ipe.onem2m.service.Onem2mApiService;
import kr.re.keti.sc.ipe.onem2m.vo.*;
import kr.re.keti.sc.ipe.util.DateUtil;
import kr.re.keti.sc.ipe.util.ValidateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class SementicInterworkingService {
    @Autowired
    private SementicInterworkingCacheManager sementicInterworkingCacheManager;
    @Autowired
    private Onem2mApiService onem2mApiService;
    @Autowired
    private DataHubApiService dataHubApiService;

    @Value("${onem2m.sementic.platform-id}")
    private String sementicPlatformId;
    @Value("${onem2m.sementic.base-uri}")
    private String sementicBaseUri;
    @Value("${onem2m.sementic.context-path}")
    private String sementicContextPath;
    @Value("${onem2m.sementic.regist-application-id}")
    private String sementicRegistApplicationId;
    @Value("${onem2m.sementic.init-interworking-enabled}")
    private Boolean initInterworkingEnabled;
    @Value("${onem2m.sementic.notification.receive-uri}")
    private String sementicNotificationReceiveUri;

    @Value("${datahub.sementic.base-uri}")
    private String datahubBaseUri;
    @Value("${datahub.sementic.sub-uri.collect}")
    private String datahubCollectUri;

    private XmlMapper xmlMapper = new XmlMapper();
    private final Pattern ENTITY_ID_PATTERN = Pattern.compile("\\{(.+)\\}");

    @PostConstruct
    public void init() {

        if(!initInterworkingEnabled) {
            return;
        }

        // 1. Discovery sementicNgsiLdContainer uris (only container uri)
        List<NgsiLdContainerUriVO> rootContainerUriVO = discoveryNgsiLdSementic();

        // 2. Discovery all sub-container uris (container, latest contentInstance, latest smd uri)
        List<NgsiLdContainerUriVO> ngsiLdContainerUriVOs = discoveryAllSubContainerUris(rootContainerUriVO);

        // 3. Retrieve sementicDescriptor and Parsing & Caching
        retrieveAndCachingSementicDescriptor(ngsiLdContainerUriVOs);

        // 4. oneM2M 리소스 조회 및 DataHub 적재 전송
        retrieveAndCachingResource(ngsiLdContainerUriVOs);

        // 5. oneM2M 리소스 구독
        subscriptionOneM2MResource(ngsiLdContainerUriVOs);
    }

    public List<NgsiLdContainerUriVO>  discoveryNgsiLdSementic() {
        UriDiscoveryVO uriDiscoveryVO = onem2mApiService.discoveryNgsiLdSementic(sementicBaseUri, sementicContextPath, sementicRegistApplicationId);
        if(uriDiscoveryVO != null && uriDiscoveryVO.getM2mUril() != null && uriDiscoveryVO.getM2mUril().size() > 0) {
            List<NgsiLdContainerUriVO> ngsiLdContainerUriVOs = new ArrayList<>();
            for(String m2mUri : uriDiscoveryVO.getM2mUril()) {
                NgsiLdContainerUriVO ngsiLdContainerUriVO = new NgsiLdContainerUriVO();
                ngsiLdContainerUriVO.setContainer(m2mUri);
                ngsiLdContainerUriVOs.add(ngsiLdContainerUriVO);
            }
            return ngsiLdContainerUriVOs;
        }
        return null;
    }

    public Onem2mContainerVO retrieveContainer(String m2mUri) {
        return onem2mApiService.retrieveContainer(sementicBaseUri, m2mUri, sementicRegistApplicationId);
    }

    public Onem2mContainerInstanceVO retrieveContainerInstance(String m2mUri) {
        return onem2mApiService.retrieveContainerInstance(sementicBaseUri, m2mUri, sementicRegistApplicationId);
    }

    public Onem2mSementicDescriptorVO retrieveSementicDescriptor(String m2mUri) {
        return onem2mApiService.retrieveSementicDescriptor(sementicBaseUri, m2mUri, sementicRegistApplicationId);
    }

    public NgsiLdContainerUriVO receiveNgsiLdContainerUris(String resourceUri, String maxLevelStr) {

        // default 1
        int maxLevel = 1;
        if(!ValidateUtil.isEmptyData(maxLevelStr)) {
            if(maxLevelStr.equals(IpeCode.DiscoveryMaxLevel.ALL.getCode())) {
                maxLevel = Integer.MAX_VALUE;
            } else {
                try {
                    maxLevel = Integer.parseInt(maxLevelStr);
                } catch (NumberFormatException e) {
                    log.warn("SementicInterworkingService maxLevel parsing error. maxLevel={}", maxLevelStr);
                }
            }
        }

        int currentLevel = 1;

        try {
            UriDiscoveryVO uriDiscoveryVO = new UriDiscoveryVO();
            uriDiscoveryVO.setM2mUril(Arrays.asList(resourceUri));
            List<NgsiLdContainerUriVO> ngsiLdContainerUriVOs = discoverySubContainerAndSemantic(uriDiscoveryVO, maxLevel, currentLevel);
            if(ngsiLdContainerUriVOs != null && ngsiLdContainerUriVOs.size() > 0) {
                return ngsiLdContainerUriVOs.get(0);
            }
        } catch(Exception e) {
            log.error("receiveNgsiLdContainerUris error. resourceUri={}", resourceUri, e);
        }
        return null;
    }

    private List<NgsiLdContainerUriVO> discoverySubContainerAndSemantic(UriDiscoveryVO uriDiscoveryVO, int maxLevel, int currentLevel) {
        if(uriDiscoveryVO == null || uriDiscoveryVO.getM2mUril() == null || uriDiscoveryVO.getM2mUril().size() == 0) {
            return null;
        }

        if(currentLevel > 1 && maxLevel <= currentLevel) {
            return null;
        }

        currentLevel++;

        List<NgsiLdContainerUriVO> ngsiLdContainerUriVOs = new ArrayList<>();
        List<String> m2mUris = uriDiscoveryVO.getM2mUril();
        for(String m2mUri : m2mUris) {
            NgsiLdContainerUriVO ngsiLdContainerUriVO = new NgsiLdContainerUriVO();
            ngsiLdContainerUriVO.setContainer(m2mUri);
            ngsiLdContainerUriVO.setLatest(m2mUri + Constants.ONEM2M_LATEST_SUBURI);
            // 최종 생성/수정된 sementicDescriptor 검색 및 세팅
            ngsiLdContainerUriVO.setSementicDescriptor(onem2mApiService.discoveryLastSementicDescriptorUri(sementicBaseUri, m2mUri, sementicRegistApplicationId));
            // sub 컨테이너 & sementicDescriptor 검색
            UriDiscoveryVO subContainerUriDiscoveryVO = onem2mApiService.discoveryContainer(sementicBaseUri, m2mUri, sementicRegistApplicationId);
            List<NgsiLdContainerUriVO> subContainerUris = discoverySubContainerAndSemantic(subContainerUriDiscoveryVO, maxLevel, currentLevel);
            ngsiLdContainerUriVO.setSubContainer(subContainerUris);
            ngsiLdContainerUriVOs.add(ngsiLdContainerUriVO);
        }
        return ngsiLdContainerUriVOs;
    }

    private List<NgsiLdContainerUriVO> discoveryAllSubContainerUris(List<NgsiLdContainerUriVO> ngsiLdContainerUriVOs) {

        if(ngsiLdContainerUriVOs != null && ngsiLdContainerUriVOs.size() > 0) {
            UriDiscoveryVO uriDiscoveryVO = new UriDiscoveryVO();
            uriDiscoveryVO.setM2mUril(new ArrayList<>());
            for(NgsiLdContainerUriVO ngsiLdContainerUriVO : ngsiLdContainerUriVOs) {
                uriDiscoveryVO.getM2mUril().add(ngsiLdContainerUriVO.getContainer());
            }
            return discoverySubContainerAndSemantic(uriDiscoveryVO, Integer.MAX_VALUE, 1);
        }

        return null;
    }

    public SementicDescriptor parseSementicDescriptor(String xml) throws JacksonException {
        return xmlMapper.readValue(xml, SementicDescriptor.class);
    }

    private void retrieveAndCachingSementicDescriptor(List<NgsiLdContainerUriVO> ngsiLdContainerUriVOs) {

        List<SementicDescriptorCacheVO> sementicDescriptorCacheVOs = generateSementicDescriptorCaches(ngsiLdContainerUriVOs);

        if(sementicDescriptorCacheVOs != null) {
            for(SementicDescriptorCacheVO sementicDescriptorCacheVO : sementicDescriptorCacheVOs) {
                sementicInterworkingCacheManager.addSementicDescriptorCache(sementicDescriptorCacheVO.getContainerUri(), sementicDescriptorCacheVO);
            }
        }
    }

    private List<SementicDescriptorCacheVO> generateSementicDescriptorCaches(List<NgsiLdContainerUriVO> ngsiLdContainerUriVOs) {

        List<SementicDescriptorCacheVO> sementicDescriptorCacheVOs = null;
        for(NgsiLdContainerUriVO subContainerUriVO : ngsiLdContainerUriVOs) {
            Onem2mSementicDescriptorVO onem2mSementicDescriptorVO = onem2mApiService.retrieveSementicDescriptor(
                    sementicBaseUri, subContainerUriVO.getSementicDescriptor(), sementicRegistApplicationId);

            if(onem2mSementicDescriptorVO != null) {
                String dsp = onem2mSementicDescriptorVO.getM2mSmd().getDsp();
                if(dsp != null) {
                    Base64.Decoder decoder = Base64.getDecoder();
                    String decodedDsp = new String(decoder.decode(dsp));
                    SementicDescriptor sementicDescriptorVO = null;
                    try {
                        sementicDescriptorVO = parseSementicDescriptor(decodedDsp);
                    } catch (JacksonException e) {
                        log.error("Parsing SementicDescriptor error", e);
                        continue;
                    }

                    SementicDescriptorCacheVO sementicDescriptorCacheVO = new SementicDescriptorCacheVO();
                    sementicDescriptorCacheVO.setContainerUri(subContainerUriVO.getContainer());
                    sementicDescriptorCacheVO.setSementicDescriptorUri(subContainerUriVO.getSementicDescriptor());
                    sementicDescriptorCacheVO.setSementicDescriptor(sementicDescriptorVO);

                    if(sementicDescriptorCacheVOs == null) sementicDescriptorCacheVOs = new ArrayList<>();
                    sementicDescriptorCacheVOs.add(sementicDescriptorCacheVO);

                    if(subContainerUriVO.getSubContainer() != null) {
                        List<SementicDescriptorCacheVO> subSementicDescriptorCacheVOs = generateSementicDescriptorCaches(subContainerUriVO.getSubContainer());
                        sementicDescriptorCacheVOs.addAll(subSementicDescriptorCacheVOs);
                    }
                }
            }
        }
        return sementicDescriptorCacheVOs;
    }

    private void retrieveAndCachingResource(List<NgsiLdContainerUriVO> ngsiLdContainerUriVOs) {
        if(ngsiLdContainerUriVOs == null || ngsiLdContainerUriVOs.size() == 0) {
            return;
        }

        for(NgsiLdContainerUriVO ngsiLdContainerUriVO : ngsiLdContainerUriVOs) {
            String containerUri = ngsiLdContainerUriVO.getContainer();
            String latestContentInstanceUri = ngsiLdContainerUriVO.getLatest();
            Onem2mContainerInstanceVO onem2mContainerInstanceVO = onem2mApiService.retrieveContainerInstance(
                    sementicBaseUri, latestContentInstanceUri, sementicRegistApplicationId);

            if(onem2mContainerInstanceVO != null && onem2mContainerInstanceVO.getM2mCin() != null) {
                convertAndCollect(containerUri, onem2mContainerInstanceVO.getM2mCin());
            }

            retrieveAndCachingResource(ngsiLdContainerUriVO.getSubContainer());
        }
    }

    public void convertAndCollect(String containerUri, M2mCinVO m2mCin) {
        SementicDescriptorCacheVO sementicDescriptorCacheVO = sementicInterworkingCacheManager.getSementicDescriptorCacheByContainerUri(containerUri);

        SementicDescriptorCacheVO entityIdCacheVO = getSementicCacheHasEntityId(sementicDescriptorCacheVO);
        String entityId = extractEntityId(entityIdCacheVO);
        String domainType = extractTextAfterSeparator(entityIdCacheVO.getDomainType(), "#");

        SementicDescriptor sementicDescriptor = sementicDescriptorCacheVO.getSementicDescriptor();
        Entity entity = sementicDescriptor.getEntity();
        List<HasProperty> hasProperties = entity.getHasProperty();
        if(hasProperties != null && hasProperties.size() > 0) {

            NgsiLdCollectRequestVO ngsiLdCollectRequestVO = new NgsiLdCollectRequestVO();
            ngsiLdCollectRequestVO.setId(entityId);
            ngsiLdCollectRequestVO.setType(domainType);
            if(sementicDescriptor.getMapping() != null) {
                ngsiLdCollectRequestVO.setContext(entityIdCacheVO.getSementicDescriptor().getMapping().getNgsiLdContext());
            } else if(entityIdCacheVO.getSementicDescriptor().getMapping() != null) {
                ngsiLdCollectRequestVO.setContext(entityIdCacheVO.getSementicDescriptor().getMapping().getNgsiLdContext());
            }

            for(HasProperty hasProperty : hasProperties) {
                putAttributeVO(ngsiLdCollectRequestVO, hasProperty, m2mCin);
            }

            log.info("OneM2M Resource To Datahub Resource. \n\toneM2M ResourceId={}, \n\tdatahub ResourceId={}, \n\toneM2M Resource={}, \n\tdatahub Resource={}" ,
                    containerUri, entityId, m2mCin.toString(), ngsiLdCollectRequestVO.toString());

            dataHubApiService.entityOperations(datahubBaseUri, datahubCollectUri, "", ngsiLdCollectRequestVO);
        }
    }

    private void putAttributeVO(Map<String, Object> parentMap, HasProperty hasProperty, M2mCinVO m2mCin) {
        Property property = hasProperty.getProperty();
        if(property != null) {

            String propertyName = extractTextAfterSeparator(property.getAbout(), "#");
            Object attributeValue = getAttributeValue(property, m2mCin);

            AttributeVO attributeVO = new AttributeVO();
            attributeVO.setType(IpeCode.AttributeType.PROPERTY);
            attributeVO.setValue(attributeValue);
            parentMap.put(propertyName, attributeVO);

            if(property.getHasProperty() != null && property.getHasProperty().size() > 0) {
                for(HasProperty innerHasProperty : property.getHasProperty()) {
                    putAttributeVO(attributeVO, innerHasProperty, m2mCin);
                }
            }
        }

        GeoProperty geoProperty = hasProperty.getGeoProperty();
        if(geoProperty != null) {

            String propertyName = extractTextAfterSeparator(geoProperty.getAbout(), "#");
            String geoJsonType = geoProperty.getTargetValueType();

            GeoPropertyVO geoPropertyVO = new GeoPropertyVO();
            geoPropertyVO.setType(IpeCode.GeoJsonValueType.parseType(geoJsonType));
            geoPropertyVO.setCoordinates(new ArrayList<>(Arrays.asList(null, null)));

            List<HasValue> hasValues = geoProperty.getHasValue();
            if(hasValues != null && geoProperty.getHasValue().size() == 2) {
                for(HasValue hasValue : hasValues) {
                    String targetKey = hasValue.getValue().getTargetKey();
                    Double coordinates = (Double)getAttributeValue(hasValue.getValue(), m2mCin);

                    Integer index = extractIndex(targetKey);
                    if(index != null) {
                        geoPropertyVO.getCoordinates().set(index, coordinates);
                    }
                }
            }

            AttributeVO attributeVO = new AttributeVO();
            attributeVO.setType(IpeCode.AttributeType.GEO_PROPERTY);
            attributeVO.setValue(geoPropertyVO);

            parentMap.put(propertyName, attributeVO);
        }

        ObservedAt observedAt = hasProperty.getObservedAt();
        if(observedAt != null) {
            String dateStr = (String)getAttributeValue(observedAt, m2mCin);
            parentMap.put(IpeCode.DefaultAttributeKey.OBSERVED_AT.getCode(), dateStr);
        }
    }

    private Integer extractIndex(String text) {
        Pattern patt = Pattern.compile("\\[(\\d+)\\]");
        Matcher match = patt.matcher(text);
        while(match.find()){
            return Integer.parseInt(match.group(1));
        }
        return null;
    }

    private Object getAttributeValue(kr.re.keti.sc.ipe.interworking.sementic.vo.sementicdescriptor.Value value, M2mCinVO m2mCin) {

        String targetValue = value.getTargetValue();
        String sourceValueType = extractTextAfterSeparator(value.getSourceValueType(), "#");
        IpeCode.OneM2MSementicDataType dataType = IpeCode.OneM2MSementicDataType.fromString(sourceValueType);
        String sourceValue = value.getSourceValue();
        String sourceKey = value.getSourceKey();

        // 1. targetValue가 존재하는 경우 해당 값을 반환
        if(!ValidateUtil.isEmptyData(targetValue)) {
             return targetValue;
        }

        // 2. sourceValue가 존재하는 경우 해당 값을 반환
        if(!ValidateUtil.isEmptyData(sourceValue)) {
            return parseSourceValue(sourceValue, dataType);
        }

        // 3. sourceKey가 존재하는 경우 해당 값을 retrieve 하여 반환
        if(!ValidateUtil.isEmptyData(sourceKey)) {
            if(sourceKey.startsWith("." + Constants.ONEM2M_LATEST_SUBURI)) {
                String[] hierarchyPath = sourceKey.replace("." + Constants.ONEM2M_LATEST_SUBURI + ".", "").split("\\.");
                if(hierarchyPath != null) {
                    Object object = m2mCin.get(hierarchyPath[0]);
                    if(hierarchyPath.length > 1) {
                        for(int i=1; i<=hierarchyPath.length; i++) {
                            if(object instanceof Map) {
                                object = getObjectMember((Map<String, Object>)object, hierarchyPath[i]);
                            } else {
                                object = parseSourceValue(object, dataType);
                            }
                        }
                    }
                    return object;
                }
            }
        }
        return null;
    }

    private Object getObjectMember(Map<String, Object> map, String key) {
        return map.get(key);
    }

    private Object parseSourceValue(Object value, IpeCode.OneM2MSementicDataType dataType) {
        if(dataType == null) {
            return value;
        } else {
            switch(dataType) {
                case STRING:
                    return String.valueOf(value);
                case FLOAT:
                    return Float.parseFloat(String.valueOf(value));
                case DOUBLE:
                    return Double.parseDouble(String.valueOf(value));
                case DATETIMESTAMP:
                    try {
                        return DateUtil.onem2mDateStrToDataHubDateStr(String.valueOf(value));
                    } catch (ParseException e) {
                        log.error("ParseToSourceType dateFormat parsing error. value={}", value, e);
                    }
                default:
                    return value;
            }
        }
    }

    public SementicDescriptorCacheVO getSementicCacheHasEntityId(SementicDescriptorCacheVO sementicDescriptorCacheVO) {
        if(sementicDescriptorCacheVO != null) {
            String entityId = sementicDescriptorCacheVO.getEntityId();
            if(entityId != null) {
                return sementicDescriptorCacheVO;
            }

            // sementicDescriptor에 entityId가 포함되지 않았기 때문에 상위 container의 descriptor 에서 entityId 검색
            String parentContainerUri = getParentUri(sementicDescriptorCacheVO.getContainerUri());
            return getSementicCacheHasEntityId(sementicInterworkingCacheManager.getSementicDescriptorCacheByContainerUri(parentContainerUri));
        }
        return null;
    }

    private String getParentUri(String uri) {
        String[] hierarchyUriArr = uri.split("/");

        if(hierarchyUriArr != null && hierarchyUriArr.length > 1) {
            StringBuilder parentUri = new StringBuilder();
            for(int i=0; i<hierarchyUriArr.length-1; i++) {
                if(i>0) parentUri.append("/");
                parentUri.append(hierarchyUriArr[i]);
            }
            return parentUri.toString();
        }
        return null;
    }

    private String extractEntityId(SementicDescriptorCacheVO entityIdCacheVO) {
        String entityId = entityIdCacheVO.getEntityId();
        if(entityId != null) {
            Matcher matcherForUpdate = ENTITY_ID_PATTERN.matcher(entityId);

            String variablePath = null;
            if(matcherForUpdate.find()) {
                variablePath = matcherForUpdate.group(1);
            }

            if(variablePath != null) {
                String targetUri = null;
                String variablePathWithoutRelativePath = null;
                if(variablePath.startsWith("./")) {
                    variablePathWithoutRelativePath = variablePath.substring("./".length());
                    targetUri = entityIdCacheVO.getContainerUri();
                } else if(variablePath.startsWith("../")) {
                    variablePathWithoutRelativePath = variablePath.substring("../".length());
                    targetUri = entityIdCacheVO.getContainerUri();
                } else {
                    // TODO : Error - 미지원
                }

                String[] hierarchyResourceArr = variablePathWithoutRelativePath.split("\\.");
                if(hierarchyResourceArr != null && hierarchyResourceArr.length >= 2) {
                    String resourceType = hierarchyResourceArr[0];
                    String resourceValue = hierarchyResourceArr[1];
                    if(resourceType.startsWith(IpeCode.OneM2MResourceType.CONTAINER.getType())) {

                        Onem2mContainerVO onem2mContainerVO = onem2mApiService.retrieveContainer(
                                sementicBaseUri, targetUri, sementicRegistApplicationId);

                        if(onem2mContainerVO != null) {
                            String replaceValue = (String)onem2mContainerVO.getM2mCnt().get(resourceValue);
                            return entityId.replace("{" + variablePath + "}", replaceValue);
                        }

                    } else if(resourceType.startsWith(IpeCode.OneM2MResourceType.CONTAINER_INSTANCE.getType())) {

                        Onem2mContainerInstanceVO onem2mContainerInstanceVO = onem2mApiService.retrieveContainerInstance(
                                sementicBaseUri, targetUri + Constants.ONEM2M_LATEST_SUBURI, sementicRegistApplicationId);

                        if(onem2mContainerInstanceVO != null) {
                            String replaceValue = (String)onem2mContainerInstanceVO.getM2mCin().get(resourceValue);
                            return entityId.replace(variablePath, replaceValue);
                        }
                    }
                }
            }
        }
        return entityId;
    }

    private String extractTextAfterSeparator(String text, String separator) {
        if(text.contains(separator)) {
            return text.split(separator, 2)[1];
        }
        return text;
    }


    private void subscriptionOneM2MResource(List<NgsiLdContainerUriVO> ngsiLdContainerUriVOs) {

        if(ngsiLdContainerUriVOs != null) {
            for(NgsiLdContainerUriVO ngsiLdContainerUriVO : ngsiLdContainerUriVOs) {
                // OneM2M 플랫폼 구독
                onem2mApiService.subscription(sementicBaseUri, ngsiLdContainerUriVO.getContainer(), sementicNotificationReceiveUri, sementicRegistApplicationId);
                subscriptionOneM2MResource(ngsiLdContainerUriVO.getSubContainer());
            }
        }
    }

}

package kr.re.keti.sc.ipe.interworking.label.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;
import kr.re.keti.sc.ipe.common.code.IpeCode;
import kr.re.keti.sc.ipe.datahub.service.DataHubApiService;
import kr.re.keti.sc.ipe.datahub.vo.AttributeVO;
import kr.re.keti.sc.ipe.datahub.vo.NgsiLdCollectRequestVO;
import kr.re.keti.sc.ipe.datahub.vo.GeoPropertyVO;
import kr.re.keti.sc.ipe.interworking.label.vo.ContainerMetadataBaseVO;
import kr.re.keti.sc.ipe.interworking.label.vo.ContainerMetadataVO;
import kr.re.keti.sc.ipe.interworking.label.vo.IwkedMappingRuleVO;
import kr.re.keti.sc.ipe.interworking.label.vo.LabelVO;
import kr.re.keti.sc.ipe.onem2m.mapper.ContainerMetadataMapper;
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
public class LabelInterworkingService {
    @Autowired
    private Onem2mApiService onem2MApiService;
    @Autowired
    private ContainerMetadataMapper containerMetadataMapper;
    @Autowired
    private DataHubApiService dataHubApiService;
    @Autowired
    private LabelInterworkingCacheManager interworkingCacheManager;

    private Pattern objectMemberNamePattern = Pattern.compile("\\[(?<objectMemberName>.*)\\]");

    @Value("${onem2m.label.platform-id}")
    private String labelPlatformId;
    @Value("${onem2m.label.base-uri}")
    private String labelBaseUri;
    @Value("${onem2m.label.context-path}")
    private String labelContextPath;
    @Value("${onem2m.label.regist-application-id}")
    private String labelRegistApplicationId;
    @Value("${onem2m.label.init-interworking-enabled}")
    private Boolean initInterworkingEnabled;
    @Value("${onem2m.label.notification.receive-uri}")
    private String labelNotificationReceiveUri;
    @Value("${onem2m.label.dataset-id}")
    private String datasetId;

    @Value("${datahub.label.base-uri}")
    private String datahubBaseUri;
    @Value("${datahub.label.sub-uri.collect}")
    private String datahubCollectUri;

    @PostConstruct
    public void init() {

        if (!initInterworkingEnabled) {
            return;
        }

        // 1. oneM2M 리소스 Discovery 및 Retrieve
        List<ContainerMetadataVO> containerMetadataVOs = retrieveContainerMetadata();

        // 2. oneM2M 리소스 metadata 저장 및 캐싱
        List<ContainerMetadataBaseVO> containerMetadataBaseVOs = storeContainerMetadatas(containerMetadataVOs);

        // 3. Mapping rule 파싱
        parseMappingRule(containerMetadataBaseVOs);

        // 4. oneM2M 리소스 조회 및 캐싱
        retrieveAndCachingResource(containerMetadataBaseVOs);

        // 5. oneM2M 리소스 구독
        subscriptionOneM2MResource(containerMetadataBaseVOs);
    }

    public void upsetResource(Onem2mNotificationVO notificationVO) {
        List<ContainerMetadataVO> containerMetadataVOs = new ArrayList<>();

        String m2mUri = notificationVO.getM2mSgn().getSur().replace("/subscription", "");

        ContainerMetadataVO containerMetadataVO = onem2MApiService.retrieveContainerMetadata(labelBaseUri, m2mUri, labelPlatformId, labelRegistApplicationId);
        if(containerMetadataVO != null) {
            containerMetadataVO.setM2mCnt(notificationVO.getM2mSgn().getNev().getRep().getM2mCnt());
            containerMetadataVOs.add(containerMetadataVO);
        }

        storeContainerMetadatas(containerMetadataVOs);
    }

    private void retrieveAndCachingResource(List<ContainerMetadataBaseVO> containerMetadataBaseVOs) {
        for (ContainerMetadataBaseVO containerMetadataBaseVO : containerMetadataBaseVOs) {

            LabelVO labelVO = containerMetadataBaseVO.getLabelVO();
            // 1. 수집 대상인 onem2m 리소스 인지 여부 체크
            if (labelVO == null || !labelVO.getIwkedDirection().contains(IpeCode.IwkedDirection.ONEM2M_TO_NGSILD)) {
                continue;
            }

            // 2. 최종값 캐싱을 위한 oneM2M 리소스 조회
            // String subUri = containerMetadataBaseVO.getSubUri();
            Onem2mResourceVO onem2mResourceVO = onem2MApiService.retrieveResourcesByMetadata(labelBaseUri, containerMetadataBaseVO, labelRegistApplicationId);
            if (onem2mResourceVO == null) {
                continue;
            }

            // 3. IwkedMappingRule 기반 oneM2M 리소스 파싱 (Ngsild 적재 데이터 생성)
            NgsiLdCollectRequestVO ngsiLdCollectRequestVO = onem2mResourceToDatahubResource(labelVO, onem2mResourceVO);

            // 4. Ngsild 적재 데이터 캐싱
            interworkingCacheManager.addDatahubEntityCache(labelVO.getIwkedEntityId(), ngsiLdCollectRequestVO);
        }
    }

    public NgsiLdCollectRequestVO onem2mResourceToDatahubResource(LabelVO labelVO, Onem2mResourceVO onem2mResourceVO) {

        String datahubEntityId = labelVO.getIwkedEntityId();
        String datahubEntityType = labelVO.getIwkedEntityType();
        List<IwkedMappingRuleVO> iwkedmappingRuleVOs = labelVO.getIwkedmappingRuleVOs();
        List<String> relatedResource = labelVO.getIwkedRelatedResources();
        List<String> datahubContext = labelVO.getNgsiLdContext();

        NgsiLdCollectRequestVO datahubCollectVO = new NgsiLdCollectRequestVO();
        datahubCollectVO.setId(datahubEntityId);
        datahubCollectVO.setType(datahubEntityType);
        datahubCollectVO.setContext(datahubContext);
        datahubCollectVO.setDatasetId(labelVO.getIwkedDatasetId() != null ? labelVO.getIwkedDatasetId() : datasetId);

        for (IwkedMappingRuleVO iwkedmappingRuleVO : iwkedmappingRuleVOs) {
            String oneM2MResourceId = iwkedmappingRuleVO.getOneM2MResourceId();
            String oneM2MJsonPath = iwkedmappingRuleVO.getOneM2MJsonPath();
            String oneM2MAttributeName = iwkedmappingRuleVO.getOneM2MAttributeName();
            IpeCode.OneM2MDataType oneM2MDataType = iwkedmappingRuleVO.getOneM2MDataType();
            String oneM2MDateFormat = iwkedmappingRuleVO.getOneM2MDateFormat();

            String ngsiLdQueryTermAttributePath = iwkedmappingRuleVO.getNgsiLdQueryTermAttributePath();
            IpeCode.AttributeType ngsiLdAttributeType = iwkedmappingRuleVO.getNgsiLdAttributeType();
            IpeCode.AttributeValueType ngsiLdAttributeDataType = iwkedmappingRuleVO.getNgsiLdAttributeDataType();
            List<IwkedMappingRuleVO.ParentInformation> ngsiLdAttributeParentInformation = iwkedmappingRuleVO.getNgsiLdAttributeParentInformation();
            String ngsiLdDateFormat = iwkedmappingRuleVO.getNgsiLdDateFormat();
            Integer ngsiLdArrayIndex = iwkedmappingRuleVO.getNgsiLdArrayIndex();

            try {
                // objectMember 가 있는 경우 objectMemberName 추출
                String objectMemberName = null;
                Matcher matcher = objectMemberNamePattern.matcher(ngsiLdQueryTermAttributePath);
                if (matcher.find()) {
                    objectMemberName = matcher.group("objectMemberName");
                }

                // objectMember를 제외한 계층구조 attributeNames 추출
                List<String> hierarchyAttrNames = null;
                String attributeName = ngsiLdQueryTermAttributePath;
                if (ngsiLdQueryTermAttributePath.contains("[")) {
                    attributeName = ngsiLdQueryTermAttributePath.substring(0, ngsiLdQueryTermAttributePath.indexOf("["));
                }
                hierarchyAttrNames = Arrays.asList(attributeName.split("\\."));

                // oneM2M resourceId가 세팅되어 있는 경우 retrieve 한 전체 oneM2M 리소스 중 대상 추출 (related Resource 추출)
                Onem2mResourceVO targetResourceVO = getResourceById(onem2mResourceVO, oneM2MResourceId);
                if (targetResourceVO == null) {
                    log.warn("Not found onem2m resource. oneM2MResourceId={}", oneM2MResourceId);
                    continue;
                }

                // onem2m attribute 추출
                M2mCinVO m2mCinVO = targetResourceVO.getM2mCin();
                Object onem2mResourceValue = m2mCinVO.get(oneM2MAttributeName);

                if (onem2mResourceValue == null) {
                    log.warn("Not found onem2mAttriobute. oneM2MAttributeName={}", oneM2MAttributeName);
                    continue;
                }

                Object targetValue = null;
                if (oneM2MJsonPath == null) { // jsonpath 미 사용
                    targetValue = onem2mResourceValue;
                } else { // jsonpath 를 통한 하위 탐색
                    try {
                        targetValue = JsonPath.parse(onem2mResourceValue).read(oneM2MJsonPath);
                    } catch (JsonPathException e) { }
                }
                if (targetValue == null) {
                    log.debug("Not found onem2mResourceValue. onem2mResourceValue={}, oneM2MJsonPath={}", onem2mResourceValue, oneM2MJsonPath);
                    continue;
                }

                AttributeVO currentAttributeVO = null;
                // 현재 Attribute 의 name 계층구조 loop (미 생성된 Parrent Attribute 모두 생성하여 hierarchy 구조 객체 모두 생성 해버림)
                for (int i = 0; i < hierarchyAttrNames.size(); i++) {
                    String attrName = hierarchyAttrNames.get(i);

                    // observedAt 과 unitCode의 경우 attrName이 xxx.observedAt 형태이기 때문에 currentAttributeVO가 생성 된 후 아래 로직을 탐
                    if (attrName.equals(IpeCode.PropertyKey.OBSERVED_AT.getCode()) && currentAttributeVO != null) {
                        String observedAt = (String) toNgsildData(IpeCode.AttributeType.PROPERTY, IpeCode.AttributeValueType.DATE,
                                targetValue, oneM2MDateFormat, ngsiLdDateFormat);
                        currentAttributeVO.setObservedAt(observedAt);
                        break;
                    } else if (attrName.equals(IpeCode.PropertyKey.UNIT_CODE.getCode()) && currentAttributeVO != null) {
                        currentAttributeVO.setUnitCode(String.valueOf(targetValue));
                        break;
                    }

                    // attrName 이 1level인 경우 해당됨
                    if (currentAttributeVO == null) {
                        currentAttributeVO = (AttributeVO) datahubCollectVO.get(attrName);

                        // attributeVO 가 미생성된 경우 생성하여 수집데이터VO의 1레벨에 세팅
                        if (currentAttributeVO == null) { // 아직 생성되어 있지 않은 경우 생성
                            currentAttributeVO = new AttributeVO();
                            currentAttributeVO.setType(getNgsiLdAttributeTypeByName(iwkedmappingRuleVOs, attrName));
                            datahubCollectVO.put(attrName, currentAttributeVO);
                        }

                    // attrName 이 계층구조이며 1레벨보다 높은 경우 해당됨
                    } else {
                        AttributeVO childAttributeVO = (AttributeVO) currentAttributeVO.get(attrName);

                        // childAttributeVO 가 미 생성된 경우 생성하여 하위 Attribute 로 세팅함
                        if (childAttributeVO == null) {
                            childAttributeVO = new AttributeVO();
                            childAttributeVO.setType(getNgsiLdAttributeTypeByName(iwkedmappingRuleVOs, attrName));
                            currentAttributeVO.put(attrName, childAttributeVO);
                        }
                        currentAttributeVO = childAttributeVO;
                    }

                    // 계층구조인 attrName이 가장 마지막 index인 경우 : Attribute 값 설정
                    if (i == hierarchyAttrNames.size() - 1) {
                        // objectMember를 가지지 않은 경우
                        if (objectMemberName == null) {

                            // Geo Property 인 경우
                            if(ngsiLdAttributeType == IpeCode.AttributeType.GEO_PROPERTY) {

                                GeoPropertyVO geoPropertyVO = (GeoPropertyVO) currentAttributeVO.getValue();

                                if(geoPropertyVO == null) {
                                    geoPropertyVO = new GeoPropertyVO();
                                    geoPropertyVO.setType(IpeCode.GeoJsonValueType.POINT);
                                    currentAttributeVO.setValue(geoPropertyVO);
                                }

                                if(ngsiLdArrayIndex == null || ngsiLdArrayIndex >= 2) {
                                    // TODO: error. point GeoProperty 설정할 수 없음
                                }

                                Object attributeValue = toNgsildData(ngsiLdAttributeType, ngsiLdAttributeDataType, targetValue, oneM2MDateFormat, ngsiLdDateFormat);
                                geoPropertyVO.getCoordinates().set(ngsiLdArrayIndex, (Double)attributeValue);

                            // Property 또는 Relationship 인 경우
                            } else {
                                // on2M2M 및 ngsiLd 모두 Object Type인 경우 full value copy
                                if (ngsiLdAttributeDataType == IpeCode.AttributeValueType.OBJECT && oneM2MDataType == IpeCode.OneM2MDataType.OBJECT) {

                                    currentAttributeVO.setValue(targetValue);

                                // on2M2M 및 ngsiLd 모두 ArrayObject Type인 경우 full value copy
                                } else if (ngsiLdAttributeDataType == IpeCode.AttributeValueType.ARRAY_OBJECT && oneM2MDataType == IpeCode.OneM2MDataType.ARRAY_OBJECT) {

                                    currentAttributeVO.setValue(targetValue);

                                } else if (ngsiLdAttributeDataType == IpeCode.AttributeValueType.ARRAY_STRING
                                        || ngsiLdAttributeDataType == IpeCode.AttributeValueType.ARRAY_INTEGER
                                        || ngsiLdAttributeDataType == IpeCode.AttributeValueType.ARRAY_DOUBLE
                                        || ngsiLdAttributeDataType == IpeCode.AttributeValueType.ARRAY_BOOLEAN) {

                                    if(currentAttributeVO.getValue() == null) {
                                        currentAttributeVO.setValue(new ArrayList<Object>());
                                    }

                                    // array의 index 자리에 값 설정
                                    if(ngsiLdArrayIndex != null) {
                                        IpeCode.AttributeValueType primitiveValueType = getPrimitiveTypeByFromArrayType(ngsiLdAttributeDataType);
                                        Object attributeValue = toNgsildData(ngsiLdAttributeType, primitiveValueType, targetValue, oneM2MDateFormat, ngsiLdDateFormat);

                                        List<Object> arrayAttribute = (List<Object>)currentAttributeVO.getValue();
                                        // 값을 입력할 대상 list의 크기가 작을 경우 null 로 해당 크기만큼 값을 채움
                                        if(arrayAttribute.size() < ngsiLdArrayIndex+1) {
                                            int diffSize = ngsiLdArrayIndex - arrayAttribute.size();
                                            for(int z=0; z<diffSize; z++) {
                                                arrayAttribute.add(null);
                                            }
                                        }
                                        // 설정한 index위치에 값 입력
                                        ((List<Object>)currentAttributeVO.getValue()).set(ngsiLdArrayIndex, attributeValue);

                                    } else {
                                        Object attributeValue = toNgsildData(ngsiLdAttributeType, ngsiLdAttributeDataType, targetValue, oneM2MDateFormat, ngsiLdDateFormat);
                                        currentAttributeVO.setValue(attributeValue);
                                    }

                                // Primitive Type인 경우
                                } else {
                                    Object attributeValue = toNgsildData(ngsiLdAttributeType, ngsiLdAttributeDataType, targetValue, oneM2MDateFormat, ngsiLdDateFormat);
                                    currentAttributeVO.setValue(attributeValue);
                                }
                            }

                        // attrName에 objectMember가 포함된 경우. ex) address[addressRegion]
                        } else {

                            // objectMember 들을 put하기 위한 객체 생성 (Map or List<Map>)
                            if (currentAttributeVO.getValue() == null) {

                                IpeCode.AttributeValueType objectMemberValueType = getNgsiLdAttributeValueTypeByName(iwkedmappingRuleVOs, attrName);

                                // objectMember가 arrayObject인 경우
                                if(objectMemberValueType == IpeCode.AttributeValueType.ARRAY_OBJECT) {
                                    currentAttributeVO.setValue(new ArrayList<Map<String, Object>>());

                                // 그 외 모두 object 인 것으로 처리 (geoJson 도 포함)
                                } else {
                                    currentAttributeVO.setValue(new HashMap<String, Object>());
                                }
                            }

                            // oneM2M data를 ngsiLd data 값으로 변환
                            Object attributeValue = toNgsildData(ngsiLdAttributeType, ngsiLdAttributeDataType, targetValue, oneM2MDateFormat, ngsiLdDateFormat);

                            if(currentAttributeVO.getValue() instanceof HashMap) {
                                Map<String, Object> objectMemberMap = (Map<String, Object>) currentAttributeVO.getValue();
                                objectMemberMap.put(objectMemberName, attributeValue);

                            } else if (currentAttributeVO.getValue() instanceof ArrayList) {
                                List<Map<String, Object>> objectMemberList = (List<Map<String, Object>>) currentAttributeVO.getValue();
                            }
                        }
                    }
                }
            } catch (ParseException e) {
                log.warn("onem2mResourceToDatahubResource error. oneM2MResourceId={}, oneM2MJsonPath={}, oneM2MAttributeName={}, oneM2MDataType={}, ngsiLdQueryTermAttributePath={}, ngsiLdAttributeType={}, ngsiLdAttributeDataType={}", oneM2MResourceId, oneM2MJsonPath, oneM2MAttributeName, oneM2MDataType, ngsiLdQueryTermAttributePath, ngsiLdAttributeType, ngsiLdAttributeDataType, e);
            } catch (Exception e) {
                log.warn("onem2mResourceToDatahubResource error. oneM2MResourceId={}, oneM2MJsonPath={}, oneM2MAttributeName={}, oneM2MDataType={}, ngsiLdQueryTermAttributePath={}, ngsiLdAttributeType={}, ngsiLdAttributeDataType={}", oneM2MResourceId, oneM2MJsonPath, oneM2MAttributeName, oneM2MDataType, ngsiLdQueryTermAttributePath, ngsiLdAttributeType, ngsiLdAttributeDataType, e);
            }
        }

        log.info("OneM2M Resource To Datahub Resource. \n\toneM2M ResourceId={}, \n\tdatahub ResourceId={}, \n\toneM2M Resource={}, \n\tdatahub Resource={}", onem2mResourceVO.getOnem2mResourceId(), labelVO.getIwkedEntityId(), onem2mResourceVO.getM2mCin().toString(), datahubCollectVO.toString());

        return datahubCollectVO;
    }

    private IpeCode.AttributeValueType getPrimitiveTypeByFromArrayType(IpeCode.AttributeValueType attributeValueType) {
        if(attributeValueType == IpeCode.AttributeValueType.ARRAY_STRING) {
            return IpeCode.AttributeValueType.STRING;
        } else if(attributeValueType == IpeCode.AttributeValueType.ARRAY_INTEGER) {
            return IpeCode.AttributeValueType.INTEGER;
        } else if(attributeValueType == IpeCode.AttributeValueType.ARRAY_DOUBLE) {
            return IpeCode.AttributeValueType.DOUBLE;
        } else if(attributeValueType == IpeCode.AttributeValueType.ARRAY_BOOLEAN) {
            return IpeCode.AttributeValueType.BOOLEAN;
        }
        return null;
    }

    private IpeCode.AttributeType getNgsiLdAttributeTypeByName(List<IwkedMappingRuleVO> iwkedmappingRuleVOs, String attrName) {
        for(IwkedMappingRuleVO iwkedmappingRuleVO : iwkedmappingRuleVOs) {

            String ngsiLdQueryTermAttributePath = iwkedmappingRuleVO.getNgsiLdQueryTermAttributePath();
            List<IwkedMappingRuleVO.ParentInformation> ngsiLdAttributeParentInformation = iwkedmappingRuleVO.getNgsiLdAttributeParentInformation();

            if(ngsiLdQueryTermAttributePath != null && ngsiLdQueryTermAttributePath.equals(attrName)) {
                return iwkedmappingRuleVO.getNgsiLdAttributeType();
            }
            if(ngsiLdAttributeParentInformation != null) {
                for(IwkedMappingRuleVO.ParentInformation parentInformation : ngsiLdAttributeParentInformation) {
                    if(parentInformation.getNgsiLdQueryTermAttributePath().equals(attrName)) {
                        return parentInformation.getNgsiLdAttributeType();
                    }
                }
            }
        }
        return null;
    }

    private IpeCode.AttributeValueType getNgsiLdAttributeValueTypeByName(List<IwkedMappingRuleVO> iwkedmappingRuleVOs, String attrName) {
        for(IwkedMappingRuleVO iwkedmappingRuleVO : iwkedmappingRuleVOs) {

            String ngsiLdQueryTermAttributePath = iwkedmappingRuleVO.getNgsiLdQueryTermAttributePath();
            List<IwkedMappingRuleVO.ParentInformation> ngsiLdAttributeParentInformation = iwkedmappingRuleVO.getNgsiLdAttributeParentInformation();

            if(ngsiLdQueryTermAttributePath != null && ngsiLdQueryTermAttributePath.equals(attrName)) {
                return iwkedmappingRuleVO.getNgsiLdAttributeDataType();
            }

            if(ngsiLdAttributeParentInformation != null) {
                for(IwkedMappingRuleVO.ParentInformation parentInformation : ngsiLdAttributeParentInformation) {
                    if(parentInformation.getNgsiLdQueryTermAttributePath().equals(attrName)) {
                        return parentInformation.getNgsiLdAttributeDataType();
                    }
                }
            }
        }
        return null;
    }


    private Onem2mResourceVO getResourceById(Onem2mResourceVO onem2mResourceVO, String oneM2MResourceId) {

        if(ValidateUtil.isEmptyData(oneM2MResourceId)
            || onem2mResourceVO.getOnem2mResourceId().equals(oneM2MResourceId)) {

            return onem2mResourceVO;

        } else if(onem2mResourceVO.getRelatedResources() != null){
            for(Onem2mResourceVO relatedResourceVO : onem2mResourceVO.getRelatedResources()) {
                if(relatedResourceVO.getOnem2mResourceId().equals(oneM2MResourceId)) {
                    return relatedResourceVO;
                }
            }
        }
        return null;
    }

    public List<ContainerMetadataVO> retrieveContainerMetadata() {
        UriDiscoveryVO labelDiscoveryVO = onem2MApiService.discoveryNgsiLdLabel(labelBaseUri, labelContextPath, labelRegistApplicationId);
        if(labelDiscoveryVO == null) {
            return null;
        }

        List<String> m2mUris = labelDiscoveryVO.getM2mUril();
        if(ValidateUtil.isEmptyData(m2mUris)) {
            return null;
        }

        List<ContainerMetadataVO> containerMetadataVOs = new ArrayList<>();

        for(String m2mUri : m2mUris) {
            ContainerMetadataVO containerMetadataVO = onem2MApiService.retrieveContainerMetadata(labelBaseUri, m2mUri, labelPlatformId, labelRegistApplicationId);
            if(containerMetadataVO != null && containerMetadataVO.getM2mCnt() != null) {
                containerMetadataVOs.add(containerMetadataVO);
            }
        }

        return containerMetadataVOs;
    }

    public List<ContainerMetadataBaseVO> storeContainerMetadatas(List<ContainerMetadataVO> containerMetadataVOs) {

        List<ContainerMetadataBaseVO> containerMetadataBaseVOs = new ArrayList<>();

        for (ContainerMetadataVO containerMetadataVO : containerMetadataVOs) {
            ContainerMetadataBaseVO conditionVO = new ContainerMetadataBaseVO();
            conditionVO.setId(containerMetadataVO.getOneM2MPlatformId());
            conditionVO.setSubUri(containerMetadataVO.getSubUri());

            ContainerMetadataBaseVO retrieveVO = containerMetadataMapper.retrieveMappingRule(conditionVO);

            ContainerMetadataBaseVO containerMetadataBaseVO = toDaoVO(containerMetadataVO);

            // 미 존재 시 신규 생성
            if (retrieveVO == null) {
                containerMetadataMapper.createContainerMetadata(containerMetadataBaseVO);
                interworkingCacheManager.addContainerMetadataCache(containerMetadataBaseVO);

            // 기 존재 시 last modified 비교하여 갱신된 경우만 업데이트
            } else if (retrieveVO.getLt() != null && retrieveVO.getLt().compareTo(containerMetadataVO.getM2mCnt().getLt()) < 0) {
                containerMetadataMapper.updateContainerMetadata(containerMetadataBaseVO);
                interworkingCacheManager.addContainerMetadataCache(containerMetadataBaseVO);

            // 캐쉬만 갱신
            } else {
                interworkingCacheManager.addContainerMetadataCache(containerMetadataBaseVO);
            }
            containerMetadataBaseVOs.add(containerMetadataBaseVO);
        }
        return containerMetadataBaseVOs;
    }

    private ContainerMetadataBaseVO toDaoVO(ContainerMetadataVO containerMetadataVO) {

        ContainerMetadataVO.MetadataVO metadataVO = containerMetadataVO.getM2mCnt();
        ContainerMetadataBaseVO containerMetadataBaseVO = new ContainerMetadataBaseVO();
        containerMetadataBaseVO.setId(containerMetadataVO.getOneM2MPlatformId());
        containerMetadataBaseVO.setSubUri(containerMetadataVO.getSubUri());
        containerMetadataBaseVO.setPi(metadataVO.getPi());
        containerMetadataBaseVO.setRi(metadataVO.getRi());
        containerMetadataBaseVO.setTy(metadataVO.getTy());
        containerMetadataBaseVO.setCt(metadataVO.getCt());
        containerMetadataBaseVO.setSt(metadataVO.getSt());
        containerMetadataBaseVO.setRn(metadataVO.getRn());
        containerMetadataBaseVO.setLt(metadataVO.getLt());
        containerMetadataBaseVO.setEt(metadataVO.getEt());
        containerMetadataBaseVO.setLbl(metadataVO.getLbl());
        containerMetadataBaseVO.setCr(metadataVO.getCr());
        containerMetadataBaseVO.setMni(metadataVO.getMni());
        containerMetadataBaseVO.setMbs(metadataVO.getMbs());
        containerMetadataBaseVO.setMia(metadataVO.getMia());
        containerMetadataBaseVO.setCni(metadataVO.getCni());
        containerMetadataBaseVO.setCbs(metadataVO.getCbs());
        return containerMetadataBaseVO;
    }

    private void parseMappingRule(List<ContainerMetadataBaseVO> containerMetadataBaseVOs) {

        for(ContainerMetadataBaseVO containerMetadataBaseVO : containerMetadataBaseVOs) {
            // 1. label 정보 파싱
            List<String> label = containerMetadataBaseVO.getLbl();
            try {
                LabelVO lavelVO = new LabelVO(label);
                if(lavelVO.getIwkedEntityId() == null) {
                    lavelVO.setIwkedEntityId("urn:datahub:" + labelPlatformId + ":" + containerMetadataBaseVO.getRi());
                }
                containerMetadataBaseVO.setLabelVO(lavelVO);
            } catch (JsonProcessingException e) {
                log.warn("oneM2M lbl parsing error. label={}", label, e);
                continue;
            }
        }
    }

    private void subscriptionOneM2MResource(List<ContainerMetadataBaseVO> containerMetadataBaseVOs) {

        for(ContainerMetadataBaseVO containerMetadataBaseVO : containerMetadataBaseVOs) {

            List<IpeCode.IwkedDirection> iwkedDirection = containerMetadataBaseVO.getLabelVO().getIwkedDirection();

            // OneM2M 플랫폼 구독
            if(iwkedDirection.contains(IpeCode.IwkedDirection.ONEM2M_TO_NGSILD)) {
                onem2MApiService.subscription(labelBaseUri, containerMetadataBaseVO.getSubUri(), labelNotificationReceiveUri, labelRegistApplicationId);
                List<String> relatedResourceIds = containerMetadataBaseVO.getLabelVO().getIwkedRelatedResources();
                if(relatedResourceIds != null) {
                    for(String relatedResourceId : relatedResourceIds) {
                        onem2MApiService.subscription(labelBaseUri, relatedResourceId, labelNotificationReceiveUri, labelRegistApplicationId);
                    }
                }
            }
        }
    }

    private Object toNgsildData(IpeCode.AttributeType ngsiLdAttributeType, IpeCode.AttributeValueType ngsiLdAttributeDataType,
                                Object onem2mResourceValue, String oneM2MDateFormat, String ngsiLdDateFormat) throws ParseException {

        if(ngsiLdAttributeType == IpeCode.AttributeType.PROPERTY) {
            switch (ngsiLdAttributeDataType) {
                case STRING:
                    return String.valueOf(onem2mResourceValue);
                case INTEGER:
                    return Integer.parseInt(String.valueOf(onem2mResourceValue));
                case DOUBLE:
                    return Double.parseDouble(String.valueOf(onem2mResourceValue));
                case DATE:
                    // oneM2M Date String을 ngsiLd Date Format String 으로 컨버팅
                    return DateUtil.dateToStr(DateUtil.strToDate(String.valueOf(onem2mResourceValue), oneM2MDateFormat), ngsiLdDateFormat);
                case BOOLEAN:
                    return Boolean.parseBoolean(String.valueOf(onem2mResourceValue));
                case ARRAY_STRING:
                    List<String> strList = new ArrayList<>();
                    List<Object> originList = (List<Object>)onem2mResourceValue;
                    for(Object obj : originList) {
                        strList.add(String.valueOf(obj));
                    }
                    return strList;
                case ARRAY_INTEGER:
                    List<Integer> intList = new ArrayList<>();
                    originList = (List<Object>)onem2mResourceValue;
                    for(Object obj : originList) {
                        intList.add(Integer.parseInt(String.valueOf(obj)));
                    }
                    return intList;
                case ARRAY_DOUBLE:
                    List<Double> doubleList = new ArrayList<>();
                    originList = (List<Object>)onem2mResourceValue;
                    for(Object obj : originList) {
                        doubleList.add(Double.parseDouble(String.valueOf(obj)));
                    }
                    return doubleList;
                case ARRAY_BOOLEAN:
                    List<Boolean> booleanList = new ArrayList<>();
                    originList = (List<Object>)onem2mResourceValue;
                    for(Object obj : originList) {
                        booleanList.add(Boolean.parseBoolean(String.valueOf(obj)));
                    }
                    return booleanList;
                default:
                    return null;
            }

        } else if(ngsiLdAttributeType == IpeCode.AttributeType.GEO_PROPERTY) {

            switch (ngsiLdAttributeDataType) {
                case GEO_JSON:
                    return Double.parseDouble(String.valueOf(onem2mResourceValue));
                default:
                    return null;
            }

        } else if(ngsiLdAttributeType == IpeCode.AttributeType.RELATIONSHIP) {

            return String.valueOf(onem2mResourceValue);

        } else {
            // TODO: error
        }
        return null;
    }

    public void convertAndCollect(Onem2mResourceVO onem2mResourceVO) {
        // 1. Notification oneM2M Resource와 관련있는 모든 mapping rule 조회
        List<ContainerMetadataBaseVO> containerMetadataBaseVOs = interworkingCacheManager.getContainerMetadataByResourceId(onem2mResourceVO.getOnem2mResourceId(), IpeCode.IwkedDirection.ONEM2M_TO_NGSILD);
        if (containerMetadataBaseVOs != null) {
            // 2. IwkedMappingRule 기반 oneM2M 리소스 파싱 및 기존 Cache 데이터와 merge
            for (ContainerMetadataBaseVO containerMetadataBaseVO : containerMetadataBaseVOs) {
                LabelVO labelVO = containerMetadataBaseVO.getLabelVO();
                // onem2m resource -> datahub(NGSI-LD) 데이터로 컨버팅
                NgsiLdCollectRequestVO ngsiLdCollectRequestVO = onem2mResourceToDatahubResource(labelVO, onem2mResourceVO);
                // cache datahub resource 조회
                NgsiLdCollectRequestVO cacheCollectVO = interworkingCacheManager.getDatahubEntityCache(ngsiLdCollectRequestVO.getId());
                // cache datahub resource에 수집 데이터 merge
                mergeDatahubResource(cacheCollectVO, ngsiLdCollectRequestVO);
                interworkingCacheManager.addDatahubEntityCache(ngsiLdCollectRequestVO.getId(), cacheCollectVO);
                // 3. send Notification to datahub
                dataHubApiService.entityOperations(datahubBaseUri, datahubCollectUri, "", cacheCollectVO);
            }
        }
    }

    private void mergeDatahubResource(NgsiLdCollectRequestVO cacheIngestVO, NgsiLdCollectRequestVO collectIngestVO) {
        if (cacheIngestVO == null) {
            cacheIngestVO = collectIngestVO;
            return;
        }

        if (cacheIngestVO != null && collectIngestVO != null) {
            cacheIngestVO.putAll(collectIngestVO);
        }
    }
}

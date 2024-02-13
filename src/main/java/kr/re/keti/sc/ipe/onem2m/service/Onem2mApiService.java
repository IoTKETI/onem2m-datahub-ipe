package kr.re.keti.sc.ipe.onem2m.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.re.keti.sc.ipe.common.code.Constants;
import kr.re.keti.sc.ipe.common.code.IpeCode;
import kr.re.keti.sc.ipe.common.exception.BadRequestException;
import kr.re.keti.sc.ipe.interworking.label.vo.ContainerMetadataBaseVO;
import kr.re.keti.sc.ipe.interworking.label.vo.ContainerMetadataVO;
import kr.re.keti.sc.ipe.onem2m.vo.*;
import kr.re.keti.sc.ipe.util.ValidateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class Onem2mApiService {
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    // authorization 요구사항 검증을 위해 임시 개발
    @Value("${onem2m.authorization.token}")
    private String onem2mAuthorizationToken;

    // for label
    @Value("${onem2m.label.discovery-lbl}")
    private String labelDiscoveryLbl;

    // for sementic
    @Value("${onem2m.sementic.ontology-reference}")
    private String sementicOntologyReference;
    @Value("${onem2m.sementic.query-indicator}")
    private String sementicQueryIndicator;
    @Value("${onem2m.sementic.filter}")
    private String sementicFilter;
    @Value("${onem2m.sementic.notification.receive-uri}")
    private String sementicNotificationReceiveUri;

    public UriDiscoveryVO discoveryNgsiLdLabel(String baseUri, String contextPath, String registApplicationId) {

        String requestUri = generateRequestUri(baseUri, contextPath);
        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(requestUri)
                .queryParam(IpeCode.Onem2mQueryParamKey.FILTER_USAGE.getCode(), IpeCode.OneM2MFilterUsage.DISCOVERY.getCode())
                .queryParam(IpeCode.Onem2mQueryParamKey.LABEL.getCode(), labelDiscoveryLbl)
                .build();

        return requestExchange(uriComponents.toUri(), HttpMethod.GET, null, UriDiscoveryVO.class, Arrays.asList(HttpStatus.OK), registApplicationId);
    }

    public UriDiscoveryVO discoveryNgsiLdSementic(String baseUri, String contextPath, String registApplicationId) {

        String requestUri = generateRequestUri(baseUri, contextPath);
        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(requestUri)
                .queryParam(IpeCode.Onem2mQueryParamKey.FILTER_USAGE.getCode(), IpeCode.OneM2MFilterUsage.DISCOVERY.getCode())
                .queryParam(IpeCode.Onem2mQueryParamKey.SEMENTIC_QUERY_INDICATOR.getCode(), sementicQueryIndicator)
                .queryParam(IpeCode.Onem2mQueryParamKey.SEMENTIC_FILTER.getCode(), sementicFilter)
                .build();

        return requestExchange(uriComponents.toUri(), HttpMethod.GET, null, UriDiscoveryVO.class, Arrays.asList(HttpStatus.OK), registApplicationId);
    }

    public UriDiscoveryVO discoveryContainer(String baseUri, String m2mUri, String registApplicationId) {

        String requestUri = generateRequestUri(baseUri, m2mUri);
        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(requestUri)
                .queryParam(IpeCode.Onem2mQueryParamKey.FILTER_USAGE.getCode(), IpeCode.OneM2MFilterUsage.DISCOVERY.getCode())
                .queryParam(IpeCode.Onem2mQueryParamKey.RESOURCE_TYPE.getCode(), IpeCode.OneM2MResourceType.CONTAINER.getCode())
                .build();

        return requestExchange(uriComponents.toUri(), HttpMethod.GET, null, UriDiscoveryVO.class, Arrays.asList(HttpStatus.OK), registApplicationId);
    }

    public UriDiscoveryVO discoverySementicDescriptor(String baseUri, String m2mUri, String registApplicationId) {

        String requestUri = generateRequestUri(baseUri, m2mUri);
        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(requestUri)
                .queryParam(IpeCode.Onem2mQueryParamKey.FILTER_USAGE.getCode(), IpeCode.OneM2MFilterUsage.DISCOVERY.getCode())
                .queryParam(IpeCode.Onem2mQueryParamKey.RESOURCE_TYPE.getCode(), IpeCode.OneM2MResourceType.SEMENTIC_DESCRIPTOR.getCode())
                .queryParam(IpeCode.Onem2mQueryParamKey.ONTOLOGY_REFERENCE.getCode(), sementicOntologyReference)
                .build();

        return requestExchange(uriComponents.toUri(), HttpMethod.GET, null, UriDiscoveryVO.class, Arrays.asList(HttpStatus.OK), registApplicationId);
    }

    public String discoveryLastSementicDescriptorUri(String baseUri, String m2mUri, String registApplicationId) {

        String requestUri = generateRequestUri(baseUri, m2mUri);
        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(requestUri)
                .queryParam(IpeCode.Onem2mQueryParamKey.FILTER_USAGE.getCode(), IpeCode.OneM2MFilterUsage.DISCOVERY.getCode())
                .queryParam(IpeCode.Onem2mQueryParamKey.RESOURCE_TYPE.getCode(), IpeCode.OneM2MResourceType.SEMENTIC_DESCRIPTOR.getCode())
                .queryParam(IpeCode.Onem2mQueryParamKey.ONTOLOGY_REFERENCE.getCode(), sementicOntologyReference)
                .build();

        UriDiscoveryVO labelDiscoveryVO =  requestExchange(uriComponents.toUri(), HttpMethod.GET, null, UriDiscoveryVO.class,
                Arrays.asList(HttpStatus.OK), registApplicationId);

        Onem2mSementicDescriptorVO lastCreatedDescriptor = null;
        String lastCreatedDescriptorUri = null;
        if(labelDiscoveryVO != null && labelDiscoveryVO.getM2mUril() != null && labelDiscoveryVO.getM2mUril().size() > 0) {
            StringBuilder logBuilder = new StringBuilder("Discovery Latest SementicDescriptor Uri. Container Uri=").append(m2mUri);
            for(String smdUri : labelDiscoveryVO.getM2mUril()) {

                // 현재 Container의 SementicDescriptor가 아닌 경우 skip (하위 Container의 smd 까지 조회되므로 하위 smd는 skip)
                if(smdUri.length() <= m2mUri.length() || smdUri.substring(m2mUri.length()+1).indexOf("/") > -1) {
                    continue;
                }

                Onem2mSementicDescriptorVO onem2mSementicDescriptorVO = retrieveSementicDescriptor(baseUri, smdUri, registApplicationId);
                if(onem2mSementicDescriptorVO != null && onem2mSementicDescriptorVO.getM2mSmd() != null) {

                    logBuilder.append("\n\tDiscovery smdUri=").append(smdUri)
                              .append(", lt=").append((String)onem2mSementicDescriptorVO.getM2mSmd().get(IpeCode.OneM2mSmdKey.LT.getCode()));

                    if(lastCreatedDescriptor == null) {
                        lastCreatedDescriptor = onem2mSementicDescriptorVO;
                        lastCreatedDescriptorUri = smdUri;
                    } else {
                        if(smdUri.length() > m2mUri.length()) {
                            String lt = (String)onem2mSementicDescriptorVO.getM2mSmd().get(IpeCode.OneM2mSmdKey.LT.getCode());
                            String currentLt = (String)lastCreatedDescriptor.getM2mSmd().get(IpeCode.OneM2mSmdKey.LT.getCode());
                            if(currentLt.compareTo(lt) < 0) {
                                lastCreatedDescriptor = onem2mSementicDescriptorVO;
                                lastCreatedDescriptorUri = smdUri;
                            }
                        }
                    }
                }
            }
            logBuilder.append("\nLatest SementicDescriptorUri=").append(lastCreatedDescriptorUri);
            log.info(logBuilder.toString());
        }
        return lastCreatedDescriptorUri;
    }

    public ContainerMetadataVO retrieveContainerMetadata(String baseUri, String m2mUri, String onem2mPlatformId, String registApplicationId) {

        String requestUri = generateRequestUri(baseUri, m2mUri);
        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(requestUri).build();

        ContainerMetadataVO containerMetadataVO = requestExchange(uriComponents.toUri(), HttpMethod.GET, null, ContainerMetadataVO.class, Arrays.asList(HttpStatus.OK), registApplicationId);
        containerMetadataVO.setOneM2MPlatformId(onem2mPlatformId);
        containerMetadataVO.setSubUri(m2mUri);
        return containerMetadataVO;
    }

    public Onem2mResourceVO retrieveResourcesByMetadata(String baseUri, ContainerMetadataBaseVO containerMetadataBaseVO, String registApplicationId) {

        // 1. retrieve resource
        String latestUri = generateRequestUri(baseUri, containerMetadataBaseVO.getSubUri() + Constants.ONEM2M_LATEST_SUBURI);
        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(latestUri).build();

        Onem2mContainerInstanceVO representationVO = requestExchange(uriComponents.toUri(), HttpMethod.GET, null, Onem2mContainerInstanceVO.class, Arrays.asList(HttpStatus.OK), registApplicationId);
        if (representationVO == null || representationVO.getM2mCin() == null) {
            return null;
        }

        Onem2mResourceVO targetResourceVO = new Onem2mResourceVO();
        targetResourceVO.setOnem2mResourceId(containerMetadataBaseVO.getSubUri());
        targetResourceVO.setM2mCin(representationVO.getM2mCin());

        // 2. retrieve related resource
        List<String> relatedResourceSubUris = containerMetadataBaseVO.getLabelVO().getIwkedRelatedResources();
        if (relatedResourceSubUris != null && relatedResourceSubUris.size() > 0) {
            targetResourceVO.setRelatedResources(new ArrayList<>(relatedResourceSubUris.size()));

            for (String relatedResourceSubUri : relatedResourceSubUris) {
                latestUri = generateRequestUri(baseUri, relatedResourceSubUri + Constants.ONEM2M_LATEST_SUBURI);
                uriComponents = UriComponentsBuilder.fromHttpUrl(latestUri).build();
                Onem2mContainerInstanceVO relatedOnem2mRepresentationVO = requestExchange(uriComponents.toUri(), HttpMethod.GET, null, Onem2mContainerInstanceVO.class, Arrays.asList(HttpStatus.OK), registApplicationId);
                if (relatedOnem2mRepresentationVO == null || relatedOnem2mRepresentationVO.getM2mCin() == null) {
                    // TODO: error?
                }
                Onem2mResourceVO relatedResourceVO = new Onem2mResourceVO();
                relatedResourceVO.setOnem2mResourceId(relatedResourceSubUri);
                relatedResourceVO.setM2mCin(relatedOnem2mRepresentationVO.getM2mCin());
                targetResourceVO.getRelatedResources().add(relatedResourceVO);
            }
        }
        return targetResourceVO;
    }

    public Onem2mContainerVO retrieveContainer(String baseUri, String subUri, String registApplicationId) {
        String requestUri = generateRequestUri(baseUri, subUri);
        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(requestUri).build();

        return requestExchange(uriComponents.toUri(), HttpMethod.GET, null, Onem2mContainerVO.class,
                Arrays.asList(HttpStatus.OK), registApplicationId);
    }

    public Onem2mContainerInstanceVO retrieveContainerInstance(String baseUri, String subUri, String registApplicationId) {
        String requestUri = generateRequestUri(baseUri, subUri);
        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(requestUri).build();
        return requestExchange(uriComponents.toUri(), HttpMethod.GET, null, Onem2mContainerInstanceVO.class,
                Arrays.asList(HttpStatus.OK), registApplicationId);
    }

    public Onem2mSementicDescriptorVO retrieveSementicDescriptor(String baseUri, String subUri, String registApplicationId) {
        String requestUri = generateRequestUri(baseUri, subUri);
        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(requestUri).build();
        return requestExchange(uriComponents.toUri(), HttpMethod.GET, null, Onem2mSementicDescriptorVO.class,
                Arrays.asList(HttpStatus.OK), registApplicationId);
    }

    public Onem2mSubscriptionVO subscription(String baseUri, String subUri, String notificatilReceiveUri, String registApplicationId) {

        // 1. create header
        String contentType = MediaType.APPLICATION_JSON_VALUE + ";ty=" + IpeCode.OneM2MResourceType.SUBSCRIPTION.getCode();
        MultiValueMap<String, String> headerMap = generateHeader(contentType, registApplicationId);

        // 2. create body
        Onem2mSubscriptionVO subscriptionVO = new Onem2mSubscriptionVO();
        M2mSubVO m2mSub = new M2mSubVO();
        m2mSub.setRn(Constants.subscriptionResourceName);
        m2mSub.setNu(Arrays.asList(notificatilReceiveUri));
        M2mSubVO.EncVO encVO = new M2mSubVO.EncVO();
        encVO.setNet(Arrays.asList(1L, 3L));
        m2mSub.setEnc(encVO);
        subscriptionVO.setM2mSub(m2mSub);

        // 3. request subscription
        String requestUri = generateRequestUri(baseUri, subUri);
        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(requestUri).build();
        return requestExchange(uriComponents.toUri(), HttpMethod.POST, subscriptionVO, Onem2mSubscriptionVO.class,
                Arrays.asList(HttpStatus.CREATED, HttpStatus.CONFLICT), headerMap, registApplicationId);
    }


    private <T1, T2> T2 requestExchange(URI requestUri, HttpMethod httpMethod, T1 bodyObj, Class<T2> returnType, List<HttpStatus> suceessStatus, String onem2mRegistApplicationId) {
        return requestExchange(requestUri, httpMethod, bodyObj, returnType, suceessStatus, null, onem2mRegistApplicationId);
    }

    private <T1, T2> T2 requestExchange(URI requestUri, HttpMethod httpMethod, T1 bodyObj, Class<T2> returnType, List<HttpStatus> suceessStatus, MultiValueMap<String, String> headerMap, String onem2mRegistApplicationId) {

        // 1. create request entity
        if(headerMap == null) {
            headerMap = generateDefaultHeader(onem2mRegistApplicationId);
        }

        RequestEntity<T1> requestEntity = null;
        if(bodyObj == null) {
            requestEntity = new RequestEntity<>(headerMap, httpMethod, requestUri);
        } else {
            requestEntity = new RequestEntity<>(bodyObj, headerMap, httpMethod, requestUri);
        }

        log.info("OneM2M API request. requestUri={}, method={}, header={}, body={}", requestUri, httpMethod, headerMap, bodyObj);

        ResponseEntity<T2> responseEntity = null;
        try {
            responseEntity = restTemplate.exchange(requestEntity, returnType);

            log.info("OneM2M API response. statusCode={}, response={}", responseEntity.getStatusCode(), responseEntity.getBody());

            if (suceessStatus.contains(responseEntity.getStatusCode())) {
                return responseEntity.getBody();
            } else {
                throw new BadRequestException(IpeCode.ErrorCode.INVALID_PARAMETER, "OneM2M API request error. " + " responseCode=" + responseEntity.getStatusCode() + ", requestUri=" + requestUri + ", method=" + httpMethod);
            }
        } catch (HttpClientErrorException e) {
            log.info("OneM2M API response. statusCode={}, message={}", e.getStatusCode(), e.getMessage());

            // 최근데이터 조회 시 데이터가 없을 경우 체크
            if ( requestUri.toString().lastIndexOf("/la") > -1 && e.getMessage().indexOf("csebase is not found") > -1 ) {
                // System.out.println( requestUri.toString().lastIndexOf("/la") );
                // System.out.println( e.getMessage().indexOf("csebase is not found") );
                return null;
            }
            // la 로 끝나며, csebase is not found 포함되어 있으면 null로 리턴?

            if ( !suceessStatus.contains(e.getStatusCode()) ) {
                throw new BadRequestException(IpeCode.ErrorCode.INVALID_PARAMETER, "OneM2M API request error. " + " responseCode=" + e.getStatusCode() + ", requestUri=" + requestUri + ", method=" + httpMethod);
            }
        } catch(Exception e) {
            throw new BadRequestException(IpeCode.ErrorCode.INVALID_PARAMETER, "OneM2M API request error. requestUri=" + requestUri + ", method=" + httpMethod, e);
        }
        return null;
    }

    private String generateRequestUri(String baseUri, String subUri) {
        if(ValidateUtil.isEmptyData(subUri)) {
            return null;
        }

        StringBuilder requestUri = new StringBuilder(baseUri);
        if(!subUri.startsWith("/")) {
            requestUri.append("/");
        }
        requestUri.append(subUri);
        return requestUri.toString();
    }

    private MultiValueMap<String, String> generateDefaultHeader(String onem2mRegistApplicationId) {
        return generateHeader(null, onem2mRegistApplicationId);
    }

    private MultiValueMap<String, String> generateHeader(String contentType, String onem2mRegistApplicationId) {
        MultiValueMap<String, String> headerMap = new LinkedMultiValueMap<>();
        if(!ValidateUtil.isEmptyData(contentType)) {
            headerMap.set(HttpHeaders.CONTENT_TYPE, contentType);
        } else {
            headerMap.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        }
        headerMap.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headerMap.set(IpeCode.OneM2MHeaderKey.X_M2M_RI.getCode(), generateTransactionId());
        headerMap.set(IpeCode.OneM2MHeaderKey.X_M2M_ORIGIN.getCode(), onem2mRegistApplicationId);

        if(!ValidateUtil.isEmptyData(onem2mAuthorizationToken)) {
            headerMap.set(HttpHeaders.AUTHORIZATION, onem2mAuthorizationToken);
        }
        return headerMap;
    }

    private String generateTransactionId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

}

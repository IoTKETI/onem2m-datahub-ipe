package kr.re.keti.sc.ipe.datahub.service;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.ipe.common.code.IpeCode;
import kr.re.keti.sc.ipe.common.exception.BadRequestException;
import kr.re.keti.sc.ipe.datahub.vo.NGsiLdCollectResponseVO;
import kr.re.keti.sc.ipe.datahub.vo.NgsiLdCollectRequestVO;
import kr.re.keti.sc.ipe.util.ValidateUtil;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DataHubApiService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public NGsiLdCollectResponseVO entityOperations(String baseUri, String subUri, String datasetId, NgsiLdCollectRequestVO commonEntityFullVO) {

        String requestUri = generateRequestUri(baseUri, subUri);
        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(requestUri).build();

        try {
            Map<String, Object> mBody = new HashMap<>();
            mBody.put("datasetId", commonEntityFullVO.getDatasetId());
            mBody.put("entities", Arrays.asList(commonEntityFullVO));

            for (Map<String, Object> m : (List<Map>) mBody.get("entities")) {
                m.remove("datasetId");
            }
            
            String ingestBody = objectMapper.writeValueAsString(mBody);
            /*
            {
                "datasetId": "VibrationDataSet01",
                "entities": [
                    {
                        "@context": [
                            "http://220.76.205.230:18094/n2m/ngsi-ld/v1/context.jsonld"
                        ],
                        "id": "urn:datahub:VibrationDataSet01:VibrationDataSet01_data_#datasetID#",
                        "type": "SampleData01",
                        "Resourceid": {
                            "type": "Property",
                            "value": "#pi#"
                        },
                        "Vibration": {
                            "type": "Property",
                            "value": "#frequency#"
                        }
                    }
                ]
            }
            */

            return requestExchange(uriComponents.toUri(), HttpMethod.POST, ingestBody, NGsiLdCollectResponseVO.class, Arrays.asList(HttpStatus.OK, HttpStatus.NO_CONTENT));
        } catch (JsonProcessingException e) {
            log.error("entityOperations error. json parsing fail. commonEntityFullVO={}", commonEntityFullVO, e);
        }
        return null;
    }

    public NGsiLdCollectResponseVO entityOperationsOld(String baseUri, String subUri, NgsiLdCollectRequestVO commonEntityFullVO) {

        String requestUri = generateRequestUri(baseUri, subUri);
        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(requestUri)
                .build();

        try {
            Map<String, Object> mBody = new HashMap<>();
            mBody.put(requestUri, "");
            mBody.put("entities", Arrays.asList(commonEntityFullVO));
            
            String ingestBody = objectMapper.writeValueAsString(Arrays.asList(commonEntityFullVO));
            
            return requestExchange(uriComponents.toUri(), HttpMethod.POST, ingestBody, NGsiLdCollectResponseVO.class, Arrays.asList(HttpStatus.OK, HttpStatus.NO_CONTENT));
        } catch (JsonProcessingException e) {
            log.error("entityOperations error. json parsing fail. commonEntityFullVO={}", commonEntityFullVO, e);
        }
        return null;
    }

    private <T1, T2> T2 requestExchange(URI requestUri, HttpMethod httpMethod, Map<String, String> header, T1 bodyObj, Class<T2> returnType, List<HttpStatus> suceessStatus) {

        // 1. create request entity
        MultiValueMap<String, String> headerMap = new LinkedMultiValueMap<>();
        for (Map.Entry<String, String> entry : header.entrySet()) {
            headerMap.set(entry.getKey(), entry.getValue());
        }

        RequestEntity<T1> requestEntity = null;
        if(bodyObj == null) {
            requestEntity = new RequestEntity<>(headerMap, httpMethod, requestUri);
        } else {
            requestEntity = new RequestEntity<>(bodyObj, headerMap, httpMethod, requestUri);
        }

        log.info("Datahub API request. requestUri={}, method={}, header={}, body={}", requestUri, httpMethod, headerMap, bodyObj);

        ResponseEntity<T2> responseEntity = null;
        try {
            responseEntity = restTemplate.exchange(requestEntity, returnType);

            log.info("Datahub API response. statusCode={}, response={}", responseEntity.getStatusCode(), responseEntity.getBody());

            if(suceessStatus.contains(responseEntity.getStatusCode())) {
                return responseEntity.getBody();
            } else {
                throw new BadRequestException(IpeCode.ErrorCode.INVALID_PARAMETER, "Datahub API request error. "
                        + " responseCode=" + responseEntity.getStatusCode() + ", requestUri=" + requestUri + ", method=" + httpMethod);
            }

        } catch(HttpClientErrorException e) {
            if(!suceessStatus.contains(e.getStatusCode())) {
                throw new BadRequestException(IpeCode.ErrorCode.INVALID_PARAMETER, "Datahub API request error. "
                        + " responseCode=" + e.getStatusCode() + ", requestUri=" + requestUri + ", method=" + httpMethod);
            }
        } catch(Exception e) {
            throw new BadRequestException(IpeCode.ErrorCode.INVALID_PARAMETER,
                    "Datahub API request error. requestUri=" + requestUri + ", method=" + httpMethod, e);
        }
        return null;
    }

    private <T1, T2> T2 requestExchange(URI requestUri, HttpMethod httpMethod, T1 bodyObj, Class<T2> returnType, List<HttpStatus> suceessStatus) {

        // 1. create request entity
        MultiValueMap<String, String> headerMap = generateDefaultHeader();

        RequestEntity<T1> requestEntity = null;
        if(bodyObj == null) {
            requestEntity = new RequestEntity<>(headerMap, httpMethod, requestUri);
        } else {
            requestEntity = new RequestEntity<>(bodyObj, headerMap, httpMethod, requestUri);
        }

        log.info("Datahub API request. requestUri={}, method={}, header={}, body={}", requestUri, httpMethod, headerMap, bodyObj);

        ResponseEntity<T2> responseEntity = null;
        try {
            responseEntity = restTemplate.exchange(requestEntity, returnType);

            log.info("Datahub API response. statusCode={}, response={}", responseEntity.getStatusCode(), responseEntity.getBody());

            if(suceessStatus.contains(responseEntity.getStatusCode())) {
                return responseEntity.getBody();
            } else {
                throw new BadRequestException(IpeCode.ErrorCode.INVALID_PARAMETER, "Datahub API request error. "
                        + " responseCode=" + responseEntity.getStatusCode() + ", requestUri=" + requestUri + ", method=" + httpMethod);
            }

        } catch(HttpClientErrorException e) {
            if(!suceessStatus.contains(e.getStatusCode())) {
                throw new BadRequestException(IpeCode.ErrorCode.INVALID_PARAMETER, "Datahub API request error. "
                        + " responseCode=" + e.getStatusCode() + ", requestUri=" + requestUri + ", method=" + httpMethod);
            }
        } catch(Exception e) {
            throw new BadRequestException(IpeCode.ErrorCode.INVALID_PARAMETER,
                    "Datahub API request error. requestUri=" + requestUri + ", method=" + httpMethod, e);
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

    private MultiValueMap<String, String> generateDefaultHeader() {
        MultiValueMap<String, String> headerMap = new LinkedMultiValueMap<>();
        headerMap.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headerMap.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        return headerMap;
    }
/*

    public DatahubSubscriptionVO subscription(ContainerMetadataBaseVO containerMetadataBaseVO) {

        // 1. body 생성
        String entityId = containerMetadataBaseVO.getLabelVO().getIwkedEntityId();
        DatahubSubscriptionVO subscriptionVO = new DatahubSubscriptionVO();
        subscriptionVO.setId(datahubPlatformId + "_" + entityId);

        DatahubSubscriptionVO.EntityInfo entityInfo = new DatahubSubscriptionVO.EntityInfo();
        entityInfo.setId(entityId);
        subscriptionVO.setEntities(Arrays.asList(entityInfo));

        DatahubSubscriptionVO.NotificationParams notificationParams = new DatahubSubscriptionVO.NotificationParams();
        DatahubSubscriptionVO.NotificationParams.Endpoint endpoint = new DatahubSubscriptionVO.NotificationParams.Endpoint();
        endpoint.setUri(notificationReceiveUri);
        endpoint.setAccept(MediaType.APPLICATION_JSON_VALUE);
        notificationParams.setEndpoint(endpoint);
        subscriptionVO.setNotification(notificationParams);

        // 2. subscription 요청
        return requestExchange(datahubCollectUri, HttpMethod.POST, subscriptionVO, DatahubSubscriptionVO.class, Arrays.asList(HttpStatus.CREATED, HttpStatus.CONFLICT));
    }


    public DataModelVO retrieveDataModel(String requestUri, String modelTypeUri) {

        requestUri = requestUri + "?typeUri=" + modelTypeUri;

        MultiValueMap<String, String> headerMap = getDefaultHeaderParam();

        RequestEntity<Void> requestEntity = new RequestEntity<>(headerMap, HttpMethod.GET, URI.create(requestUri));

        ResponseEntity<DataModelVO> responseEntity = null;
        try {
            responseEntity = restTemplate.exchange(requestEntity, DataModelVO.class);
        } catch (RestClientException e) {
            throw new BadRequestException(IpeCode.ErrorCode.INVALID_PARAMETER, "Retrieve DataHub Model error. requestUri=" + requestUri, e);
        }

        if(responseEntity.getStatusCode() == HttpStatus.OK) {
            return responseEntity.getBody();
        } else {
            throw new BadRequestException(IpeCode.ErrorCode.INVALID_PARAMETER, "Retrieve DataHub Model error. "
                    + " responseCode=" + responseEntity.getStatusCode() + ", requestUri=" + requestUri);
        }
    }

    public boolean createDataModel(String requestUri, DataModelVO datamodelVO) {

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(datamodelVO), headers);

            ResponseEntity<String> responseEntity = restTemplate.postForEntity(requestUri, entity, String.class);

            if(responseEntity == null) {
                log.warn("createDataModel Response is null. Request datamodel={}", datamodelVO);
            } else {
                log.info("createDataModel Response code={}, Request datamodel={}", responseEntity.getStatusCodeValue(), datamodelVO);
            }

            if(responseEntity.getStatusCodeValue() == HttpStatus.CREATED.value()) {
                return true;
            }

        } catch(HttpClientErrorException e) {
            log.warn("createDataModel error. Response code={}. Request datamodel={}", e.getRawStatusCode(), datamodelVO, e);
        } catch(Exception e) {
            log.warn("createDataModel error. Request datamodel{}", datamodelVO, e);
        }
        return false;
    }

    public boolean updateDataModel(String requestUri, DataModelVO datamodelVO) {

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(datamodelVO), headers);

            ResponseEntity<String> responseEntity = restTemplate.exchange(requestUri, HttpMethod.PUT, entity, String.class);

            if(responseEntity == null) {
                log.warn("createDataModel Response is null. Request datamodel={}", datamodelVO);
            } else {
                log.info("createDataModel Response code={}, Request datamodel={}", responseEntity.getStatusCodeValue(), datamodelVO);
            }

            if(responseEntity.getStatusCodeValue() == HttpStatus.NO_CONTENT.value()) {
                return true;
            }

        } catch(HttpClientErrorException e) {
            log.warn("createDataModel error. Response code={}. Request datamodel={}", e.getRawStatusCode(), datamodelVO, e);
        } catch(Exception e) {
            log.warn("createDataModel error. Request datamodel{}", datamodelVO, e);
        }
        return false;
    }
 */
}

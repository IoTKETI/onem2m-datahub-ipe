package kr.re.keti.sc.ipe.onem2m.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.re.keti.sc.ipe.interworking.label.service.LabelInterworkingService;
import kr.re.keti.sc.ipe.interworking.sementic.service.SementicInterworkingService;
import kr.re.keti.sc.ipe.onem2m.vo.M2mCinVO;
import kr.re.keti.sc.ipe.onem2m.vo.Onem2mNotificationVO;
import kr.re.keti.sc.ipe.onem2m.vo.Onem2mResourceVO;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class Onem2mApiController {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private LabelInterworkingService labelInterworkingService;
    @Autowired
    private SementicInterworkingService sementicInterworkingService;

    @PostMapping(value="/onem2m/label/notifications")
    public ResponseEntity<Void> receiveLabelNotification(HttpServletRequest request,
                                                         HttpServletResponse response,
                                                         @RequestBody String notification) {

        Onem2mNotificationVO notificationVO = null;

        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        log.info("receive oneM2M label Notification. data={}", notification);
        try {
            notificationVO = objectMapper.readValue(notification, Onem2mNotificationVO.class);
        } catch (JsonProcessingException e) {
            log.error("Invalid oneM2M label Notification data.", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        // 1. Resource 변경 이벤트 여부 체크
        if (isValidEntityChangeEvent(notificationVO)) {
            // 2. oneM2M Resource 객체 생성
            String sur = notificationVO.getM2mSgn().getSur();
            String onem2mResourceId = sur.substring(0, sur.lastIndexOf("/"));
            M2mCinVO m2mCinVO = notificationVO.getM2mSgn().getNev().getRep().getM2mCin();
            Onem2mResourceVO onem2mResourceVO = new Onem2mResourceVO();
            onem2mResourceVO.setOnem2mResourceId(onem2mResourceId);
            onem2mResourceVO.setM2mCin(m2mCinVO);

            // 3. Mapping Rule을 통해 NGSI-LD 메시지로 변환 및 datahub 로 전송
            labelInterworkingService.convertAndCollect(onem2mResourceVO);
        }
        else {
            labelInterworkingService.upsetResource(notificationVO);
        }

        return ResponseEntity.status(HttpStatus.OK).build();
    }


    @PostMapping(value="/onem2m/sementic/notifications")
    public ResponseEntity<Void> receiveSementicNotification(HttpServletRequest request,
                                                            HttpServletResponse response,
                                                            @RequestBody String notification) {

        Onem2mNotificationVO notificationVO = null;

        log.info("receive oneM2M sementic Notification. data={}", notification);
        try {
            notificationVO = objectMapper.readValue(notification, Onem2mNotificationVO.class);
        } catch (JsonProcessingException e) {
            log.error("Invalid oneM2M sementic Notification data.", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        // 1. Resource 변경 이벤트 여부 체크
        if(isValidEntityChangeEvent(notificationVO)) {

            // 2. oneM2M Resource 객체 생성
            String sur = notificationVO.getM2mSgn().getSur();
            String containerUri = sur.substring(0, sur.lastIndexOf("/"));
            M2mCinVO m2mCinVO = notificationVO.getM2mSgn().getNev().getRep().getM2mCin();

            // 3. SementicDescriptor를 통해 NGSI-LD 메시지로 변환 및 datahub 로 전송
            sementicInterworkingService.convertAndCollect(containerUri, m2mCinVO);
        }

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    private boolean isValidEntityChangeEvent(Onem2mNotificationVO notificationVO) {
        if (notificationVO == null
                || notificationVO.getM2mSgn() == null
                || notificationVO.getM2mSgn().getNev() == null
                || notificationVO.getM2mSgn().getNev().getRep() == null
                || notificationVO.getM2mSgn().getNev().getRep().getM2mCin() == null) {
            return false;
        }

        if( notificationVO.getM2mSgn().getSur() == null || !notificationVO.getM2mSgn().getSur().endsWith("/subscription") ) {
            return false;
        }

        return true;
    }
}

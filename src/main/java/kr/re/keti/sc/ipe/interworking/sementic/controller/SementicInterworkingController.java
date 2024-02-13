package kr.re.keti.sc.ipe.interworking.sementic.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import kr.re.keti.sc.ipe.interworking.sementic.service.SementicInterworkingService;
import kr.re.keti.sc.ipe.interworking.sementic.vo.NgsiLdContainerUriVO;
import kr.re.keti.sc.ipe.onem2m.vo.Onem2mContainerInstanceVO;
import kr.re.keti.sc.ipe.onem2m.vo.Onem2mContainerVO;
import kr.re.keti.sc.ipe.onem2m.vo.Onem2mSementicDescriptorVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@Slf4j
public class SementicInterworkingController {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private SementicInterworkingService sementicInterworkingService;

    //API 1 - NGSI-LD ROOT 컨테이너 uri 목록 조회
    @GetMapping(value="/onem2m/discovery/sementic-container")
    public ResponseEntity<List<NgsiLdContainerUriVO>> receiveNgsiLdSementicRootContainerUri(HttpServletRequest request,
                                                                    HttpServletResponse response,
                                                                    @RequestParam(required = false,
                                                                                    defaultValue = "1") String maxLevel) {

        List<NgsiLdContainerUriVO> ngsiLdContainerUriVOs = sementicInterworkingService.discoveryNgsiLdSementic();
        return new ResponseEntity(ngsiLdContainerUriVOs, HttpStatus.OK);
    }

    //API 2 - 특정 ROOT 컨테이너의 sub 컨테이너 및 최신 sementic(la) 의 URI 조회
    @GetMapping(value="/onem2m/discovery/container")
    public ResponseEntity<NgsiLdContainerUriVO> receiveNgsiLdContainerUri(HttpServletRequest request,
                                                                                HttpServletResponse response,
                                                                                @RequestParam(required = true) String resourceuri,
                                                                                @RequestParam(required = false,
                                                                                        defaultValue = "1") String maxLevel) {

        NgsiLdContainerUriVO ngsiLdContainerUriVO = sementicInterworkingService.receiveNgsiLdContainerUris(resourceuri, maxLevel);

        return new ResponseEntity(ngsiLdContainerUriVO, HttpStatus.OK);
    }

    //API 3 - 컨테이너 상세정보 (m2m:cnt) 조회
    @GetMapping(value="/onem2m/container")
    public ResponseEntity<Onem2mContainerVO> receiveNgsiLdContainer(HttpServletRequest request,
                                                                    HttpServletResponse response,
                                                                    @RequestParam(required = true) String resourceuri) {

        Onem2mContainerVO onem2mContainerVO = sementicInterworkingService.retrieveContainer(resourceuri);
        return new ResponseEntity(onem2mContainerVO, HttpStatus.OK);
    }

    //API 4 - 컨테이너의 최신 인스턴스 (m2m:cin) 조회(la)
    @GetMapping(value="/onem2m/container/instance")
    public ResponseEntity<Onem2mContainerInstanceVO> receiveNgsiLdContainerInstance(HttpServletRequest request,
                                                                                    HttpServletResponse response,
                                                                                    @RequestParam(required = true) String resourceuri) {

        Onem2mContainerInstanceVO onem2mRepresentationVO = sementicInterworkingService.retrieveContainerInstance(resourceuri);
        return new ResponseEntity(onem2mRepresentationVO, HttpStatus.OK);
    }


    //API 5 - sementic 정보 조회 (m2m:smd) 조회
    @GetMapping(value="/onem2m/container/sementicdescriptor")
    public ResponseEntity<Onem2mSementicDescriptorVO> receiveNgsiLdSementicDescriptor(HttpServletRequest request,
                                                                                      HttpServletResponse response,
                                                                                      @RequestParam(required = true) String resourceuri) {

        Onem2mSementicDescriptorVO onem2mSementicDescriptorVO = sementicInterworkingService.retrieveSementicDescriptor(resourceuri);
        return new ResponseEntity(onem2mSementicDescriptorVO, HttpStatus.OK);
    }

}

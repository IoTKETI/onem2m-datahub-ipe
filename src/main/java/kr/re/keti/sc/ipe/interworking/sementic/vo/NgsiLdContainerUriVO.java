package kr.re.keti.sc.ipe.interworking.sementic.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class NgsiLdContainerUriVO {
    private String container;
    private String latest;
    private String sementicDescriptor;
    private List<NgsiLdContainerUriVO> subContainer;
}

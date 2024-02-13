package kr.re.keti.sc.ipe.onem2m.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Onem2mNotificationVO {

    @JsonProperty("m2m:sgn")
    private M2mSgnVO m2mSgn;
}

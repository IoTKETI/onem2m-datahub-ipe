package kr.re.keti.sc.ipe.onem2m.vo;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Onem2mContainerVO {
    @JsonProperty("m2m:cnt")
    private M2mCntVO m2mCnt;
}

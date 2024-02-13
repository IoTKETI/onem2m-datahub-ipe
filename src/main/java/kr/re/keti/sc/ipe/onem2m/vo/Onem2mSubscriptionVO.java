package kr.re.keti.sc.ipe.onem2m.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Onem2mSubscriptionVO {

    @JsonProperty("m2m:sub")
    private M2mSubVO m2mSub;
    @JsonProperty("m2m:dbg")
    private String m2mDbg;
}

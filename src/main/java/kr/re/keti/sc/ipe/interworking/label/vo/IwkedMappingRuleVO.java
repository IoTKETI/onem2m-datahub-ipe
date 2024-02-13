package kr.re.keti.sc.ipe.interworking.label.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import kr.re.keti.sc.ipe.common.code.IpeCode;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class IwkedMappingRuleVO {
    private String oneM2MResourceId;
    private String oneM2MAttributeName = "con"; // (O) Default: “con”
    private String oneM2MJsonPath; // (O) “con”의 내부가 JSON일 경우에만 사용
    private IpeCode.OneM2MDataType oneM2MDataType; // (O) 해당 타입을 그대로 사용할 경우에는 명시되지 않음
    private String oneM2MDateFormat; // oneM2MDataType이 “Date”일 경우에만 사용

    @JsonProperty("ngsi-ldQueryTermAttributePath")
    private String ngsiLdQueryTermAttributePath; // (M) Query Language의 QueryTerm의 AttributePath 부분
    @JsonProperty("ngsi-ldAttributeType")
    private IpeCode.AttributeType ngsiLdAttributeType; // (M)
    @JsonProperty("ngsi-ldAttributeDataType")
    private IpeCode.AttributeValueType ngsiLdAttributeDataType; // (O) 해당 타입을 그대로 사용할 경우에는 명시되지 않음
    @JsonProperty("ngsi-ldDateFormat")
    private String ngsiLdDateFormat; // (O) ngsi-ldAttributeDataType이 “Date”일 경우에만 사용
    @JsonProperty("ngsi-ldAttributeParentInformation")
    private List<ParentInformation> ngsiLdAttributeParentInformation; // (O) 상위에 존재하는 Attribute들의 전체 정보
    @JsonProperty("ngsi-ldArrayIndex")
    private Integer ngsiLdArrayIndex; // (O) Ngsi-ldAttributeDataType이 Array 일 경우 특정 Index 값만 연동할 경우 활용

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ParentInformation {
        @JsonProperty("ngsi-ldQueryTermAttributePath")
        private String ngsiLdQueryTermAttributePath; // (M) Query Language의 QueryTerm의 AttributePath 부분
        @JsonProperty("ngsi-ldAttributeType")
        private IpeCode.AttributeType ngsiLdAttributeType; // (M)
        @JsonProperty("ngsi-ldAttributeDataType")
        private IpeCode.AttributeValueType ngsiLdAttributeDataType; // (O)
    }
}

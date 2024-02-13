package kr.re.keti.sc.ipe.datahub.vo;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class NGsiLdCollectResponseVO {
    List<String> success;
    List<BatchEntityErrorVO> errors;

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BatchEntityErrorVO {
        String entityId;
        ErrorPayload error;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ErrorPayload {
        private String type;
        private String title;
        private String detail;
        private String debugMessage;
    }
}

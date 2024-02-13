package kr.re.keti.sc.ipe.interworking.sementic.vo.sementicdescriptor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Value {
    @JacksonXmlProperty(localName = "targetValueType", namespace = SementicDescriptor.NAMESPACE_ANNOTATION)
    private String targetValueType;
    @JacksonXmlProperty(localName = "targetKey", namespace = SementicDescriptor.NAMESPACE_ANNOTATION)
    private String targetKey;
    @JacksonXmlProperty(localName = "targetValue", namespace = SementicDescriptor.NAMESPACE_ANNOTATION)
    private String targetValue;
    @JacksonXmlProperty(localName = "sourceValueType", namespace = SementicDescriptor.NAMESPACE_ANNOTATION)
    private String sourceValueType;
    @JacksonXmlProperty(localName = "sourceKey", namespace = SementicDescriptor.NAMESPACE_ANNOTATION)
    private String sourceKey;
    @JacksonXmlProperty(localName = "sourceValue", namespace = SementicDescriptor.NAMESPACE_ANNOTATION)
    private String sourceValue;
}

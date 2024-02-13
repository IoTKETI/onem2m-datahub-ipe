package kr.re.keti.sc.ipe.interworking.sementic.vo.sementicdescriptor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class HasValue {
    @JacksonXmlElementWrapper(localName = "Value", namespace = SementicDescriptor.NAMESPACE_NGSI_LD)
    private Value value;
}

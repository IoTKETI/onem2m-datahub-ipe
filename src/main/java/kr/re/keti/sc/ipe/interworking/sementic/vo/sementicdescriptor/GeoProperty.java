package kr.re.keti.sc.ipe.interworking.sementic.vo.sementicdescriptor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeoProperty {
    @JacksonXmlProperty(isAttribute = true, localName = "about", namespace = SementicDescriptor.NAMESPACE_RDF)
    private String about;
    @JacksonXmlProperty(localName = "targetValueType", namespace = SementicDescriptor.NAMESPACE_ANNOTATION)
    private String targetValueType;
    @JacksonXmlElementWrapper(useWrapping = false, localName = "hasValue", namespace = SementicDescriptor.NAMESPACE_ANNOTATION)
    private List<HasValue> hasValue;
}

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
public class Property extends Value {
    @JacksonXmlProperty(isAttribute = true, localName = "about", namespace = SementicDescriptor.NAMESPACE_RDF)
    private String about;
    @JacksonXmlElementWrapper(useWrapping = false, localName = "hasProperty", namespace = SementicDescriptor.NAMESPACE_NGSI_LD)
    private List<HasProperty> hasProperty;

}

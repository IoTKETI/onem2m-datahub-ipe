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
public class Mapping {
        @JacksonXmlProperty(localName = "about", isAttribute = true, namespace = SementicDescriptor.NAMESPACE_RDF)
        private String about;
        @JacksonXmlElementWrapper(localName = "ngsi-ldContext", useWrapping = false, namespace = SementicDescriptor.NAMESPACE_ANNOTATION)
        @JacksonXmlProperty(localName = "ngsi-ldContext", namespace = SementicDescriptor.NAMESPACE_ANNOTATION)
        private List<String> ngsiLdContext;
}

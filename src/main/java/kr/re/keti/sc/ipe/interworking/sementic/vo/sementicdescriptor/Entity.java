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
public class Entity {
        @JacksonXmlProperty(localName = "about", isAttribute = true, namespace = SementicDescriptor.NAMESPACE_RDF)
        private String about;
        @JacksonXmlElementWrapper(localName = "hasProperty", useWrapping = false, namespace = SementicDescriptor.NAMESPACE_NGSI_LD)
        private List<HasProperty> hasProperty;
        @JacksonXmlProperty(localName = "hasEntityId", namespace = SementicDescriptor.NAMESPACE_ANNOTATION)
        private String hasEntityId;
        @JacksonXmlElementWrapper(localName = "hasDomainType", namespace = SementicDescriptor.NAMESPACE_ANNOTATION)
        private HasDomainType hasDomainType;
}

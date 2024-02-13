package kr.re.keti.sc.ipe.interworking.sementic.vo.sementicdescriptor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class HasDomainType {
    @JacksonXmlProperty(localName = "resource", isAttribute = true, namespace = SementicDescriptor.NAMESPACE_RDF)
    private String resource;
}

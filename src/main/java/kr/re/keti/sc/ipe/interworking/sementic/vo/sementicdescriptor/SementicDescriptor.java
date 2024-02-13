package kr.re.keti.sc.ipe.interworking.sementic.vo.sementicdescriptor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

@JacksonXmlRootElement(localName = "RDF", namespace = SementicDescriptor.NAMESPACE_RDF)
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SementicDescriptor {

    public static final String NAMESPACE_RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    public static final String NAMESPACE_NGSI_LD = "http://uri.etsi.org/ngsi-ld/";
    public static final String NAMESPACE_ANNOTATION = "http://www.citydatahub.kr/ontologies/2021/1/sm_annotation#";

    @JacksonXmlElementWrapper(localName = "Mapping", namespace = SementicDescriptor.NAMESPACE_ANNOTATION)
    private Mapping mapping;
    @JacksonXmlElementWrapper(localName = "Entity", namespace = SementicDescriptor.NAMESPACE_NGSI_LD)
    private Entity entity;
}

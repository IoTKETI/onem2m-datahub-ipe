package kr.re.keti.sc.ipe.interworking.sementic.vo.sementicdescriptor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class HasProperty {
    @JacksonXmlElementWrapper(localName = "Property", namespace = SementicDescriptor.NAMESPACE_NGSI_LD)
    private Property property;
    @JacksonXmlElementWrapper(localName = "GeoProperty", namespace = SementicDescriptor.NAMESPACE_NGSI_LD)
    private GeoProperty geoProperty;
    @JacksonXmlElementWrapper(localName = "observedAt", namespace = SementicDescriptor.NAMESPACE_NGSI_LD)
    private ObservedAt observedAt;
}

package kr.re.keti.sc.ipe.interworking.sementic.vo;

import kr.re.keti.sc.ipe.interworking.sementic.vo.sementicdescriptor.SementicDescriptor;
import lombok.Data;

import java.util.List;

@Data
public class SementicDescriptorCacheVO {
    private String containerUri;
    private String sementicDescriptorUri;
    private String sementicDescriptorLt;
    private SementicDescriptor sementicDescriptor;

    public String getEntityId() {
        if(sementicDescriptor != null
                && sementicDescriptor.getEntity() != null) {

        }
        return sementicDescriptor.getEntity().getHasEntityId();
    }

    public String getDomainType() {
        if(sementicDescriptor != null
                && sementicDescriptor.getEntity() != null
                && sementicDescriptor.getEntity().getHasDomainType() != null) {

            return sementicDescriptor.getEntity().getHasDomainType().getResource();
        }
        return null;
    }
}

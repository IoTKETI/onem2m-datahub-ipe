package kr.re.keti.sc.ipe.onem2m.vo;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import kr.re.keti.sc.ipe.interworking.label.vo.ContainerMetadataVO.MetadataVO;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Onem2mContainerInstanceVO {
    @JsonProperty("m2m:cin")
    private M2mCinVO m2mCin;

    @JsonProperty("m2m:cnt")
    private MetadataVO m2mCnt;

    /*
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MetadataVO {
        private String pi; // parentID
        private String ri; // resourceID
        private String ty; // resourceType
        private String ct; // creationTime
        private Long st; // stateTag
        private String rn; // resourceName
        private String lt; // lastModifiedTime
        private String et; // expirationTime
        private List<String> lbl; // label
        private String cr;
        private Long mni; // maxNrOfInstance
        private Long mbs; // maxByteSize
        private Long mia; // maxInstanceAge
        private Long cni; // currentNrOfInstances
        private Long cbs; // currentByteSize
    }
    */
}

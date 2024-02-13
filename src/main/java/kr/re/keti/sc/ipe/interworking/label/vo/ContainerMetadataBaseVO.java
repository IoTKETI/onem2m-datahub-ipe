package kr.re.keti.sc.ipe.interworking.label.vo;

import kr.re.keti.sc.ipe.interworking.label.vo.LabelVO;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ContainerMetadataBaseVO {
    private String id;
    private String subUri;
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
    private Date createDatetime;
    private String creatorId;
    private Date modifyDatetime;
    private String modifierId;

    private LabelVO labelVO;
}

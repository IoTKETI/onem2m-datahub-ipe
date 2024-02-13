package kr.re.keti.sc.ipe.onem2m.vo;

import lombok.Data;

import java.util.List;

@Data
public class Onem2mResourceVO {
    private String onem2mResourceId;
    private M2mCinVO m2mCin;
    private List<Onem2mResourceVO> relatedResources;
}

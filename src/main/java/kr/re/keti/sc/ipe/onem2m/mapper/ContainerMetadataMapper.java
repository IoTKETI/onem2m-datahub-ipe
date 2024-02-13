package kr.re.keti.sc.ipe.onem2m.mapper;

import kr.re.keti.sc.ipe.interworking.label.vo.ContainerMetadataBaseVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ContainerMetadataMapper {

    boolean createContainerMetadata(ContainerMetadataBaseVO containerMetadataBaseVO);

    boolean updateContainerMetadata(ContainerMetadataBaseVO containerMetadataBaseVO);

    boolean deleteContainerMetadata(ContainerMetadataBaseVO containerMetadataBaseVO);

    ContainerMetadataBaseVO retrieveMappingRule(ContainerMetadataBaseVO containerMetadataBaseVO);

    List<ContainerMetadataBaseVO> retrieveMappingRules(ContainerMetadataBaseVO containerMetadataBaseVO);
}

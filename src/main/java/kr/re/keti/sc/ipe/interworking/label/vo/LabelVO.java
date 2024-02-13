package kr.re.keti.sc.ipe.interworking.label.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import kr.re.keti.sc.ipe.common.code.IpeCode;
import kr.re.keti.sc.ipe.util.ConvertUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Data
@Slf4j
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LabelVO {
    private String iwkedTechnology;
    private List<IpeCode.IwkedDirection> iwkedDirection;
    private String iwkedEntityType;
    private List<IwkedMappingRuleVO> iwkedmappingRuleVOs;
    private String iwkedEntityId;
    private String iwkedDatasetId;
    private List<String> iwkedRelatedResources;
    private List<String> ngsiLdContext;

    private LabelVO() {

    }

    public LabelVO(List<String> oneM2MLabel) throws JsonProcessingException {
        if(oneM2MLabel != null) {
            for(String label : oneM2MLabel) {
                String[] kvArray = label.split(":", 2);
                if(kvArray == null || kvArray.length != 2) {
                    continue;
                }
                String key = kvArray[0];
                String value = kvArray[1];
                IpeCode.OneM2MLabelKey oneM2MLabelKey = IpeCode.OneM2MLabelKey.fromString(key);
                if(oneM2MLabelKey == null) {
                    log.warn("OneM2M label parsing warn. Unknown label key. key={}", key);
                    continue;
                }
                if(value == null) {
                    log.warn("OneM2M label parsing warn. value is null. key={}", key);
                    continue;
                }

                switch (oneM2MLabelKey) {
                    case IWKED_TECHNOLOGY:
                        this.iwkedTechnology = value;
                        break;
                    case IWKED_DIRECTION:
                        List<String> directions = ConvertUtil.fromJson(value, new TypeReference<List<String>>() {});
                        if(directions != null && directions.size() > 0) {
                            this.iwkedDirection = new ArrayList<> ();
                            for(String direction : directions) {
                                IpeCode.IwkedDirection iwkedDirection = IpeCode.IwkedDirection.fromString(direction);
                                if(iwkedDirection != null) {
                                    this.iwkedDirection.add(iwkedDirection);
                                }
                            }
                        }
                        break;
                    case IWKED_ENTITY_TYPE:
                        this.iwkedEntityType = value;
                        break;
                    case IWKED_DATASET_ID:
                        this.iwkedDatasetId = value;
                        break;
                    case IWKED_MAPPING_RULE:
                        this.iwkedmappingRuleVOs = ConvertUtil.fromJson(value, new TypeReference<List<IwkedMappingRuleVO>>() {});
                        break;
                    case IWKED_ENTITY_ID:
                        this.iwkedEntityId = value;
                        break;
                    case IWKED_RELATED_RESOURCES:
                        this.iwkedRelatedResources = ConvertUtil.fromJson(value, new TypeReference<List<String>>() {});
                        break;
                    case NGSI_LD_CONTEXT:
                        this.ngsiLdContext = ConvertUtil.fromJson(value, new TypeReference<List<String>>() {});
                        break;
                    default:
                        break;
                }
            }
        }
    }
}

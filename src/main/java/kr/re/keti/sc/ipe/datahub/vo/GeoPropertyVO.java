package kr.re.keti.sc.ipe.datahub.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import kr.re.keti.sc.ipe.common.code.IpeCode;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeoPropertyVO {
    private IpeCode.GeoJsonValueType type;
    private List<Double> coordinates = new ArrayList<>(Arrays.asList(null, null));

    @Override
    public String toString() {
        return super.toString();
    }
}

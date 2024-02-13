package kr.re.keti.sc.ipe.onem2m.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import kr.re.keti.sc.ipe.common.code.IpeCode;
import lombok.Data;

import java.util.HashMap;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class M2mSmdVO extends HashMap<String, Object> {

    public String getDsp() {
        return (String)super.get(IpeCode.OneM2mSmdKey.DSP.getCode());
    }

    @Override
    public String toString() {
        return super.toString();
    }
}

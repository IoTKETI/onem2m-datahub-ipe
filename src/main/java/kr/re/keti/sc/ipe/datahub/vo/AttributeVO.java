package kr.re.keti.sc.ipe.datahub.vo;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import kr.re.keti.sc.ipe.common.code.IpeCode;
import lombok.Data;

@SuppressWarnings("serial")
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AttributeVO  extends HashMap<String, Object> {

    public IpeCode.AttributeType getType() {
        return (IpeCode.AttributeType) super.get(IpeCode.PropertyKey.TYPE.getCode());
    }

    public void setType(IpeCode.AttributeType type) {
        super.put(IpeCode.PropertyKey.TYPE.getCode(), type);
    }

    public Object getValue() {
        return super.get(IpeCode.PropertyKey.VALUE.getCode());
    }

    public void setValue(Object value) {
        super.put(IpeCode.PropertyKey.VALUE.getCode(), value);
    }

    public String getObservedAt() {
        return (String) super.get(IpeCode.PropertyKey.OBSERVED_AT.getCode());
    }

    public void setObservedAt(String observedAt) {
        super.put(IpeCode.PropertyKey.OBSERVED_AT.getCode(), observedAt);
    }

    public String getUnitCode() {
        return (String) super.get(IpeCode.PropertyKey.UNIT_CODE.getCode());
    }

    public void setUnitCode(String unitCode) {
        super.put(IpeCode.PropertyKey.UNIT_CODE.getCode(), unitCode);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}

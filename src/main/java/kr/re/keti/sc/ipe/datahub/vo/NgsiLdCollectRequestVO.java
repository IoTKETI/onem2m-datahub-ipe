package kr.re.keti.sc.ipe.datahub.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import kr.re.keti.sc.ipe.common.code.IpeCode;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class NgsiLdCollectRequestVO extends CommonEntityVO {

	public String getType() {
		return (String) super.get(IpeCode.DefaultAttributeKey.TYPE.getCode());
	}
	public void setType(String type) {
		super.put(IpeCode.DefaultAttributeKey.TYPE.getCode(), type);
	}

	@Override
	public String toString() {
		return super.toString();
	}
}

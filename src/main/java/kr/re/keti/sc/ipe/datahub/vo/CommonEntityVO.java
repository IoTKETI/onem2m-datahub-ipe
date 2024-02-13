package kr.re.keti.sc.ipe.datahub.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import kr.re.keti.sc.ipe.common.code.IpeCode;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("serial")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommonEntityVO extends HashMap<String, Object> {

	@SuppressWarnings("unchecked")
	public List<String> getContext() {
		return (List<String>) super.get(IpeCode.DefaultAttributeKey.CONTEXT.getCode());
	}
	public void setContext(List<String> context) {
		super.put(IpeCode.DefaultAttributeKey.CONTEXT.getCode(), context);
	}

	public String getId() {
		return (String) super.get(IpeCode.DefaultAttributeKey.ID.getCode());
	}
	public void setId(String id) {
		super.put(IpeCode.DefaultAttributeKey.ID.getCode(), id);
	}
	
	public String getType() {
		return (String) super.get(IpeCode.DefaultAttributeKey.TYPE.getCode());
	}
	public void setType(String type) {
		super.put(IpeCode.DefaultAttributeKey.TYPE.getCode(), type);
	}

	public String getDatasetId() {
		return (String) super.get(IpeCode.DefaultAttributeKey.DATASET_ID.getCode());
	}
	public void setDatasetId(String datasetId) {
		super.put(IpeCode.DefaultAttributeKey.DATASET_ID.getCode(), datasetId);
	}

	public String getCreatedAt() {
		return (String) super.get(IpeCode.DefaultAttributeKey.CREATED_AT.getCode());
	}
	public void setCreatedAt(Date createdAt) {
		super.put(IpeCode.DefaultAttributeKey.CREATED_AT.getCode(), createdAt);
	}

	public String getModifiedAt() {
		return (String) super.get(IpeCode.DefaultAttributeKey.MODIFIED_AT.getCode());
	}
	
	public void setModifiedAt(Date modifiedAt) {
		super.put(IpeCode.DefaultAttributeKey.MODIFIED_AT.getCode(), modifiedAt);
	}

	@Override
	public String toString() {
		return super.toString();
	}
}

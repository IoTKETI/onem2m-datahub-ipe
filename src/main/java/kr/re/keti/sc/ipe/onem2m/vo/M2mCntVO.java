package kr.re.keti.sc.ipe.onem2m.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.HashMap;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class M2mCntVO extends HashMap<String, Object> {
//    private String rn;
//    private String ty;
//    private String pi;
//    private String ri;
//    private String ct;
//    private String lt;
//    private String st;
//    private String et;
//    private String cs;
//    private Object con;
//    private String cr;
    @Override
    public String toString() {
    return super.toString();
}
}

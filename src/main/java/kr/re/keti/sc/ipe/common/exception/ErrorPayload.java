package kr.re.keti.sc.ipe.common.exception;

public class ErrorPayload {

	private String type;
	private String title;
	private String detail;
	private String debugMessage;
	
    public ErrorPayload(String type, String title, String detail) {
        this.type = type;
        this.title = title;
        this.detail = detail;
    }

    public ErrorPayload(String type, String title, String detail, String debugMessage) {
    	this.type = type;
        this.title = title;
        this.detail = detail;
        this.debugMessage = debugMessage;
    }

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public String getDebugMessage() {
		return debugMessage;
	}

	public void setDebugMessage(String debugMessage) {
		this.debugMessage = debugMessage;
	}
   
}

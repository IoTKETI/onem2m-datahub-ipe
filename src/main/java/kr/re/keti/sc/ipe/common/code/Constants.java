package kr.re.keti.sc.ipe.common.code;

/**
 * 공통 상수 관리 클래스
 */
public class Constants {
    /** 기본 패키지 경로 */
    public static final String BASE_PACKAGE = "kr.re.keti.sc";
    /** Content Date Format */
    public static final String CONTENT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    public static final String[] ONEM2M_MULTI_DATE_FORMATS = new String[] {
            "yyyyMMdd'T'HHmmss",
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd'T'HH:mm:ss,SSSXXX",
            "yyyy-MM-dd HH:mm:ss.SSSXXX",
            "yyyy-MM-dd HH:mm:ss,SSSXXX"
    };

    /** Content Date TimeZone */
    public static final String CONTENT_DATE_TIMEZONE = "Asia/Seoul";
    /** PostgreSQL INSERT Date Format */
    public static final String POSTGRES_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

    public static final String ONEM2M_LATEST_SUBURI = "/la";

    public static final String subscriptionResourceName = "subscription";

}

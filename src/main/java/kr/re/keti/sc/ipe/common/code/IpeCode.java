package kr.re.keti.sc.ipe.common.code;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 공통 코드 클래스
 */
public class IpeCode {

    public static enum OneM2MSementicDataType {
        STRING("string"),
        FLOAT("float"),
        DOUBLE("double"),
        DATETIMESTAMP("dateTimeStamp"),
        ;

        private String code;
        private OneM2MSementicDataType(String code) {
            this.code = code;
        }
        public String getCode() {
            return code;
        }

        public static OneM2MSementicDataType fromString(String code) {
            for (OneM2MSementicDataType value : values()) {
                if (value.getCode().equals(code)) {
                    return value;
                }
            }
            return null;
        }
    }

    public static enum OneM2mSmdKey {
        TY("ty"),
        ET("et"),
        CT("ct"),
        LT("lt"),
        RI("ri"),
        RN("rn"),
        PI("pi"),
        DCRP("dcrp"),
        DSP("dsp"),
        OR("or"),
        ;

        private String code;
        private OneM2mSmdKey(String code) {
            this.code = code;
        }
        public String getCode() {
            return code;
        }
    }

    public static enum Onem2mQueryParamKey {
        FILTER_USAGE("fu"),
        RESOURCE_TYPE("ty"),
        ONTOLOGY_REFERENCE("or"),
        SEMENTIC_QUERY_INDICATOR("sqi"),
        SEMENTIC_FILTER("smf"),
        LABEL("lbl"),
        ;

        private String code;

        private Onem2mQueryParamKey(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    public static enum DiscoveryMaxLevel {
        ALL("all");

        private String code;

        private DiscoveryMaxLevel(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    public static enum OneM2MFilterUsage {
        DISCOVERY("1"),
        ;

        private String code;

        private OneM2MFilterUsage(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    public static enum OneM2MResourceType {
        CONTAINER("3", "cnt"),
        CONTAINER_INSTANCE("4", "la"),
        SUBSCRIPTION("23", "sub"),
        SEMENTIC_DESCRIPTOR("24", "smd"),
        ;

        private String code;
        private String type;

        private OneM2MResourceType(String code, String type) {
            this.code = code;
            this.type = type;
        }

        public String getCode() {
            return code;
        }
        public String getType() {
            return type;
        }
    }

    public static enum OneM2MDataType {
        @JsonProperty("String")
        STRING("String"),
        @JsonProperty("Integer")
        INTEGER("Integer"),
        @JsonProperty("Double")
        DOUBLE("Double"),
        @JsonProperty("Object")
        OBJECT("Object"),
        @JsonProperty("Date")
        DATE("Date"),
        @JsonProperty("Boolean")
        BOOLEAN("Boolean"),
        @JsonProperty("GeoJson")
        GEO_JSON("GeoJson"),
        @JsonProperty("ArrayString")
        ARRAY_STRING("ArrayString"),
        @JsonProperty("ArrayInteger")
        ARRAY_INTEGER("ArrayInteger"),
        @JsonProperty("ArrayDouble")
        ARRAY_DOUBLE("ArrayDouble"),
        @JsonProperty("ArrayBoolean")
        ARRAY_BOOLEAN("ArrayBoolean"),
        @JsonProperty("ArrayObject")
        ARRAY_OBJECT("ArrayObject"),
        ;

        private String code;

        private OneM2MDataType(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        public static OneM2MDataType fromString(String code) {
            for (OneM2MDataType value : values()) {
                if (value.getCode().equals(code)) {
                    return value;
                }
            }
            return null;
        }
    }

    public static enum IwkedDirection {
        ONEM2M_TO_NGSILD("oneM2MtoNGSI-LD"),
        NGSILD_TO_ONEM2M("NGSI-LDtooneM2M"),
        ;

        private String code;

        private IwkedDirection(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        public static IwkedDirection fromString(String code) {
            for (IwkedDirection iwkedDirection : values()) {
                if (iwkedDirection.getCode().equals(code)) {
                    return iwkedDirection;
                }
            }
            return null;
        }
    }

    public static enum OneM2MLabelKey {
        IWKED_TECHNOLOGY("Iwked-Technology"),
        IWKED_DIRECTION("Iwked-Direction"),
        IWKED_ENTITY_TYPE("Iwked-Entity-Type"),
        IWKED_DATASET_ID("Iwked-Dataset-ID"),
        IWKED_MAPPING_RULE("Iwked-mapping-rule"),
        IWKED_ENTITY_ID("Iwked-Entity-ID"),
        IWKED_RELATED_RESOURCES("Iwked-Related-Resources"),
        NGSI_LD_CONTEXT("NGSI-LD-Context"),
        ;

        private String code;

        private OneM2MLabelKey(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        public static OneM2MLabelKey fromString(String code) {
            for (OneM2MLabelKey labelKey : values()) {
                if (labelKey.getCode().equals(code)) {
                    return labelKey;
                }
            }
            return null;
        }
    }

    public static enum OneM2MHeaderKey {
        X_M2M_RI("X-M2M-RI"),
        X_M2M_ORIGIN("X-M2M-Origin"),
        ;

        private String code;

        private OneM2MHeaderKey(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }


    public static enum UseYn {

        YES("Y"),
        NO("N");

        private String code;

        private UseYn(String code) {
            this.code = code;
        }

        @JsonCreator
        public String getCode() {
            return code;
        }

        @JsonValue
        public static UseYn parseType(String code) {
            for (UseYn useYn : values()) {
                if (useYn.getCode().equals(code)) {
                    return useYn;
                }
            }
            return null;
        }
    }

    public static enum GeoJsonValueType {
        @JsonProperty("Point")
        POINT("Point"),
        @JsonProperty("MultiPoint")
        MULTIPOINT("MultiPoint"),
        @JsonProperty("LineString")
        LINESTRING("LineString"),
        @JsonProperty("MultiLineString")
        MULTILINESTRING("MultiLineString"),
        @JsonProperty("Polygon")
        POLYGON("Polygon"),
        @JsonProperty("MultiPolygon")
        MULTIPOLYGON("MultiPolygon"),

        ;

        private String code;

        @JsonCreator
        private GeoJsonValueType(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        @JsonValue
        public static GeoJsonValueType parseType(String code) {
            for (GeoJsonValueType geoJsonValueType : values()) {
                if (geoJsonValueType.getCode().equals(code)) {
                    return geoJsonValueType;
                }
            }
            return null;
        }
    }

    public static enum PropertyKey {
        TYPE("type"),
        VALUE("value"),
        OBSERVED_AT("observedAt"),
        OBJECT("object"),
        COORDINATES("coordinates"),
        UNIT_CODE("unitCode"),
        CREATED_AT("createdAt"),
        MODIFIED_AT("modifiedAt"),
        ;

        private String code;

        private PropertyKey(String code) {
            this.code = code;
        }

        @JsonCreator
        public String getCode() {
            return code;
        }

        @JsonValue
        public static PropertyKey parseType(String code) {
            for (PropertyKey propertyKey : values()) {
                if (propertyKey.getCode().equals(code)) {
                    return propertyKey;
                }
            }
            return null;
        }
    }

    public static enum DefaultAttributeKey {
        CONTEXT("@context"),
        ID("id"),
        DATASET_ID("datasetId"),
        DATA_MODEL_ID("dataModelId"),
        CREATED_AT("createdAt"),
        MODIFIED_AT("modifiedAt"),
        OPERATION("operation"),
        TYPE("type"),
        ATTR_ID("attrId"),
    	OBSERVED_AT("observedAt");

        private String code;

        private DefaultAttributeKey(String code) {
            this.code = code;
        }

        @JsonCreator
        public String getCode() {
            return code;
        }

        @JsonValue
        public static DefaultAttributeKey parseType(String code) {
            for (DefaultAttributeKey defaultAttributeKey : values()) {
                if (defaultAttributeKey.getCode().equals(code)) {
                    return defaultAttributeKey;
                }
            }
            return null;
        }
    }

    public static enum AttributeType {
        @JsonProperty("Property")
        PROPERTY("Property"),
        @JsonProperty("GeoProperty")
        GEO_PROPERTY("GeoProperty"),
        @JsonProperty("Relationship")
        RELATIONSHIP("Relationship");

        private String code;

        @JsonCreator
        private AttributeType(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        @JsonValue
        public static AttributeType fromString(String code) {
            for (AttributeType attributeType : values()) {
                if (attributeType.getCode().equals(code)) {
                    return attributeType;
                }
            }
            return null;
        }
    }

    public static enum AttributeValueType {
        @JsonProperty("String")
        STRING("String"),
        @JsonProperty("Integer")
        INTEGER("Integer"),
        @JsonProperty("Double")
        DOUBLE("Double"),
        @JsonProperty("Object")
        OBJECT("Object"),
        @JsonProperty("Date")
        DATE("Date"),
        @JsonProperty("Boolean")
        BOOLEAN("Boolean"),
        @JsonProperty("GeoJson")
        GEO_JSON("GeoJson"),
        @JsonProperty("ArrayString")
        ARRAY_STRING("ArrayString"),
        @JsonProperty("ArrayInteger")
        ARRAY_INTEGER("ArrayInteger"),
        @JsonProperty("ArrayDouble")
        ARRAY_DOUBLE("ArrayDouble"),
        @JsonProperty("ArrayBoolean")
        ARRAY_BOOLEAN("ArrayBoolean"),
        @JsonProperty("ArrayObject")
        ARRAY_OBJECT("ArrayObject"),
        ;

        private String code;

        @JsonCreator
        private AttributeValueType(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        @JsonValue
        public static AttributeValueType fromString(String code) {
            for (AttributeValueType attributeValueType : values()) {
                if (attributeValueType.getCode().equals(code)) {
                    return attributeValueType;
                }
            }
            return null;
        }
    }

    public static enum ErrorCode {
        UNKNOWN_ERROR("C001"),
        NOT_EXIST_ENTITY("C002"),
        SQL_ERROR("C003"),
        MEMORY_QUEUE_INPUT_ERROR("C004"),
        REQUEST_MESSAGE_PARSING_ERROR("C005"),
        RESPONSE_MESSAGE_PARSING_ERROR("C006"),
        INVALID_ENTITY_TYPE("C007"),
        INVALID_ACCEPT_TYPE("C008"),
        INVALID_PARAMETER("C009"),
        INVALID_AUTHORIZATION("C0010"),

        LENGTH_REQUIRED("C0011"),
        ALREADY_EXISTS("C0012"),
        NOT_EXIST_ID("C013"),
        NOT_EXIST_ENTITY_ATTR("C014"),
        LOAD_DATA_MODEL_ERROR("C015"),
        CREATE_ENTITY_TABLE_ERROR("C016"),
        OPERATION_NOT_SUPPORTED("C017"),

        NOT_SUPPORTED_METHOD("C101"),

        NOT_EXISTS_DATAMODEL("C111"),
        INVALID_DATAMODEL("C112"),
        PROVISIONING_ERROR("C114"),

        NOT_EXISTS_DATASET("C121"),
        NOT_EXISTS_DATASETFLOW("C131"),

        INVALID_NOTIFICATION_URI("C141");

        private String code;

        private ErrorCode(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }


        public static ErrorCode parseCode(String code) {
            for (ErrorCode errorCode : values()) {
                if (errorCode.getCode().equals(code)) {
                    return errorCode;
                }
            }
            return null;
        }
    }


}

package kr.re.keti.sc.ipe.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.re.keti.sc.ipe.common.configuration.JacksonConfiguration;

import java.nio.ByteBuffer;

public class ConvertUtil {

	public static int bytesToint(byte[] value) {
		return ByteBuffer.wrap(value).getInt();
	}

	public static byte[] intTobytes(int value) {
		return ByteBuffer.allocate(4).putInt(value).array();
	}

	public static ObjectMapper objectMapper;

	static {
		objectMapper = new JacksonConfiguration().objectMapper();
	}

	public static String toJson(Object object) throws JsonProcessingException {
		return objectMapper.writeValueAsString(object);
	}

	public static <T> T fromJson(String json, Class<T> classType) throws JsonProcessingException {
		return objectMapper.readValue(json, classType);
	}

	public static <T> T fromJson(String json, TypeReference<T> typeReference) throws JsonProcessingException {
		return objectMapper.readValue(json, typeReference);
	}
}
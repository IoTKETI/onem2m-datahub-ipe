package kr.re.keti.sc.ipe.common.serialize;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import kr.re.keti.sc.ipe.common.code.Constants;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MultiDateDeserializer extends StdDeserializer<Date> {
    private static final long serialVersionUID = 1L;

    private static List<DateTimeFormatter> dateFormats = new ArrayList<>();

    static {
    	for (String dateFormat : Constants.ONEM2M_MULTI_DATE_FORMATS) {
    		dateFormats.add(DateTimeFormatter.ofPattern(dateFormat));
    	}
    }

    public MultiDateDeserializer() {
        this(null);
    }

    public MultiDateDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Date deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);
        final String date = node.textValue();

        for (DateTimeFormatter dateFormat : dateFormats) {
        	try {
        		return Date.from(LocalDateTime.parse(date, dateFormat).atZone(ZoneId.systemDefault()).toInstant());
        	} catch(DateTimeParseException e) {
        		
        	} catch (Exception e) {
        		
        	}
        }
        throw new JsonParseException(jp, "Unparseable date: " + date + ". Supported formats: " + Arrays.toString(Constants.ONEM2M_MULTI_DATE_FORMATS));
    }
}

package kr.re.keti.sc.ipe.common.configuration;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import kr.re.keti.sc.ipe.common.code.Constants;
import kr.re.keti.sc.ipe.common.serialize.MultiDateDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

@Configuration
public class JacksonConfiguration {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(Include.NON_NULL);
        objectMapper.setDateFormat(new SimpleDateFormat(Constants.CONTENT_DATE_FORMAT));
        objectMapper.setTimeZone(TimeZone.getDefault());
        //SimpleModule module = new SimpleModule();
        //module.addDeserializer(Date.class, new MultiDateDeserializer());
        //objectMapper.registerModule(module);
        return objectMapper;
    }
}
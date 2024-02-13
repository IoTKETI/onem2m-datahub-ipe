package kr.re.keti.sc.ipe.util;

import kr.re.keti.sc.ipe.common.code.Constants;


import java.text.ParseException;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class DateUtil {

	private static final DateTimeFormatter ngsiLddateFormatter = DateTimeFormatter.ofPattern(Constants.CONTENT_DATE_FORMAT);
	private static final DateTimeFormatter dbDateFormatter = DateTimeFormatter.ofPattern(Constants.POSTGRES_TIMESTAMP_FORMAT);

	private static final List<DateTimeFormatter> dateFormats = new ArrayList<>();

    static {
    	for (String dateFormat : Constants.ONEM2M_MULTI_DATE_FORMATS) {
    		dateFormats.add(DateTimeFormatter.ofPattern(dateFormat));
    	}
    }

	public static String dateToDbFormatString(Date date) {
		LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.of(Constants.CONTENT_DATE_TIMEZONE));
		return localDateTime.format(dbDateFormatter);
	}

	public static Date strToDate(String dateStr, String dateFormat) throws ParseException {
		
		try {
			return Date.from(LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern(dateFormat)).atZone(ZoneId.systemDefault()).toInstant());
		} catch(DateTimeParseException e) {

		} catch (Exception e) {

		}
		throw new ParseException("StrToDate parsing error. date String=" + dateStr + ", format=" + dateFormat, 0);
	}

	public static String dateToStr(Date date, String dateFormat) throws ParseException {
    	try {
			ZonedDateTime zdt = date.toInstant().atZone(ZoneId.systemDefault());
			return zdt.format(DateTimeFormatter.ofPattern(dateFormat));
		} catch (IllegalArgumentException | DateTimeException e) {

		} catch (Exception e) {

		}
		throw new ParseException("DateToStr parsing error. date=" + date + ", format=" + dateFormat, 0);
	}

	public static String onem2mDateStrToDataHubDateStr(String dateStr) throws ParseException {

		for (DateTimeFormatter dateFormat : dateFormats) {
			try {
				Date date = Date.from(LocalDateTime.parse(dateStr, dateFormat).atZone(ZoneId.systemDefault()).toInstant());
				return dateToStr(date, Constants.CONTENT_DATE_FORMAT);
			} catch(DateTimeParseException e) {

			} catch (Exception e) {

			}
		}
		throw new ParseException("Invalid date format: " + dateStr + ". Supported formats: " + Arrays.toString(Constants.ONEM2M_MULTI_DATE_FORMATS), 0);
	}
}

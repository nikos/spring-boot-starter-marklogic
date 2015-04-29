package de.nava.marklogic.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {

    public static final String ISO_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

    public static final String DATE_FORMAT = "yyyy-MM-dd";

    public static Date getFirstDateOfCurrentYear() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_YEAR, 1);
        return cal.getTime();
    }

    public static Date getDateOffsetByDays(int days) {
        Calendar cal = Calendar.getInstance();
        cal.add( Calendar.DAY_OF_YEAR, days);
        return cal.getTime();
    }

    public static String toDateString(Date date) {
        return new SimpleDateFormat(DATE_FORMAT).format(date);
    }

    public static Date toDate(String s) throws ParseException {
        return new SimpleDateFormat(DATE_FORMAT).parse(s);
    }

}

package com.niemisami.movies.utilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Sami on 29.1.2017.
 */

public class DateUtils {

    //"mm/dd/yyyy"
    public static Date stringToDate(String dateStringFormat, String dateString) {
        try {
            return new SimpleDateFormat(dateStringFormat).parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String dateToString(String dateFormat, Date date) {
        return new SimpleDateFormat(dateFormat).format(date);
    }
}

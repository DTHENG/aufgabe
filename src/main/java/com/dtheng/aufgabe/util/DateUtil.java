package com.dtheng.aufgabe.util;

import rx.Observable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
public class DateUtil {

    private static final SimpleDateFormat MYSQL_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
    private static final SimpleDateFormat USER_FORMAT = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");

    public static Observable<Date> parse(String string) {
        try {
            return Observable.just(MYSQL_FORMAT.parse(string));
        } catch (ParseException pe) {
            return Observable.error(pe);
        }
    }

    public static String toString(Date date) {
        return USER_FORMAT.format(date);
    }
}

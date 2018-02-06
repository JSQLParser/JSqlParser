package net.sf.jsqlparser.expression;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.junit.Test;

import net.sf.jsqlparser.JSQLParserException;

/**
 * @author Linyu Chen
 */
public class TimestampValueTest {

    @Test
    public void testTimestampValue_issue525() throws JSQLParserException {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        TimestampValue tv = new TimestampValue(currentDate);
        System.out.println(tv.toString());
    }

    @Test
    public void testTimestampValueWithQuotation_issue525() throws JSQLParserException {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        TimestampValue tv = new TimestampValue("'" + currentDate + "'");
        System.out.println(tv.toString());
    }
}

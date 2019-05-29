package tech.stacktrace.jrodns.pac; /**
 *
 */

import java.util.regex.Pattern;

/**
 * @author yinqiwen
 *
 */
public class UrlRegexRule implements GFWListRule {
    public static int count = 0;
    public boolean is_raw_regex;
    public Pattern urlReg;

    @Override
    public boolean init(String rule) {
        count++;
        if (is_raw_regex) {
            urlReg = Pattern.compile(rule);
        } else {
            urlReg = StringHelper.prepareRegexPattern(rule);
        }
        return true;
    }

    @Override
    public boolean match(String host) {
        return urlReg.matcher(host).matches();
    }

}
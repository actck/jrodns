package tech.stacktrace.jrodns.pac; /**
 *
 */

/**
 * @author yinqiwen
 *
 */
public class UrlWildcardRule implements GFWListRule {

    public static int count = 0;
    public String urlRule;

    @Override
    public boolean init(String rule) {
        count++;
        if (!rule.contains("*")) {
            rule = "*" + rule;
        }
        urlRule = rule;
        return true;
    }

    @Override
    public boolean match(String host) {
        return StringHelper.wildCardMatch(host, urlRule);
    }
}
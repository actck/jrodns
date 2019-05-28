package tech.stacktrace.jrodns.pac; /**
 *
 */

/**
 * @author yinqiwen
 *
 */
public class UrlWildcardRule implements GFWListRule {
    public String urlRule;

    @Override
    public boolean init(String rule) {
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
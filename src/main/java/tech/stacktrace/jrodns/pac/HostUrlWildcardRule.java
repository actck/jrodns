package tech.stacktrace.jrodns.pac; /**
 *
 */

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author yinqiwen
 *
 */
public class HostUrlWildcardRule implements GFWListRule {

    public static int count = 0;
    public boolean onlyHttp;
    private String urlRule;
    private String origin;

    private Pattern hostPattern;
    private String hostRule;
    private void initHostPattern(String rule) {

        this.hostRule = rule;

        String ruleQuote = "";

        String[] parts = hostRule.split("\\*");
        if(parts.length == 1) {
            ruleQuote = Pattern.quote(rule);
        } else {
            for (int i = 0; i < parts.length; i++) {
                ruleQuote += Pattern.quote(parts[i]);
                if(i < parts.length - 1) {
                    ruleQuote += "\\S*";
                }
            }
        }
        // 只匹配某一级的域名，忽略上级或者下级
        hostPattern = Pattern.compile("(^|\\.)" + ruleQuote + "(\\.|$)");
    }

    @Override
    public boolean init(String rule) {
        count++;
        origin = rule;
        if (!rule.contains("/")) {
            initHostPattern(rule);
            return true;
        }
        String[] rules = rule.split("/", 2);
        initHostPattern(rules[0]);
        if (rules.length == 2) {
            urlRule = rules[1];
        }
        return true;
    }

    @Override
    public boolean match(String host) {
        if (hostRule != null) {
            return hostPattern.matcher(host).find();
        }
        return false;
    }

    public static void main(String[] args) {

        String quote = Pattern.quote("g.com");

        Pattern hostPattern = Pattern.compile("(^|\\.)" + quote + "(\\.|$)");

        Matcher m = hostPattern.matcher("timgmb04.bdimg.com");

        System.out.println(m.find());

        HostUrlWildcardRule hostUrlWildcardRule = new HostUrlWildcardRule();
        hostUrlWildcardRule.init("cdn*.i-scmp.com");
        System.out.println(hostUrlWildcardRule.match("cdn.i-scmp.com"));
    }
}
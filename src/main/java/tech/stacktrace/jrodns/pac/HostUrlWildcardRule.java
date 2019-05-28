package tech.stacktrace.jrodns.pac; /**
 *
 */

/**
 * @author yinqiwen
 *
 */
public class HostUrlWildcardRule implements GFWListRule {
    public boolean onlyHttp;
    public String hostRule;
    public String urlRule;
    private String origin;

    @Override
    public boolean init(String rule) {
        origin = rule;
        if (!rule.contains("/")) {
            hostRule = rule;
            return true;
        }
        String[] rules = rule.split("/", 2);
        hostRule = rules[0];
        if (rules.length == 2) {
            urlRule = rules[1];
        }
        return true;
    }

    @Override
    public boolean match(String host) {

        /*URL uri;
        try {
            uri = new URL(host);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }*/


        if (hostRule != null) {
            return StringHelper.wildCardMatch(host, hostRule);
        }

        /*if (null != urlRule) {
            return StringHelper.wildCardMatch(uri.getPath(), urlRule);
        }*/

        //System.out.println("###WildcardRule for " + HttpHeaders.getHost(req) +  "##" + origin);
        return false;
    }
}
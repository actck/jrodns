package tech.stacktrace.jrodns.pac; /**
 *
 */

/**
 * @author yinqiwen
 *
 */
public interface GFWListRule {
    public boolean init(String rule);

    public boolean match(String host);
}
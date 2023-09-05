package com.mdc.mcat.engine.mapping;

import java.util.*;
import java.util.regex.Pattern;

public abstract class AbstractMapping implements Comparable<AbstractMapping> {
    protected Set<String> urlStrs = new HashSet<>();
    protected Set<Pattern> patterns = new HashSet<>();
    protected int priority;
    protected boolean isDefault = false;

    public AbstractMapping() {
        this(new String[]{});
    }

    public AbstractMapping(String[] patternUrls) {
        for (var url : patternUrls) {
            if ("/".equals(url)) {
                isDefault = true;
                break;
            }
            urlStrs.add(url);
            patterns.add(buildPattern(url));
        }
    }

    protected Pattern buildPattern(String patternUrl) {
        // 满足tomcat的url匹配规则
        // 1 精确路径匹配（不包含*符号）
        if (!patternUrl.contains("*")) {
            priority = patternUrl.length();
            return Pattern.compile(patternUrl);
        } else {
            // 通配符匹配
            StringBuilder sb = new StringBuilder();
            for (char ch : patternUrl.toCharArray()) {
                if (ch == '*') {
                    sb.append(".*");
                } else if (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch >= '0' && ch <= '9') {
                    sb.append(ch);
                } else {
                    sb.append('\\').append(ch);
                }
            }
            priority = sb.toString().length();
            return Pattern.compile(sb.toString());
        }
    }

    @Override
    public int compareTo(AbstractMapping abstractMapping) {
        return priority - abstractMapping.priority;
    }

    public boolean match(String uri) {
        return patterns.stream().anyMatch(p -> p.matcher(uri).matches());
    }

    public Collection<String> getMappings() {
        return urlStrs;
    }

    public int getPriority() {
        return priority;
    }

    public boolean getIsDefault() {
        return isDefault;
    }

    public boolean addMapping(String url) {
        if (urlStrs.contains(url)) {
            return false;
        }
        urlStrs.add(url);
        patterns.add(buildPattern(url));
        return true;
    }
}

package com.mdc.mcat.engine.mapping;

import java.util.regex.Pattern;

public abstract class AbstractMapping implements Comparable<AbstractMapping> {
    private int priority;
    private Pattern pattern;

    @Override
    public int compareTo(AbstractMapping abstractMapping) {
        return 0;
    }
}

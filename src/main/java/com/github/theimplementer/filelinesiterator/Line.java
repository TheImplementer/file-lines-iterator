package com.github.theimplementer.filelinesiterator;

import static java.lang.String.format;

public class Line {

    private final String content;
    private final int length;

    private Line(String content, int length) {
        this.content = content;
        this.length = length;
    }

    public static Line line(String content, int length) {
        return new Line(content, length);
    }

    public String getContent() {
        return content;
    }

    public int getLength() {
        return length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Line line = (Line) o;

        if (length != line.length) return false;
        return content.equals(line.content);
    }

    @Override
    public int hashCode() {
        int result = content.hashCode();
        result = 31 * result + length;
        return result;
    }

    @Override
    public String toString() {
        return format("%s:%d", content, length);
    }
}

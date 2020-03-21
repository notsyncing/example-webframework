package org.example.webframework.lesson5.file;

public class Range {
    private long start;
    private long end;

    public Range(long start, long end) {
        this.start = start;
        this.end = end;
    }

    public static Range parse(String str, long end) {
        final var index = str.indexOf('-');
        final var start = Long.parseLong(str.substring(0, index));

        if (index < str.length() - 1) {
            end = Long.parseLong(str.substring(index + 1));
        }

        return new Range(start, end);
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public void setIfNoEnd(long end) {
        if (this.end < 0) {
            this.end = end;
        }
    }

    @Override
    public String toString() {
        return start + "-" + (end < 0 ? "" : end);
    }

    public long getLength() {
        if (end < 0) {
            return -1;
        }

        return end - start + 1;
    }

    public boolean inRange(long length) {
        if (end < 0) {
            return true;
        }

        return end < length;
    }
}

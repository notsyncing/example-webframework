package org.example.webframework.lesson10;

public class Cookie {
    private String name;
    private String value;
    private boolean maxAgeSet;
    private long maxAge;
    private String domain;
    private String path;
    private boolean secure;
    private boolean httpOnly;

    public Cookie() {
    }

    public Cookie(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public long getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(long maxAge) {
        this.maxAge = maxAge;
        maxAgeSet = true;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public boolean isHttpOnly() {
        return httpOnly;
    }

    public void setHttpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
    }

    @Override
    public String toString() {
        final var s = new StringBuilder();

        s.append(getName())
                .append("=")
                .append(getValue());

        if (maxAgeSet) {
            s.append("; Max-Age=").append(getMaxAge());
        }

        if (domain != null && !domain.isEmpty()) {
            s.append("; Domain=").append(domain);
        }

        if (path != null && !path.isEmpty()) {
            s.append("; Path=").append(path);
        }

        if (isSecure()) {
            s.append("; Secure");
        }

        if (isHttpOnly()) {
            s.append("; HttpOnly");
        }

        return s.toString();
    }
}

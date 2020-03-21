package org.example.webframework.lesson12.routing;

import org.example.webframework.lesson12.HttpMethod;

import java.net.URL;
import java.util.Map;
import java.util.regex.Pattern;

public class RegexRoute extends Route {
    private Pattern regex;
    private Map<String, Integer> namedGroups;

    public RegexRoute(HttpMethod method, Pattern regex) {
        super(method);
        this.regex = regex;
        fetchNamedGroups();
    }

    public RegexRoute(HttpMethod method, String pattern) {
        this(method, Pattern.compile(pattern));
    }

    private void fetchNamedGroups() {
        try {
            final var method = regex.getClass().getDeclaredMethod("namedGroups");
            method.setAccessible(true);
            namedGroups = (Map<String, Integer>) method.invoke(regex);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public MatchedRoute matchUrl(URL url) {
        final var path = url.getPath();
        final var matcher = regex.matcher(path);

        if (!matcher.find()) {
            return null;
        }

        final var mr = new MatchedRoute(url, this);

        for (final var groupName : namedGroups.keySet()) {
            mr.addMatchedParameter(groupName, matcher.group(groupName));
        }

        return mr;
    }
}

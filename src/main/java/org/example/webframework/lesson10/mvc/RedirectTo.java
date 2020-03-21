package org.example.webframework.lesson10.mvc;

import org.example.webframework.lesson10.Context;
import org.example.webframework.lesson10.HttpMethod;

public class RedirectTo implements MvcResult {
    private String url;
    private boolean permanently;
    private boolean canFallbackToGet;

    public RedirectTo(String url, boolean permanently, boolean canFallbackToGet) {
        this.url = url;
        this.permanently = permanently;
        this.canFallbackToGet = canFallbackToGet;
    }

    public RedirectTo(String url, boolean permanently) {
        this(url, permanently, false);
    }

    public RedirectTo(String url) {
        this(url, false);
    }

    @Override
    public void process(Context context, MvcHandler handler) throws Exception {
        var statusCode = permanently ? 301 : 302;

        if (!canFallbackToGet && context.getRequest().getMethod() != HttpMethod.GET) {
            statusCode = permanently ? 308 : 307;
        }

        if (!url.contains("://") && !url.startsWith("/")) {
            var prefix = context.getRequest().getUrl().getPath();
            final var index = prefix.lastIndexOf('/');

            if (index >= 0) {
                prefix = prefix.substring(0, index);
            }

            if (!prefix.endsWith("/")) {
                prefix += "/";
            }

            url = prefix + url;
        }

        context.getResponse()
                .setStatusCode(statusCode)
                .addHeader("Location", url)
                .setContentLength(0)
                .end();
    }
}

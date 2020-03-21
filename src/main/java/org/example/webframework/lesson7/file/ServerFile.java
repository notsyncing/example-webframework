package org.example.webframework.lesson7.file;

import org.example.webframework.lesson7.Context;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ServerFile {
    private Path path;
    private String newName;

    public ServerFile(Path path) {
        this.path = path.toAbsolutePath().normalize();
    }

    public ServerFile setNewName(String newName) {
        this.newName = newName;
        return this;
    }

    public void send(Context context) throws IOException {
        if (!Files.exists(path)) {
            return;
        }

        final var lastModified = Files.getLastModifiedTime(path).toInstant();
        final var ifModifiedSince = context.getRequest().getHeaderDateValue("If-Modified-Since");

        if (ifModifiedSince != null && lastModified.getEpochSecond() <= ifModifiedSince.getEpochSecond()) {
            context.getResponse().endWithStatusCode(304);
            return;
        }

        final var ifRange = context.getRequest().getHeaderDateValue("If-Range");
        final var ranges = context.getRequest().getRanges();

        if (ifRange != null && lastModified.getEpochSecond() <= ifRange.getEpochSecond()) {
            ranges.clear();
        }

        final var length = Files.size(path);
        final var contentType = Files.probeContentType(path);

        if (ranges.isEmpty()) {
            final var resp = context.getResponse()
                    .setStatusCode(200)
                    .setContentType(contentType)
                    .setContentLength(length)
                    .setDateHeader("Last-Modified", lastModified)
                    .setHeader("Accept-Ranges", "bytes");

            if (newName != null) {
                resp.setHeader("Content-Disposition",
                        "attachment; filename*=UTF-8''" + URLEncoder.encode(newName, StandardCharsets.UTF_8));
            }

            resp.sendFile(path);
        } else {
            if (!checkRanges(ranges, length)) {
                context.getResponse().endWithStatusCode(406);
                return;
            }

            context.getResponse()
                    .setContentType(contentType)
                    .setDateHeader("Last-Modified", lastModified)
                    .sendFilePartially(path, ranges);
        }
    }

    private boolean checkRanges(List<Range> ranges, long length) {
        return ranges.stream().allMatch(r -> r.inRange(length));
    }
}

package org.example.webframework.lesson11.mvc;

import org.example.webframework.lesson11.Context;

public interface MvcResult {
    void process(Context context, MvcHandler handler) throws Exception;
}

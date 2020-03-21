package org.example.webframework.lesson12.mvc;

import org.example.webframework.lesson12.Context;

public interface MvcResult {
    void process(Context context, MvcHandler handler) throws Exception;
}

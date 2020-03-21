package org.example.webframework.lesson9.mvc;

import org.example.webframework.lesson9.Context;

public interface MvcResult {
    void process(Context context, MvcHandler handler) throws Exception;
}

package org.example.webframework.lesson10.mvc;

import org.example.webframework.lesson10.Context;

public interface MvcResult {
    void process(Context context, MvcHandler handler) throws Exception;
}

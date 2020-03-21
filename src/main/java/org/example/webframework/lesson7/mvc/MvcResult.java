package org.example.webframework.lesson7.mvc;

import org.example.webframework.lesson7.Context;

public interface MvcResult {
    void process(Context context, MvcHandler handler) throws Exception;
}

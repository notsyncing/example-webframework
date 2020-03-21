package org.example.webframework.lesson8.mvc;

import org.example.webframework.lesson8.Context;

public interface MvcResult {
    void process(Context context, MvcHandler handler) throws Exception;
}

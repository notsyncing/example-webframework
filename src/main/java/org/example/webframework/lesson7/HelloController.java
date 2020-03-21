package org.example.webframework.lesson7;

import org.example.webframework.lesson7.mvc.*;

@MvcRoute("/")
public class HelloController {
    @MvcRoute("order")
    public View order() {
        return new View("order.template");
    }

    @MvcRoute(value = "submit", methods = HttpMethod.POST)
    public View submit(@Param("name") String name, @Param("mobile") String mobile, @Param("address") String address) {
        final var model = new OrderInfo();
        model.setName(name);
        model.setMobile(mobile);
        model.setAddress(address);

        return new View("order-info.template", model);
    }

    @MvcRoute("order/{id}")
    @CacheControl("no-cache, no-store")
    public Json data(@Param("id") String id) {
        final var model = new OrderInfo();
        model.setName("Order " + id);
        model.setMobile("123456");
        model.setAddress("abcdef");

        return new Json(model);
    }

    @MvcRoute("download")
    public File file() {
        return new File("static/index.html", "测试.txt");
    }
}

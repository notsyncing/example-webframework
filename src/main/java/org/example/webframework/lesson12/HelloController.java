package org.example.webframework.lesson12;

import org.example.webframework.lesson12.ioc.Container;
import org.example.webframework.lesson12.mvc.*;

import javax.inject.Named;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Named
@MvcRoute("/")
public class HelloController implements Controller {
    private final Container container;

    public HelloController(Container container) {
        this.container = container;
    }

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

    @MvcRoute(value = "order/{id}", methods = { HttpMethod.GET, HttpMethod.POST })
    @CacheControl("no-cache, no-store")
    public Json data(@Param("id") String id) {
        final var model = new OrderInfo();
        model.setName("Order " + id);
        model.setMobile("123456");
        model.setAddress("abcdef");

        return new Json(model);
    }

    @MvcRoute("data")
    @Cors(headers = "x-a")
    public Json data2(@Param("name") String name) {
        Map<String, String> data = new HashMap<>();
        data.put("name", name);

        return new Json(data);
    }

    @MvcRoute("orderR")
    public MvcResult redirectToOrder() {
        return new RedirectTo("order");
    }

    @MvcRoute("redirect/orderA")
    public MvcResult redirectToOrderA() {
        return new RedirectTo("/order");
    }

    @MvcRoute("redirect/data")
    public MvcResult redirectToData() {
        return new RedirectTo("/order/1");
    }

    @MvcRoute("redirect/data2")
    public MvcResult redirectToData2() {
        return new RedirectTo("/data");
    }

    @MvcRoute("redirect/data2n")
    public MvcResult redirectToData2N() {
        return new RedirectTo("/data?name=a");
    }

    @MvcRoute("forward/order")
    public MvcResult forwardToOrder() {
        return new ForwardTo("/order");
    }

    @MvcRoute("forward/data")
    public MvcResult forwardToData() {
        return new ForwardTo("/order/2");
    }

    @MvcRoute("forward/data2")
    public MvcResult forwardToData2() {
        return new ForwardTo("/data");
    }

    @MvcRoute("image/upload")
    public View uploadImage() {
        return new View("upload.template");
    }

    @MvcRoute(value = "upload", methods = HttpMethod.POST)
    public Json upload(@Param("name") String name, Context context) {
        final var data = new HashMap<String, Object>();
        data.put("name", name);

        data.put("files", context.getRequest().getUploadedFiles()
                .stream()
                .map(f -> {
                    final var map = new HashMap<String, String>();
                    map.put("name", f.getName());
                    map.put("filename", f.getFilename());
                    map.put("path", f.getPath().toString());
                    return map;
                })
                .collect(Collectors.toList()));

        return new Json(data);
    }

    @MvcRoute("scopes")
    public Json scopeTests() throws IllegalAccessException, InstantiationException, InvocationTargetException {
        final var data = new HashMap<String, Object>();
        data.put("normal1", String.valueOf(container.get(NormalObject.class)));
        data.put("normal2", String.valueOf(container.get(NormalObject.class)));
        data.put("request1", String.valueOf(container.get(RequestScopedObject.class)));
        data.put("request2", String.valueOf(container.get(RequestScopedObject.class)));
        data.put("session", String.valueOf(container.get(SessionScopedObject.class)));
        data.put("singleton", String.valueOf(container.get(SingletonObject.class)));

        return new Json(data);
    }
}

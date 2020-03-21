package org.example.webframework.lesson8.mvc;

import org.example.webframework.lesson8.Context;
import org.example.webframework.lesson8.Handler;
import org.example.webframework.lesson8.HandlerChain;
import org.example.webframework.lesson8.routing.*;
import org.example.webframework.lesson8.template.TemplateEngine;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.ArrayList;

public class MvcHandler implements Handler {
    private final Router router = new Router();
    private final TemplateEngine templateEngine = new TemplateEngine();

    public MvcHandler() throws IOException {
        templateEngine.registerTemplateDirectory(Paths.get("static"));
    }

    public TemplateEngine getTemplateEngine() {
        return templateEngine;
    }

    public Router getRouter() {
        return router;
    }

    public MvcHandler registerController(Class<?> controllerClass) {
        scanActions(controllerClass);
        return this;
    }

    private void scanActions(Class<?> controllerClass) {
        final var controllerRouteAnnotation = controllerClass.getAnnotation(MvcRoute.class);
        final var routePrefix = controllerRouteAnnotation == null ? "" : controllerRouteAnnotation.value();

        for (final var action : controllerClass.getMethods()) {
            final var routeAnnotation = action.getAnnotation(MvcRoute.class);

            if (routeAnnotation == null) {
                continue;
            }

            Route route = null;

            for (final var method : routeAnnotation.methods()) {
                if (routeAnnotation.simple()) {
                    route = new SimpleRoute(method, routePrefix + routeAnnotation.value());
                } else {
                    route = new RegexRoute(method, routePrefix + routeAnnotation.value());
                }

                route.setAttachment(new MvcRouteTarget(controllerClass, action));
                router.register(route);
            }
        }
    }

    @Override
    public void handle(Context context, HandlerChain chain) {
        final MatchedRoute matchedRoute;

        try {
            matchedRoute = router.match(context.getRequest().getMethod(), context.getRequest().getUrl());
            context.getRequest().setRoute(matchedRoute);
        } catch (Exception e) {
            e.printStackTrace();

            context.getResponse()
                    .setStatusCode(500)
                    .setContentLength(0)
                    .end();

            return;
        }

        if (matchedRoute == null) {
            chain.nextHandler(context);
            return;
        }

        try {
            if (matchedRoute.getRoute().getMethod().hasBody) {
                final var bodyDecoder = BodyDecoder.factory(context.getRequest().getContentType());
                bodyDecoder.decode(context.getRequest());
            }

            action(matchedRoute, context);
        } catch (Exception e) {
            e.printStackTrace();

            context.getResponse()
                    .setStatusCode(500)
                    .setContentLength(0)
                    .end();

            return;
        }

        chain.nextHandler(context);
    }

    public void action(MatchedRoute matchedRoute, Context context) throws Exception {
        preprocessAction(matchedRoute, context);
        final var result = dispatchAction(matchedRoute, context);
        result.process(context, this);
    }

    private void preprocessAction(MatchedRoute route, Context context) {
        final var target = (MvcRouteTarget) route.getRoute().getAttachment();

        CacheControl cacheControlAnnotation = target.getAction().getAnnotation(CacheControl.class);

        if (cacheControlAnnotation != null) {
            context.getResponse().setHeader("Cache-Control", cacheControlAnnotation.value());
        }
    }

    private MvcResult dispatchAction(MatchedRoute route, Context context) throws IllegalAccessException,
            InvocationTargetException, InstantiationException {
        final var target = (MvcRouteTarget) route.getRoute().getAttachment();
        final var paramValues = new ArrayList<>();

        for (final var param : target.getAction().getParameters()) {
            final var paramAnnotation = param.getAnnotation(Param.class);
            final var paramName = paramAnnotation != null ? paramAnnotation.value() : param.getName();

            Object rawValue = route.getQueryParameter(paramName);

            if (rawValue == null) {
                rawValue = route.getMatchedParameters().get(paramName);
            }

            if (rawValue == null) {
                rawValue = context.getRequest().getParameter(paramName);
            }

            Object paramValue = null;

            if (rawValue != null) {
                paramValue = String.valueOf(rawValue);
            }

            paramValues.add(paramValue);
        }

        final var controller = target.getControllerClass().getConstructors()[0].newInstance();
        return (MvcResult) target.getAction().invoke(controller, paramValues.toArray());
    }
}

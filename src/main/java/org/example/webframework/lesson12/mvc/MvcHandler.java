package org.example.webframework.lesson12.mvc;

import org.example.webframework.lesson12.Context;
import org.example.webframework.lesson12.Handler;
import org.example.webframework.lesson12.HandlerChain;
import org.example.webframework.lesson12.cors.CorsConfig;
import org.example.webframework.lesson12.cors.CorsHandler;
import org.example.webframework.lesson12.ioc.Container;
import org.example.webframework.lesson12.routing.*;
import org.example.webframework.lesson12.template.TemplateEngine;

import javax.inject.Named;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

@Named
public class MvcHandler implements Handler {
    private final Container container;
    private final Router router;
    private final TemplateEngine templateEngine;

    public MvcHandler(Container container, Router router, TemplateEngine templateEngine) {
        this.container = container;
        this.router = router;
        this.templateEngine = templateEngine;

        container.getRegistry().getListOfType(Controller.class)
                .forEach(this::registerController);
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

            Cors corsAnnotation = action.getAnnotation(Cors.class);

            if (corsAnnotation != null) {
                CorsHandler.addConfig(route, new CorsConfig()
                        .allowHeader(corsAnnotation.headers())
                        .allowCredentials(corsAnnotation.credentials())
                        .allowMethods(corsAnnotation.methods())
                        .allowOrigins(corsAnnotation.origins())
                        .exposeHeader(corsAnnotation.exposeHeaders())
                        .maxAge(corsAnnotation.maxAge()));
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
                try (final var bodyDecoder = BodyDecoder.factory(context.getRequest().getContentType())) {
                    bodyDecoder.decode(context.getRequest());
                }
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
            } else {
                if (param.getType().isAssignableFrom(Context.class)) {
                    paramValue = context;
                }
            }

            paramValues.add(paramValue);
        }

        final var controller = container.get(target.getControllerClass());
        return (MvcResult) target.getAction().invoke(controller, paramValues.toArray());
    }
}

package com.ote.test;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import io.swagger.models.HttpMethod;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.discovery.DiscoveryClientRouteLocator;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Optional;

@Slf4j
public class ZuulFilterOnServerSwagger extends ZuulFilter {

    @Value("${zuul.routes.server.swagger-uri}")
    private String swaggerUri;

    private static final String ContextPath = "/server";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DiscoveryClientRouteLocator routeLocator;

    /**
     * - pre filters are executed before the request is routed,
     * - route filters can handle the actual routing of the request,
     * - post filters are executed after the request has been routed,
     * - error filters execute if an error occurs in the course of handling the request.
     */
    @Override
    public String filterType() {
        return "route";
    }

    @Override
    public int filterOrder() {
        return 1;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {

        RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletRequest request = requestContext.getRequest();

        String requestUri = URI.create(request.getRequestURI()).getPath();

        if (requestUri.startsWith(ContextPath)) {

            String apiUri = requestUri.replaceAll(ContextPath, "");

            Route matchingRoute = routeLocator.getMatchingRoute(ContextPath);
            String location = matchingRoute.getLocation();
            HttpMethod method = HttpMethod.valueOf(request.getMethod());

            Swagger swagger = new SwaggerParser().read(location + swaggerUri);

            if (!Optional.ofNullable(swagger.getPath(apiUri)).
                    filter(p -> p.getOperationMap().containsKey(method)).isPresent()) {
                log.info(String.format("%s request to %s REJECTED", request.getMethod(), request.getRequestURL().toString()));
                return respondNotFound(requestContext);
            }

            log.info(String.format("%s request to %s", request.getMethod(), request.getRequestURL().toString()));

        }
        return null;
    }

    private Object respondNotFound(RequestContext requestContext) {
        requestContext.unset();
        requestContext.getResponse().setContentType(MediaType.TEXT_HTML_VALUE);
        requestContext.setResponseStatusCode(HttpStatus.NOT_FOUND.value());
        requestContext.setSendZuulResponse(false);
        return null;
    }
}

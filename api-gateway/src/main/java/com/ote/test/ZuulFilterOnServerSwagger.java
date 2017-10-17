package com.ote.test;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.discovery.DiscoveryClientRouteLocator;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Map;

@Slf4j
public class ZuulFilterOnServerSwagger extends ZuulFilter {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DiscoveryClientRouteLocator routeLocator;

    /**
     * - pre filters are executed before the request is routed,
     * - route filters can handle the actual routing of the request,
     * - post filters are executed after the request has been routed,
     * - error filters execute if an error occurs in the course of handling the request.
     *
     * @return
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

        String zuulServerContextPath = "/server";

        RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletRequest request = requestContext.getRequest();

        String requestUri = URI.create(request.getRequestURI()).getPath();
        String requestPrefix = requestUri.replaceAll(zuulServerContextPath, "");

        Route matchingRoute = routeLocator.getMatchingRoute(zuulServerContextPath);
        String location = matchingRoute.getLocation();

        log.info(String.format("%s request to %s", request.getMethod(), request.getRequestURL().toString()));
        try {
            // Call swagger for server
            Map serverSwagger = restTemplate.getForObject(location + "/v2/api-docs", Map.class);

            // Get all paths
            Map paths = (Map) serverSwagger.get("paths");

            // Check URI exists
            Map uri = (Map) paths.get(requestPrefix);
            if (uri == null) {
                return respondNotFound(requestContext);
            }

            // Check method available for this URI
            Map method = (Map) uri.get(request.getMethod().toLowerCase());
            if (method == null) {
                return respondNotFound(requestContext);
            }
        } catch (Exception e) {
            return respondNotFound(requestContext);
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

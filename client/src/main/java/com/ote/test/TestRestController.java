package com.ote.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping(value = "/api/v1")
@Slf4j
public class TestRestController {

    @Value(value = "${service.url}")
    private String serviceUrl;

    @Autowired
    private RestTemplate restTemplate;

    private AtomicInteger count = new AtomicInteger(0);

    @CrossOrigin
    @RequestMapping(value = "/remote-test", method = RequestMethod.GET)
    public boolean ping() {
        log.info("Ping #" + count.getAndIncrement());
        return restTemplate.getForObject(serviceUrl + "/api/v1/test", Boolean.class);
    }
}

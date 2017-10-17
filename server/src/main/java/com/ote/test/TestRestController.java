package com.ote.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping(value = "/api/v1")
@Slf4j
public class TestRestController {

    private AtomicInteger count = new AtomicInteger(0);

    @CrossOrigin
    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public boolean ping() {
        log.info("Ping #" + count.getAndIncrement());
        return true;
    }
}

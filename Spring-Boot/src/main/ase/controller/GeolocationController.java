package ase.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
public class GeolocationController {

    @RequestMapping("/")
    public String locationMock() {
        return "[EXAMPLE COORDINATES]";
    }
}

package ec2springboot.test;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController {

    @GetMapping(value = "/", produces = "text/plain")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Welcome");
    }


}

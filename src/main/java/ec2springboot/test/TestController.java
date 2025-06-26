package ec2springboot.test;

import ec2springboot.test.db.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class TestController {

    @Autowired
    private FileService fileService;

    @Autowired
    private UserService userService;

    @GetMapping(value = "/", produces = "text/plain")
    public String test() {
        return "Welcome";
    }

    @PostMapping("/upload")
    public void upload(@RequestParam("file") MultipartFile file) throws IOException {
        fileService.uploadFile(file.getOriginalFilename(), file.getBytes());
    }

    @GetMapping(value = "/{id}", produces = "text/plain")
    public String getDetails(@PathVariable int id) {
        return userService.getUserName(id);
    }


}

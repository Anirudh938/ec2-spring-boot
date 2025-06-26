package ec2springboot.test.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepo userRepo;

    public String getUserName(Integer id) {
        Optional<User> user =  userRepo.findById(id);
        return user.map(User::getName).orElse("not found");
    }
}

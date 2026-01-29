package practice.javal1.exception.user;

public class UsernameExist extends RuntimeException {
    public UsernameExist(String message) {
        super(message);
    }
}

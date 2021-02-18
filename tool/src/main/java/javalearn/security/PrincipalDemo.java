package javalearn.security;

import javax.security.auth.Subject;
import java.security.Principal;

public class PrincipalDemo implements Principal {
    private String name;

    public PrincipalDemo(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}

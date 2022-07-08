package models;

import lombok.Data;

@Data
public class User {
    private final String name;
    private final String login;
    private final String password;

}

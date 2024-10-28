package user;

import java.sql.Date;

public class User {
    private String email;
    private String name;
    private Date dob;
    private String user_type;

    public User(String email, String name, Date dob, String user_type) {
        this.email = email;
        this.name = name;
        this.dob = dob;
        this.user_type = user_type;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public Date getDob() {
        return dob;
    }
    
    public String getuser_type() {
        return user_type;
    }
}


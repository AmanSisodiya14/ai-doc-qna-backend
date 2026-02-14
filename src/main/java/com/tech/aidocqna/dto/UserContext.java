package com.tech.aidocqna.dto;

import com.tech.aidocqna.model.Role;
import lombok.Data;


@Data
public class UserContext {

    private static final ThreadLocal<UserContext> CONTEXT = new ThreadLocal<>();
    private String name;
    private String email;
    private Role role;


    // --- ThreadLocal accessors ---
    public static void set(UserContext context) {
        CONTEXT.set(context);
    }

    public static UserContext get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}

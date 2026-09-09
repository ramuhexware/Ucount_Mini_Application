package com.freddieapp.loginfrontend.specification;

import com.freddieapp.loginfrontend.enums.LoginState;

public class LoginSpecification {

    public static boolean isSuccessful(LoginState state) {
        return state == LoginState.SUCCESS;
    }
}

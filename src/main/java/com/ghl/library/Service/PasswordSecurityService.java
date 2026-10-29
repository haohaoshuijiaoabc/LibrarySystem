package com.ghl.library.Service;

public interface PasswordSecurityService {
    void handleFailure(Integer id);


    void resetErrorCount(Integer id);
}

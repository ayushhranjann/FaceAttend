package com.faceattend.dao;

import com.faceattend.model.User;
import java.sql.SQLException;

public interface UserDAO {
    User authenticate(String username, String password) throws SQLException;
}

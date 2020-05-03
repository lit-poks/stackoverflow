package com.upgrad.stackoverflow.service.business;


import com.upgrad.stackoverflow.service.dao.CommonDao;
import com.upgrad.stackoverflow.service.dao.UserDao;
import com.upgrad.stackoverflow.service.entity.UserAuthEntity;
import com.upgrad.stackoverflow.service.entity.UserEntity;
import com.upgrad.stackoverflow.service.exception.AuthorizationFailedException;
import com.upgrad.stackoverflow.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommonBusinessService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private CommonDao commonDao;

    /**
     * The method implements the business logic for userProfile endpoint.
     */
    //this function retrieves the user by uuid
    public UserEntity getUser(String uuid, String authorization) throws UserNotFoundException, AuthorizationFailedException {

        final UserAuthEntity userAuthEntity = userDao.getUserAuthByAccesstoken(authorization);
        if(userAuthEntity==null){
            throw new AuthorizationFailedException("ATHR-001","User has not signed in");
        }

        if(userAuthEntity.getLogoutAt()!=null){
            throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to get user details");
        }

        final UserEntity userEntity = commonDao.getUserByUuid(uuid);
        // if the user with the paticular uuid does not exits in the database the exception is thrown
        if(userEntity==null){
            throw new UserNotFoundException("USR-001","User with entered uuid does not exist");
        }

        return userEntity;

    }
}

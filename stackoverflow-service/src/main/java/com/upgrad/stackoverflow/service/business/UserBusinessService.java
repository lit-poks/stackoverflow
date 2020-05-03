package com.upgrad.stackoverflow.service.business;

import com.upgrad.stackoverflow.service.common.JwtTokenProvider;
import com.upgrad.stackoverflow.service.dao.UserDao;
import com.upgrad.stackoverflow.service.entity.UserAuthEntity;
import com.upgrad.stackoverflow.service.entity.UserEntity;
import com.upgrad.stackoverflow.service.exception.AuthenticationFailedException;
import com.upgrad.stackoverflow.service.exception.SignOutRestrictedException;
import com.upgrad.stackoverflow.service.exception.SignUpRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.UUID;

@Service
public class UserBusinessService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordCryptographyProvider passwordCryptographyProvider;

    /**
     * The method implements the business logic for signup endpoint.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public UserEntity signup(UserEntity userEntity) throws SignUpRestrictedException {
        String[] encryptedText = passwordCryptographyProvider.encrypt(userEntity.getPassword());
        userEntity.setSalt(encryptedText[0]);
        userEntity.setPassword(encryptedText[1]);
        userEntity.setRole("nonadmin");

        //if the user has entered a email id that already exists in the database
        if(userDao.getUserByEmail(userEntity.getEmail())!=null){
            throw new SignUpRestrictedException("SGR-002","This user has already been registered, try with any other emailId");
        }

        //f the user tries to use a username that is already present in the database the exception is thrown
        if(userDao.getUserByUsername(userEntity.getUserName())!=null){
            throw new SignUpRestrictedException("SGR-001","Try any other Username, this Username has already been taken");
        }

        return userDao.createUser(userEntity);
    }

    /**
     * The method implements the business logic for signin endpoint.
     * it authenticates if the username or password corresponds to the paticular user
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public UserAuthEntity authenticate(final String username,final String password) throws AuthenticationFailedException {

        UserEntity userEntity = userDao.getUserByUsername(username);
        if(userEntity==null){
            throw new AuthenticationFailedException("ATH-001","This username does not exist");
        }

        final String encryptedPassword=PasswordCryptographyProvider.encrypt(password,userEntity.getSalt());
        if(userEntity.getPassword().equals(encryptedPassword)){
            JwtTokenProvider jwtTokenProvider=new JwtTokenProvider(encryptedPassword);
            UserAuthEntity userAuthEntity=new UserAuthEntity();
            userAuthEntity.setUuid(UUID.randomUUID().toString());
            userAuthEntity.setUser(userEntity);
            ZonedDateTime now=ZonedDateTime.now();
            ZonedDateTime expiresAt=now.plusHours(8);
            userAuthEntity.setAccessToken(jwtTokenProvider.generateToken(userEntity.getUuid(),now,expiresAt));
            userAuthEntity.setExpiresAt(expiresAt);
            userAuthEntity.setLoginAt(now);
            userAuthEntity.setLogoutAt(null);

            final UserAuthEntity createdUserAuthEntity=userDao.createUserAuth(userAuthEntity);
            return createdUserAuthEntity;
        }
        else{
            throw new AuthenticationFailedException("ATH-002","Password failed");
        }


    }

    /**
     * The method implements the business logic for signout endpoint.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public UserAuthEntity signout(String authorization) throws SignOutRestrictedException {

        final UserAuthEntity userAuthEntity = userDao.getUserAuthByAccesstoken(authorization);
        if(userAuthEntity==null){
            throw new SignOutRestrictedException("SGR-001","User is not Signed in");
        }

        userAuthEntity.setLogoutAt(ZonedDateTime.now());
        //logs out the user 
        final UserAuthEntity updatedUserAuth=userDao.updateUserAuth(userAuthEntity);
        return updatedUserAuth;
    }
}

package com.upgrad.stackoverflow.api.controller;

import com.upgrad.stackoverflow.api.model.SigninResponse;
import com.upgrad.stackoverflow.api.model.SignoutResponse;
import com.upgrad.stackoverflow.api.model.SignupUserRequest;
import com.upgrad.stackoverflow.api.model.SignupUserResponse;
import com.upgrad.stackoverflow.service.business.UserBusinessService;
import com.upgrad.stackoverflow.service.entity.UserAuthEntity;
import com.upgrad.stackoverflow.service.entity.UserEntity;
import com.upgrad.stackoverflow.service.exception.AuthenticationFailedException;
import com.upgrad.stackoverflow.service.exception.SignOutRestrictedException;
import com.upgrad.stackoverflow.service.exception.SignUpRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;
import java.util.UUID;
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserBusinessService userBusinessService;

    /**
     * A controller method for user signup.
     *
     * @param signupUserRequest - This argument contains all the attributes required to store user details in the database.
     * @return - ResponseEntity<SignupUserResponse> type object along with Http status CREATED.
     * @throws SignUpRestrictedException
     */

    @RequestMapping(method = RequestMethod.POST,path = "/user/signup",consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignupUserResponse> signup(final SignupUserRequest signupUserRequest) throws SignUpRestrictedException{

        final UserEntity userEntity=new UserEntity();
        userEntity.setUuid(UUID.randomUUID().toString());
        userEntity.setFirstName(signupUserRequest.getFirstName());
        userEntity.setLastName(signupUserRequest.getLastName());
        userEntity.setUserName(signupUserRequest.getUserName());
        userEntity.setEmail(signupUserRequest.getEmailAddress());
        userEntity.setPassword(signupUserRequest.getPassword());
        userEntity.setCountry(signupUserRequest.getCountry());
        userEntity.setAboutMe(signupUserRequest.getAboutMe());
        userEntity.setDob(signupUserRequest.getDob());
        userEntity.setContactNumber(signupUserRequest.getContactNumber());

        final UserEntity createdUserEntity=userBusinessService.signup(userEntity);
        final SignupUserResponse userResponse=new SignupUserResponse().id(createdUserEntity.getUuid()).status("USER SUCCESSFULLY REGISTERED");
        return new ResponseEntity<SignupUserResponse>(userResponse,HttpStatus.CREATED);
    }

    /**
     * A controller method for user authentication.
     *
     * @param authorization - A field in the request header which contains the user credentials as Basic authentication.
     * @return - ResponseEntity<SigninResponse> type object along with Http status OK.
     * @throws AuthenticationFailedException
     */
    @RequestMapping(method = RequestMethod.POST,path = "/user/signin",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SigninResponse> signin(@RequestHeader("authorisation") String authorization) throws AuthenticationFailedException{

        byte[] decode=Base64.getDecoder().decode(authorization.split("Basic")[1]);
        String decoded=new String(decode);
        String userCredentials[]=decoded.split(":");
        final UserAuthEntity userAuthEntity=userBusinessService.authenticate(userCredentials[0],userCredentials[1]);
        final SigninResponse signinResponse=new SigninResponse().id(userAuthEntity.getUuid()).message("SIGNED IN SUCCESSFULLY");

        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.add("access-token",userAuthEntity.getAccessToken());

        return new ResponseEntity<SigninResponse>(signinResponse,httpHeaders,HttpStatus.OK);
    }

    /**
     * A controller method for user signout.
     *
     * @param authorization - A field in the request header which contains the JWT token.
     * @return - ResponseEntity<SignoutResponse> type object along with Http status OK.
     * @throws SignOutRestrictedException
     */
    @RequestMapping(method = RequestMethod.POST,path = "/user/signout",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignoutResponse> signout(@RequestHeader("authorisation") String authorization) throws SignOutRestrictedException{
    byte[] decode=Base64.getDecoder().decode(authorization.split("Bearer")[1]);
    String decodedToken=new String(decode);
    UserAuthEntity userAuthEntity=userBusinessService.signout(decodedToken);
    SignoutResponse signoutResponse=new SignoutResponse().id(userAuthEntity.getUuid()).message("SIGNED OUT SUCCESSFULLY");

    return new ResponseEntity<SignoutResponse>(signoutResponse,HttpStatus.OK);
    }
}

package com.upgrad.stackoverflow.api.controller;

import com.upgrad.stackoverflow.api.model.UserDeleteResponse;
import com.upgrad.stackoverflow.service.business.AdminBusinessService;
import com.upgrad.stackoverflow.service.entity.UserEntity;
import com.upgrad.stackoverflow.service.exception.AuthorizationFailedException;
import com.upgrad.stackoverflow.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminBusinessService adminBusinessService;

    /**
     * A controller method to delete a user in the database.
     *
     * @param userId        - The uuid of the user to be deleted from the database.
     * @param authorization - A field in the request header which contains the JWT token.
     * @return - ResponseEntity<UserDeleteResponse> type object along with Http status OK.
     * @throws AuthorizationFailedException
     * @throws UserNotFoundException
     */
    @RequestMapping(method = RequestMethod.DELETE,path = "/admin/user/{userId}",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UserDeleteResponse> userDelete(@PathVariable("userId")String userId, @RequestHeader("authorization")String authorization)
    throws AuthorizationFailedException, UserNotFoundException {
        //the below commented code is implemented when the authorisation comes in base64encoded form
        //but for this purpose we assume the authorisation code is sent in the header

//        byte[] decode= Base64.getDecoder().decode(authorization.split("Bearer ")[1]);
//        String decodedAuthorization=new String(decode);

        UserEntity userEntity=adminBusinessService.deleteUser(authorization,userId);
        UserDeleteResponse userDeleteResponse=new UserDeleteResponse().id(userEntity.getUuid()).status("USER SUCCESSFULLY DELETED");
        return new ResponseEntity<UserDeleteResponse>(userDeleteResponse, HttpStatus.OK);
    }


}

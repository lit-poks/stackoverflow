package com.upgrad.stackoverflow.api.controller;

import com.upgrad.stackoverflow.api.model.*;
import com.upgrad.stackoverflow.service.business.QuestionBusinessService;
import com.upgrad.stackoverflow.service.entity.QuestionEntity;
import com.upgrad.stackoverflow.service.exception.AuthorizationFailedException;
import com.upgrad.stackoverflow.service.exception.InvalidQuestionException;
import com.upgrad.stackoverflow.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.TypedQuery;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/question")
public class QuestionController {

    @Autowired
    private QuestionBusinessService questionBusinessService;

    /**
     * A controller method to create a question.
     *
     * @param questionRequest - This argument contains all the attributes required to store question details in the database.
     * @param authorization   - A field in the request header which contains the JWT token.
     * @return - ResponseEntity<QuestionResponse> type object along with Http status CREATED.
     * @throws AuthorizationFailedException
     */
    @RequestMapping(method = RequestMethod.POST,path = "/question/create",consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionResponse> createQuestion(QuestionRequest questionRequest,@RequestHeader("authorization") String authorization) throws AuthorizationFailedException{
//        byte[] decode= Base64.getDecoder().decode(authorization.split("Bearer ")[1]);
//        String decodedAuth=new String(decode);

        final QuestionEntity questionEntity=new QuestionEntity();
        questionEntity.setUuid(UUID.randomUUID().toString());
        questionEntity.setContent(questionRequest.getContent());
        questionEntity.setDate(ZonedDateTime.now());
        final QuestionEntity createdQuestionEntity=questionBusinessService.createQuestion(questionEntity,authorization);
        final QuestionResponse questionResponse=new QuestionResponse().id(createdQuestionEntity.getUuid()).status("QUESTION CREATED");

        return new ResponseEntity<QuestionResponse>(questionResponse,HttpStatus.CREATED);
    }

    /**
     * A controller method to fetch all the questions from the database.
     *
     * @param authorization - A field in the request header which contains the JWT token.
     * @return - ResponseEntity<List<QuestionDetailsResponse>> type object along with Http status OK.
     * @throws AuthorizationFailedException
     */
    @RequestMapping(method = RequestMethod.GET,path = "/question/all",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<QuestionDetailsResponse>> getAllQuestions(@RequestHeader("authorization") String authorization) throws AuthorizationFailedException{
//        byte[] decode= Base64.getDecoder().decode(authorization.split("Bearer ")[1]);
//        final String decodedAuthorization=new String(decode);
        final TypedQuery<QuestionEntity> allQuestions=questionBusinessService.getQuestions(authorization);
        final List<QuestionEntity> listOfQuestions=allQuestions.getResultList();
        final List<QuestionDetailsResponse> questionResponseList=new ArrayList<QuestionDetailsResponse>();

        for(QuestionEntity e:listOfQuestions){
            questionResponseList.add(new QuestionDetailsResponse().id(e.getUuid()).content(e.getContent()));
        }

        return new ResponseEntity<List<QuestionDetailsResponse>>(questionResponseList,HttpStatus.OK);
    }

    /**
     * A controller method to edit the question in the database.
     *
     * @param questionEditRequest - This argument contains all the attributes required to edit the question details in the database.
     * @param questionId          - The uuid of the question to be edited in the database.
     * @param authorization       - A field in the request header which contains the JWT token.
     * @return - ResponseEntity<QuestionEditResponse> type object along with Http status OK.
     * @throws AuthorizationFailedException
     * @throws InvalidQuestionException
     */
    @RequestMapping(method = RequestMethod.PUT,path = "/question/edit/{questionId}",consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionEditResponse> editQuestionContent(QuestionEditRequest questionEditRequest,@PathVariable("questionId") String questionId,@RequestHeader("authorization")String authorization)
    throws  AuthorizationFailedException,InvalidQuestionException{
//        byte[] decode= Base64.getDecoder().decode(authorization.split("Bearer ")[1]);
//        final String decodedAuthorization=new String(decode);
            QuestionEntity questionEntity=new QuestionEntity();
            questionEntity.setContent(questionEditRequest.getContent());
            final QuestionEntity editedQuestionEntity=questionBusinessService.editQuestionContent(questionEntity,questionId,authorization);
            QuestionEditResponse questionEditResponse=new QuestionEditResponse().id(editedQuestionEntity.getUuid()).status("QUESTION EDITED");
            return new ResponseEntity<QuestionEditResponse>(questionEditResponse,HttpStatus.OK);
    }
    /**
     * A controller method to delete the question in the database.
     *
     * @param questionId    - The uuid of the question to be deleted in the database.
     * @param authorization - A field in the request header which contains the JWT token.
     * @return - ResponseEntity<QuestionDeleteResponse> type object along with Http status OK.
     * @throws AuthorizationFailedException
     * @throws InvalidQuestionException
     */
    @RequestMapping(method = RequestMethod.DELETE,path = "/question/delete/{questionId}",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionDeleteResponse> deleteQuestion(@RequestHeader("authorization") String authorization,@PathVariable("questionId")String questionId)
    throws AuthorizationFailedException,InvalidQuestionException{
        //the below commented code is implemented when the authorisation comes in base64encoded form
        //but for this purpose we assume the authorisation code is sent in the header

//        byte[] decode= Base64.getDecoder().decode(authorization.split("Bearer ")[1]);
//        final String decodedAuthorization=new String(decode);
        QuestionEntity deletedQuestionEntity=questionBusinessService.deleteQuestion(questionId,authorization);
        QuestionDeleteResponse questionDeleteResponse=new QuestionDeleteResponse().id(deletedQuestionEntity.getUuid()).status("QUESTION DELETED");
        return new ResponseEntity<QuestionDeleteResponse>(questionDeleteResponse,HttpStatus.OK);
    }

    /**
     * A controller method to fetch all the questions posted by a specific user.
     *
     * @param userId        - The uuid of the user whose questions are to be fetched from the database.
     * @param authorization - A field in the request header which contains the JWT token.
     * @return - ResponseEntity<List<QuestionDetailsResponse>> type object along with Http status OK.
     * @throws AuthorizationFailedException
     * @throws UserNotFoundException
     */
    @RequestMapping(method = RequestMethod.GET,path = "question/all/{userId}",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<QuestionDetailsResponse>> getAllQuestionsByUser(@PathVariable("userId")String userId,@RequestHeader("authorization")String authorization)
    throws AuthorizationFailedException,UserNotFoundException{
//        byte[] decode= Base64.getDecoder().decode(authorization.split("Bearer ")[1]);
//        final String decodedAuthorization=new String(decode);
        TypedQuery<QuestionEntity> responseQuery=questionBusinessService.getQuestionsByUser(userId,authorization);
        List<QuestionEntity> questionEntityList=responseQuery.getResultList();
        List<QuestionDetailsResponse> responses=new ArrayList<QuestionDetailsResponse>();

        for(QuestionEntity e:questionEntityList){
            responses.add(new QuestionDetailsResponse().id(e.getUuid()).content(e.getContent()));
        }
        return new ResponseEntity<List<QuestionDetailsResponse>>(responses,HttpStatus.OK);
    }

}

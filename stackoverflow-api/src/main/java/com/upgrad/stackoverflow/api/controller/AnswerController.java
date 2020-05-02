package com.upgrad.stackoverflow.api.controller;

import com.upgrad.stackoverflow.api.model.*;
import com.upgrad.stackoverflow.service.business.AnswerBusinessService;
import com.upgrad.stackoverflow.service.entity.AnswerEntity;
import com.upgrad.stackoverflow.service.entity.QuestionEntity;
import com.upgrad.stackoverflow.service.exception.AnswerNotFoundException;
import com.upgrad.stackoverflow.service.exception.AuthorizationFailedException;
import com.upgrad.stackoverflow.service.exception.InvalidQuestionException;
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
@RequestMapping("/")
public class AnswerController {


    @Autowired
    private AnswerBusinessService answerBusinessService;

    /**
     * A controller method to post an answer to a specific question.
     *
     * @param answerRequest - This argument contains all the attributes required to store answer details in the database.
     * @param questionId    - The uuid of the question whose answer is to be posted in the database.
     * @param authorization - A field in the request header which contains the JWT token.
     * @return - ResponseEntity<AnswerResponse> type object along with Http status CREATED.
     * @throws AuthorizationFailedException
     * @throws InvalidQuestionException
     */
    @RequestMapping(method = RequestMethod.POST,path = "/question/{questionId}/answer/create",consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerResponse> createAnswer(AnswerRequest answerRequest,@PathVariable("questionId")String questionId,@RequestHeader("authorization")String authorization)
    throws AuthorizationFailedException,InvalidQuestionException{

//        byte[] decode= Base64.getDecoder().decode(authorization.split("Bearer ")[1]);
//        String decodedAuthorization=new String(decode);

        AnswerEntity answerEntity=new AnswerEntity();
        answerEntity.setAns(answerRequest.getAnswer());
        answerEntity.setUuid(UUID.randomUUID().toString());
        QuestionEntity questionEntity=answerBusinessService.getQuestionByUuid(questionId);
        if(questionEntity==null){
            throw new InvalidQuestionException("QUES-001","The question entered is invalid");
        }
        answerEntity.setQuestion(questionEntity);
        AnswerEntity createdAnswer=answerBusinessService.createAnswer(answerEntity,authorization);
        AnswerResponse answerResponse=new AnswerResponse().id(createdAnswer.getUuid()).status("ANSWER CREATED");

        return  new ResponseEntity<AnswerResponse>(answerResponse,HttpStatus.CREATED);
    }

    /**
     * A controller method to edit an answer in the database.
     *
     * @param answerEditRequest - This argument contains all the attributes required to store edited answer details in the database.
     * @param answerId          - The uuid of the answer to be edited in the database.
     * @param authorization     - A field in the request header which contains the JWT token.
     * @return - ResponseEntity<AnswerEditResponse> type object along with Http status OK.
     * @throws AuthorizationFailedException
     * @throws AnswerNotFoundException
     */
    @RequestMapping(method = RequestMethod.PUT,path = "/answer/edit/{answerId}",consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerEditResponse> editAnswerContent(AnswerEditRequest answerEditRequest,@PathVariable("answerId")String answerId,@RequestHeader("authorization")String authorization)
    throws AuthorizationFailedException,AnswerNotFoundException{

//        byte[] decode= Base64.getDecoder().decode(authorization.split("Bearer ")[1]);
//        String decodedAuthorization=new String(decode);

        AnswerEntity answerEntity=new AnswerEntity();
        answerEntity.setAns(answerEditRequest.getContent());
        AnswerEntity editedAnswer=answerBusinessService.editAnswerContent(answerEntity,answerId,authorization);
        AnswerEditResponse answerEditResponse=new AnswerEditResponse().id(editedAnswer.getUuid()).status("ANSWER EDITED");
        return new ResponseEntity<AnswerEditResponse>(answerEditResponse,HttpStatus.OK);
    }

    /**
     * A controller method to delete an answer in the database.
     *
     * @param answerId      - The uuid of the answer to be deleted in the database.
     * @param authorization - A field in the request header which contains the JWT token.
     * @return - ResponseEntity<AnswerDeleteResponse> type object along with Http status OK.
     * @throws AuthorizationFailedException
     * @throws AnswerNotFoundException
     */
    @RequestMapping(method = RequestMethod.DELETE,path = "/answer/delete/{answerId}",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerDeleteResponse> deleteAnswer(@PathVariable("answerId")String answerId,@RequestHeader("authorization")String authorization)
    throws AuthorizationFailedException,AnswerNotFoundException{
//
//        byte[] decode= Base64.getDecoder().decode(authorization.split("Bearer ")[1]);
//        String decodedAuthorization=new String(decode);

        AnswerEntity answerEntity=answerBusinessService.deleteAnswer(answerId,authorization);
        AnswerDeleteResponse answerDeleteResponse=new AnswerDeleteResponse().id(answerEntity.getUuid()).status("ANSWER DELETED");
        return new ResponseEntity<AnswerDeleteResponse>(answerDeleteResponse,HttpStatus.OK);
    }
    /**
     * A controller method to fetch all the answers for a specific question in the database.
     *
     * @param questionId    - The uuid of the question whose answers are to be fetched from the database.
     * @param authorization - A field in the request header which contains the JWT token.
     * @return - ResponseEntity<List<AnswerDetailsResponse>> type object along with Http status OK.
     * @throws AuthorizationFailedException
     * @throws InvalidQuestionException
     */
    @RequestMapping(method = RequestMethod.GET,path = "answer/all/{questionId}",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<AnswerDetailsResponse>> getAllAnswersToQuestion(@PathVariable("questionId")String questionId,@RequestHeader("authorization")String authorization)
    throws AuthorizationFailedException,InvalidQuestionException{
//        byte[] decode= Base64.getDecoder().decode(authorization.split("Bearer ")[1]);
//        String decodedAuthorization=new String(decode);

        TypedQuery<AnswerEntity> queriedAnswers=answerBusinessService.getAnswersByQuestion(questionId,authorization);
        List<AnswerEntity> answerList=queriedAnswers.getResultList();
        List<AnswerDetailsResponse> answerDetailsResponses=new ArrayList<AnswerDetailsResponse>();

        for(AnswerEntity a:answerList){
            answerDetailsResponses.add(new AnswerDetailsResponse().id(a.getUuid()).questionContent(a.getQuestion().getContent()).answerContent(a.getAns()));
        }
    return new ResponseEntity<List<AnswerDetailsResponse>>(answerDetailsResponses,HttpStatus.OK);
    }


}

package com.upgrad.stackoverflow.service.business;


import com.upgrad.stackoverflow.service.dao.AnswerDao;
import com.upgrad.stackoverflow.service.dao.UserDao;
import com.upgrad.stackoverflow.service.entity.AnswerEntity;
import com.upgrad.stackoverflow.service.entity.QuestionEntity;
import com.upgrad.stackoverflow.service.entity.UserAuthEntity;
import com.upgrad.stackoverflow.service.exception.AnswerNotFoundException;
import com.upgrad.stackoverflow.service.exception.AuthorizationFailedException;
import com.upgrad.stackoverflow.service.exception.InvalidQuestionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;
import java.time.ZonedDateTime;

@Service
public class AnswerBusinessService {


    @Autowired
    private UserDao userDao;

    @Autowired
    private AnswerDao answerDao;


    /**
     * The method implements the business logic for createAnswer endpoint.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity createAnswer(final AnswerEntity answerEntity,final String authorization) throws AuthorizationFailedException {

        final UserAuthEntity userAuthEntity = userDao.getUserAuthByAccesstoken(authorization);
        if(userAuthEntity==null){
            throw new AuthorizationFailedException("ATHR-001","User has not signed in");
        }

        if(userAuthEntity.getLogoutAt()!=null){
            throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to post an answer");
        }
        answerEntity.setDate(ZonedDateTime.now());
        answerEntity.setUser(userAuthEntity.getUser());
        final AnswerEntity createdAnswer=answerDao.createAnswer(answerEntity);

        return answerEntity;

    }

    public QuestionEntity getQuestionByUuid(String Uuid) throws InvalidQuestionException {

        QuestionEntity questionEntity = answerDao.getQuestionByUuid(Uuid);

        return questionEntity;
    }


    /**
     * The method implements the business logic for editAnswerContent endpoint.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity editAnswerContent(final AnswerEntity answerEntity,final String answerId,final String authorization) throws AuthorizationFailedException, AnswerNotFoundException {
        final UserAuthEntity userAuthEntity = userDao.getUserAuthByAccesstoken(authorization);
        if(userAuthEntity==null){
            throw new AuthorizationFailedException("ATHR-001","User has not signed in");
        }

        if(userAuthEntity.getLogoutAt()!=null){
            throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to edit an answer");
        }

        final AnswerEntity oldAnswer=answerDao.getAnswerByUuid(answerId);
        if(oldAnswer==null){
            throw new AnswerNotFoundException("ANS-001","Entered answer uuid does not exist");
        }

        if(!userAuthEntity.getUser().getUuid().equals(oldAnswer.getUser().getUuid())){
            throw new AuthorizationFailedException("ATHR-003","Only the answer owner can edit the answer");
        }

        oldAnswer.setAns(answerEntity.getAns());
        oldAnswer.setDate(ZonedDateTime.now());
        final AnswerEntity editedAnswer=answerDao.editAnswer(oldAnswer);
        return editedAnswer;
    }

    /**
     * The method implements the business logic for deleteAnswer endpoint.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public AnswerEntity deleteAnswer(String answerId, String authorization) throws AuthorizationFailedException, AnswerNotFoundException {
        UserAuthEntity userAuthEntity = userDao.getUserAuthByAccesstoken(authorization);
        if(userAuthEntity==null){
            throw new AuthorizationFailedException("ATHR-001","User has not signed in");
        }

        if(userAuthEntity.getLogoutAt()!=null){
            throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to delete an answer");
        }
        final AnswerEntity answerEntity=answerDao.getAnswerByUuid(answerId);
        if(answerEntity==null){
            throw new AnswerNotFoundException("ANS-001","Entered answer uuid does not exist");
        }

        if((!userAuthEntity.getUser().getUuid().equals(answerEntity.getUser().getUuid()))&&userAuthEntity.getUser().getRole().equals("nonadmin")){
            throw new AuthorizationFailedException("ATHR-003","Only the answer owner or admin can delete the answer");
        }

        final AnswerEntity deletedQuestion=answerDao.deleteAnswer(answerEntity);
        return deletedQuestion;

    }

    /**
     * The method implements the business logic for getAllAnswersToQuestion endpoint.
     */
    public TypedQuery<AnswerEntity> getAnswersByQuestion(String questionId, String authorization) throws AuthorizationFailedException, InvalidQuestionException {
        UserAuthEntity userAuthEntity = userDao.getUserAuthByAccesstoken(authorization);

        if(userAuthEntity==null){
            throw new AuthorizationFailedException("ATHR-001","User has not signed in");
        }

        if(userAuthEntity.getLogoutAt()!=null){
            throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to delete an answer");
        }

       final QuestionEntity questionEntity=getQuestionByUuid(questionId);
        if(questionEntity==null){
            throw new InvalidQuestionException("QUES-001","The question with entered uuid whose details are to be seen does not exist");
        }
        final TypedQuery<AnswerEntity> answerEntityTypedQuery=answerDao.getAnswersByQuestion(questionEntity);
        return answerEntityTypedQuery;

    }
}

package com.upgrad.stackoverflow.service.business;


import com.upgrad.stackoverflow.service.dao.QuestionDao;
import com.upgrad.stackoverflow.service.dao.UserDao;
import com.upgrad.stackoverflow.service.entity.QuestionEntity;
import com.upgrad.stackoverflow.service.entity.UserAuthEntity;
import com.upgrad.stackoverflow.service.entity.UserEntity;
import com.upgrad.stackoverflow.service.exception.AuthorizationFailedException;
import com.upgrad.stackoverflow.service.exception.InvalidQuestionException;
import com.upgrad.stackoverflow.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;
import java.time.ZonedDateTime;

@Service
public class QuestionBusinessService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private QuestionDao questionDao;


    /**
     * The method implements the business logic for createQuestion endpoint.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public QuestionEntity createQuestion(final QuestionEntity questionEntity, final String authorization) throws AuthorizationFailedException {

        final UserAuthEntity userAuthEntity = userDao.getUserAuthByAccesstoken(authorization);
        if(userAuthEntity==null){
            throw new AuthorizationFailedException("ATHR-001","User has not signed in");
        }

        if(userAuthEntity.getLogoutAt()!=null){
            throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to post a question");
        }

        questionEntity.setUser(userAuthEntity.getUser());
        final QuestionEntity createdQuestionEntity=questionDao.createQuestion(questionEntity);
        return createdQuestionEntity;

    }

    /**
     * The method implements the business logic for getAllQuestions endpoint.
     */
    public TypedQuery<QuestionEntity> getQuestions(String authorization) throws AuthorizationFailedException {

        UserAuthEntity userAuthEntity = userDao.getUserAuthByAccesstoken(authorization);
        if(userAuthEntity==null){
            throw new AuthorizationFailedException("ATHR-001","User has not signed in");
        }

        if(userAuthEntity.getLogoutAt()!=null){
            throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to get all questions");
        }

        final TypedQuery<QuestionEntity> receivedQuestions=questionDao.getQuestions();
        return receivedQuestions;
    }

    /**
     * The method implements the business logic for editQuestionContent endpoint.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public QuestionEntity editQuestionContent(QuestionEntity questionEntity, String questionId, String authorization) throws AuthorizationFailedException, InvalidQuestionException {
        UserAuthEntity userAuthEntity = userDao.getUserAuthByAccesstoken(authorization);
        if(userAuthEntity==null){
            throw new AuthorizationFailedException("ATHR-001","User has not signed in");
        }

        if(userAuthEntity.getLogoutAt()!=null){
            throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to edit the question");
        }
        QuestionEntity oldQuestionEntity=questionDao.getQuestionByUuid(questionId);

        if(oldQuestionEntity==null){
            throw new InvalidQuestionException("QUES-001","Entered question uuid does not exist");
        }

        if(userAuthEntity.getUser().getUuid()!=oldQuestionEntity.getUser().getUuid()){
            throw new AuthorizationFailedException("ATHR-003","Only the question owner can edit the question");
        }


        oldQuestionEntity.setContent(questionEntity.getContent());
        QuestionEntity updatedQuestionEnitiy=questionDao.editQuestion(oldQuestionEntity);
        return updatedQuestionEnitiy;

    }

    /**
     * The method implements the business logic for deleteQuestion endpoint.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public QuestionEntity deleteQuestion(String questionId, String authorization) throws AuthorizationFailedException, InvalidQuestionException {
        UserAuthEntity userAuthEntity = userDao.getUserAuthByAccesstoken(authorization);
        if(userAuthEntity==null){
            throw new AuthorizationFailedException("ATHR-001","User has not signed in");
        }

        if(userAuthEntity.getLogoutAt()!=null){
            throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to delete a question");
        }
        QuestionEntity questionEntity=questionDao.getQuestionByUuid(questionId);
        if(questionEntity==null){
            throw new InvalidQuestionException("QUES-001","Entered question uuid does not exist");
        }

        if(userAuthEntity.getUser().getUuid()!=questionEntity.getUser().getUuid()&&userAuthEntity.getUser().getRole()=="nonadmin"){
            throw new AuthorizationFailedException("ATHR-003","Only the question owner or admin can delete the question");
        }


        QuestionEntity deletedQuestionEntity=questionDao.deleteQuestion(questionEntity);
        return deletedQuestionEntity;
    }

    /**
     * The method implements the business logic for getAllQuestionsByUser endpoint.
     */
    public TypedQuery<QuestionEntity> getQuestionsByUser(String userId, String authorization) throws AuthorizationFailedException, UserNotFoundException {
        UserAuthEntity userAuthEntity = userDao.getUserAuthByAccesstoken(authorization);
        if(userAuthEntity==null){
            throw new AuthorizationFailedException("ATHR-001","User has not signed in");
        }

        if(userAuthEntity.getLogoutAt()!=null){
            throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to get all questions posted by a specific user");
        }
        UserEntity userEntity=userDao.getUserByUsername(userId);
        if(userEntity==null){
            throw new UserNotFoundException("USR-001","User with entered uuid whose question details are to be seen does not exist");
        }

        TypedQuery<QuestionEntity> quesetionSet=questionDao.getQuestionsByUser(userEntity);
        return quesetionSet;

    }

}

package com.ecommerce.service;

import com.ecommerce.exception.UserException;
import com.ecommerce.model.User;
import com.ecommerce.request.IssueRequest;
import com.ecommerce.request.IssueRespondRequest;
import com.ecommerce.response.IssueResponse;

import java.util.List;

public interface IssueService {

    IssueResponse createIssue(User user, IssueRequest issueRequest) throws UserException;

    List<IssueResponse> getUserIssues(User user) throws UserException;

    List<IssueResponse> getAllIssues() throws UserException;

    IssueResponse respondToIssue(Long issueId, IssueRespondRequest issueRespondRequest) throws UserException;

}

package com.ecommerce.controller;

import com.ecommerce.exception.UserException;
import com.ecommerce.model.User;
import com.ecommerce.request.IssueRequest;
import com.ecommerce.response.IssueResponse;
import com.ecommerce.service.IssueService;
import com.ecommerce.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/issues")
public class IssueController {

    private final IssueService issueService;
    private final UserService userService;

    @PostMapping("/create")
    public ResponseEntity<IssueResponse> createIssue(@RequestBody IssueRequest issueRequest,
                                                     @RequestHeader("Authorization") String jwt) throws UserException {

        User user = userService.findUserProfileByJwt(jwt);
        IssueResponse issueResponse = issueService.createIssue(user, issueRequest);
        return new ResponseEntity<>(issueResponse, HttpStatus.CREATED);

    }

    @GetMapping("/")
    public ResponseEntity<List<IssueResponse>> getUserIssues(@RequestHeader("Authorization") String jwt) throws UserException {

        User user = userService.findUserProfileByJwt(jwt);
        List<IssueResponse> issues = issueService.getUserIssues(user);
        return new ResponseEntity<>(issues, HttpStatus.OK);

    }

}

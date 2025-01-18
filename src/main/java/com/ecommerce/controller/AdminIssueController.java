package com.ecommerce.controller;

import com.ecommerce.exception.UserException;
import com.ecommerce.request.IssueRespondRequest;
import com.ecommerce.response.IssueResponse;
import com.ecommerce.service.IssueService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/admin/issues")
public class AdminIssueController {

    private final IssueService issueService;

    @GetMapping("/")
    public ResponseEntity<List<IssueResponse>> getAllIssues(@RequestHeader("Authorization") String jwt) throws UserException {

        List<IssueResponse> issues = issueService.getAllIssues();
        return new ResponseEntity<>(issues, HttpStatus.OK);

    }

    @PostMapping("/respond/{issueId}")
    public ResponseEntity<IssueResponse> respondToIssue(@PathVariable Long issueId,
                                                        @RequestBody IssueRespondRequest issueRespondRequest,
                                                        @RequestHeader("Authorization") String jwt) throws UserException {

        IssueResponse issueResponse = issueService.respondToIssue(issueId, issueRespondRequest);
        return new ResponseEntity<>(issueResponse, HttpStatus.OK);

    }

}

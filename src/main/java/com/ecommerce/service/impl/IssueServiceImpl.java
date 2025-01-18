package com.ecommerce.service.impl;

import com.ecommerce.exception.UserException;
import com.ecommerce.model.Issue;
import com.ecommerce.model.User;
import com.ecommerce.repository.IssueRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.request.IssueRequest;
import com.ecommerce.request.IssueRespondRequest;
import com.ecommerce.response.IssueResponse;
import com.ecommerce.service.IssueService;
import com.ecommerce.user.domain.IssueStatus;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class IssueServiceImpl implements IssueService {

    private final IssueRepository issueRepository;
    private final UserRepository userRepository;

    @Override
    public IssueResponse createIssue(User user, IssueRequest issueRequest) throws UserException {

        String referenceNumber = generateReferenceNumber();
        Issue issue = new Issue();
        issue.setTitle(issueRequest.getTitle());
        issue.setDescription(issueRequest.getDescription());
        issue.setStatus(IssueStatus.OPEN);  // Default status is OPEN
        issue.setReferenceNumber(referenceNumber);
        issue.setUser(user);  // Set the user for the issue
        issue.setCreatedAt(LocalDateTime.now());
        issue.setUpdatedAt(LocalDateTime.now());

        issue = issueRepository.save(issue);

        return new IssueResponse(
                issue.getId(),
                issue.getUser().getId(),
                issue.getReferenceNumber(),
                issue.getTitle(),
                issue.getDescription(),
                issue.getStatus().name(),
                null
        );
    }

    private String generateReferenceNumber() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = RandomStringUtils.randomAlphanumeric(6).toUpperCase();  // 6-character random string
        return "REF-" + datePart + randomPart;
    }

    @Override
    public List<IssueResponse> getUserIssues(User user) throws UserException {

        return issueRepository.findByUser(user).stream()
                .map(issue -> new IssueResponse(
                        issue.getId(),
                        issue.getUser().getId(),
                        issue.getReferenceNumber(),
                        issue.getTitle(),
                        issue.getDescription(),
                        issue.getStatus().name(),
                        issue.getReply())
                )
                .collect(Collectors.toList());
    }

    @Override
    public List<IssueResponse> getAllIssues() throws UserException {
        return issueRepository.findAll().stream()
                .map(issue -> new IssueResponse(
                        issue.getId(),
                        issue.getUser().getId(),
                        issue.getReferenceNumber(),
                        issue.getTitle(),
                        issue.getDescription(),
                        issue.getStatus().name(),
                        issue.getReply())
                )
                .collect(Collectors.toList());
    }

    @Override
    public IssueResponse respondToIssue(Long issueId, IssueRespondRequest issueRespondRequest) throws UserException {

        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found"));

        String reply = issueRespondRequest.getReply();
        IssueStatus issueStatus = IssueStatus.valueOf(issueRespondRequest.getStatus().toUpperCase());

        issue.setReply(reply);
        issue.setStatus(issueStatus);
        issue.setUpdatedAt(LocalDateTime.now());

        issue = issueRepository.save(issue);

        return new IssueResponse(
                issue.getId(),
                issue.getUser().getId(),
                issue.getReferenceNumber(),
                issue.getTitle(),
                issue.getDescription(),
                issue.getStatus().name(),
                issue.getReply()
        );
    }

}

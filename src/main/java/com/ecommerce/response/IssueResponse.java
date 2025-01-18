package com.ecommerce.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IssueResponse {

    private Long id;
    private Long userId;
    private String referenceNumber;
    private String title;
    private String description;
    private String status;
    private String reply;

}

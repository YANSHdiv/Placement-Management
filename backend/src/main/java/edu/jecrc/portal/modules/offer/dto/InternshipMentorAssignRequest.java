package edu.jecrc.portal.modules.offer.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternshipMentorAssignRequest {

    @NotNull(message = "Faculty mentor user ID is required")
    private UUID facultyMentorUserId;

    private String industryMentorName;
    private String industryMentorEmail;
    private String industryMentorPhone;
}

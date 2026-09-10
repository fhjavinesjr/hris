package com.primehr.rsp.evaluation.api;
import com.primehr.rsp.evaluation.domain.CommitteeMember; import jakarta.validation.Valid; import jakarta.validation.constraints.*; import java.time.*; import java.util.*;
public final class CommitteeDtos {private CommitteeDtos(){}
 public record MemberInput(@NotBlank @Size(max=100)String employeeNo,@NotNull CommitteeMember.Role role,@Size(max=300)String representation,@NotNull LocalDate effectiveFrom,LocalDate effectiveTo){}
 public record SaveCommittee(@NotBlank @Size(max=80)String code,@NotBlank @Size(max=200)String name,@NotBlank @Size(max=1000)String legalBasis,@Min(1)int minimumVotingMembers,@NotEmpty List<@Valid MemberInput> members,Long recordVersion){}
 public record Publish(@NotNull Long recordVersion,@NotNull LocalDate effectiveFrom,LocalDate effectiveTo){} public record Transition(@NotNull Long recordVersion){}
 public record MemberResponse(String id,String employeeNo,String role,String representation,LocalDate effectiveFrom,LocalDate effectiveTo){}
 public record CommitteeResponse(String id,String code,String type,String name,String legalBasis,int definitionVersion,String supersedesId,String status,LocalDate effectiveFrom,LocalDate effectiveTo,int minimumVotingMembers,String publishedBy,Instant publishedAt,long recordVersion,List<MemberResponse> members){}
}

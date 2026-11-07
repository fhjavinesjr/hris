package com.primehr.succession.domain;
import jakarta.persistence.*;
@Entity @Table(name="succession_committee_charter",uniqueConstraints=@UniqueConstraint(name="uk_succ_charter_code",columnNames={"agency_id","normalized_code"}))
public class SuccessionCommitteeCharter extends SuccessionNamedDefinition { protected SuccessionCommitteeCharter(){} public SuccessionCommitteeCharter(String agencyId,String code){super(agencyId,code);} }

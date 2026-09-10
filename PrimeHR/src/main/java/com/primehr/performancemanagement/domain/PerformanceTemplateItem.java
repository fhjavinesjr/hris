package com.primehr.performancemanagement.domain;
import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity @Table(name="spms_template_item",uniqueConstraints={
    @UniqueConstraint(name="uk_spms_template_item_indicator",columnNames={"agency_id","section_id","indicator_version_id"}),
    @UniqueConstraint(name="uk_spms_template_item_order",columnNames={"agency_id","section_id","display_order"})})
public class PerformanceTemplateItem extends RspAuditedEntity {
    @Column(name="section_id",nullable=false,length=36) private String sectionId;
    @Column(name="indicator_version_id",nullable=false,length=36) private String indicatorVersionId;
    @Column(name="label_override",length=200) private String labelOverride;
    @Column(name="weight_percent",nullable=false,precision=7,scale=4) private BigDecimal weightPercent;
    @Column(name="required_item",nullable=false) private boolean required;
    @Column(name="evidence_override",length=2000) private String evidenceOverride;
    @Column(name="display_order",nullable=false) private int displayOrder;
    protected PerformanceTemplateItem() {}
    public PerformanceTemplateItem(String agency,String section,String indicator,String label,BigDecimal weight,boolean required,String evidence,int order){super(agency);sectionId=requiredText(section,"sectionId");indicatorVersionId=requiredText(indicator,"indicatorVersionId");labelOverride=optionalText(label);weightPercent=weight;this.required=required;evidenceOverride=optionalText(evidence);displayOrder=order;if(weight==null||weight.signum()<=0||order<1)throw new IllegalArgumentException("Item weight and order must be positive");}
    public PerformanceTemplateItem(String agency,String section,String indicator,BigDecimal weight,int order){this(agency,section,indicator,null,weight,true,null,order);}
    public String getSectionId(){return sectionId;} public String getIndicatorVersionId(){return indicatorVersionId;} public String getLabelOverride(){return labelOverride;} public BigDecimal getWeightPercent(){return weightPercent;} public boolean isRequired(){return required;} public String getEvidenceOverride(){return evidenceOverride;} public int getDisplayOrder(){return displayOrder;}
}

package com.primehr.performancemanagement.domain;
import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.Objects;

@Entity @Table(name="spms_template_section",uniqueConstraints={
    @UniqueConstraint(name="uk_spms_template_section_code",columnNames={"agency_id","template_version_id","section_code"}),
    @UniqueConstraint(name="uk_spms_template_section_order",columnNames={"agency_id","template_version_id","display_order"})})
public class PerformanceTemplateSection extends RspAuditedEntity {
    public enum Type { CORE,STRATEGIC,SUPPORT,OTHER }
    @Column(name="template_version_id",nullable=false,length=36) private String templateVersionId;
    @Enumerated(EnumType.STRING) @Column(name="section_type",nullable=false,length=20) private Type type;
    @Column(name="section_code",nullable=false,length=80) private String code;
    @Column(nullable=false,length=200) private String title; @Column(length=2000) private String description;
    @Column(name="weight_percent",nullable=false,precision=7,scale=4) private BigDecimal weightPercent;
    @Column(name="display_order",nullable=false) private int displayOrder;
    protected PerformanceTemplateSection() {}
    public PerformanceTemplateSection(String agency,String version,Type type,String code,String title,String description,BigDecimal weight,int order){super(agency);templateVersionId=requiredText(version,"templateVersionId");this.type=Objects.requireNonNull(type,"type");this.code=requiredText(code,"code").toUpperCase(Locale.ROOT);this.title=requiredText(title,"title");this.description=optionalText(description);weightPercent=weight;displayOrder=order;if(weight==null||weight.signum()<=0||order<1)throw new IllegalArgumentException("Section weight and order must be positive");}
    public PerformanceTemplateSection(String agency,String version,Type type,String title,BigDecimal weight,int order){this(agency,version,type,type==null?null:type.name(),title,null,weight,order);}
    public String getTemplateVersionId(){return templateVersionId;} public Type getType(){return type;} public String getCode(){return code;} public String getTitle(){return title;} public String getDescription(){return description;} public BigDecimal getWeightPercent(){return weightPercent;} public int getDisplayOrder(){return displayOrder;}
}

package com.primehr.performancemanagement.domain;
import com.primehr.rsp.domain.RspAuditedEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.Objects;

@Entity @Table(name="spms_indicator_dimension",uniqueConstraints={
    @UniqueConstraint(name="uk_spms_indicator_dimension_code",columnNames={"agency_id","indicator_version_id","dimension_code"}),
    @UniqueConstraint(name="uk_spms_indicator_dimension_order",columnNames={"agency_id","indicator_version_id","display_order"})})
public class PerformanceIndicatorDimension extends RspAuditedEntity {
    public enum Type { QUALITY,EFFICIENCY,TIMELINESS,AGENCY_DEFINED }
    @Column(name="indicator_version_id",nullable=false,length=36) private String indicatorVersionId;
    @Enumerated(EnumType.STRING) @Column(name="dimension_type",nullable=false,length=30) private Type type;
    @Column(name="dimension_code",nullable=false,length=80) private String code;
    @Column(nullable=false,length=200) private String label;
    @Column(name="weight_percent",nullable=false,precision=7,scale=4) private BigDecimal weightPercent;
    @Column(name="display_order",nullable=false) private int displayOrder;
    protected PerformanceIndicatorDimension() {}
    public PerformanceIndicatorDimension(String agency,String version,Type type,String code,String label,BigDecimal weight,int order){super(agency);indicatorVersionId=requiredText(version,"indicatorVersionId");this.type=Objects.requireNonNull(type,"type");this.code=requiredText(code,"code").toUpperCase(Locale.ROOT);if(type!=Type.AGENCY_DEFINED&&!this.code.equals(type.name()))throw new IllegalArgumentException("Controlled dimensions must use their canonical code");this.label=requiredText(label,"label");weightPercent=weight;if(weight==null||weight.signum()<=0)throw new IllegalArgumentException("Dimension weight must be positive");if(order<1)throw new IllegalArgumentException("displayOrder must be positive");displayOrder=order;}
    public PerformanceIndicatorDimension(String agency,String version,Type type,String label,BigDecimal weight,int order){this(agency,version,type,type==null?null:type.name(),label,weight,order);}
    public String getIndicatorVersionId(){return indicatorVersionId;} public Type getType(){return type;} public String getCode(){return code;}
    public String getLabel(){return label;} public BigDecimal getWeightPercent(){return weightPercent;} public int getDisplayOrder(){return displayOrder;}
}

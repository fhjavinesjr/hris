package com.payroll.entitymodels;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "annualization_snapshot_item",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_annualization_snapshot_item", columnNames = {"snapshotId", "itemNo"}),
        indexes = @Index(name = "idx_annualization_item_snapshot", columnList = "snapshotId"))
public class AnnualizationSnapshotItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long snapshotId;

    @Column(nullable = false, length = 4)
    private String itemNo;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSnapshotId() { return snapshotId; }
    public void setSnapshotId(Long snapshotId) { this.snapshotId = snapshotId; }
    public String getItemNo() { return itemNo; }
    public void setItemNo(String itemNo) { this.itemNo = itemNo; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}

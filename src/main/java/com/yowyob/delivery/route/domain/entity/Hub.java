package com.yowyob.delivery.route.domain.entity;

import com.yowyob.delivery.route.domain.enums.HubType;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a Hub or a geographical point in the logistics network.
 * Persistent in the 'hubs' table. This entity is reactive-compatible (R2DBC).
 */
@Table("hubs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Hub {

    /**
     * Unique identifier for the Hub.
     */
    @Id
    private UUID id;

    /**
     * Human-readable address associated with the Hub.
     */
    @Column("address")
    private String address;

    /**
     * The functional type of the Hub (e.g., WAREHOUSE, TRANSIT_POINT).
     */
    @Column("type")
    private HubType type;

    /**
     * Geographical location of the point, stored as a Well-Known Text (WKT) String.
     * R2DBC doesn't natively support JTS Geometry types, so we use WKT (POINT)
     * for persistence and perform conversions in the service/mapper layer.
     */
    @Column("location")
    private String location;

    /**
     * Timestamp indicating when the Hub record was created.
     */
    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    /**
     * Timestamp indicating the last time the Hub record was modified.
     */
    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;
}

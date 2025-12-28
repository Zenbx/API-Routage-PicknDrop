package com.yowyob.delivery.route.domain.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

/**
 * Entity representing a directed connection between two hubs in the logistics
 * network.
 * Essential for graph-based routing algorithms (like Dijkstra or A*).
 */
@Table("hub_connections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HubConnection {

    /**
     * Unique identifier for the connection.
     */
    @Id
    private UUID id;

    /**
     * ID of the origin hub.
     */
    @Column("from_hub_id")
    private UUID fromHubId;

    /**
     * ID of the destination hub.
     */
    @Column("to_hub_id")
    private UUID toHubId;

    /**
     * The edge weight of the connection.
     * Usually represents distance in kilometers or travel time in minutes.
     */
    @Column("weight")
    private Double weight;
}

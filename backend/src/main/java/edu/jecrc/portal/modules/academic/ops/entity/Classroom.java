package edu.jecrc.portal.modules.academic.ops.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "classrooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Classroom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "classroom_id")
    private Integer id;

    @Column(name = "room_number", nullable = false, unique = true, length = 50)
    private String roomNumber;

    @Column(name = "building_name", nullable = false, length = 100)
    private String buildingName;

    @Builder.Default
    @Column(name = "capacity", nullable = false)
    private Integer capacity = 60;

    @Builder.Default
    @Column(name = "room_type", nullable = false, length = 50)
    private String roomType = "LECTURE_HALL"; // LECTURE_HALL, COMPUTER_LAB, SEMINAR_ROOM, AUDITORIUM

    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    @Builder.Default
    @Column(name = "geofence_radius_meters", nullable = false)
    private Integer geofenceRadiusMeters = 60;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }
}

package ch.supsi.sporthub.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a profile image associated with a user.
 * Stores image metadata and binary data.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_images")
public class UserImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String fileType;

    @Column(nullable = false)
    private Long fileSize;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGBLOB")
    private byte[] imageData;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
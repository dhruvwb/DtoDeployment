package com.ng.dtogen.persistence;

import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ng.dtogen.model.SupportedFormat;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stored_payload")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class StoredPayload {

    @Id @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    private SupportedFormat format;

    @Lob
    private String payload;

    private Instant createdAt;

    public static StoredPayload of(SupportedFormat f, String p) {
        return StoredPayload.builder()
            .format(f).payload(p).createdAt(Instant.now()).build();
    }
}



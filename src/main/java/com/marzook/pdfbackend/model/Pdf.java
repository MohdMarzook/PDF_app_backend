package com.marzook.pdfbackend.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.stereotype.Component;

@Entity
@Component
@Setter
@Getter
@NoArgsConstructor
@ToString
@Table(name = "pdf")
public class Pdf {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String userId;
    private String fromLanguage;
    private String toLanguage;
    @Column(name = "\"pdf_key\"")
    private String pdf_key;
    private UUID pdfId;
    @Enumerated(EnumType.STRING)
    private ProcessingStatus status;
    @CreationTimestamp
    private LocalDateTime created_at;

    public enum ProcessingStatus{
        QUEUED,
        TRANSLATING,
        COMPLETED,
        ERROR
    }

    public Pdf(String userId ,String fromLanguage, String toLanguage, String pdf_key, UUID pdfId) {
        this.userId = userId;
        this.fromLanguage = fromLanguage;
        this.toLanguage = toLanguage;
        this.pdf_key = pdf_key;
        this.pdfId = pdfId;
    }


}

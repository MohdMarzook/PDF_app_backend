package com.marzook.pdfbackend.repository;

import com.marzook.pdfbackend.model.Pdf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface PdfRepo extends JpaRepository<Pdf,Integer> {
    Pdf findPdfByPdfId(UUID pdfId);
    @Query(value = "SELECT 1", nativeQuery = true)
    Integer healthCheck();


    List<Pdf> findAllByUserIdOrderByCreatedAtDesc(String userId);

    int countByUserIdAndStatusIn(String userId, Collection<Pdf.ProcessingStatus> statuses);
}

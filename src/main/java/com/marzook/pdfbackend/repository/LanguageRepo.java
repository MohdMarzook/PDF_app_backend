package com.marzook.pdfbackend.repository;

import com.marzook.pdfbackend.model.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LanguageRepo extends JpaRepository<Language,Integer> {

}

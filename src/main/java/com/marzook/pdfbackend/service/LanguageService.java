package com.marzook.pdfbackend.service;

import com.marzook.pdfbackend.model.Language;
import com.marzook.pdfbackend.repository.LanguageRepo;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class LanguageService {

    private final LanguageRepo languageRepo;

    public LanguageService(LanguageRepo languageRepo) {
        this.languageRepo = languageRepo;
    }

    public List<Language> getAllLanguages(){
        return languageRepo.findAll();
    }



}

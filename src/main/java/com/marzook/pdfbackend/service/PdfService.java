package com.marzook.pdfbackend.service;

import com.marzook.pdfbackend.model.Pdf;
import com.marzook.pdfbackend.repository.PdfRepo;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PdfService {

    private final PdfRepo pdfRepo;


    public PdfService(PdfRepo pdfRepo) {
        this.pdfRepo = pdfRepo;
    }

    @Transactional
    public String updatePdfDetails(String userId, String FromLanguage, String ToLanguage, String pdf_key){
        UUID pdfId = UUID.randomUUID();
        Pdf pdf = new Pdf(userId, FromLanguage, ToLanguage, pdf_key, pdfId);
        pdf.setStatus(Pdf.ProcessingStatus.QUEUED);
        pdfRepo.save(pdf);
        return pdfId.toString();
    }

    @Transactional(readOnly = true)
    public Map<String, String> checkPdfStatus(String userId, UUID pdfId){

        Pdf data = pdfRepo.findPdfByPdfId(pdfId);
        Pdf.ProcessingStatus status = data.getStatus();
        Map<String, String >  result = new HashMap<>();
        if(status == Pdf.ProcessingStatus.TRANSLATING){
            result.put("pdfId", data.getPdfId().toString());
        }
        result.put("status", status.toString());
        return result;
    }

    @Transactional(readOnly = true)
    public Pdf getPdfDetails(String userId, String pdfId){
        Pdf data = pdfRepo.findPdfByPdfId(UUID.fromString(pdfId.strip()));
        if (data == null || !data.getUserId().equals(userId))
            return null;
        return data;
    }

    @Transactional(readOnly = true)
    public List<Pdf> getAllTranslatedPdf(String userid){
        return pdfRepo.findAllByUserIdOrderByCreatedAtDesc(userid);
    }

    @Transactional(readOnly = true)
    public int db_status(){
        return pdfRepo.healthCheck();
    }

    @Transactional
    public void deletePdf(long id){
        pdfRepo.deleteById((int) id);
    }

    @Transactional(readOnly = true)
    public int is_processing(String userid){
        List<Pdf.ProcessingStatus> activeStatuses = Arrays.asList(Pdf.ProcessingStatus.TRANSLATING, Pdf.ProcessingStatus.QUEUED);

        return pdfRepo.countByUserIdAndStatusIn(userid, activeStatuses);
    }

}

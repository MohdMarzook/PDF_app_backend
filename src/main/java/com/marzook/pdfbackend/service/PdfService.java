package com.marzook.pdfbackend.service;

import com.marzook.pdfbackend.model.Pdf;
import com.marzook.pdfbackend.repository.PdfRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PdfService {

    private final PdfRepo pdfRepo;

    @Autowired
    public PdfService(PdfRepo pdfRepo) {
        this.pdfRepo = pdfRepo;
    }

    public String updatePdfDetails(String userId, String FromLanguage, String ToLanguage, String pdf_key){
        UUID pdfId = UUID.randomUUID();
        Pdf pdf = new Pdf(userId, FromLanguage, ToLanguage, pdf_key, pdfId);
        pdf.setStatus(Pdf.ProcessingStatus.QUEUED);
        pdfRepo.save(pdf);
        return pdfId.toString();
    }

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

    public Pdf getPdfDetails(String userId, String pdfId){
        Pdf data = pdfRepo.findPdfByPdfId(UUID.fromString(pdfId.strip()));
        if (data == null || !data.getUserId().equals(userId))
            return null;
        return data;
    }
    public List<Pdf> getAllTranslatedPdf(String userid){
        return pdfRepo.findAllByUserIdOrderByCreatedAtDesc(userid);
    }
    public int db_status(){
        return pdfRepo.healthCheck();
    }

    public void deletePdf(long id){
        pdfRepo.deleteById((int) id);
    }

    public int is_processing(String userid){
        List<Pdf.ProcessingStatus> activeStatuses = Arrays.asList(Pdf.ProcessingStatus.TRANSLATING, Pdf.ProcessingStatus.QUEUED);

        return pdfRepo.countByUserIdAndStatusIn(userid, activeStatuses);
    }

}

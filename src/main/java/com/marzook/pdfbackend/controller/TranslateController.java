package com.marzook.pdfbackend.controller;

import com.marzook.pdfbackend.model.Pdf;
import com.marzook.pdfbackend.service.HtmlFileService;
import com.marzook.pdfbackend.service.PdfFileService;
import com.marzook.pdfbackend.service.PdfService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/translated/")
public class TranslateController {

    private final PdfService pdfService;
    private final PdfFileService pdfFileService;
    private final HtmlFileService htmlFileService;

    TranslateController(PdfService pdfService , PdfFileService pdfFileService, HtmlFileService htmlFileService) {
        this.htmlFileService = htmlFileService;
        this.pdfService = pdfService;
        this.pdfFileService = pdfFileService;
    }

    @GetMapping("pdf/{pdfId}")
    public ResponseEntity<Map<String, String>> translate(
            @CookieValue(value = "userid" , required = true) String userid,
            @PathVariable("pdfId") String pdfId
    ){

        Pdf pdfData = pdfService.getPdfDetails(userid, pdfId);
        if(pdfData == null){
            return ResponseEntity.badRequest().body(Map.of("success", "false", "message","Unauthorized access not allowed"));
        }
        String originalPdfKey = pdfData.getPdf_key();


//        String viewOgPdfLink= pdfFileService.generatePdfViewableURL(originalPdfKey, 30);
        String originalPdfLink = pdfFileService.generatePdfDownloadURL(originalPdfKey, 30);


        Map<String, String> response = new HashMap<>();
        response.put("pdfid", pdfId);
        response.put("success", "true");
//        response.put("viewOgPdfLink", viewOgPdfLink);
        response.put("originalPdfLink", originalPdfLink);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/html/{pdfId}")
    public ResponseEntity<Map<String, String>> translateHtml(
            @CookieValue(value = "userid" , required = true) String userid,
            @PathVariable("pdfId") String pdfId
    ){
        Map<String, String> response = new HashMap<>();
        Pdf pdfData = pdfService.getPdfDetails(userid, pdfId);
        if(pdfData == null){
            return ResponseEntity.badRequest().body(Map.of("success", "false", "message","Unauthorized access not allowed"));
        }

        if( pdfData.getStatus() == Pdf.ProcessingStatus.COMPLETED){
            String translatedPdfKey = getHtmlFileName(pdfData);
            String viewHtmlLink = htmlFileService.generateHtmlViewableURL(translatedPdfKey, 30);
            String downloadHtmlLink = htmlFileService.generateHtmlDownloadableURL(translatedPdfKey, 30);
            response.put("viewHtmlLink", viewHtmlLink);
            response.put("downloadHtmlLink", downloadHtmlLink);
            response.put("status", pdfData.getStatus().toString());
            return ResponseEntity.ok(response);
        }
        if ( pdfData.getStatus() == Pdf.ProcessingStatus.ERROR){
            response.put("error", "true");
            return ResponseEntity.ok(response);
        }
        response.put("success", "false");
        return ResponseEntity.ok(response);
    }

    private static String getHtmlFileName(Pdf pdfData) {
        String translatedPdfKey = Path.of(pdfData.getPdf_key()).getFileName().toString();
        String translatedPdfKeyWithoutExt = "";
        int dotIndex = translatedPdfKey.lastIndexOf('.');
        if (dotIndex > 0) {
            translatedPdfKeyWithoutExt = translatedPdfKey.substring(0, dotIndex);
        } else {
            translatedPdfKeyWithoutExt = translatedPdfKey;
        }
        translatedPdfKey = translatedPdfKeyWithoutExt + "_" + pdfData.getFromLanguage() + "_to_" + pdfData.getToLanguage() + ".html";
        return translatedPdfKey;
    }

}

package com.marzook.pdfbackend.controller;

import com.marzook.pdfbackend.model.Language;
import com.marzook.pdfbackend.service.PdfFileService;
import com.marzook.pdfbackend.service.LanguageService;
import com.marzook.pdfbackend.service.MessageService;
import com.marzook.pdfbackend.service.PdfService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/")
public class HomeController {

    private final PdfFileService pdfFileService;
    private final LanguageService languageService;
    private final PdfService pdfService;
    private final MessageService messageService;


    @Autowired
    public HomeController(LanguageService languageService, PdfService pdfService, PdfFileService pdfFileService, MessageService messageService) {
        this.languageService = languageService;
        this.pdfService = pdfService;
        this.pdfFileService = pdfFileService;
        this.messageService = messageService;
    }

    @GetMapping("/loaderio-699bdedb8ba85c8f9a2208ae40ac1a70/")
    public String Test() {
        return "loaderio-699bdedb8ba85c8f9a2208ae40ac1a70";
    }

    @GetMapping("/languagelist")
    public List<Language> fromLanguageList() {
        List<Language> langlist =  messageService.getlandlist();
        if(langlist == null || langlist.isEmpty()){
            langlist = languageService.getAllLanguages();
            messageService.addlanglist(langlist);
            return langlist;
        }
        else {
            return langlist;
        }
    }

    @GetMapping("/getprofile")
    public ResponseEntity<String> getProfile(
            @CookieValue(name = "userid", required = false) String useridCookie
    ) {
        // If the cookie doesn't exist, create a new one.
        if (useridCookie == null) {
            String newUserId = UUID.randomUUID().toString();

            // Build the secure cookie
            ResponseCookie cookie = ResponseCookie.from("userid", newUserId)
                    .path("/")
                    .secure(true) // Only send over HTTPS
                    .httpOnly(true) // Prevent access from JavaScript
                    .maxAge(TimeUnit.DAYS.toSeconds(30)) // Expires in 30 days
                    .sameSite("Lax")
                    .build();

            // Return the new ID in the body AND the cookie in the header
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(newUserId);
        }

        // If the cookie already exists, just return its value.
        return ResponseEntity.ok(useridCookie);
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> upload(
            @CookieValue(name = "userid", required = true) String userid,
            @RequestParam("from_language")  String from_language,
            @RequestParam("to_language") String to_language,
            @RequestParam("pdf") MultipartFile file
    ) {

        if(file.isEmpty()){
           return ResponseEntity.badRequest().body(Map.of("msg","file is empty"));
        }
        if(pdfService.is_processing(userid) > 0){
            return ResponseEntity.badRequest().body(Map.of("TooManyPdf","A PDF is already being translated"));
        }

        try{
//           pdf key
            PdfFileService.response data = pdfFileService.UploadPdfFile(file);
            // updating the db
            String pdfId  = pdfService.updatePdfDetails(userid, from_language, to_language, data.pdf_key());
            if (messageService.sendMessage(data.pdf_key(), from_language, to_language))
                return ResponseEntity.ok(Map.of("success", "true", "statusId", pdfId));
            else
                throw new RuntimeException("Can't add to the message queue");



        } catch (Exception e) {
            System.out.println("cant upload the file to the system");
            System.out.println(e.getMessage());
           return ResponseEntity.badRequest().body(Map.of("success", "false", "error", e.getMessage()));
        }
    }
    @GetMapping("/download/{download_key}")
    public String download(@PathVariable("download_key") String download_key){
        return  pdfFileService.generatePdfDownloadURL(download_key, 5);
    }

    @GetMapping("/status/{statusid}")
    public ResponseEntity<Map<String, String>> status(
            @CookieValue(value = "userid" , required = true) String userid,
            @PathVariable("statusid") UUID statusId
    ){
        Map<String,String> data = pdfService.checkPdfStatus(userid, statusId);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/keep-alive")
    public ResponseEntity<Map<String, Integer>> keep_alive(){
        return ResponseEntity.ok(Map.of("status", pdfService.db_status()));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String , String>> render_health_cheack(){
        return ResponseEntity.ok(Map.of("health", "good"));
    }

}


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

    @GetMapping("/languagelist")
    public List<Language> fromLanguageList() {
        return languageService.getAllLanguages();
    }

    @GetMapping("/getprofile")
    public ResponseEntity<String> getProfile(
            @CookieValue(name = "userId", required = false) String tempUserId,
            HttpServletResponse response
    ){

        String userId;
        if (tempUserId == null) {
            userId = UUID.randomUUID().toString();

            ResponseCookie cookie = ResponseCookie.from("userId", userId)
                    .path("/")
                    .secure(true)
                    .maxAge(TimeUnit.DAYS.toSeconds(30))
                    .httpOnly(true)
                    .partitioned(true) // <-- Here is the new attribute
                    .build();

            ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body("got profile!");
        }
        else{
            userId = tempUserId;
        }
        return ResponseEntity.ok(userId);
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> upload(
            @CookieValue(name = "userId", required = true) String userId,
            @RequestParam("from_language")  String from_language,
            @RequestParam("to_language") String to_language,
            @RequestParam("pdf") MultipartFile file
    ) {
        System.out.println(userId);
        System.out.println(from_language);
        System.out.println(to_language);
        if(file.isEmpty()){
           return ResponseEntity.badRequest().body(Map.of("msg","file is empty"));
        }

        try{
//           pdf key
            PdfFileService.response data = pdfFileService.UploadPdfFile(file);
            // updating the db
            String pdfId  = pdfService.updatePdfDetails(userId, from_language, to_language, data.pdf_key());
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
            @CookieValue(value = "userId" , required = true) String userId,
            @PathVariable("statusid") UUID statusId
    ){
        Map<String,String> data = pdfService.checkPdfStatus(userId, statusId);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/keep-alive")
    public ResponseEntity<Map<String, Integer>> keep_alive(){
        return ResponseEntity.ok(Map.of("status", pdfService.db_status()));
    }

}


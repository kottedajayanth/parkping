package com.parkping.parkping.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class NotifyController {

    @Value("${telegram.bot.token}")
    private String token;

    @Value("${telegram.chat.id}")
    private String chatId;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/notify-v2")
    @ResponseBody
    public ResponseEntity<String> notifyOwner(
            @RequestParam(name = "note", required = false) String note,
            @RequestParam(name = "image", required = false) MultipartFile image) {

        RestTemplate rest = new RestTemplate();
        String baseUrl = "https://api.telegram.org/bot" + token;

        try {
            String message = "🚗 *ParkPing Alert*\nYour vehicle is requested to be moved.";
            if (note != null && !note.isBlank()) {
                message += "\n\n*Note:* " + note;
            }

            rest.postForObject(baseUrl + "/sendMessage?chat_id=" + chatId + "&text=" + message + "&parse_mode=Markdown",
                    null, String.class);

            if (image != null && !image.isEmpty()) {
                LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                body.add("chat_id", chatId);
                body.add("photo", image.getResource());

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.MULTIPART_FORM_DATA);

                HttpEntity<LinkedMultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);
                rest.postForObject(baseUrl + "/sendPhoto", entity, String.class);
            }

            return ResponseEntity.ok("Notification Sent");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send");
        }
    }
}
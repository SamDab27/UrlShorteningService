package com.example.urlShorteningService.controller;

import com.example.urlShorteningService.model.Url;
import com.example.urlShorteningService.model.UrlErrorResponseDto;
import com.example.urlShorteningService.service.UrlService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UrlShorteningController {

    @Autowired
    private UrlService urlService;

    @PostMapping("/generate")
    public ResponseEntity<?> generateShortLink(@RequestBody com.example.urlShorteningService.model.UrlDto urlDto) {
        try {
            Url urlToRet = urlService.generateShortLink(urlDto);

            if (urlToRet == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "There was an error processing your request. Please try again."));
            }

            return ResponseEntity.ok(urlToRet);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unexpected server error. Please try again later."));
        }
    }

    @GetMapping("/{shortLink}")
    public ResponseEntity<?> redirectToOriginalUrl(@PathVariable String shortLink, HttpServletResponse response) throws IOException {
        if (StringUtils.isEmpty(shortLink)) {
            UrlErrorResponseDto errorResponse = new UrlErrorResponseDto();
            errorResponse.setStatus("400");
            errorResponse.setError("Invalid URL.");
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        Url urlToRet = urlService.getEncodedUrl(shortLink);

        if (urlToRet == null) {
            UrlErrorResponseDto errorResponse = new UrlErrorResponseDto();
            errorResponse.setStatus("404");
            errorResponse.setError("URL does not exist or might have expired.");
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }

        if (urlToRet.getExpirationDate().isBefore(Instant.now())) {
            urlService.deleteShortLink(urlToRet);

            UrlErrorResponseDto errorResponse = new UrlErrorResponseDto();
            errorResponse.setStatus("410");  // Gone
            errorResponse.setError("URL expired. Please generate a new one.");
            return new ResponseEntity<>(errorResponse, HttpStatus.GONE);
        }

        response.sendRedirect(urlToRet.getOriginalUrl());
        return null;
    }
}

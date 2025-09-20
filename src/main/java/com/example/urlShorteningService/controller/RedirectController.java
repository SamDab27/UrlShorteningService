package com.example.urlShorteningService.controller;

import com.example.urlShorteningService.model.Url;
import com.example.urlShorteningService.model.UrlErrorResponseDto;
import com.example.urlShorteningService.service.UrlService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Instant;

@RestController
public class RedirectController {

    @Autowired
    private UrlService urlService;

    @GetMapping("/{shortLink:[a-zA-Z0-9]{8}}")
    public ResponseEntity<?> redirectToOriginalUrl(@PathVariable String shortLink, HttpServletResponse response) throws IOException {
        Url url = urlService.getEncodedUrl(shortLink);

        if (url == null) {
            UrlErrorResponseDto error = new UrlErrorResponseDto();
            error.setStatus("404");
            error.setError("URL does not exist or it might have expired!");
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }

        if (url.getExpirationDate().isBefore(Instant.now())) {
            urlService.deleteShortLink(url);
            UrlErrorResponseDto error = new UrlErrorResponseDto();
            error.setStatus("410");
            error.setError("URL expired. Please try generating a fresh one.");
            return new ResponseEntity<>(error, HttpStatus.GONE);
        }

        response.sendRedirect(url.getOriginalUrl());
        return null;
    }
}



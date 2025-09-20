package com.example.urlShorteningService.service;

import com.example.urlShorteningService.model.Url;
import com.example.urlShorteningService.model.UrlDto;
import com.example.urlShorteningService.repository.UrlRepository;
import com.google.common.hash.Hashing;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class UrlServiceImpl implements UrlService {

    @Autowired
    private UrlRepository urlRepository;

    @Override
    public Url generateShortLink(UrlDto urlDto) {
        if (StringUtils.isNotEmpty(urlDto.getUrl())) {
            String encodedUrl = encodeUrl(urlDto.getUrl());
            Instant nowUtc = Instant.now();

            Url urlToPersist = new Url();
            urlToPersist.setCreationDate(nowUtc);
            urlToPersist.setOriginalUrl(urlDto.getUrl());
            urlToPersist.setShortLink(encodedUrl);

            // Expiration: either user-provided or default 3 hours later
            Instant expiration = getExpirationInstant(urlDto.getExpirationDate(), nowUtc);
            urlToPersist.setExpirationDate(expiration);

            return persistShortLink(urlToPersist);
        }
        return null;
    }


    private Instant getExpirationInstant(String expirationDate, Instant creationUtc) {
        if (StringUtils.isBlank(expirationDate)) {
            // Default 3 hours from now
            return creationUtc.plusSeconds(3 * 3600);
        }

        try {
            Instant parsed = Instant.parse(expirationDate); // parses ISO UTC string
            if (parsed.isBefore(creationUtc)) {
                return creationUtc.plusSeconds(3 * 3600);
            }
            return parsed;
        } catch (Exception e) {
            return creationUtc.plusSeconds(3 * 3600);
        }
    }


    private String encodeUrl(String url) {
        Instant now = Instant.now();
        return Hashing.murmur3_32()
                .hashString(url + now.toString(), StandardCharsets.UTF_8)
                .toString();
    }

    @Override
    public Url persistShortLink(Url url) {
        return urlRepository.save(url);
    }

    @Override
    public Url getEncodedUrl(String shortLink) {
        return urlRepository.findByShortLink(shortLink);
    }

    @Override
    public void deleteShortLink(Url url) {
        urlRepository.delete(url);
    }
}






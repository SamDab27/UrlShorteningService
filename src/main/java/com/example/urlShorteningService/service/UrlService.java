package com.example.urlShorteningService.service;

import com.example.urlShorteningService.model.Url;
import com.example.urlShorteningService.model.UrlDto;

public interface UrlService {

    public Url generateShortLink(UrlDto urlDto);
    public Url persistShortLink(Url url);

    Url getEncodedUrl(String url);

    public void deleteShortLink(Url url);
}

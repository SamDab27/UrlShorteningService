package com.example.urlShorteningService.repository;

import com.example.urlShorteningService.model.Url;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UrlRepository extends MongoRepository<Url, String> {
    public Url findByShortLink(String shortLink);
}

package com.cvns.service;

import java.net.URI;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import com.cvns.custom_exceptions.ApiException;
import com.cvns.dtos.ResponseDtos.LocationSearchResponse;
import com.fasterxml.jackson.databind.JsonNode;

@Service
public class LocationSearchService {
    private static final Duration CACHE_TIME = Duration.ofHours(24);
    private final RestClient client;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private long lastRequestAt;

    @Value("${services.nominatim.base-url:https://nominatim.openstreetmap.org}")
    private String baseUrl;

    public LocationSearchService(RestClient.Builder builder) {
        this.client = builder.defaultHeader("User-Agent", "ChildVaccinationNotifierSystem/1.0").build();
    }

    public synchronized List<LocationSearchResponse> search(String query) {
        String value = query == null ? "" : query.trim();
        if (value.length() < 3) throw new ApiException("Enter at least 3 characters to search");
        String key = value.toLowerCase(Locale.ROOT);
        CacheEntry saved = cache.get(key);
        if (saved != null && saved.createdAt().plus(CACHE_TIME).isAfter(Instant.now())) return saved.items();

        respectPublicRateLimit();
        try {
            URI uri = UriComponentsBuilder.fromUriString(baseUrl)
                    .path("/search")
                    .queryParam("format", "jsonv2")
                    .queryParam("addressdetails", 1)
                    .queryParam("limit", 5)
                    .queryParam("q", value)
                    .build().encode().toUri();
            JsonNode root = client.get().uri(uri).retrieve().body(JsonNode.class);
            List<LocationSearchResponse> result = new ArrayList<>();
            if (root != null && root.isArray()) {
                for (JsonNode item : root) {
                    result.add(new LocationSearchResponse(
                            item.path("osm_type").asText("osm"),
                            item.path("osm_id").asText(),
                            item.path("display_name").asText("Location"),
                            Double.parseDouble(item.path("lat").asText()),
                            Double.parseDouble(item.path("lon").asText())));
                }
            }
            List<LocationSearchResponse> immutable = List.copyOf(result);
            cache.put(key, new CacheEntry(Instant.now(), immutable));
            return immutable;
        } catch (Exception e) {
            throw new ApiException("Address search is temporarily unavailable");
        }
    }

    private void respectPublicRateLimit() {
        long now = System.currentTimeMillis();
        long remaining = 1000 - (now - lastRequestAt);
        if (remaining > 0) {
            try {
                Thread.sleep(remaining);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ApiException("Address search was interrupted");
            }
        }
        lastRequestAt = System.currentTimeMillis();
    }

    private record CacheEntry(Instant createdAt, List<LocationSearchResponse> items) {}
}

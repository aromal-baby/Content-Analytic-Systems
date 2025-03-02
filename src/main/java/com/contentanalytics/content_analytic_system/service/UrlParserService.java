package com.contentanalytics.content_analytic_system.service;


import com.contentanalytics.content_analytic_system.exception.PlatformOperationException;
import com.contentanalytics.content_analytic_system.model.dto.ContentIdentifierDTO;
import com.contentanalytics.content_analytic_system.model.enums.Platform;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class UrlParserService {

    private static final Map<String, Platform> DOMAIN_PLATFORM_MAP = new HashMap<>();

    // Initializing domain to platform mapping
    static {
        // YouTube domains
        DOMAIN_PLATFORM_MAP.put("youtube.com", Platform.YOUTUBE);
        DOMAIN_PLATFORM_MAP.put("youtu.be", Platform.YOUTUBE);
        DOMAIN_PLATFORM_MAP.put("www.youtube.com", Platform.YOUTUBE);

        // Medium domains
        DOMAIN_PLATFORM_MAP.put("medium.com", Platform.MEDIUM);
        DOMAIN_PLATFORM_MAP.put("www.medium.com", Platform.MEDIUM);

        // WordPress - needs to be determined dynamically as it can be on any domain

    }

    // YouTube ID pattern
    private static final Pattern YOUTUBE_ID_PATTERN = Pattern.compile("(?:watch\\\\?v=|/videos/|/v/|youtu\\\\.be/|/embed/)([\\\\w-]{11})");


    // Medium story ID pattern
    private static final Pattern MEDIUM_ID_PATTERN = Pattern.compile("/([\\\\w-]+)-([a-f0-9]+)(?:\\\\?|$)");


    // Parse a content URL and extract platform and identifier
    public ContentIdentifierDTO parseUrl(String url) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            String path = uri.getPath();
            String query = uri.getQuery();

            log.debug("Parsing URL: {}, Host: {}, Path: {}", url, host, path);

            // Try to determine platform from domain
            Platform platform = DOMAIN_PLATFORM_MAP.get(host);

            // If platform is YouTube, extract video ID
            if (platform == Platform.YOUTUBE) {
                String videoId = extractYouTubeId(url);
                if(videoId != null) {
                    return new ContentIdentifierDTO(platform, videoId, url);
                }
            }

            // If platform is Medium, extract story ID
            else if (platform == Platform.MEDIUM) {
                String storyId = extractMediumId(url);
                if(storyId != null) {
                    return new ContentIdentifierDTO(platform, storyId, url);
                }
            }

            // Check if it's a WordPress site
            else if (isWordPressSite(url)) {
                platform = Platform.WORDPRESS;
                String postId = extractWordPressId(url);
                if (postId != null) {
                    return new ContentIdentifierDTO(platform, postId, url);
                }
            }

            throw new PlatformOperationException("Could not determine platform or extract identifier from URL: " + url);

        } catch (URISyntaxException e) {
            log.error("Invalid URL format: {}", url, e);
            throw new PlatformOperationException("Invalid URL format: " + e.getMessage());
        }
    }

    // Extracting YouTube video ID from various YouTube URL formats
    private String extractYouTubeId(String url) {
        Matcher matcher = YOUTUBE_ID_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    //Extracting Medium Story ID from its URL
    private String extractMediumId(String url) {
        Matcher matcher = MEDIUM_ID_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.group(2);
        }
        return null;
    }

    // Determining if a URL is from a WordPress site by checking for common WordPress endpoints
    private boolean isWordPressSite(String url) {
        try {
            // Make a HEAD request to check if /wp-json/ endpoint exists
            return url.contains("/wp-content/") || url.contains("/wp-includes/") || url.contains("/wp-json/");
        } catch (Exception e) {
            return false;
        }
    }

    // Extracting WordPress post ID from URL
    private String extractWordPressId(String url) {
        try {
            URI uri = new URI(url);
            String path = uri.getPath();

            // Trying to find path id in path segments
            if(path.contains("p=")) {
                Pattern p = Pattern.compile("p=(\\d+)");
                Matcher m = p.matcher(url);
                if (m.find()) {
                    return m.group(1);
                }
            }

            // Fallback: use the slug as identifier
            String[] segments = path.split("/");
            for (int i = segments.length; i >= 0; i--) {
                if (!segments[i].isEmpty()) {
                    return "wp-" + segments[i];
                }
            }

            // Last resort: hash the URL
            return "wp-" + Math.abs(url.hashCode());

        }catch (Exception e) {
            log.error("Error extracting word press id: {}", url, e);
            return null;
        }
    }
}

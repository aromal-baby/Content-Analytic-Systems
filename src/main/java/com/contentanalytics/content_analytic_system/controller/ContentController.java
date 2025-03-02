package com.contentanalytics.content_analytic_system.controller;

import com.contentanalytics.content_analytic_system.exception.ContentNotFoundException;
import com.contentanalytics.content_analytic_system.exception.PlatformOperationException;
import com.contentanalytics.content_analytic_system.model.entity.Content;
import com.contentanalytics.content_analytic_system.model.enums.Platform;
import com.contentanalytics.content_analytic_system.repository.sql.IContentRepository;
import com.contentanalytics.content_analytic_system.service.ContentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/api/content")
// Controller for handling the basic content operations basically for CRUD
public class ContentController {

    private final IContentRepository contentRepository;
    private final ContentService contentService;

    @Autowired
    public ContentController(IContentRepository contentRepository, ContentService contentService) {
        this.contentRepository = contentRepository;
        this.contentService = contentService;

        System.out.println("ContentController initialized");
    }

    // Test dose(db)
    @GetMapping("/test-db")
    public ResponseEntity<String> testDatabase() {
        try {
            long count = contentRepository.count();
            return ResponseEntity.ok("Database connected! Found " + count + " records");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Database error: " + e.getMessage());
        }
    }

    //CREATE
    @PostMapping
    public ResponseEntity<Content> createContent(@Valid @RequestBody Content content) {
        log.info("Creating new content: {}", content.getTitle());
        return ResponseEntity.ok(contentService.addContent(content));
    }


    // READ
    @GetMapping
    public ResponseEntity<Page<Content>> getAllContents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(contentRepository.findAll(pageable));
    }



    // To get content by ID
    @GetMapping("/{id}")
    public ResponseEntity<Content> getContent(@PathVariable Long id) {
        return contentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ContentNotFoundException("Content not found with ID: " + id));
    }


    //To get content by platform
    @GetMapping("/platform/{platform}")
    public ResponseEntity<List<Content>> getContentByPlatform(@PathVariable Platform platform) {
        List<Content> contents = contentService.getContentByPlatform(platform);
        return ResponseEntity.ok(contents);
    }


    /*Adding a content
    @PostMapping
    public ResponseEntity<Content> addContent(@RequestBody Content content) {
        content.setCreatedAt(LocalDateTime.now());
        content.setUpdatedAt(LocalDateTime.now());
        content.setStatus(Content.ContentStatus.ACTIVE);
        Content savedContent = contentRepository.save(content);
        return ResponseEntity.ok(savedContent);
    }*/



    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Content> updateContent(
            // First checks the content exists or not
            @PathVariable Long id,
            @RequestBody Content content) {
        return contentRepository.findById(id)
                .map(existingContent -> {
                    existingContent.setTitle(content.getTitle());
                    existingContent.setPlatform(content.getPlatform());
                    existingContent.setContentUrl(content.getContentUrl());
                    existingContent.setViews(content.getViews());
                    existingContent.setLikes(content.getLikes());
                    existingContent.setComments(content.getComments());
                    existingContent.setUpdatedAt(LocalDateTime.now());
                    return ResponseEntity.ok(contentRepository.save(existingContent));
                })
                .orElseThrow(() -> new ContentNotFoundException("Content not found with ID: " + id));
    }

    //To delete content - DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContent(@PathVariable Long id) {
        log.info("Deleting content: {}", id);
        //Same as in update
        if(!contentRepository.existsById(id)) {
            throw new ContentNotFoundException("Content not found with ID: " + id);
        }
        contentRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}

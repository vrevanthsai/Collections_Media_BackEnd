package com.manga.collectionBend.controllers;

import com.manga.collectionBend.dto.ApiResponse;
import com.manga.collectionBend.dto.PublicCollectionDto;
import com.manga.collectionBend.entities.CollectionEntity;
import com.manga.collectionBend.service.CollectionService;
import com.manga.collectionBend.service.CollectionServiceImpl;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/user/{userId}/community")
public class CommunityController {

    private final CollectionServiceImpl collectionService;

//    Contractor Dependency Injection(D.I)
    public CommunityController(CollectionServiceImpl collectionService) {
        this.collectionService = collectionService;
    }

    // GET- Api- returns only Public Marked Collections to FE upto 50 max latest records only
    @GetMapping("/get-public-collections")
    public ApiResponse<List<PublicCollectionDto>> getCommunityCollections() {
        int maxRecords = 3;
        List<CollectionEntity> collections = collectionService.getPublicCollections(maxRecords);
        // map to DTOs as needed
        return ApiResponse.success(collections.stream().map(PublicCollectionDto::fromEntity).toList());
    }
}

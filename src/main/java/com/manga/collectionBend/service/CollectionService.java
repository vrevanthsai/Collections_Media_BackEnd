package com.manga.collectionBend.service;

import com.manga.collectionBend.dto.CollectionDto;
import com.manga.collectionBend.dto.CollectionPageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface CollectionService {
    CollectionDto addCollection(CollectionDto collectionDto, MultipartFile file) throws IOException;

    CollectionDto getCollection(Integer collectionId);

    List<CollectionDto> getAllCollections();

    CollectionDto updateCollection(Integer collectionId, CollectionDto collectionDto, MultipartFile file) throws IOException;

    String deleteCollection(Integer collectionId) throws IOException;

    CollectionPageResponse getAllCollectionsWithPagination(Integer pageNumber, Integer pageSize);

    CollectionPageResponse getAllCollectionsWithPaginationAndSorting(Integer pageNumber, Integer pageSize,
                                                                     String sortBy, String dir);
}

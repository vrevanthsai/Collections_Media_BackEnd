package com.manga.collectionBend.dto;

import java.util.List;

//this method send customised response for pagination
public record CollectionPageResponse(List<CollectionDto> collectionDtos,
//                                     current page number of user
                                     Integer pageNumber,
//                                     total records per page
                                     Integer pageSize,
//                                     total records for all pages
                                     Long totalElements,
                                     int totalPages,
//                                     used for knowing whether page is at last record or not
                                     boolean isLast) {

}

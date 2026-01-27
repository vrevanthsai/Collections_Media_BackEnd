package com.manga.collectionBend.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.manga.collectionBend.dto.CollectionDto;
import com.manga.collectionBend.dto.CollectionPageResponse;
import com.manga.collectionBend.exceptions.EmptyFileException;
import com.manga.collectionBend.service.CollectionService;
import com.manga.collectionBend.utils.AppConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/collection")
public class CollectionController {

    private final CollectionService collectionService;

//    constructor dependency injection
    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

//    POST API-ADD
// this is the Syntax for Pre-Authorization- Role-based - Access-Restriction(one of the Security filter) and user's without 'ADMIN' role cant access this API and throws Error(Bad Request)
//    and this is linked with UserEntity class- GrantedAuthority() method which has logged-In user role value
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/add-collection")
    public ResponseEntity<CollectionDto> addCollectionHandler(@RequestPart MultipartFile file,
                                                              @RequestPart String collectionDto) throws IOException, EmptyFileException {
        // it receives json/string data part and image file part from client
        // collectionDto Type will be String if it comes from FormData and,
        // it will be direct json(CollectionDto- where springboot will convert automatically from json to DTO object) if it is raw data from PostMan
        // if we use String type then we need to convert String data to DTO object to send to service method

        //        validation for file
        if(file.isEmpty()){
            throw new EmptyFileException("File is empty! Please send another file!");
        }

        CollectionDto dto = convertToCollectionDto(collectionDto);
//        HttpStatus.CREATED may bt 200/202 status code
        return new ResponseEntity<>(collectionService.addCollection(dto,file), HttpStatus.CREATED);
    };

    private CollectionDto convertToCollectionDto(String collectionDtoObj) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.readValue() is used for mapping/converting given string/other(1st args) data to required class object(2nd args) and returns that converted class object
        CollectionDto collectionDto = objectMapper.readValue(collectionDtoObj, CollectionDto.class);
        return collectionDto;
    };

//    GET-APIS
//    GET-single collection api
//    PathVariable value comes from client and both path variable and method args should be same
    @GetMapping("/{collectionId}")
    public ResponseEntity<CollectionDto> getCollectionHandler(@PathVariable Integer collectionId){
        return ResponseEntity.ok(collectionService.getCollection(collectionId));
    }

//    GET-all collections api
    @GetMapping("/all")
    public ResponseEntity<List<CollectionDto>> getAllCollectionsHandler(){
        return ResponseEntity.ok(collectionService.getAllCollections());
    }

//    Get-All collections Api with Pagination logic
    @GetMapping("/allCollectionsPage")
    public ResponseEntity<CollectionPageResponse> getCollectionsPagination(
//            if Client Url does not have Params then we use our own default values
//    @RequestParam() takes values directly from Url (?)-Params Values here and
//  same Param-var name should be in Url like /allCollectionsPage?pageNumber=1?pageSize=2
//  while sending Api request from Fend or Postman
            @RequestParam(defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize
    ){
        return ResponseEntity.ok(collectionService.getAllCollectionsWithPagination(pageNumber, pageSize));
    };

    //    Get-All collections Api with Pagination logic
    @GetMapping("/allCollectionsPageSort")
    public ResponseEntity<CollectionPageResponse> getCollectionsPaginationAndSorting(
//            if Client Url does not have Params then we use our own default values
            @RequestParam(defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(defaultValue = AppConstants.SORT_BY, required = false) String sortBy,
            @RequestParam(defaultValue = AppConstants.SORT_DIR, required = false) String dir
    ){
        return ResponseEntity.ok(collectionService.getAllCollectionsWithPaginationAndSorting(pageNumber, pageSize, sortBy, dir));
    };

//    PUT-UPDATE API
    @PutMapping("/update/{collectionId}")
    public ResponseEntity<CollectionDto> updateCollectionHandler(@PathVariable Integer collectionId,
                                                                 @RequestPart String collectionDtoObj,
                                                                 @PathVariable MultipartFile file) throws IOException {
//        setting file value to null if client side file is not provided to be updated
        if(file.isEmpty()) file = null;
        CollectionDto collectionDto = convertToCollectionDto(collectionDtoObj);
        return ResponseEntity.ok(collectionService.updateCollection(collectionId, collectionDto, file));
    }

//    DELETE-API
    @DeleteMapping("/delete/{collectionId}")
//    we should give Type-String because it returns string in service method
    public ResponseEntity<String> deleteCollectionHandler(@PathVariable Integer collectionId) throws IOException {
        return ResponseEntity.ok(collectionService.deleteCollection(collectionId));
    }

}

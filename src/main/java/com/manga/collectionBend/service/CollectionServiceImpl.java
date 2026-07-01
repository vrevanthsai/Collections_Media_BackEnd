package com.manga.collectionBend.service;

import com.manga.collectionBend.auth.entities.UserEntity;
import com.manga.collectionBend.auth.repositories.UserRepo;
import com.manga.collectionBend.dto.ApiResponse;
import com.manga.collectionBend.dto.CollectionDto;
import com.manga.collectionBend.dto.CollectionPageResponse;
import com.manga.collectionBend.entities.CategoryEntity;
import com.manga.collectionBend.entities.CollectionEntity;
import com.manga.collectionBend.exceptions.CollectionNotFoundExpception;
import com.manga.collectionBend.exceptions.FileExistsException;
import com.manga.collectionBend.repositories.CategoryRepo;
import com.manga.collectionBend.repositories.CollectionRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class CollectionServiceImpl implements CollectionService{

    private final UserRepo userRepo;
    private final CollectionRepo collectionRepo;
    private final FileService fileService;
    private final CategoryRepo categoryRepo;

    @Value("${project.collectionImage}")
    private String path;

    @Value("${base.url}")
    private String baseUrl;

    public CollectionServiceImpl(UserRepo userRepo, CollectionRepo collectionRepo, FileService fileService, CategoryRepo categoryRepo) {
        this.userRepo = userRepo;
        this.collectionRepo = collectionRepo;
        this.fileService = fileService;
        this.categoryRepo = categoryRepo;
    }

    @Override
    public ApiResponse<CollectionDto> addCollection(CollectionDto collectionDto, MultipartFile file) throws IOException {
//        if imageName of collection already exists in our images folder which was uploaded in past
//        and again user tries to upload same image or different image with same name then we send error
//        to make him try with another imageName, so that no duplication happens
        if(file != null && !file.isEmpty() && Files.exists(Paths.get(path + File.separator + file.getOriginalFilename()))){
//            throw new FileExistsException("File name already exists! Please try with another file name!");
            return ApiResponse.error("File name already exists! Please try with another file name! - " + file.getOriginalFilename());
        }

        //        to add userReference data into Collection table- not userId-Integer
        UserEntity user = userRepo.findById(collectionDto.getUserId())
                .orElseThrow();
//        Validation to prevent duplicate data entries with same collection name
        List<CollectionEntity> collections = collectionRepo.findByUserId(user);
        // Check for duplicate category name (case-insensitive) to prevent duplicate data creations
        boolean isDuplicate = collections.stream()
                .anyMatch(collection -> collection.getName()
                        .equalsIgnoreCase(collectionDto.getName().trim()));

        if (isDuplicate) {
            return ApiResponse.error("Collection '" + collectionDto.getName() + "' already exists, pls try with new Collection Name.");
        }

        String uploadedFileName = "";
//        only store img- if file is sent from frontend - because img field is optional
        if(file != null && !file.isEmpty()){
            //        upload the file -> path comes from properties and file from controller
            uploadedFileName = fileService.uploadFile(path,file);

//        set the value of field 'imageName' in collectionDto- if file is there
            collectionDto.setImagename(uploadedFileName);
        } else {
            collectionDto.setImagename(""); // mark it as null or "" empty string - if file/img is not there
        }

//        map dto object to Collection Entity object
        CollectionEntity collection = new CollectionEntity(
                null, // id should be null for saving because we're already using autoGeneration Ids in entity file for ID
                collectionDto.getName(),
                null, // value will be added below
                null,
                collectionDto.getRating(),
                collectionDto.getReview(),
                collectionDto.getProgress(),
                collectionDto.getPrivacy(),
                collectionDto.getAddedDate(),
                collectionDto.getImagename()
        );

//      same reason as above
        CategoryEntity category = categoryRepo.findById(collectionDto.getCategory())
                .orElseThrow();
//      adding other tabless references and saving them into Collection table record
        collection.setUserId(user);
        collection.setCategory(category);

//        save the collection entity object to DB using repo methods
//        save() - saves new primaryId as new record in table if it does not exist
//        and if it exists then it updates that record of ID
        CollectionEntity savedCollection = collectionRepo.save(collection);

        String collectionUrl = ""; // send empty string as img path if collection does not have image-file
        if(file != null && !file.isEmpty()){
            //  if image-file is there then generate the CollectionImage URL to send to client- it is an API(image retrieve) from fileService
            collectionUrl = baseUrl + "/file/" + uploadedFileName;
        }

//        map collectionEntity object to DTO object and return it
        CollectionDto response = new CollectionDto(
            savedCollection.getCollectionId(),
                savedCollection.getName(),
                savedCollection.getCategory().getCategoryId(), // Collection table has - Category table reference - inside that we get Category id field
                savedCollection.getUserId().getUserId(),
                savedCollection.getRating(),
                savedCollection.getReview(),
                savedCollection.getProgress(),
                savedCollection.getPrivacy(),
                savedCollection.getAddedDate(),
                savedCollection.getImagename(),
                collectionUrl
        );
//        to send new field to frontend
        response.setCategoryName(collection.getCategory().getCategoryName());

        return ApiResponse.success(response);
    }

    @Override
    public CollectionDto getCollection(Integer collectionId) {
//        check the data in DB and if it exists, fetch the data of given ID
        CollectionEntity collection = collectionRepo.findById(collectionId)
                .orElseThrow(() -> new CollectionNotFoundExpception("Collection not found with id = " + collectionId));

        String collectionUrl = "";
        if(!Objects.equals(collection.getImagename(), "")){ // not equal to null
            //        generate imageURL
            collectionUrl = baseUrl + "/file/" + collection.getImagename(); // if imageName path exists in DB then create a Url path to send to Client
        }

//        map to collectionDto object and return it
        CollectionDto response = new CollectionDto(
                collection.getCollectionId(),
                collection.getName(),
                collection.getCategory().getCategoryId(),
                collection.getUserId().getUserId(),
                collection.getRating(),
                collection.getReview(),
                collection.getProgress(),
                collection.getPrivacy(),
                collection.getAddedDate(),
                collection.getImagename(),
                collectionUrl
        );
        response.setCategoryName(collection.getCategory().getCategoryName());

        return response;
    }

    @Override
    public List<CollectionDto> getAllCollections() {
//        fetch all collection data from DB
        List<CollectionEntity> collections = collectionRepo.findAll();

        List<CollectionDto> collectionDtos = new ArrayList<>();

//        iterate through the list and generate imageURL for each collection obj of retrieved data objects from DB and
//        map to CollectionDto object
        for(CollectionEntity collection : collections){
            String collectionUrl = "";
            if(!Objects.equals(collection.getImagename(), "")){ // not equal to null
                //        generate imageURL
                collectionUrl = baseUrl + "/file/" + collection.getImagename(); // if imageName path exists in DB then create a Url path to send to Client
            }
            CollectionDto collectionDto = new CollectionDto(
                    collection.getCollectionId(),
                    collection.getName(),
                    collection.getCategory().getCategoryId(),
                    collection.getUserId().getUserId(),
                    collection.getRating(),
                    collection.getReview(),
                    collection.getProgress(),
                    collection.getPrivacy(),
                    collection.getAddedDate(),
                    collection.getImagename(),
                    collectionUrl
            );
            collectionDto.setCategoryName(collection.getCategory().getCategoryName());
            collectionDtos.add(collectionDto);
        }

        return collectionDtos;
    }

    @Override
    public List<CollectionDto> getUserBasedCollections(Integer userId) {
//        fetch all collection data based UserId provided from DB
        UserEntity user = userRepo.findById(userId).get();
        List<CollectionEntity> collections = collectionRepo.findByUserId(user);

        List<CollectionDto> collectionDtos = new ArrayList<>();

//        iterate through the list and generate imageURL for each collection obj of retrieved data objects from DB and
//        map to CollectionDto object
        for(CollectionEntity collection : collections){
//            generate imageURL - only if imageUrl is present in DB or else send "" empty string- which means user not given image/file while creating a collection data- as image field is OPTIONAL
            String collectionUrl = "";
            if(!Objects.equals(collection.getImagename(), "")){ // not equal to null
                //        generate imageURL
                collectionUrl = baseUrl + "/file/" + collection.getImagename(); // if imageName path exists in DB then create a Url path to send to Client
            }
            CollectionDto collectionDto = new CollectionDto(
                    collection.getCollectionId(),
                    collection.getName(),
                    collection.getCategory().getCategoryId(),
                    collection.getUserId().getUserId(),
                    collection.getRating(),
                    collection.getReview(),
                    collection.getProgress(),
                    collection.getPrivacy(),
                    collection.getAddedDate(),
                    collection.getImagename(),
                    collectionUrl
            );
            collectionDto.setCategoryName(collection.getCategory().getCategoryName());
            collectionDtos.add(collectionDto);
        }

        return collectionDtos;
    }

    @Override
    public ApiResponse<CollectionDto> updateCollection(Integer collectionId, CollectionDto collectionDto, MultipartFile file) throws IOException {
        if(file != null && !file.isEmpty() && Files.exists(Paths.get(path + File.separator + file.getOriginalFilename()))){
//            throw new FileExistsException("File name already exists! Please try with another file name!");
            return ApiResponse.error("File name already exists! Please try with another file name! - " + file.getOriginalFilename());
        }
        //        check if collection object/record exists with given collectionId or not
        CollectionEntity existingCollection = collectionRepo.findById(collectionId)
                .orElseThrow(() -> new CollectionNotFoundExpception("Collection not found with id = " + collectionId));

//        if file/image is null then do nothing
//        but if file is not null then delete existing file/image associated with the record(imageName in DB and actual image in Bend-imagefolder)
//        and upload the new image/file
        String fileName = existingCollection.getImagename();
        if(file != null){
            if(!Objects.equals(fileName, "")){
                //            deleting old image
                Files.deleteIfExists(Paths.get(path + File.separator + fileName));
            }
//            uploading new image
            fileName = fileService.uploadFile(path, file);
        }

//        set new imageName to collectionDto object
        collectionDto.setImagename(fileName);

//        map it to collection entity object
        CollectionEntity collection = new CollectionEntity(
                existingCollection.getCollectionId(), // providing id which will update this ID's record in table
                collectionDto.getName(),
                null,
                null,
                collectionDto.getRating(),
                collectionDto.getReview(),
                collectionDto.getProgress(),
                collectionDto.getPrivacy(),
                collectionDto.getAddedDate(),
                collectionDto.getImagename()
        );

        UserEntity user = userRepo.findById(collectionDto.getUserId())
                .orElseThrow();

        CategoryEntity category = categoryRepo.findById(collectionDto.getCategory())
                .orElseThrow();

        collection.setUserId(user);
        collection.setCategory(category);

//        save the updated collection object, it returns saved/updated collection object
        // save()- will update the record where collection(entity object) has ID in it.
        CollectionEntity updatedCollection = collectionRepo.save(collection);

//        generate imageURL for it
        String collectionUrl = "";
        if(!Objects.equals(collection.getImagename(), "")){ // not equal to null
            //        generate imageURL
            collectionUrl = baseUrl + "/file/" + collection.getImagename(); // if imageName path exists in DB then create a Url path to send to Client
        }

//        map to CollectionDto for it
        CollectionDto response = new CollectionDto(
                collection.getCollectionId(),
                collection.getName(),
                collection.getCategory().getCategoryId(),
                collection.getUserId().getUserId(),
                collection.getRating(),
                collection.getReview(),
                collection.getProgress(),
                collection.getPrivacy(),
                collection.getAddedDate(),
                collection.getImagename(),
                collectionUrl
        );
        response.setCategoryName(collection.getCategory().getCategoryName());

        return ApiResponse.success(response);
    }

    @Override
    public String deleteCollection(Integer collectionId) throws IOException {
        //        check if collection object/record exists with given collectionId or not
        CollectionEntity existingCollection = collectionRepo.findById(collectionId)
                .orElseThrow(() -> new CollectionNotFoundExpception("Collection not found with id = " + collectionId));
        String collectionName = existingCollection.getName();
//         Integer id = existingCollection.getCollectionId();

//        delete the file/image associated with this object which will be deleted from table/db
        Files.deleteIfExists(Paths.get(path + File.separator + existingCollection.getImagename()));

//        delete the collection object from record/table
        collectionRepo.delete(existingCollection);

        return "Collection deleted with name = " + collectionName;
    }

    @Override
    public CollectionPageResponse getAllCollectionsWithPagination(Integer pageNumber, Integer pageSize) {
//        Pageable is interface and PageRequest.of() returns Pageable object
//        import Pageable from data.domain package only
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

//        import correct Page value from domain package only
//        this findAll() only accepts Pageable object only and this buildIn method of JpaRepository-
//        returns our Records(are in Entity format) Data with Pagination directly from DB without any additional logics
        Page<CollectionEntity> collectionPages = collectionRepo.findAll(pageable);
//        storing all fetched data in List var
        List<CollectionEntity>  collections = collectionPages.getContent();

        List<CollectionDto> collectionDtos = new ArrayList<>();

//        iterate through the list and generate imageURL for each collection obj of retrieved data objects from DB and
//        map to CollectionDto object
        for(CollectionEntity collection : collections){
            String collectionUrl = baseUrl + "/file/" + collection.getImagename();
            CollectionDto collectionDto = new CollectionDto(
                    collection.getCollectionId(),
                    collection.getName(),
                    collection.getCategory().getCategoryId(),
                    collection.getUserId().getUserId(),
                    collection.getRating(),
                    collection.getReview(),
                    collection.getProgress(),
                    collection.getPrivacy(),
                    collection.getAddedDate(),
                    collection.getImagename(),
                    collectionUrl
            );
            collectionDto.setCategoryName(collection.getCategory().getCategoryName());
//            we convert entity data to DTO object and send that DTO only to controller, not direct entity object
            collectionDtos.add(collectionDto);
        }

//        include all required Args using buildIn methods of Pageable var and send our custom response for Pagination API
        return new CollectionPageResponse(collectionDtos, pageNumber, pageSize,
                                        collectionPages.getTotalElements(),
                                        collectionPages.getTotalPages(),
                                        collectionPages.isLast());
    }

    @Override
    public CollectionPageResponse getAllCollectionsWithPaginationAndSorting(Integer pageNumber, Integer pageSize,
                                                                            String sortBy, String dir) {
//        Sort class must be imported from data.domain package only
//        this Sort var has Sorting format value to be used later in Pageable Args
//        asc value can be changed
        Sort sort = dir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
                                                                : Sort.by(sortBy).descending();

        //       Pageable is interface and PageRequest.of() returns Pageable object
//        import Pageable from data.domain package only
//        this VAr has both Pagination and Sorting format which will be used in Repo.findAll() method
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

//        import correct Page value from domain package only
//        this findAll() only accepts Pageable object only and this buildIn method of JpaRepository-
//        returns our Records(are in Entity format) Data with Pagination directly from DB without any additional logics
        Page<CollectionEntity> collectionPages = collectionRepo.findAll(pageable);
//        storing all fetched data in List var
        List<CollectionEntity>  collections = collectionPages.getContent();

        List<CollectionDto> collectionDtos = new ArrayList<>();

//        iterate through the list and generate imageURL for each collection obj of retrieved data objects from DB and
//        map to CollectionDto object
        for(CollectionEntity collection : collections){
            String collectionUrl = baseUrl + "/file/" + collection.getImagename();
            CollectionDto collectionDto = new CollectionDto(
                    collection.getCollectionId(),
                    collection.getName(),
                    collection.getCategory().getCategoryId(),
                    collection.getUserId().getUserId(),
                    collection.getRating(),
                    collection.getReview(),
                    collection.getProgress(),
                    collection.getPrivacy(),
                    collection.getAddedDate(),
                    collection.getImagename(),
                    collectionUrl
            );
            collectionDto.setCategoryName(collection.getCategory().getCategoryName());
//            we convert entity data to DTO object and send that DTO only to controller, not direct entity object
            collectionDtos.add(collectionDto);
        }

//        include all required Args using buildIn methods of Pageable var and send our custom response for Pagination API
        return new CollectionPageResponse(collectionDtos, pageNumber, pageSize,
                collectionPages.getTotalElements(),
                collectionPages.getTotalPages(),
                collectionPages.isLast());

    }


}

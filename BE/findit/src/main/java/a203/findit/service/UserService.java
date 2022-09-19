package a203.findit.service;

import a203.findit.model.repository.UserRepository;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;


import a203.findit.model.dto.req.User.CreateUserDTO;
import a203.findit.model.dto.req.User.LoginUserDTO;
import a203.findit.model.dto.req.User.UpdateFormDTO;
import a203.findit.model.dto.res.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public interface UserService {


    void setValue(String key, String data);
    String getStringValue(String key);
    void setValue(String key, String[] data);
    Set<String> getSetValue(String key);
    void setValue(String key, Object obj1, Object obj2);
    Object getHashValue(String key, String hash);
    void setStringValueAndExpire(String key, String token, long expireDate);
    void deleteKey(String key);
    void deleteKey(String hashKey, String key);
    void setTokenBlackList(String token, String value, long expireTime);
    
    ApiResponse createUser(CreateUserDTO createUserDTO);

    ResponseEntity createUser(CreateUserDTO createUserDTO);

    ResponseEntity login(@Valid LoginUserDTO loginUserDTO);

    ResponseEntity logout();

    ResponseEntity userDetails(String userId);

    ResponseEntity updateForm(UpdateFormDTO updateFormDTO);

    ResponseEntity getImgList(MultipartFile img);

    ResponseEntity updateUser();

    ResponseEntity deleteUser();

    ResponseEntity createTreasure();


    ApiResponse getTreasure();

    ResponseEntity getTreasure();

}

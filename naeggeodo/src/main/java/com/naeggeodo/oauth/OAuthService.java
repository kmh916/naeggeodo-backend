package com.naeggeodo.oauth;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.naeggeodo.entity.user.Authority;
import com.naeggeodo.entity.user.Users;
import com.naeggeodo.oauth.config.InMemoryProviderRepository;
import com.naeggeodo.oauth.config.OauthProvider;
import com.naeggeodo.oauth.dto.KakaoOAuthDto;
import com.naeggeodo.oauth.dto.NaverOAuthDto;
import com.naeggeodo.oauth.dto.OAuthDto;
import com.naeggeodo.oauth.dto.OauthAuthorized;
import com.naeggeodo.oauth.dto.SimpleUser;
import com.naeggeodo.repository.UserRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class OAuthService {

	@Autowired
    private InMemoryProviderRepository inMemoryProviderRepository;
	@Autowired
	private SocialLogin oauthDetail;
    private UserRepository userRepository;
    private OAuthDtoMapper oauthMapper;

    @Transactional
    public SimpleUser getAuth(String code, String providerName) throws JSONException, Exception {
    	Map<String, String> requestHeaders = new HashMap<>();
    	
    	log.info("getAuth : ");
    	OAuthDto oauthUserInfo = setOAuthDto(providerName);    	
    	OauthProvider provider = inMemoryProviderRepository.findByProviderName(providerName);
    	
    	OauthAuthorized authorization = oauthDetail.requestAccessToken(code, provider);
    	
    	requestHeaders.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
    	requestHeaders.put("Authorization", "Bearer "+authorization.getAccessToken());
    	
    	String responseBody = oauthDetail.get(provider.getUserInfoUrl(), requestHeaders);
    	
    	ObjectMapper objectMapper = new ObjectMapper();
    	objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    	new Users().setJoindate(LocalDateTime.now());
    	try {
    		oauthUserInfo = objectMapper.readValue(
    				responseBody, 
    				oauthUserInfo.getClass());
    		
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return getUser(oauthUserInfo);
    	
    }
    
    public SimpleUser getAuth(OauthAuthorized oauthToken, String providerName) {
    	log.info("Mobile getAuth : " + oauthToken.toString());
    	OAuthDto oauthUserInfo = setOAuthDto(providerName);
    	
    	return getUser(
    			oauthDetail.requestUserInfo(
	    			inMemoryProviderRepository.findByProviderName(providerName).getUserInfoUrl(),
	    			oauthToken,
	    			oauthUserInfo	
	    			)
    			);
    }
    
    private OAuthDto setOAuthDto(String providerName) {
    	switch(providerName) {
    	case "naver":
    		return new NaverOAuthDto();
    	case "kakao":
    		return new KakaoOAuthDto();
    	default:
    		throw new NullPointerException();
    	}
    }

    @Transactional 
    @NotFound(action = NotFoundAction.IGNORE)
    protected SimpleUser getUser(OAuthDto oauthDto) {
    	Users user = userRepository.findBySocialIdAndAuthority(oauthDto.getId(), Authority.MEMBER);
    	
    	if(user==null) {
    		user = oauthMapper.mappingUser(oauthDto);
    		user.setId(UUID.randomUUID().toString());
    		user.setAuthority(Authority.MEMBER);
    		user.setJoindate(LocalDateTime.now());
    		
    		user = userRepository.save(user);
    	}
    	

    	return new SimpleUser(user.getId(), user.getAddress(), user.getAuthority(), user.getBuildingCode());

    }



   
}


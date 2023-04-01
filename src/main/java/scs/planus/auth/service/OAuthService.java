package scs.planus.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import scs.planus.auth.dto.OAuth2TokenResponseDto;
import scs.planus.auth.dto.OAuthLoginResponseDto;
import scs.planus.auth.jwt.JwtProvider;
import scs.planus.auth.jwt.Token;
import scs.planus.auth.jwt.redis.RedisService;
import scs.planus.auth.userinfo.attribute.MemberProfile;
import scs.planus.auth.userinfo.attribute.OAuthAttributes;
import scs.planus.domain.Member;
import scs.planus.repository.MemberRepository;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthService {

    private final InMemoryClientRegistrationRepository clientRegistrations;
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;
    private final RedisService redisService;

    public OAuthLoginResponseDto login(String providerName, String code) {
        ClientRegistration client = clientRegistrations.findByRegistrationId(providerName);
        Map<String, Object> attributes = getUserAttributes(client, code);
        MemberProfile profile = OAuthAttributes.extract(providerName, attributes);

        Member member = saveMember(profile);
        Token token = jwtProvider.generateToken(member.getEmail());
        redisService.saveValue(member.getEmail(), token);

        return OAuthLoginResponseDto.builder()
                .memberId(member.getId())
                .accessToken(token.getAccessToken())
                .refreshToken(token.getRefreshToken())
                .build();
    }

    private Map<String, Object> getUserAttributes(ClientRegistration client, String code) {
        OAuth2TokenResponseDto token = getToken(client, code);
        return WebClient.create()
                .post()
                .uri(client.getProviderDetails().getUserInfoEndpoint().getUri())
                .headers(header -> {
                    header.setBearerAuth(token.getAccessToken());
                })
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .block();
    }

    private OAuth2TokenResponseDto getToken(ClientRegistration client, String code) {
        log.info("client=[{}], client.TokenUri=[{}]", client.getRegistrationId(), client.getProviderDetails().getTokenUri());
        return WebClient.create()
                .post()
                .uri(client.getProviderDetails().getTokenUri())
                .headers(header -> {
                    header.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                    header.setAcceptCharset(Collections.singletonList(StandardCharsets.UTF_8));
                })
                .bodyValue(accessTokenRequest(client, code))
                .retrieve()
                .bodyToMono(OAuth2TokenResponseDto.class)
                .block();
    }

    private MultiValueMap<String, String> accessTokenRequest(ClientRegistration client, String code) {
        LinkedMultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("code", code);
        data.add("client_id", client.getClientId());
        data.add("client_secret", client.getClientSecret());
        data.add("redirect_uri", client.getRedirectUri());
        data.add("grant_type", "authorization_code");
        return data;
    }

    private Member saveMember(MemberProfile profile) {
        Member member = memberRepository.findByEmail(profile.getEmail()).orElse(null);
        if (member == null) {
            member = memberRepository.save(profile.toEntity());
        }
        return member;
    }
}

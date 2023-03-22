package scs.planus.auth.userinfo.impl;

import lombok.AllArgsConstructor;
import scs.planus.auth.userinfo.OAuth2UserInfo;

import java.util.Map;

@AllArgsConstructor
public class GoogleUserInfo implements OAuth2UserInfo {

    private Map<String, Object> attributes;

    @Override
    public String getRegistrationId() {
        return (String) attributes.get("sub");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getNickName() {
        return (String) attributes.get("name");
    }

}

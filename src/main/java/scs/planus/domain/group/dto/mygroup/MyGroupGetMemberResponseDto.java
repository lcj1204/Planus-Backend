package scs.planus.domain.group.dto.mygroup;

import lombok.Builder;
import lombok.Getter;
import scs.planus.domain.member.entity.Member;

@Getter
@Builder
public class MyGroupGetMemberResponseDto {

    private Long memberId;
    private String nickname;
    private Boolean isLeader;
    private Boolean isOnline;
    private String description;
    private String profileImageUrl;

    public static MyGroupGetMemberResponseDto of(Member member, Boolean isLeader, Boolean isOnline) {
        return MyGroupGetMemberResponseDto.builder()
                .memberId(member.getId())
                .nickname(member.getNickname())
                .isLeader(isLeader)
                .isOnline(isOnline)
                .description(member.getDescription())
                .profileImageUrl(member.getProfileImageUrl())
                .build();
    }
}
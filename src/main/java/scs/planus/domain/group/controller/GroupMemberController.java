package scs.planus.domain.group.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import scs.planus.domain.group.dto.GroupMemberResponseDto;
import scs.planus.domain.group.service.GroupMemberService;
import scs.planus.global.auth.entity.PrincipalDetails;
import scs.planus.global.common.response.BaseResponse;

@RestController
@RequestMapping("/app/groupMembers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "GroupMember", description = "GroupMember API Document")
public class GroupMemberController {
    private final GroupMemberService groupMemberService;

    @PatchMapping("/groups/{groupId}/members/{memberId}")
    @Operation(summary = "(리더용) 그룹 회원 일정 권한 변경 API")
    public BaseResponse<GroupMemberResponseDto> changeTodoAuthority(@AuthenticationPrincipal PrincipalDetails principalDetails,
                                                                    @PathVariable("groupId") Long groupId,
                                                                    @PathVariable("memberId") Long memberId){
        Long leaderId = principalDetails.getId();
        GroupMemberResponseDto responseDto = groupMemberService.changeTodoAuthority(groupId, leaderId, memberId);
        return new BaseResponse<>(responseDto);
    }
}

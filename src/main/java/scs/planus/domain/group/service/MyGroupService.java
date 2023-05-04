package scs.planus.domain.group.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import scs.planus.domain.group.dto.mygroup.GroupBelongInResponseDto;
import scs.planus.domain.group.dto.mygroup.MyGroupResponseDto;
import scs.planus.domain.group.entity.Group;
import scs.planus.domain.group.entity.GroupMember;
import scs.planus.domain.group.entity.GroupTag;
import scs.planus.domain.group.repository.GroupMemberRepository;
import scs.planus.domain.group.repository.GroupTagRepository;
import scs.planus.domain.member.entity.Member;
import scs.planus.domain.member.repository.MemberRepository;
import scs.planus.global.exception.PlanusException;

import java.util.List;
import java.util.stream.Collectors;

import static scs.planus.global.exception.CustomExceptionStatus.INTERNAL_SERVER_ERROR;
import static scs.planus.global.exception.CustomExceptionStatus.NONE_USER;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class MyGroupService {

    private final MemberRepository memberRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupTagRepository groupTagRepository;

    public List<GroupBelongInResponseDto> getMyGroupsInDropDown(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new PlanusException(NONE_USER));

        List<GroupMember> groupMembers = groupMemberRepository.findAllByActiveGroupAndMemberId(member.getId());
        List<GroupBelongInResponseDto> responseDtos = groupMembers.stream()
                .map(gm -> GroupBelongInResponseDto.of(gm.getGroup()))
                .collect(Collectors.toList());

        return responseDtos;
    }

    public List<MyGroupResponseDto> getMyGroups(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new PlanusException(NONE_USER));

        List<GroupMember> myGroupMembers = groupMemberRepository.findAllByActiveGroupAndMemberId(member.getId());
        List<MyGroupResponseDto> responseDtos = getMyGroupResponseDtos(myGroupMembers);

        return responseDtos;
    }

    private List<MyGroupResponseDto> getMyGroupResponseDtos(List<GroupMember> myGroupMembers) {
        List<Group> myGroups = myGroupMembers.stream()
                .map(GroupMember::getGroup)
                .collect(Collectors.toList());

        List<GroupMember> allGroupMembers = groupMemberRepository.findAllGroupMemberInGroups(myGroups);
        List<GroupTag> allGroupTags = groupTagRepository.findAllTagInGroups(myGroups);

        List<MyGroupResponseDto> responseDtos = myGroups.stream().map(group -> {
                    List<GroupTag> eachGroupTags = allGroupTags.stream()
                            .filter(groupTag -> groupTag.getGroup().getId().equals(group.getId()))
                            .collect(Collectors.toList());
                    List<GroupMember> eachGroupMembers = allGroupMembers.stream()
                            .filter(groupMember -> groupMember.getGroup().getId().equals(group.getId()))
                            .collect(Collectors.toList());
                    Boolean onlineStatus = myGroupMembers.stream().filter(groupMember -> groupMember.getGroup().getId().equals(group.getId()))
                            .map(GroupMember::isOnlineStatus)
                            .findFirst().orElseThrow(() -> new PlanusException(INTERNAL_SERVER_ERROR));
                    long onlineCount = eachGroupMembers.stream().filter(GroupMember::isOnlineStatus).count();

                    return MyGroupResponseDto.of(group, eachGroupTags, onlineStatus, onlineCount);
                })
                .collect(Collectors.toList());

        return responseDtos;
    }
}

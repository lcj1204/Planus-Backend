package scs.planus.domain.group.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import scs.planus.domain.group.dto.GroupMemberResponseDto;
import scs.planus.domain.group.entity.Group;
import scs.planus.domain.group.entity.GroupMember;
import scs.planus.domain.group.repository.GroupMemberRepository;
import scs.planus.domain.group.repository.GroupRepository;
import scs.planus.domain.member.entity.Member;
import scs.planus.domain.member.repository.MemberRepository;
import scs.planus.global.exception.PlanusException;

import static scs.planus.global.exception.CustomExceptionStatus.*;
import static scs.planus.global.exception.CustomExceptionStatus.NOT_JOINED_GROUP;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class GroupMemberService {
    private final MemberRepository memberRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;

    /**
     * 리더 권한 변경 서비스
     */

    /**
     * 일정 권한 변경 서비스
     */
    public GroupMemberResponseDto changeTodoAuthority(Long groupId, Long leaderId, Long memberId) {
        groupRepository.findById(groupId)
                .orElseThrow(() -> new PlanusException(NOT_EXIST_GROUP));

        GroupMember groupLeader = groupMemberRepository.findByMemberIdAndGroupId(leaderId, groupId)
                .orElseThrow(() -> new PlanusException(NOT_JOINED_GROUP));

        GroupMember groupMember = groupMemberRepository.findByMemberIdAndGroupId(memberId, groupId)
                .orElseThrow(() -> new PlanusException(NOT_JOINED_GROUP));

        validateLeaderPermission(groupLeader.getMember(), groupLeader.getGroup());
        groupMember.changeTodoAuthority();
        return GroupMemberResponseDto.of(groupMember);
    }

    private void validateLeaderPermission(Member member, Group group) {
        GroupMember groupLeader = groupMemberRepository.findWithGroupAndLeaderByGroup(group)
                .orElseThrow(() -> new PlanusException(NOT_EXIST_LEADER));

        if (!member.equals(groupLeader.getMember())) {
            throw new PlanusException(NOT_GROUP_LEADER_PERMISSION);
        }
    }
}

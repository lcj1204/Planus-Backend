package scs.planus.domain.group.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import scs.planus.domain.group.dto.GroupMemberResponseDto;
import scs.planus.domain.group.entity.Group;
import scs.planus.domain.group.entity.GroupMember;
import scs.planus.domain.group.repository.GroupMemberRepository;
import scs.planus.domain.group.repository.GroupRepository;
import scs.planus.domain.member.entity.Member;
import scs.planus.domain.member.repository.MemberRepository;
import scs.planus.global.exception.PlanusException;
import scs.planus.support.ServiceTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static scs.planus.global.exception.CustomExceptionStatus.NOT_GROUP_LEADER_PERMISSION;

class GroupMemberServiceTest extends ServiceTest {
    private final static Long LEADER_ID = 1L;
    private final static Long MEMBER_ID = 2L;
    private final static Long GROUP_ID = 1L;

    private final GroupMemberService groupMemberService;

    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;
    private final GroupMemberRepository groupMemberRepository;

    @Autowired
    public GroupMemberServiceTest(GroupRepository groupRepository,
                                  MemberRepository memberRepository,
                                  GroupMemberRepository groupMemberRepository) {
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.groupMemberRepository = groupMemberRepository;

        groupMemberService = new GroupMemberService(
                memberRepository,
                groupRepository,
                groupMemberRepository
        );
    }

    @DisplayName("그룹 회원 [일정 권한 변경] 서비스 테스트")
    @Nested
    class changeTodoAuthority{
        private Group group;
        private Member leader;
        private Member member;

        @BeforeEach
        void init() {
            group = groupRepository.findById(GROUP_ID).orElseThrow();
            leader = memberRepository.findById(LEADER_ID).orElseThrow();
            member = memberRepository.findById(MEMBER_ID).orElseThrow();
        }

        @DisplayName("리더 권한 테스트")
        @Nested
        class changeTodoAuthority_Leader_Permission{

            @DisplayName("그룹의 리더는 [일정 권한 변경]을 요청할 수 있다.")
            @Test
            void changeTodoAuthority_Success_Leader_Permission() {
                // member 의 todoAuthority default 값은 false 이다.
                //when
                GroupMemberResponseDto responseDto = groupMemberService.changeTodoAuthority(GROUP_ID, LEADER_ID, MEMBER_ID);
                GroupMember groupMember = groupMemberRepository.findById(responseDto.getGroupMemberId()).get();

                //then
                assertThat(responseDto.getGroupMemberId()).isEqualTo(MEMBER_ID);
                assertThat(groupMember.isTodoAuthority()).isTrue();
            }

            @DisplayName("리더가 아닌 사람이 [일정 권한 변경]을 요청하면 예외가 발생한다.")
            @Test
            void changeTodoAuthority_Fail_Leader_Permission() {
                //when & then
                assertThatThrownBy(() -> groupMemberService.changeTodoAuthority(GROUP_ID, MEMBER_ID, MEMBER_ID))
                        .isInstanceOf(PlanusException.class)
                        .extracting("status")
                        .isEqualTo(NOT_GROUP_LEADER_PERMISSION);
            }
        }

        @DisplayName("일정 권한 변경 테스트")
        @Nested
        class changeTodoAuthority_Change_TodoAuthority{

            @DisplayName("일정 권한이 true에서 false로 변경되어야 한다.")
            @Test
            void changeTodoAuthority_Success_True_To_False() {
                //given
                Member trueMember = memberRepository.save(Member.builder().build());
                GroupMember groupMember = groupMemberRepository.save(GroupMember.builder()
                        .todoAuthority(true)
                        .member(trueMember)
                        .group(group)
                        .build()
                );

                //when
                GroupMemberResponseDto responseDto = groupMemberService.changeTodoAuthority(GROUP_ID, LEADER_ID, trueMember.getId());

                //then
                assertThat(responseDto.getGroupMemberId()).isEqualTo(groupMember.getId());
                assertThat(groupMember.isTodoAuthority()).isFalse();
            }

            @DisplayName("일정 권한이 false에서 true로 변경되어야 한다.")
            @Test
            void changeTodoAuthority_Success_False_To_True() {
                //given
                Member falseMember = memberRepository.save(Member.builder().build());
                GroupMember groupMember = groupMemberRepository.save(GroupMember.builder()
                        .todoAuthority(false)
                        .member(falseMember)
                        .group(group)
                        .build()
                );

                //when
                GroupMemberResponseDto responseDto = groupMemberService.changeTodoAuthority(GROUP_ID, LEADER_ID, falseMember.getId());

                //then
                assertThat(responseDto.getGroupMemberId()).isEqualTo(groupMember.getId());
                assertThat(groupMember.isTodoAuthority()).isTrue();
            }
        }

    }
}
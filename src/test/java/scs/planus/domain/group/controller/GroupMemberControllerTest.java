package scs.planus.domain.group.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import scs.planus.domain.group.dto.GroupMemberResponseDto;
import scs.planus.domain.group.service.GroupMemberService;
import scs.planus.support.ControllerTest;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GroupMemberController.class)
class GroupMemberControllerTest extends ControllerTest {
    private static final String BASE_URI = "/app/groupMembers";
    @MockBean
    private GroupMemberService groupMemberService;

    @DisplayName("그룹 회원 [일정 권한 변경] API 테스트")
    @Nested
    class changeTodoAuthority{
        @DisplayName("API 호출에 성공하면 OK(200)를 반환한다.")
        @Test
        void changeTodoAuthority_Success() throws Exception {
            //given
            String path = BASE_URI + "/groups/{groupId}/members/{memberId}";
            Long groupId = 1L;
            Long memberId = 2L;

            given(groupMemberService.changeTodoAuthority(anyLong(), anyLong(), anyLong()))
                    .willReturn(GroupMemberResponseDto.builder().groupMemberId(memberId).build());

            //when & then
            mockMvc
                    .perform(multipart(HttpMethod.PATCH, path, groupId, memberId)
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @DisplayName("groupId를 PathVariable로 넘기지 않으면 NOT_FOUND(404)을 반환한다.")
        @Test
        void changeTodoAuthority_Throw_Exception_If_Not_GroupId() throws Exception {
            //given
            String path = BASE_URI + "/groups/members/{memberId}";
            Long memberId = 2L;

            //when & then
            mockMvc
                    .perform(multipart(HttpMethod.PATCH, path, memberId)
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @DisplayName("memberId를 PathVariable로 넘기지 않으면 NOT_FOUND(404)을 반환한다.")
        @Test
        void changeTodoAuthority_Throw_Exception_If_Not_MemberId() throws Exception {
            //given
            String path = BASE_URI + "/groups/{groupId}/members";
            Long groupId = 1L;

            //when & then
            mockMvc
                    .perform(multipart(HttpMethod.PATCH, path, groupId)
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

}
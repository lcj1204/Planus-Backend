package scs.planus.dto.todo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import scs.planus.domain.todo.Todo;

import java.time.LocalTime;

@Getter
@Builder
@ToString
public class TodoDailyResponseDto {

    private Long todoId;
    private String title;
    @JsonFormat(pattern = "HH:mm", shape = JsonFormat.Shape.STRING)
    private LocalTime startTime;

    private Boolean isGroupMemberTodo;
    private Boolean isPeriodTodo;
    private Boolean hasDescription;

    public static TodoDailyResponseDto of(Todo todo) {
        return TodoDailyResponseDto.builder()
                .todoId(todo.getId())
                .title(todo.getTitle())
                .startTime(todo.getStartTime())
                .isGroupMemberTodo(todo.getGroup() != null)
                .isPeriodTodo(todo.getStartDate() != todo.getEndDate())
                .hasDescription(todo.getDescription() != null)
                .build();
    }
}

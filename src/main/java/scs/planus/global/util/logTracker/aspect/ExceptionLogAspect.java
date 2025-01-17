package scs.planus.global.util.logTracker.aspect;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import scs.planus.global.auth.service.JwtProvider;
import scs.planus.global.exception.PlanusException;
import scs.planus.global.util.gson.EntityExclusionStrategy;
import scs.planus.global.util.logTracker.entity.ExceptionLog;
import scs.planus.global.util.logTracker.service.ExceptionLogService;
import scs.planus.global.util.logTracker.service.dto.ExceptionLogDto;
import scs.planus.global.util.slackAlarm.SlackAlarmGenerator;

import javax.servlet.http.HttpServletRequest;

import static scs.planus.global.exception.CustomExceptionStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ExceptionLogAspect {
    private final ExceptionLogService exceptionLogService;
    private final SlackAlarmGenerator slackAlarmGenerator;
    private final JwtProvider jwtProvider;

    @Pointcut("execution(* scs.planus..*(..))")
    private void allPlanus() {}
    @Pointcut("execution(* scs.planus.global.util.logTracker..*(..))")
    private void logTracker() {}
    @Pointcut("execution(* scs.planus.global.config..*(..))")
    private void config() {}

//    @Around("allPlanus()" +
//            "&& !logTracker()" +
//            "&& !config()")
    public Object sendLogMessage(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        }
        catch (PlanusException e) { // PlanusException인 경우 생략
            throw e;
        }
        catch (RuntimeException e) { // PlanusException이 아닌 경우 동작
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

            String token = resolveToken(request);
            String email = "";
            if (token != null) {
                email = jwtProvider.getPayload(token);
            }
            
            ExceptionLogDto exceptionLogDto = ExceptionLogDto.builder()
                    .requestURI(request.getRequestURI())
                    .httpMethod(request.getMethod())
                    .email(jwtProvider.getPayload(resolveToken(request)))
                    .parameter(getParameter(joinPoint))
                    .className(joinPoint.getTarget().getClass().getName())
                    .methodName(joinPoint.getSignature().getName())
                    .lineNumber(e.getStackTrace()[0].getLineNumber())
                    .exceptionType(e.getClass().getSimpleName())
                    .message(e.getMessage())
                    .build();

            ExceptionLog exceptionLog = exceptionLogService.save(exceptionLogDto);

            slackAlarmGenerator.sendExceptionLog(exceptionLog);

            log.info("== ExceptionLogAspect 구간 ==");
            throw new PlanusException(INTERNAL_SERVER_ERROR);
        }
    }
    
    private String getParameter(ProceedingJoinPoint joinPoint) {
        Gson gson = new GsonBuilder()
                .addSerializationExclusionStrategy(new EntityExclusionStrategy())
                .create();

        return gson.toJson(joinPoint.getArgs());
    }

    private String resolveToken(HttpServletRequest request) {
        String HEADER_AUTHORIZATION = "Authorization";
        String TOKEN_TYPE = "Bearer ";
        String bearerToken = request.getHeader(HEADER_AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_TYPE)) {
            return bearerToken.substring(TOKEN_TYPE.length());
        }
        return null;
    }
}

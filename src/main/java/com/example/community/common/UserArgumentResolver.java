package com.example.community.common;

import com.example.community.common.annotation.AuthUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

//    private final UserRepository userRepository;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthUser.class)
                && parameter.getParameterType().equals(String.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {

        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        return (String) request.getAttribute("email");
    }

    /**
     * 현재 코드는 email로 가져와 Service 계층에서 user가 있는지 검증을 하고 있는 코드인데
     * Arguments Resolver를 통해 user를 검증하고 반환하는게 더 좋을지...? 이렇게 하면 향후 security로 변경해도 어노테이션만 바꾸면 될거 같다는 생각이 듭니다..
     * 아니면 @AuthenticationPrincipal String email이 가능한가..?
     */

/*    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {

        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        String email = (String) request.getAttribute("email");
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UnauthorizedException(ErrorMessage.UNAUTHORIZED)
        );
        return user;
    }*/
}
package io.kentra.openlineage.config;

import io.kentra.openlineage.lineage.NodeMdcUtil;
import io.kentra.openlineage.lineage.model.Node;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.Set;

/**
 * This intercepts all incoming HTTP requests and if the request is modifying.
 */
@Slf4j
public class EndpointPathInterceptor implements HandlerInterceptor {
  private final Set<String> modifyingMethods = Set.of("POST", "PUT", "DELETE", "PATCH");

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    if (handler instanceof HandlerMethod) {
      if (modifyingMethods.contains(request.getMethod())) {
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        String[] requestMappings = handlerMethod.getBean().getClass().getAnnotation(RequestMapping.class).value();
        String requestMappingsString = Arrays.toString(requestMappings);
        log.debug("Intercepted request for Controller: {}", requestMappingsString);
        NodeMdcUtil.putInMdc(new Node(requestMappingsString, Node.Type.HTTP_ENDPOINT));
      }
    }
    return true;
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    NodeMdcUtil.clearNodeMdc();
  }
}

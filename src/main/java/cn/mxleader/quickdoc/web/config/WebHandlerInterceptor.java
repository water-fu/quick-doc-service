package cn.mxleader.quickdoc.web.config;

import cn.mxleader.quickdoc.entities.AccessAuthorization;
import cn.mxleader.quickdoc.security.entities.ActiveUser;
import cn.mxleader.quickdoc.web.domain.WebFile;
import cn.mxleader.quickdoc.web.domain.WebFolder;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.mxleader.quickdoc.common.CommonCode.HOME_TITLE;
import static cn.mxleader.quickdoc.common.CommonCode.SESSION_USER;
import static cn.mxleader.quickdoc.web.config.AuthenticationToolkit.*;

public class WebHandlerInterceptor extends HandlerInterceptorAdapter {

    public static final String FILES_ATTRIBUTE = "files";
    public static final String FOLDERS_ATTRIBUTE = "folders";

    private Map<AccessAuthorization.Type, String> getOwnerTypeMap() {
        Map<AccessAuthorization.Type, String> ownerTypeMap = new HashMap<>();
        ownerTypeMap.put(AccessAuthorization.Type.TYPE_GROUP, "组权限");
        ownerTypeMap.put(AccessAuthorization.Type.TYPE_PRIVATE, "个人权限");
        return ownerTypeMap;
    }

    private Map<Integer, String> getPrivilegeMap() {
        Map<Integer, String> privilegeMap = new HashMap<>();
        privilegeMap.put(1, "读");
        privilegeMap.put(2, "写");
        privilegeMap.put(3, "读写");
        privilegeMap.put(4, "删");
        privilegeMap.put(5, "读删");
        privilegeMap.put(6, "写删");
        privilegeMap.put(7, "读写删");
        return privilegeMap;
    }

    private Map<String, String> getUserGroupMap(ActiveUser activeUser) {

        Map<String, String> groupMap = new HashMap<>();
        String[] groups = activeUser.getGroups();
        if (groups != null) {
            for (String group : groups) {
                groupMap.put(group, group);
            }
        }
        return groupMap;
    }

    /**
     * 重写preHandle方法，在请求发生之前执行
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        request.setAttribute("title", HOME_TITLE);
        request.setAttribute("ownerTypeMap", getOwnerTypeMap());
        request.setAttribute("privilegeMap", getPrivilegeMap());

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(SESSION_USER) != null) {
            ActiveUser activeUser = (ActiveUser) session.getAttribute(SESSION_USER);
            request.setAttribute("groupMap", getUserGroupMap(activeUser));
            request.setAttribute("username", activeUser.getUsername());
        }
        return super.preHandle(request, response, handler);
    }

    /**
     * 重写postHandle方法，在请求完成之后执行
     *
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler,
                           ModelAndView modelAndView) throws Exception {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(SESSION_USER) != null &&
                modelAndView != null) {
            ActiveUser activeUser = (ActiveUser) session.getAttribute(SESSION_USER);
            ModelMap model = modelAndView.getModelMap();

            // 目录按授权信息进行显示
            if (model.containsAttribute(FOLDERS_ATTRIBUTE)) {
                List<WebFolder> folders = (List<WebFolder>) model.get(FOLDERS_ATTRIBUTE);
                model.remove(FOLDERS_ATTRIBUTE);
                model.addAttribute(FOLDERS_ATTRIBUTE,
                        folders.stream().filter(webFolder -> checkAuthentication(webFolder.getOpenAccess(),
                                webFolder.getAuthorizations(),
                                activeUser, READ_PRIVILEGE))
                                .map(webFolder -> {
                                    webFolder.setEditAuthorization(checkAuthentication(webFolder.getOpenAccess(),
                                            webFolder.getAuthorizations(),
                                            activeUser, WRITE_PRIVILEGE));
                                    webFolder.setDeleteAuthorization(checkAuthentication(webFolder.getOpenAccess(),
                                            webFolder.getAuthorizations(),
                                            activeUser, DELETE_PRIVILEGE));
                                    return webFolder;
                                })
                                .collect(Collectors.toList()));
            }

            // 文件按授权信息进行显示
            if (model.containsAttribute(FILES_ATTRIBUTE)) {
                List<WebFile> files = (List<WebFile>) model.get(FILES_ATTRIBUTE);
                model.remove(FILES_ATTRIBUTE);
                model.addAttribute(FILES_ATTRIBUTE,
                        files.stream().filter(file -> checkAuthentication(file.getOpenAccess(),
                                file.getAuthorizations(),
                                activeUser, READ_PRIVILEGE))
                                .map(file -> {
                                    file.setEditAuthorization(checkAuthentication(file.getOpenAccess(),
                                            file.getAuthorizations(),
                                            activeUser, WRITE_PRIVILEGE));
                                    file.setDeleteAuthorization(checkAuthentication(file.getOpenAccess(),
                                            file.getAuthorizations(),
                                            activeUser, DELETE_PRIVILEGE));
                                    return file;
                                })
                                .collect(Collectors.toList()));
            }
        }
    }
}

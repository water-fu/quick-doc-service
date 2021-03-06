package cn.mxleader.quickdoc.security.provider;

import cn.mxleader.quickdoc.security.exp.UserLogonException;
import cn.mxleader.quickdoc.entities.SysUser;
import cn.mxleader.quickdoc.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class WebAuthenticationProvider
        implements AuthenticationProvider {

    @Autowired
    private UserService userService;

    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {

        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        if (userService.validateUser(username, password)) {
            SysUser sysUser = userService.get(username);
            return new UsernamePasswordAuthenticationToken(sysUser.getUsername(),
                    sysUser.getPassword(),
                    sysUser.getAuthorities().stream()
                            .map(authority -> new WebAuthority(authority.name()))
                            .collect(Collectors.toList()));
        } else {
            throw new UserLogonException("登录错误，用户名或密码有误,请检查后重新输入！");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(
                UsernamePasswordAuthenticationToken.class);
    }
}

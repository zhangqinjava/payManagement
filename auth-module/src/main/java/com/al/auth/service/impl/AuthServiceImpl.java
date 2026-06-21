package com.al.auth.service.impl;

import com.al.auth.bean.dto.LoginDto;
import com.al.auth.bean.dto.RefreshTokenDto;
import com.al.auth.bean.vo.LoginResultVo;
import com.al.auth.bean.vo.SysLoginLogVo;
import com.al.auth.bean.vo.SysRefreshTokenVo;
import com.al.auth.bean.vo.SysUserVo;
import com.al.auth.bean.vo.UserProfileVo;
import com.al.auth.config.JwtProperties;
import com.al.auth.config.LoginProperties;
import com.al.auth.mapper.SysLoginLogMapper;
import com.al.auth.mapper.SysRefreshTokenMapper;
import com.al.auth.mapper.SysUserMapper;
import com.al.auth.security.JwtTokenProvider;
import com.al.auth.security.LoginUser;
import com.al.auth.security.LoginUserDetailsService;
import com.al.auth.security.SecurityUtils;
import com.al.auth.service.AuthService;
import com.al.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final int STATUS_ENABLED = 1;

    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private SysRefreshTokenMapper refreshTokenMapper;
    @Autowired
    private SysLoginLogMapper loginLogMapper;
    @Autowired
    private LoginUserDetailsService loginUserDetailsService;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private LoginProperties loginProperties;
    @Autowired
    private JwtProperties jwtProperties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResultVo login(LoginDto dto, String clientIp) {
        SysUserVo user = userMapper.selectOne(Wrappers.lambdaQuery(SysUserVo.class)
                .eq(SysUserVo::getUsername, dto.getUsername()));
        if (user == null) {
            saveLoginLog(null, dto.getUsername(), clientIp, 0, "用户不存在");
            throw new BusinessException("用户名或密码错误");
        }
        checkAccountAvailable(user);
        checkLoginTimeWindow(user);

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            handleLoginFail(user);
            saveLoginLog(user.getId(), user.getUsername(), clientIp, 0, "密码错误");
            throw new BusinessException("用户名或密码错误");
        }

        resetLoginFail(user);
        user.setLastLoginTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        saveLoginLog(user.getId(), user.getUsername(), clientIp, 1, "登录成功");

        LoginUser loginUser = loginUserDetailsService.buildLoginUser(user);
        JwtTokenProvider.TokenPair tokenPair = jwtTokenProvider.createTokenPair(loginUser);
        saveRefreshToken(user.getId(), tokenPair);

        return LoginResultVo.builder()
                .accessToken(tokenPair.getAccessToken())
                .refreshToken(tokenPair.getRefreshToken())
                .accessExpireAt(tokenPair.getAccessExpireAt())
                .refreshExpireAt(tokenPair.getRefreshExpireAt())
                .user(toProfile(loginUser))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            return;
        }
        try {
            io.jsonwebtoken.Claims claims = jwtTokenProvider.parseToken(refreshToken);
            if (!JwtTokenProvider.TYPE_REFRESH.equals(jwtTokenProvider.getTokenType(claims))) {
                return;
            }
            String tokenId = jwtTokenProvider.getTokenId(claims);
            SysRefreshTokenVo stored = refreshTokenMapper.selectOne(Wrappers.lambdaQuery(SysRefreshTokenVo.class)
                    .eq(SysRefreshTokenVo::getTokenId, tokenId));
            if (stored != null) {
                stored.setRevoked(1);
                refreshTokenMapper.updateById(stored);
            }
        } catch (Exception e) {
            log.warn("logout ignore invalid refresh token");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResultVo refresh(RefreshTokenDto dto) {
        io.jsonwebtoken.Claims claims = jwtTokenProvider.parseToken(dto.getRefreshToken());
        if (!JwtTokenProvider.TYPE_REFRESH.equals(jwtTokenProvider.getTokenType(claims))) {
            throw new BusinessException("刷新令牌类型不正确");
        }
        String tokenId = jwtTokenProvider.getTokenId(claims);
        SysRefreshTokenVo stored = refreshTokenMapper.selectOne(Wrappers.lambdaQuery(SysRefreshTokenVo.class)
                .eq(SysRefreshTokenVo::getTokenId, tokenId)
                .eq(SysRefreshTokenVo::getRevoked, 0));
        if (stored == null || stored.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("刷新令牌无效或已过期");
        }

        SysUserVo user = userMapper.selectById(stored.getUserId());
        if (user == null || user.getStatus() == null || user.getStatus() != STATUS_ENABLED) {
            throw new BusinessException("用户不可用");
        }
        checkLoginTimeWindow(user);

        stored.setRevoked(1);
        refreshTokenMapper.updateById(stored);

        LoginUser loginUser = loginUserDetailsService.buildLoginUser(user);
        JwtTokenProvider.TokenPair tokenPair = jwtTokenProvider.createTokenPair(loginUser);
        saveRefreshToken(user.getId(), tokenPair);

        return LoginResultVo.builder()
                .accessToken(tokenPair.getAccessToken())
                .refreshToken(tokenPair.getRefreshToken())
                .accessExpireAt(tokenPair.getAccessExpireAt())
                .refreshExpireAt(tokenPair.getRefreshExpireAt())
                .user(toProfile(loginUser))
                .build();
    }

    @Override
    public UserProfileVo currentUser() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null) {
            throw new BusinessException("未登录或登录已过期");
        }
        return toProfile(loginUser);
    }

    private void checkAccountAvailable(SysUserVo user) {
        if (user.getStatus() == null || user.getStatus() != STATUS_ENABLED) {
            throw new BusinessException("账户已停用");
        }
        if (user.getLockUntil() != null && user.getLockUntil().isAfter(LocalDateTime.now())) {
            throw new BusinessException("账户已锁定，请于 " + user.getLockUntil() + " 后再试");
        }
    }

    private void checkLoginTimeWindow(SysUserVo user) {
        LocalTime start = user.getAllowedStartTime() != null
                ? user.getAllowedStartTime()
                : LocalTime.parse(loginProperties.getDefaultAllowedStart(), TIME_FMT);
        LocalTime end = user.getAllowedEndTime() != null
                ? user.getAllowedEndTime()
                : LocalTime.parse(loginProperties.getDefaultAllowedEnd(), TIME_FMT);
        LocalTime now = LocalTime.now();
        if (start.isBefore(end)) {
            if (now.isBefore(start) || now.isAfter(end)) {
                throw new BusinessException("当前不在允许登录时段(" + start + " - " + end + ")");
            }
        } else if (now.isAfter(end) && now.isBefore(start)) {
            throw new BusinessException("当前不在允许登录时段(" + start + " - " + end + ")");
        }
    }

    private void handleLoginFail(SysUserVo user) {
        int failCount = user.getLoginFailCount() == null ? 0 : user.getLoginFailCount();
        failCount++;
        SysUserVo update = new SysUserVo();
        update.setId(user.getId());
        update.setLoginFailCount(failCount);
        update.setUpdateTime(LocalDateTime.now());
        if (failCount >= loginProperties.getMaxFailCount()) {
            update.setLockUntil(LocalDateTime.now().plusMinutes(loginProperties.getLockMinutes()));
            update.setLoginFailCount(0);
        }
        userMapper.updateById(update);
    }

    private void resetLoginFail(SysUserVo user) {
        SysUserVo update = new SysUserVo();
        update.setId(user.getId());
        update.setLoginFailCount(0);
        update.setLockUntil(null);
        userMapper.updateById(update);
    }

    private void saveRefreshToken(Long userId, JwtTokenProvider.TokenPair tokenPair) {
        SysRefreshTokenVo token = new SysRefreshTokenVo();
        token.setUserId(userId);
        token.setTokenId(tokenPair.getRefreshTokenId());
        token.setExpireTime(LocalDateTime.now().plusHours(jwtProperties.getRefreshTokenHours()));
        token.setRevoked(0);
        token.setCreateTime(LocalDateTime.now());
        refreshTokenMapper.insert(token);
    }

    private void saveLoginLog(Long userId, String username, String ip, int status, String message) {
        SysLoginLogVo logVo = new SysLoginLogVo();
        logVo.setUserId(userId);
        logVo.setUsername(username);
        logVo.setLoginIp(ip);
        logVo.setStatus(status);
        logVo.setMessage(message);
        logVo.setLoginTime(LocalDateTime.now());
        loginLogMapper.insert(logVo);
    }

    private UserProfileVo toProfile(LoginUser loginUser) {
        return UserProfileVo.builder()
                .userId(loginUser.getUserId())
                .username(loginUser.getUsername())
                .displayName(loginUser.getDisplayName())
                .roles(loginUser.getRoles())
                .permissions(loginUser.getPermissions())
                .build();
    }
}

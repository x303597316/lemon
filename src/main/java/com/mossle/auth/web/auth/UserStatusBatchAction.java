package com.mossle.auth.web.auth;

import java.util.ArrayList;
import java.util.List;

import com.mossle.api.ScopeConnector;

import com.mossle.auth.component.UserStatusChecker;
import com.mossle.auth.domain.Role;
import com.mossle.auth.domain.UserStatus;
import com.mossle.auth.manager.RoleManager;
import com.mossle.auth.manager.UserStatusManager;
import com.mossle.auth.service.AuthService;
import com.mossle.auth.support.CheckUserStatusException;

import com.mossle.core.scope.ScopeHolder;
import com.mossle.core.struts2.BaseAction;

import com.opensymphony.xwork2.ModelDriven;
import com.opensymphony.xwork2.Preparable;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceAccessor;

@Results({ @Result(name = UserStatusBatchAction.RELOAD, location = "user-status.do?operationMode=RETRIEVE", type = "redirect") })
public class UserStatusBatchAction extends BaseAction implements
        ModelDriven<UserStatus>, Preparable {
    private static Logger logger = LoggerFactory
            .getLogger(UserStatusBatchAction.class);
    public static final String RELOAD = "reload";
    private UserStatusManager userStatusManager;
    private RoleManager roleManager;
    private MessageSourceAccessor messages;
    private String userText;
    private List<Long> userIds = new ArrayList<Long>();
    private List<Long> roleIds = new ArrayList<Long>();
    private List<UserStatus> userStatuses = new ArrayList<UserStatus>();
    private List<Role> roles;
    private UserStatusChecker userStatusChecker;
    private ScopeConnector scopeConnector;
    private AuthService authService;

    public String execute() {
        return SUCCESS;
    }

    public String input() {
        Long localId = scopeConnector.findLocalId(ScopeHolder.getGlobalCode(),
                ScopeHolder.getLocalCode());

        if (userText != null) {
            for (String str : userText.split("\n")) {
                str = str.trim();

                if (str.length() == 0) {
                    continue;
                }

                UserStatus userStatus = userStatusManager.findUnique(
                        "from UserStatus where username=? and localId=?", str,
                        localId);

                if (userStatus == null) {
                    addActionMessage(str + " is not exists.");
                } else {
                    try {
                        userStatusChecker.check(userStatus);
                        userStatuses.add(userStatus);
                    } catch (CheckUserStatusException ex) {
                        addActionMessage(ex.getMessage());
                    }
                }
            }
        }

        roles = roleManager.find("from Role where localId=?", localId);

        return INPUT;
    }

    public String save() {
        Long globalId = scopeConnector
                .findGlobalId(ScopeHolder.getGlobalCode());
        Long localId = scopeConnector.findLocalId(ScopeHolder.getGlobalCode(),
                ScopeHolder.getLocalCode());
        logger.debug("userIds: {}, roleIds: {}", userIds, roleIds);

        for (Long userId : userIds) {
            authService.configUserRole(userId, roleIds, globalId, localId,
                    false);
        }

        return RELOAD;
    }

    // ~ ======================================================================
    public void prepare() {
    }

    public UserStatus getModel() {
        return null;
    }

    public void setUserStatusManager(UserStatusManager userStatusManager) {
        this.userStatusManager = userStatusManager;
    }

    public void setRoleManager(RoleManager roleManager) {
        this.roleManager = roleManager;
    }

    public void setUserStatusChecker(UserStatusChecker userStatusChecker) {
        this.userStatusChecker = userStatusChecker;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messages = new MessageSourceAccessor(messageSource);
    }

    // ~ ======================================================================
    public String getUserText() {
        return userText;
    }

    public void setUserText(String userText) {
        this.userText = userText;
    }

    public List<Long> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<Long> userIds) {
        this.userIds = userIds;
    }

    public List<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds;
    }

    public List<UserStatus> getUserStatuses() {
        return userStatuses;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setScopeConnector(ScopeConnector scopeConnector) {
        this.scopeConnector = scopeConnector;
    }

    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }
}

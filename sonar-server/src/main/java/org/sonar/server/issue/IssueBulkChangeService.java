/*
 * SonarQube, open source software quality management tool.
 * Copyright (C) 2008-2013 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * SonarQube is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * SonarQube is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.sonar.server.issue;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.issue.Issue;
import org.sonar.api.issue.IssueQuery;
import org.sonar.api.issue.IssueQueryResult;
import org.sonar.api.issue.internal.DefaultIssue;
import org.sonar.api.issue.internal.IssueChangeContext;
import org.sonar.api.web.UserRole;
import org.sonar.core.issue.IssueNotifications;
import org.sonar.core.issue.IssueUpdater;
import org.sonar.core.issue.db.IssueStorage;
import org.sonar.server.user.UserSession;

import javax.annotation.CheckForNull;
import java.util.Date;
import java.util.List;

public class IssueBulkChangeService {

  private static final Logger LOG = LoggerFactory.getLogger(IssueBulkChangeService.class);

  private final DefaultIssueFinder issueFinder;
  private final IssueUpdater issueUpdater;
  private final IssueStorage issueStorage;
  private final IssueNotifications issueNotifications;
  private final List<Action> actions;

  public IssueBulkChangeService(DefaultIssueFinder issueFinder, IssueUpdater issueUpdater, IssueStorage issueStorage, IssueNotifications issueNotifications, List<Action> actions) {
    this.issueFinder = issueFinder;
    this.issueUpdater = issueUpdater;
    this.issueStorage = issueStorage;
    this.issueNotifications = issueNotifications;
    this.actions = actions;
  }

  public IssueBulkChangeResult execute(IssueBulkChangeQuery issueBulkChangeQuery, UserSession userSession) {
    LOG.debug("BulkChangeQuery : {}", issueBulkChangeQuery);
    verifyLoggedIn(userSession);

    IssueBulkChangeResult result = new IssueBulkChangeResult();
    IssueQueryResult issueQueryResult = issueFinder.find(IssueQuery.builder().issueKeys(issueBulkChangeQuery.issues()).requiredRole(UserRole.USER).build());
    List<Issue> issues = issueQueryResult.issues();
    for (String actionName : issueBulkChangeQuery.actions()) {
      Action action = getAction(actionName);
      if (action == null) {
        throw new IllegalArgumentException("The action : '"+ actionName + "' is unknown");
      }
      action.verify(issueBulkChangeQuery.properties(actionName), issues, userSession);
    }

    IssueChangeContext issueChangeContext = IssueChangeContext.createUser(new Date(), userSession.login());
    for (Issue issue : issues) {
      for (String actionName : issueBulkChangeQuery.actions()) {
        try {
          Action action = getAction(actionName);
          ActionContext actionContext = new ActionContext(issue, issueChangeContext);
          if (action.supports(issue) && action.execute(issueBulkChangeQuery.properties(actionName), actionContext)) {
            issueStorage.save((DefaultIssue) issue);
            issueNotifications.sendChanges((DefaultIssue) issue, issueChangeContext, issueQueryResult);
            result.addIssueChanged(issue);
          } else {
            result.addIssueNotChanged(issue);
          }
        } catch (Exception e) {
          result.addIssueNotChanged(issue);
          LOG.info("An error occur when trying to apply the action : "+ actionName + " on issue : "+ issue.key() + ". This issue has been ignored.", e);
        }
      }
    }
    return result;
  }

  @CheckForNull
  private Action getAction(final String actionKey) {
    return Iterables.find(actions, new Predicate<Action>() {
      @Override
      public boolean apply(Action action) {
        return action.key().equals(actionKey);
      }
    }, null);
  }

  private void verifyLoggedIn(UserSession userSession) {
    if (!userSession.isLoggedIn()) {
      // must be logged
      throw new IllegalStateException("User is not logged in");
    }
  }

  static class ActionContext implements Action.Context {
    private final Issue issue;
    private final IssueChangeContext changeContext;

    ActionContext(Issue issue, IssueChangeContext changeContext) {
      this.issue = issue;
      this.changeContext = changeContext;
    }

    @Override
    public Issue issue() {
      return issue;
    }

    @Override
    public IssueChangeContext issueChangeContext() {
      return changeContext;
    }
  }
}

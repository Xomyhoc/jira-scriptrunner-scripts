//ScriptRunner listener
//Setting the current year's label without sending a notification
//Performed on behalf of the current user
//On "Issue Created" event
import com.atlassian.jira.issue.MutableIssue
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.user.util.UserManager
import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.issue.IssueManager
import com.atlassian.jira.issue.label.LabelManager
import java.sql.Timestamp
//**************************************************************************************
def customFieldManager = ComponentAccessor.customFieldManager
def userManager = ComponentAccessor.getUserManager()
def issueManager = ComponentAccessor.getIssueManager()
def labelManager = ComponentAccessor.getComponent(LabelManager)
def issue = event.issue as MutableIssue
//**************************************************************************************
def user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser() as ApplicationUser
def now = new Timestamp(new Date().getTime()).toLocalDateTime().getYear() as String
def sendEmailOnAdd = false
labelManager.addLabel(user, issue.id, now, sendEmailOnAdd)
issueManager.updateIssue(user, issue, EventDispatchOption.DO_NOT_DISPATCH, false)
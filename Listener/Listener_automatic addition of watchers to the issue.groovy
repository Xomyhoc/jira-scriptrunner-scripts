//Automatic determination of which of the users in the "Assistants" project role can be assigned as an watchers to a issue with a specific issue security level

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.security.roles.ProjectRoleManager
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.issue.security.IssueSecurityLevelManager
//**************************************************************************************
import org.apache.log4j.Logger
import org.apache.log4j.Level
log = Logger.getLogger("Listener -> Automatic set whatchers from issue security role")
log.setLevel(Level.INFO)
//**************************************************************************************
def userManager = ComponentAccessor.getUserManager()
def securityLevelManager = ComponentAccessor.getIssueSecurityLevelManager()
def projectRoleManager =ComponentAccessor.getComponent(ProjectRoleManager)
def watcherManager = ComponentAccessor.getWatcherManager()
def issueManager = ComponentAccessor.getIssueManager()
//**************************************************************************************
//Get current issue
def issue = event.issue
//Get project role
def developersProjectRole = projectRoleManager.getProjectRole("Аssistants")
//Get poject role users
def actorsInRole = projectRoleManager.getProjectRoleActors(developersProjectRole, issue.getProjectObject()).getUsers()
//Get security level name
def securityName = securityLevelManager.getIssueSecurityName(issue.getSecurityLevelId()) as String
//**************************************************************************************
log.info("Сorrect assistants:")
for(user in actorsInRole){
    def userSec = securityLevelManager.getUsersSecurityLevels(issue, user)*.name as String
    if(userSec.contains(securityName))
    {
        log.info("$user - Correct!")
        def set_user = userManager.getUserByName(user.getName())
        watcherManager.startWatching(set_user, issue)
    }
    else
    {
        log.info("$user - Uncorrect!")
    }
}
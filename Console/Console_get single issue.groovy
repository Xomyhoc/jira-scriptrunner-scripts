//ScriptRunner Console
import com.atlassian.jira.component.ComponentAccessor
import com.onresolve.scriptrunner.parameters.annotation.*
//**************************************************************************************    
import org.apache.log4j.Logger
import org.apache.log4j.Level
log = Logger.getLogger("Console")
log.setLevel(Level.INFO)
//**************************************************************************************
def issueManager = ComponentAccessor.getIssueManager()
//**************************************************************************************
@ShortTextInput(label = "Issue", description = "Enter a issue key")
String inputIssue
//**************************************************************************************
def issue = issueManager.getIssueObject(inputIssue)
log.info(issue.summary)
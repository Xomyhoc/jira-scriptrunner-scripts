//ScriptRunner listener
//Synchronizes the value of a custom field with the components of a specific project: 'Can be used to automatically install components when moving tasks from another project'
//Performed on behalf of the admin user
//On "ProjectComponentCreatedEvent", "ProjectComponentUpdatedEvent","ProjectComponentDeletedEvent" events
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.project.Project
import com.onresolve.scriptrunner.parameters.annotation.ProjectPicker
import com.atlassian.jira.bc.project.component.ProjectComponent
import com.atlassian.jira.bc.project.component.ProjectComponentManager
import com.atlassian.jira.issue.customfields.manager.OptionsManager
import com.atlassian.jira.issue.fields.config.manager.FieldConfigManager
import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.event.issue.AbstractIssueEventListener
import com.atlassian.jira.event.issue.IssueEvent
import com.atlassian.jira.event.type.EventType
import com.atlassian.jira.event.type.EventTypeManager
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.event.bc.project.component.ProjectComponentUpdatedEvent
import com.atlassian.jira.event.bc.project.component.ProjectComponentCreatedEvent
import com.atlassian.jira.event.bc.project.component.ProjectComponentDeletedEvent
//**************************************************************************************    
def userManager = ComponentAccessor.getUserManager()
def customFieldManager = ComponentAccessor.getCustomFieldManager()
def authContext = ComponentAccessor.getJiraAuthenticationContext()
def optionsManager = ComponentAccessor.getOptionsManager()
def projectComponentManager = ComponentAccessor.getComponent(ProjectComponentManager)
def eventTypeManager = ComponentAccessor.getEventTypeManager()
def fieldConfigManager = ComponentAccessor.getComponent(FieldConfigManager)
//**************************************************************************************
import org.apache.log4j.Logger
import org.apache.log4j.Level
log = Logger.getLogger("Component -> Custom select list field sync")
log.setLevel(Level.INFO)
//**************************************************************************************
//Set AuthenticationContext as admin
def admin = userManager.getUserByName("Administrator")
log.info("Login as: $admin.displayName")
authContext.setLoggedInUser(admin)
//**************************************************************************************
//Get field options
def field = customFieldManager.getCustomFieldObject("customfield_######") //Enter custom field id
def fieldConfig = field.getRelevantConfig(issue)
def fieldOptions = optionsManager.getOptions(fieldConfig)
int nextSequence = fieldOptions.getRootOptions().size() + 1
def OptionsList = fieldOptions as String
//**************************************************************************************
//Get project component
def project = event.projectComponent.getProjectId()
def allComponents = projectComponentManager.findAllActiveForProject(project).toList()
assert allComponents : "Could not find project with id $project"
//**************************************************************************************
if(event instanceof ProjectComponentCreatedEvent)
{
    log.info("Project Component Created Event")
    def newComponent = event.getProjectComponent()
    def optionCreate = optionsManager.findByOptionValue(newComponent.name).getAt(0)
    if(optionCreate == null)
    {
        optionsManager.createOption(fieldConfig, null, (long) nextSequence, newComponent.name)
        log.info("${newComponent.getName()}  — Added to the customfield")
    }
    else{}
}
else if(event instanceof ProjectComponentUpdatedEvent)
{
    log.info("Project Component Updated Event")
    def event = event as ProjectComponentUpdatedEvent
    def updatedComponent = event.getOldProjectComponent()
    def newComponent = event.getProjectComponent()
    def optionUpdate = optionsManager.findByOptionValue(updatedComponent.name).getAt(0)
    optionsManager.setValue(optionUpdate, newComponent.getName())
    log.info("Changed component: ${updatedComponent.getName()} -> ${newComponent.getName()} — The value in the field has been changed to ${newComponent.getName()}")
}
else if(event instanceof ProjectComponentDeletedEvent)
{
    
    log.info("Project Component Deleted Event")
    def event = event as ProjectComponentDeletedEvent
    def deletedComponent = event.getProjectComponent()
    log.info(deletedComponent)
    def optionDeleted = optionsManager.findByOptionValue(deletedComponent.name).getAt(0)
    optionsManager.deleteOptionAndChildren(optionDeleted)
    log.info("Component delited: ${deletedComponent.getName()} — Value ${optionDeleted.getValue()} delited from field")
}
else{}
//**************************************************************************************
//Clear AuthenticationContext as admin
log.info("logout: $admin.displayName")
authContext.clearLoggedInUser()

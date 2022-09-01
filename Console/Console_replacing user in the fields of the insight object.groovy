//ScriptRunner Console
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.issue.fields.CustomField
import com.onresolve.scriptrunner.parameters.annotation.*
import com.onresolve.scriptrunner.parameters.annotation.meta.*
import com.onresolve.scriptrunner.runner.customisers.WithPlugin
import com.riadalabs.jira.plugins.insight.services.model.MutableObjectAttributeBean
import com.riadalabs.jira.plugins.insight.services.model.MutableObjectBean
@WithPlugin("com.riadalabs.jira.plugins.insight")   
import com.riadalabs.jira.plugins.insight.services.model.ObjectBean
import com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectFacade
import com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectTypeFacade
import com.riadalabs.jira.plugins.insight.channel.external.api.facade.IQLFacade
import com.riadalabs.jira.plugins.insight.services.model.factory.ObjectAttributeBeanFactory
import com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectTypeAttributeFacade
import com.riadalabs.jira.plugins.insight.services.model.ObjectTypeAttributeBean
import groovy.time.TimeCategory 
import groovy.time.TimeDuration
//**************************************************************************************
import org.apache.log4j.Logger
import org.apache.log4j.Level
log = Logger.getLogger("Console")
log.setLevel(Level.INFO)
//**************************************************************************************
def authContext = ComponentAccessor.getJiraAuthenticationContext()
def userManager = ComponentAccessor.getUserManager()
def customFieldManager = ComponentAccessor.customFieldManager
//**************************************************************************************
//Set AuthenticationContext as admin
def admin = userManager.getUserByName("Administrator")
log.info("Login as: $admin.displayName")
authContext.setLoggedInUser(admin)
//**************************************************************************************
//Scriptrunner dynamic field
@ShortTextInput(label = "Issue", description = "Enter a issue key")
String inputIssue
def issue = issueManager.getIssueObject(inputIssue)
@UserPicker(label = "Replaceable User", description = "Select a user")
ApplicationUser replaceableUser
@UserPicker(label = "Replacement User", description = "Select a user")
ApplicationUser replacementUser
@Select(
    label = "Destination",
    description = "Select the update option",
    options = [
        @Option(label = "All objects in the selected scheme", value = "1"),
        @Option(label = "Single object", value = "2"),
    ]
)
String destination
@Select(
    label = "Insight Schema Id",
    description = "Select schema",
    options = [
        @Option(label = "schemaName", value = "1"),
        @Option(label = "None", value = "none"),
    ]
)
String value
def schemaId = value.toInteger()
@ShortTextInput(label = "Insight object", description = "Enter insight object key")
String selectedOject
//**************************************************************************************
def objectFacade = ComponentAccessor.getOSGiComponentInstanceOfType(ObjectFacade.class)
def objectAttributeBeanFactory = ComponentAccessor.getOSGiComponentInstanceOfType(ObjectAttributeBeanFactory.class)
def objectTypeAttributeFacade = ComponentAccessor.getOSGiComponentInstanceOfType(ObjectTypeAttributeFacade.class)
//**************************************************************************************
//All objects in the selected scheme - replacement in about ~7000 objects ~ 2m 16c
//**************************************************************************************
if(destination == "1")
{
Class iqlFacadeClass = ComponentAccessor.getPluginAccessor().getClassLoader().findClass("com.riadalabs.jira.plugins.insight.channel.external.api.facade.IQLFacade") 
def iqlFacade = ComponentAccessor.getOSGiComponentInstanceOfType(iqlFacadeClass) as IQLFacade
//Getting all the objects in the schema
def allObjects = iqlFacade.findObjectsByIQLAndSchema(schemaId, "" ) as List
//For each object
for (searchableObject in allObjects)
{
    //Getting the key of a curent object
    def searchableObjectKey = (searchableObject as MutableObjectBean).objectKey
    def selectedOjectBean = objectFacade.loadObjectBean(searchableObjectKey) as ObjectBean
    
    //Getting the attributes of a curent object
    def objectAttribute = objectFacade.loadObjectBean(searchableObjectKey).getObjectAttributeBeans() as List
    //For each attribute
    for(singleAttribute in objectAttribute)
    {
        //We find all attributes containing the selected user and not null
        def currentAttribute = singleAttribute as MutableObjectAttributeBean
        def currentSingleAttributeValue = currentAttribute.getObjectAttributeValueBeans().value as String
        if(currentSingleAttributeValue.contains("$dismissedEmployee.username"))
        {
            //Creating a collection with the value we need
            def user_list = new ArrayList<String>()
            user_list.add(replacementEmployeeKey)
            //For fields with more than 1 value, we take all except the one being replaced into the collection
            if(currentAttribute.getObjectAttributeValueBeans().value.size()>1)
            {
                for(c in currentAttribute.getObjectAttributeValueBeans().value)
                {
                    if(c != "$dismissedEmployee.username")
                    {
                       user_list.add(c as String) 
                    }
                } 
            }
            //Setting the collection to the attribute we need
            def currentSingleAttributeId = currentAttribute.getObjectTypeAttributeId()
            def changed_objectTypeAttributeBean = objectTypeAttributeFacade.loadObjectTypeAttributeBean(currentSingleAttributeId).createMutable()
            def newObjectAttributeBean = objectAttributeBeanFactory.createUserTypeAttributeByKeyOrUsername(changed_objectTypeAttributeBean, user_list)
            def changed_objectAttributeBean = objectFacade.loadObjectAttributeBean(selectedOjectBean.getId(), changed_objectTypeAttributeBean.getId())
            newObjectAttributeBean.setObjectId(changed_objectAttributeBean.getObjectId())
            try
            {
                changed_objectTypeAttributeBean = objectFacade.storeObjectAttributeBean(newObjectAttributeBean)
            }
            catch (Exception ex) 
            {
                log.warn("Could not update object attribute due to validation exception:" + ex.getMessage())
            }
        }
    }
}
}
else if(destination == "2")
{
//**************************************************************************************
//Single selected object - replacement in four fields < 1.0c
//**************************************************************************************
def objectAttribute = objectFacade.loadObjectBean(selectedOject).getObjectAttributeBeans() as List
def selectedOjectBean = objectFacade.loadObjectBean(selectedOject) as ObjectBean
//Search for the necessary attributes
for(singleAttribute in objectAttribute)
    {
    def currentAttribute = singleAttribute as MutableObjectAttributeBean
    def currentSingleAttributeValue = currentAttribute.getObjectAttributeValueBeans().value as String
    if(currentSingleAttributeValue.contains("$dismissedEmployee.username"))
        {
            //Creating a collection with the value we need
            def user_list = new ArrayList<String>()
            user_list.add(replacementEmployeeKey)
            //For fields with more than 1 value, we take all except the one being replaced into the collection
            if(currentAttribute.getObjectAttributeValueBeans().value.size()>1)
            {
                for(c in currentAttribute.getObjectAttributeValueBeans().value)
                {
                    if(c != "$dismissedEmployee.username")
                    {
                       user_list.add(c as String) 
                    }
                } 
            }
            //Setting the collection to the attribute we need
            def currentSingleAttributeId = currentAttribute.getObjectTypeAttributeId()
            def changed_objectTypeAttributeBean = objectTypeAttributeFacade.loadObjectTypeAttributeBean(currentSingleAttributeId).createMutable()
            def newObjectAttributeBean = objectAttributeBeanFactory.createUserTypeAttributeByKeyOrUsername(changed_objectTypeAttributeBean, user_list)
            def changed_objectAttributeBean = objectFacade.loadObjectAttributeBean(selectedOjectBean.getId(), changed_objectTypeAttributeBean.getId())
            newObjectAttributeBean.setObjectId(changed_objectAttributeBean.getObjectId())
            try
            {
                changed_objectTypeAttributeBean = objectFacade.storeObjectAttributeBean(newObjectAttributeBean)
            }
            catch (Exception ex)
            {
                log.warn("Could not update object attribute due to validation exception:" + ex.getMessage())
            }            
        }
    }
}
else
{}
//**************************************************************************************
//Clear AuthenticationContext as admin
log.info("logout: $admin.displayName")
authContext.clearLoggedInUser()
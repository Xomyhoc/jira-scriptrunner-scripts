//Ограничение вариантов выбора компонентов при содании задачи с определенными условиями

import com.atlassian.jira.bc.project.component.ProjectComponent
import com.onresolve.jira.groovy.user.FormField
import static com.atlassian.jira.issue.IssueFieldConstants.COMPONENTS
import com.atlassian.jira.component.ComponentAccessor
import groovy.transform.BaseScript
import com.onresolve.jira.groovy.user.FieldBehaviours
@BaseScript FieldBehaviours fieldBehaviours
//**************************************************************************************
def customFieldManager = ComponentAccessor.getCustomFieldManager()
def optionManager = ComponentAccessor.optionsManager
//**************************************************************************************
FormField component = getFieldById(COMPONENTS)

def componentCustomField = customFieldManager.getCustomFieldObject(component.fieldId)
def componentConfig = componentCustomField.getRelevantConfig(issueContext)
def componentOptions = optionManager.getOptions(componentConfig)
def listBNewOptions
//**************************************************************************************

List <ProjectComponent> components = component.getValue() as List

    component.setFormValue(["- 1", "None"])
        listBNewOptions = [
            "Component1",
            "Component2"
        ]

def listBNewOptionsMap = componentOptions?.findAll {
    it.value in listBNewOptions
   }?.collectEntries {
     [ (it.optionId.toString()) : it.value ]
   }
//**************************************************************************************
if(your condition)
{
    component.setFieldOptions(listBNewOptionsMap)
}
else{}
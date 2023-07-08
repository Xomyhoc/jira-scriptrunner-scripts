import com.atlassian.jira.bc.project.component.ProjectComponent
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.user.ApplicationUser
//**************************************************************************************    
def component = getFieldById(fieldChanged)
List <ProjectComponent> components = component.getValue() as List
def security = getFieldById("security").setReadOnly(true).setFormValue("")
//**************************************************************************************    
for (component in components)
{
    switch(component.getName()) {
        case "Component name 1":
            security.setFormValue(1)
        break
        case "Component name 2":
            security.setFormValue(2)
        break
        case "Component name 3":
            security.setFormValue(3)
        break
    }

}
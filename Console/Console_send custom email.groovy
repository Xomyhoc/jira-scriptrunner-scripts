//ScriptRunner Console
//Sending a custom email from the scriptrunner console
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.mail.Email
import com.atlassian.jira.mail.settings.MailSettings
import com.atlassian.mail.MailException
import com.atlassian.mail.server.SMTPMailServer
import com.atlassian.plugin.util.ContextClassLoaderSwitchingUtil
import com.onresolve.scriptrunner.parameters.annotation.*
//**************************************************************************************    
def customFieldManager = ComponentAccessor.customFieldManager
//**************************************************************************************
import org.apache.log4j.Logger
import org.apache.log4j.Level
log = Logger.getLogger("Console -> Send email from console")
log.setLevel(Level.INFO)
//**************************************************************************************
//Get issue
@ShortTextInput(label = "Issue", description = "Enter a issue key")
String inputIssue
def issue = issueManager.getIssueObject(inputIssue)
//Get user sender
@UserPicker(label = "Sender", description = "Select a user")
ApplicationUser sender
//Get user recipient
@UserPicker(label = "Recipient", description = "Select a user")
ApplicationUser recipient
//**************************************************************************************
//Send Email
//Enter mail subject
String subject = ""
//Enter mail body
String body = ""
def mailServer = ComponentAccessor.mailServerManager.defaultSMTPMailServer
    if (!mailServer) 
    {
        log.warn('Your mail server Object is Null, make sure to set the SMTP Mail Server Settings Correctly on your Server')
        return 'Failed to Send Mail. No SMTP Mail Server Defined'
    }
    def email = new Email(recipient.getEmailAddress())
    email.setMimeType('text/html')
    email.setSubject("$subject")
    email.setBody("$body")
    email.setFrom(sender.getEmailAddress())
        try 
        {
            // This is needed to avoid the exception about IMAPProvider
            ContextClassLoaderSwitchingUtil.runInContext(SMTPMailServer.classLoader) 
            {
                mailServer.send(email)
            }
            log.info('Email sent successfully')
            log.info(recipient.getEmailAddress())
            log.info(sender.getEmailAddress())
        } 
        catch (MailException e) 
        {
            log.error("Send mail failed with error: ${e.message}" )
        }
    else{}
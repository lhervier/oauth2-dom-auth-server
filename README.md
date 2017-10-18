OAUTH2 allows you to write *client applications* that are able to access a protected set of *resources* hosted on a *resource server*.
In real life, most of the *client applications* are web or mobile applications, and most *resources* are Rest APIs.
Have a look at https://tools.ietf.org/html/rfc6749 for more details.

The important thing is that, with OAUTH2, the *resource server* (the server that hosts the *resources*) will be able to CLEARLY identify the calling user. 

For this, the *client application* will get an *access token* from an *authorization server*, and will send this token 
with every requests made to the *resource server*. A common way of sending the *access token* is to add it to the HTTP header named "Authorization", with the "Bearer" prefix.

To validate the *access token*, the resource server will have to send it to a endpoint provided by the *authorization server*. [RFC 7662](https://tools.ietf.org/html/rfc7662) define such
an endpoint. [OpenId Connect](http://openid.net/specs/openid-connect-core-1_0.html#UserInfo) defines another one.

The goal of this project is to transform a standard IBM Domino v9.0.1 server into an OAUTH2 authorization server (also know as an OAUTH2 provider), with OpenID extensions. 
Your server will generate access tokens that client applications will be able to use to access protected resources. 
But note that nor the client applications, nor the protected resources have to be hosted on a Domino Server !

In the last chapter of this README, I will explain how to test your server with a sample notes database that contains an angular application. 
This application will get an access token from the authorization server, and use it to access the OpenId userInfo endpoint (hosted on the Domino authorization too).

# Deploy all the needed plugins

## Deploy the "Domino Spring" plugins

Download the latest release of the "Domino Spring" project from https://github.com/lhervier/dom-spring/releases. 
It is a simple zip file that contains osgi plugins.

### In your Domino Server

First, unzip the update site. Then, create an "update site" database :

- Name it the way you want. In this example, I will name it "SpringUpdateSite.nsf" and store it at the root of my Domino server.
- Use the "Eclipse Update Site" template (you will have to select a server, and click "show advanced templates" in the new database dialog box)
- Click on the "Import local update site" button
- Go find the "site.xml" that is present into your unzipped update site.
- It is recommended to disable the "Spring Sample Feature"
- Declare the name of the new database in your notes.ini, using the variable "OSGI_HTTP_DYNAMIC_BUNDLES". If it already exist, separate multiple values with a "," character.
- Restart the http task with a "restart task http" console command.

Once http has been restarted, you can check that the plugins have been loaded successfully by using the following console command :

	tell http osgi ss spring
	
If it answers something, you're good to go.

### In your Domino Designer

Do this only if you plan to play with the Domino OAUTH2 Authorization Server code... Otherwise, skip this chapter.

- Check that your Designer allows you to add plugins :
	- Go to File / Preferences
	- Go to the "Domino Designer" section
	- Check that "Enable Eclipse Plugin install" is checked.
- Go to File / Application / Install
- Choose "Search for new feature to install"
- Add a "Zip/Jar Location", and go select the update site zip file
- Click "Finish" and accept the next steps.

Once Domino Designer has restarted, you can check that the plugins have been installed by going to Help / About Domino Designer. Click the "Plugin details" button, and
check that you can see the "com.github.lhervier.domino.spring.*" plugins (sorting by plugin id make it easier to find).

## Get this project's update site

### Download from github

This is the simplest solution. If you don't plan to play with the code, you can just download the update site from the github release page. 

### Compile it yourself

If you want to generate the update site yourself from the source code, follow the next steps.

First, import the code into Designer

- Clone or download the source code from github into a local folder.
- Open the "package explorer" view.
- Use the "File / Import" menu.
- In the "General" section, select "Existing projets into workspace"
- Click "Browse" and select the folder that contains this project's sources.
- Select all projects and click "Import"

Then, compile the code :

- Open the file "site.xml" in the "com.github.lhervier.domino.oauth.update" project.
- Click the "Build All" button.

The result is in the "com.github.lhervier.domino.oauth.update" folder. This is a "standard" update site composed of :

- the "site.xml" file
- the "plugins" folder
- and the "features" folder

Then, package the update site

Zip the site.xml, plugins and features folders, and you are ready !

## Deploy the "Domino OAUTH2 Authorization Server" plugins

### On an production environment

Get the update site (the zip file) from the github releases page, or generate it yourself by using the method described above, and unzip it somewhere.

Then, create another update site database using the same technique that we used to install the "Domino Plugin" plugins. But we have the following changes :

- Name the database "Oauth2UpdateSite.nsf"
- Add the name of the database to your "OSGI_HTTP_DYNAMIC_BUNDLES" notes.ini variable. Use a simple "," character to seperate the values.
- Restart the http task with a "restart task http" console command.

Note that it is not recommanded to import multiple update sites into one same database, because we don't have an option to remove only a set of features. All we can do is remove all the database content.

### On a development environment

### Install the "IBM Domino Debug Plugin"

- Download the zip file from https://www.openntf.org/main.nsf/project.xsp?r=project/IBM%20Lotus%20Domino%20Debug%20Plugin
- In your IBM Domino Designer :
	- Go to File/Application/Install menu.
	- Select "Search for new features"
	- Add a "Zip/jar location" and go find the zip you just downloaded.
	- Click Finish, and accept licences.
	- Designer will ask to restart.
- Open the "package explorer" view
- Click on the arrow next to the green bug icon bar, and select "Debug Configurations"
- Right click on the "OSGI Framework section", and choose "New"
- Name it the way you want
- Select "Domino Osgi Framework" in the drop down list
- Set "default auto start" to "false"
- In the plugin list, UNCHECK the "target platform" plugins section.
- And in the "Workspace" section, select all the imported plugins
- Click "Debug"
- You will have to enter the installation path of your local Domino Server
- Once done, you will have to restart the http task (using "restart task http")

## Check that the plugins have been deployed

Whatever method you use to deploy the plugins on your server (update site database, or Domino Debug Plugin), you can check that they have been deployed correctly by sending the following console command :

	tell http osgi ss oauth
	
If it answer somthing like this, you're good to go :

	> tell http osgi ss oauth
	[1C04:0002-1C08] 28/09/2017 14:27:39   Framework is launched.
	[1C04:0002-1C08] 28/09/2017 14:27:39   id       State       Bundle
	[1C04:0002-1C08] 28/09/2017 14:27:39   95       <<LAZY>>    com.github.lhervier.domino.oauth.server_1.0.0.qualifier
	[1C04:0002-1C08] 28/09/2017 14:27:39   96       RESOLVED    com.github.lhervier.domino.oauth.external.commons_io_2.5.0
	[1C04:0002-1C08] 28/09/2017 14:27:39   99       RESOLVED    com.github.lhervier.domino.oauth.external.nimbus_jose_jwt_4.37.1

# Setup Domino Server as an OAUTH2 Authorization Server

## Create the keys (SSO Configurations)

I didn't want to take the risk of storing keys myself. Instead, I prefered using SSO Configurations which are natively stored by IBM in a secure manner.

So, When Domino will generate OAUTH2 access tokens, it will use a secret stored in a SSO configuration document.

You will have to generate three of them :

- One key that will be used to sign the access token.
- Another one to sign the OpenID token.
- And a last one to encrypt the refresh token.

To create the three needed SSO Configurations, open your names.nsf, go to the "Servers" view, and use the "Web/Create Web SSO Configuration" action :

- Name your configuration: For example "AccessToken", "IdToken", and "RefreshToken"
- Enter the name of your organisation: We will use "ACME" in this example.
- DNS Domain: ".acme.com". This information is mandatory, but will not be used.
- Domino Server Names: Enter the name of the servers that will act as OAUTH2 authorization servers.

## Create the oauth2 database

This database will store the definition of the authorized OAUTH2 client applications. It will also store the authorization codes. Because of this, it is a sensible database, and you will have to be strict with its ACL.

### Download from github

You can download the "oauth2.nsf" database from the release page of github. By default "LocalDomainAdmins" is set as manager in the ACL, which should be OK for most people.

Once copied on your Domino Server, don't forget to sign it.

### Generate from source

You may have already imported the projets into your Domino Designer.

From the "package explorer", right click on the "oauth2-ondisk" project, and choose "Team Development" / "Associate with new NSF". In the following dialog box, enter the name of your server, and "oauth2.nsf" as database name.

### Check the ACL

Users with the [AppManager] role will be allowed to register/edit/remove oauth2 client applications. Users with this role MUST also be able to add users in the Domino Directory. Yes, we will generate users on the fly !

Users with the [AuthCodeManager] role will be able to access all the generated authorization code documents. They are sensible data, protected with a reader field. Nobody should have this role. 
It is present for development puropose, and can be replaced by the "Full access Administrator" option of Domino Administrator.

Every other users that should be able to log into one of the OAUTH2 client application should have reader access. So, by default, in the ACL :

- Default is "reader"
- Anonymous is "no access"

## Configure the oauth2 server environment

This project uses the Domino Spring project. As such, it is using properties that are readed by the Spring Framework. The code doesn't care about where they come from. 
And Spring is able to access properties from about anywhere. 

To make out database work, we will have to declare a set properties values. But we can declare them in a number of places :

- From environment variables
- From notes.ini variables
- Or we can inject them ourself by writing a simple osgi plugin that extends the domino spring plugin (and store those properties in an external database for example).

I will show you how to declare them with any of this methods. But first, let's describe the properties and their awaited values :

- oauth2.server.db : Path to the oauth2 database. Needed to prevent the endpoint to be made available on ALL databases of the server.
- oauth2.server.nab : Path to a standard Domino Directory database in which we will generate the users associated with the registered client applications. Example value "names.nsf"
- oauth2.server.applicationRoot : When we will create a user for an application, we will name it using the application name and this prefix. Example value "/APPLICATION"
- oauth2.server.refreshTokenConfig : Name of the SSO configuration that contains the secret we will use to encrypt the refresh tokens. Example value "ACME:RefreshToken"
- oauth2.server.refreshTokenLifetime : Lifetime in seconds of the generated refresh tokens. Should be a long value (10h). Example value "36000"
- oauth2.server.authCodeLifetime : Lifetime in seconds of the generated authorization codes. This is maximum time that can pass between the user login, and the server getting the acces/refresh token. Example value "60"
- oauth2.server.core.signKey : Name of the SSO configuration that contains the secret we will use to sign the access tokens. Example value "ACME:AccessToken"
- oauth2.server.core.iss : Issuer of the access token (iss property of the JWT). Example value "https://acme.com/domino/oauth2/"
- oauth2.server.core.expiresIn : Lifetime in seconds of the generated access tokens. Example value "1800"
- oauth2.server.openid.signKey : Name of the SSO configuration that contains the secret we will use to sign the openid id tokens. Example value "ACME:IdToken"
- oauth2.server.openid.iss : Issuer of the openid id token (iss property of the JWT). Example value "https://acme.com/domino/oauth2/openid/"

Whatever the method you use to define those properties, when done, restart the http task. 

### Declare properties as notes.ini variables

Eveything is in the title. Just edit your notes.ini file, and add variables named the same way as the properties.

You can also declare the variables in a configuration document.

### Inject properties from an osgi plugin

Read the documentation of the Domino Spring project, or contact me (create an issue) if you want more details.

This method allows you to inject values from the place you want. And of course, most of my clients wants to extract parameters from a common database.

## Check that everything is working

You can access the oauth applications end points to check if everything is working

	http://<your server>/oauth2.nsf/oauth2-server/html/listApplications

Check that the user that logs in has the [AppManager] role.

# Registering OAuth2 client applications

Use your browser to access the following url :

	http://<your server>/oauth2.nsf/oauth2-server/html/listApplications
	
You will have to login with a user with the [AppManager] role.

Registering an application will add a user in the nab. The application secret is its http password (in reality, it is 'username:http_password' base64 encoded). 
Don't forget to save this value. We won't be able to extract it again.

# The OAuth2 and OpenID endpoints 

Authorize endpoint : http://server/oauth2.nsf/oauth2-server/authorize

Token endpoint : http://server/oauth2.nsf/oauth2-server/token

OpenID UserInfo end point : http://server/oauth2-server/userInfo (note that there is NO reference to oauth2.nsf !!!)

# Supported OAuth2 flows

Authorization Code, Implicit and OpenID Hybrid flows are supported.

Client Credentials, and User credentials flows are NOT supported.

# Supported scopes

All OpenID scopes are supported : openid, profile, email, address, etc...

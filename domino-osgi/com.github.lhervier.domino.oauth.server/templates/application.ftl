<!doctype html>
<html>
<body>
	<div>
		<h1>
			<#if edit == true>
				<#if newApp == true>
					Create new application
				<#else>
					Edit application
				</#if>
			<#else>
				Application
			</#if>
		</h1>
	</div>
	
	<#if edit == true>
		<form action="saveApplication" method="POST">
	</#if>
	
	<div>
		<#if edit == true>
			<button type="submit" name="action" value="saveApp">Save</button>
			<button type="button" onclick="self.location='listApplications'">Cancel</button>
		<#else>
			<button type="button" onclick="self.location='editApplication?name=${app.name}'">Edit</button>
			<button type="button" onclick="self.location='listApplications'">Close</button>
		</#if>
	</div>
	
	<#if secret??>
		<div>
			Copy/paste the secret now, because we won't be able to generate it again !
		</div>
	</#if>
	
	<table>
		<tr>
			<td>client_id :</td>
			<td>
				${app.clientId}
				<#if edit == true>
					<input type="hidden" name="clientId" value="${app.clientId?xhtml}">
				</#if>
			</td>
		</tr>
		<#if app.secret??>
			<tr>
				<td>secret :</td>
				<td style="color:red">${app.secret}</td>
			</tr>
		</#if>
		<tr>
			<td>name :</td>
			<td>
				<#if newApp == true && edit == true>
					<input type="text" name="name" value="${app.name?xhtml}">
					<#if app.error??>
						<span style="color:red">${app.nameError!}</span>
					</#if>
				<#else>
					${app.name}
					<#if edit == true>
						<input type="hidden" name="name" value="${app.name?xhtml}">
					</#if>
				</#if>
			</td>
		</tr>
		<tr>
			<td>readers :</td>
			<td>
				<#if edit == true>
					<input type="text" name="readers" value="${app.readers?xhtml}">
					<#if app.error??>
						<span style="color:red">${app.readersError!}</span>
					</#if>
				<#else>
					${app.readers}
				</#if>
			</td>
		</tr>
		<tr>
			<td>redirect_uri :</td>
			<td>
				<#if edit == true>
					<input type="text" name="redirectUri" value="${app.redirectUri?xhtml}">
					<#if app.error??>
						<span style="color:red">${app.redirectUriError!}</span>
					</#if>
				<#else>
					${app.redirectUri}
				</#if>
			</td>
		</tr>
	</table>
	
	<table>
		<tr>
			<td>
				Other redirect URIs
			</td>
		</tr>
		<#if !app.existingRedirectUris?has_content>
			<tr>
				<td>No other redirect Uris</td>
			</tr>
		</#if>
		<#list app.existingRedirectUris as redirectUri>
			<tr>
				<td>${redirectUri}</td>
			</tr>
		</#list>
		<#if edit == true>
			<tr>
				<td>
					<input type="text" name="newRedirectUri" value="${newRedirectUri!}">
					<button type="submit" name="action" value="addRedirectUri">Add redirect URI</button>
					<#if app.error??>
						<span style="color:red">${app.newRedirectUriError!}</span>
					</#if>
				</td>
			</tr>
		</#if>
	</table>
	
	<#if edit == true>
		</form>
	</#if>
</body>
</html>
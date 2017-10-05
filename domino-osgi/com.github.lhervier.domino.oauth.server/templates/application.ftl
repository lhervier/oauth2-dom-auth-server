<!doctype html>
<html>
<body>
	<#if edit == true>
		<#if newApp == true>
			<form action="createApplication" method="POST">
		<#else>
			<form action="updateApplication" method="POST">
		</#if>
	</#if>
	
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
	
	<div>
		<#if edit == true>
			<input type="submit" value="Save">
		<#else>
			<button type="button" onclick="self.location='editApplication?name=${app.name}'">Edit</button>
		</#if>
		<button type="button" onclick="self.location='listApplications'">Cancel</button>
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
		<#if secret??>
			<tr>
				<td>secret :</td>
				<td style="color:red">${secret}</td>
			</tr>
		</#if>
		<tr>
			<td>name :</td>
			<td>
				<#if newApp == true && edit == true>
					<input type="text" name="name" value="${app.name?xhtml}">
					<#if error??>
						<span style="color:red">${error.name!}</span>
					</#if>
				<#else>
					${app.name}
				</#if>
			</td>
		</tr>
		<tr>
			<td>readers :</td>
			<td>
				<#if edit == true>
					<input type="text" name="readers" value="${app.readers?xhtml}">
					<#if error??>
						<span style="color:red">${error.readers!}</span>
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
					<#if error??>
						<span style="color:red">${error.redirectUri!}</span>
					</#if>
				<#else>
					${app.redirectUri}
				</#if>
			</td>
		</tr>
	</table>
	
	<#if edit == true>
		</form>
	</#if>
</body>
</html>
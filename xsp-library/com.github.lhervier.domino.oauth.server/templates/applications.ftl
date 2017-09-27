<!doctype html>
<html>
<body>
	<div>
		<h1>Declared applications:</h1>
	</div>
	
	<#list apps as app>
		<div>
			${app}
			<a href="viewApplication?name=${app?url('UTF-8')}">Open</a>
			<a href="deleteApplication?name=${app?url('UTF-8')}">Remove</a>
		</div>
	</#list>
	
	<div>
		<button type="button" onclick="self.location = 'newApplication'">
			Add a new application
		</button>
	</div>
</body>
</html>
<html>
<head>
	<meta name="layout" content="admin"/>
	<title>Galaxy User List</title>
	<script src="${resource(dir: 'js/jQuery', file: 'jquery.dataTables.js')}"></script>
	<link rel="stylesheet" type="text/css" href="${resource(dir: 'css', file: 'jquery.dataTables.css')}">
	<script>
		jQuery(document).ready(function () {
			jQuery("#userTable").dataTable({
				"iDisplayLength": 50,
				"aLengthMenu": [[25, 50, 100, -1], [25, 50, 100, "All"]],
				"sPaginationType": "full_numbers",
				"bStateSave": true
			});
		});
	</script>
</head>
<body>
<div class="body">
	<h1>Galaxy User List</h1>
	<g:if test="${flash.message}"><div class="message">${flash.message}</div></g:if>
	<div class="list" style="margin-top:15px">
		<table id='userTable'>
			<thead>
			<tr>
				<th>User Name</th>
				<th>Galaxy Key</th>
				<th>Mail Adress</th>
			</tr>
			</thead>
			<tbody>
			<g:each in="${personList}" var="person">
				<tr>
					<td>${person.username?.encodeAsHTML()}&nbsp&nbsp&nbsp&nbsp&nbsp</td>
					<td>${person.galaxyKey?.encodeAsHTML()}&nbsp&nbsp&nbsp&nbsp&nbsp</td>
					<td>${person.mailAddress?.encodeAsHTML()}&nbsp&nbsp</td>
					<td class="actionButtons">
						<span class="actionButton">
							<g:link action="delete" id="${person.username}">Delete</g:link>
						</span>
					</td>
				</tr>
			</g:each>
			</tbody>
		</table>
	</div>
</div>
</body>
</html>

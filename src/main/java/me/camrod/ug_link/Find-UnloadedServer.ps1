$SecurePass = ConvertTo-SecureString $Env:UG_LINK_PASS -AsPlainText -Force
$Creds = New-Object System.Management.Automation.PSCredential($Env:UG_LINK_USER, $SecurePass)

$SessionID = New-SSHSession -ComputerName ug251.eecg.utoronto.ca -Credential $Creds -AcceptKey:$true
$(Invoke-SSHCommand -Index $SessionID.sessionid -Command "ruptime -rl").Output
Remove-SSHSession -SessionId $SessionID.sessionid
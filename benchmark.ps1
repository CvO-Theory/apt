$NETS_LANG = [IO.Directory]::GetFiles("nets\gpu", "test-lang-*-net.apt");
$NETS_BIT = [IO.Directory]::GetFiles("nets\gpu", "test-bit-*-net.apt");
$NETS_CYCLE = [IO.Directory]::GetFiles("nets\gpu", "test-cycle-*-net.apt");

$APTGPU = "apt.cmd cl_covera";
$APTCPU = "apt.cmd covera";

$TMPFILE = $MyInvocation.MyCommand.Name + ".tmp";
$env:TMPFILE = $TMPFILE;

function procfile([String]$filename) {
	echo ([IO.FileInfo]$filename).Name;
   
	cmd /C $APTCPU $filename 2>&1 | out-null;
	echo "CPU $(type $TMPFILE)";
	
	cmd /C $APTGPU $filename 2>&1 | out-null;
	echo "GPU $(type $TMPFILE)";
}

echo "";
echo "================================================================================";
echo "Benchmark startet on $([DateTime]::Now)";
echo "================================================================================";
echo "";

echo "Test set: Language-nets";
echo "-----------------------";
foreach($net in $NETS_LANG) {
	procfile $net;
}
echo "";

echo "Test set: Bit-nets";
echo "------------------";
foreach($net in $NETS_BIT) {
	procfile $net;
}
echo "";

echo "Test set: Cycle-nets";
echo "--------------------";
foreach($net in $NETS_CYCLE) {
	procfile $net;
}
echo "";

[IO.File]::Delete($TMPFILE);

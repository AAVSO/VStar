# Convert space delimited JD-mag files to TSV.

$num_lines = 0;
$converted = 0;
while(<>) {
    $num_lines++;
    if (/^\s*(\d+(\.\d+)?)\s+(\d+(\.\d+)?)\s*$/) {
	$converted++;
	print "$1\t$3\n";
    }
}
print STDERR "total lines: $num_lines\n";
print STDERR "converted lines: $converted\n";

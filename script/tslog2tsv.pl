# Convert space delimited JD-magnitude files to TSV.
# Additional fields after the JD and magnitude are ignored.
# Header lines or comment lines or any line not matching
# a "number whitespace number" pattern are ignored.  
# One or more files or standard input can be specified.

$num_lines = 0;
$converted = 0;
while(<>) {
    $num_lines++;
    if (/^\s*(\-?\d+(\.\d+)?)\s+(\-?\d+(\.\d+)?)\s*/) {
	$converted++;
	print "$1\t$3\n";
    }
}
print STDERR "total lines: $num_lines\n";
print STDERR "converted lines: $converted\n";

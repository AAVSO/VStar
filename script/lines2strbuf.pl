# Write lines out as Java StringBuffer appends.

$strbuf = "StringBuffer buf = new StringBuffer();\n";
while(<>) {
    chomp;
    $strbuf .= "buf.append(\"$_\\n\");\n";
}
print $strbuf;

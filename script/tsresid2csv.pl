# Convert space delimited TS residuals files to CSV.
# The first argument specifies whether to select the
# residual or model value. The JD is always output.
# field as whitespace: 
#   JD, residual, [obs ID], original, model  
# Results are sent to standard output.

use strict;

if (length(@ARGV) >= 1) {
    my $which = shift @ARGV;
    usage() if ($which !~ /^residual|model$/);
    
    while(<>) {
       my ($jd, $residual, $obs_id, $orig, $model);
       my @fields = split '\s+';
       shift @fields if ($fields[0] =~ /^\s*$/);
       
       if (length(@fields) == 5) {
           ($jd, $residual, $obs_id, $orig, $model) = @fields;	   
       } else {
	       ($jd, $residual, $orig, $model) = @fields;
       }

       if ($which eq 'residual') {
	       print "$jd,$residual\n";
       } else {
	       print "$jd,$model\n";
       }
    }
} else {
    usage();
}

sub usage {
    die "usage: tsresid2tsv.pl residual|model [file ...]\n";
}

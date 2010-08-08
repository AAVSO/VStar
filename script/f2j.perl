#!/usr/bin/perl -w
#
# Partial Fortran to Java translator.

require 5;
use strict;

my %var_map;
my %decl_map;
my @java_lines;

for my $arg (@ARGV) {
    open(F, $arg) or die "can't open $arg\n";
    my @lines = <F>;
    close(F);

    my $i = 0;

    for my $line (@lines) {
	$line =~ s/\r\n//;
	process_line($line);
    }

    print "class Class$i {\n";
    print "    // TODO: check loop limits in Fortran vs Java!\n";
    gen_fields();
    for my $java_line (@java_lines) {
	print "    $java_line\n";
    }
    print "}\n"
}

sub process_line {
    my $line = $_[0];

    $line = stmt_substs($line);

    if ($line =~ /^\s*$/) {
	push @java_lines, "";
    } elsif ($line =~ /^c(.+)\s*/) {
	push @java_lines, "// $1";
    } elsif ($line =~ /^C(\-+)\s*/) {
	push @java_lines, "// $1";
    } elsif ($line =~ /^\s*enddo|endif|end\s*/) {
	push @java_lines, "}";
    } elsif ($line =~ /format|write|print|implicit/) {
	push @java_lines, "// $line";
    } elsif ($line =~ /^\s*(float|double|int|String)\s*([^\d\:\(\)]+)/) {
	process_vars($1, $2);
	push @java_lines, "// $line;";
    } elsif ($line =~ /^\s*common\/\w+\/(.+\,?)+/) {
	process_common_block($1);
	push @java_lines, "// $line;";
    } elsif ($line =~ /^\s*\d+\s+(.+\,?)+/) {
	process_common_block($1);
	push @java_lines, "// $line;";
    } elsif ($line =~ /^\s*subroutine\s*(.+)/) {
	push @java_lines, "void $1() {";
    } elsif ($line =~ /^\s*call(\s*.+)/) {
	my $funcall = "$1";
	$funcall .= "()" if ($funcall !~ /\)/);
	push @java_lines, "$funcall;";
    } elsif ($line =~ /\s*(\d*)\s*if\s*\((.+)\)\s*(.+)\s*$/) {
	my $if_line = "if ($2)";
	$if_line = "line_$1: $if_line" if (length($1) > 0);
	$if_line .= " $3";
	if ($if_line =~ /then/) {
	    $if_line .= " {";
	} else {
	    $if_line .= ";";
	}
	push @java_lines, $if_line;
    } elsif ($line =~ /\s*(\d*)\s*do\s+(\w+)\s*=\s*([^,]+)\s*,\s*(.+)/) {
	my $variable = $2;
	my $min = $3;
	my $max = $4;
	my $loop_line = 
	    "for ($variable = $min;$variable <= $max;$variable++) {";
	$loop_line = "line_$1: $loop_line" if (length($1) > 0);
	push @java_lines, $loop_line;
    } else {
	# Expressions/statements with 1 or 2 subscripts.
	$line =~ s/(\w+)\s*\(([^\)]+)\,([^\)]+)\)/$1\[$2\]\[$3\]/g;
	$line =~ s/(\w+)\s*\(([^\)]+)\)/$1\[$2\]/g;
	push @java_lines, "$line;";
    }
}

sub process_vars {
    my ($type, $idents) = @_;

    my @ident_list = split(/\s*,\s*/, $idents);

    for my $ident (@ident_list) {
	$var_map{$ident} = $type;
    }
}

sub process_common_block {
    my $decls = $_[0];

    for my $decl (split(/\s*,\s*/, $decls)) {
	if ($decl =~ /(\w+)\s*\(((\d+:)?(\d+)(\s*\,\s*(\d+:)?(\d+)))/) {
	    my $ident = $1;
	    my $dims = "[$4,$7]";
	    $decl_map{$ident} = $dims;
	} elsif ($decl =~ /(\w+)\s*\((\d+:)?(\d+)/) {
	    my $ident = $1;
	    my $dims = "[$3]";
	    $decl_map{$ident} = $dims;
	}
    }
}

sub stmt_substs {
    my $line = $_[0];
    
    $line =~ s/\.eq\./==/g;
    $line =~ s/\.ne\./!=/g;
    $line =~ s/\.gt\./>/g;
    $line =~ s/\.ge\./>=/g;
    $line =~ s/\.lt\./</g;
    $line =~ s/\.le\./<=/g;
    $line =~ s/\.and\./&&/g;
    $line =~ s/\.or\./||/g;
    $line =~ s/(\d+)\.d0/$1.0/g;
    $line =~ s/real\*8/double/;
    $line =~ s/real/float/;
    $line =~ s/integer/int/;
    $line =~ s/character\*\d+/String/;

    return $line;
}

sub gen_fields {
    for my $var (sort(keys %var_map)) {
	my $type = $var_map{$var};
	my $field = "private $type $var";
	my $decl = $decl_map{$var};
	if (defined $decl and length($decl) > 0) {
	    $field .= "[] = new $type$decl; // TODO: num indices; inc by 1 or 2!";
	} else {
	    $field .= ";";
	}

	print "    $field\n";
    }

    print "\n";
}

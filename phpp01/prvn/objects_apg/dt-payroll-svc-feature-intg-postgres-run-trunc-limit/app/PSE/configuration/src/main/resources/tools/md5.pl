#/usr/local/bin/perl -w
##############################################################################
# md5.pl - Calculate MD5 signature for filespec
#
# 1.0 2002.01.28 ssmythe - Initial version
##############################################################################

use Getopt::Std;
use File::Path;
use File::Basename;
use Cwd;
use Digest::MD5;
use File::Find ();
use vars qw/*name *dir *prune/;


##############################################################################
# Init
##############################################################################
sub Init {
    # Initialize globals
    
    $scriptVersion="1.0";
    $scriptName=basename($0);
    $scriptAuthorName="Steve Smythe";
    $scriptAuthorEmail="steve\@smythefamily.com";
    $scriptAuthorExt="n/a";
    %opts=();
    %files=();
    $fileonly="";
    $rootpath=".";
    
    # Parse command line switches

    getopts("hvf:r:", \%opts);

    # Display script version if need be

    if (exists($opts{"v"})) {
        Version();
        exit 0;
    }

    # Display script usage if need be

    if (exists($opts{"h"})) {
        Usage();
        exit 0;
    }
    
    # Check for required switches

    %req_opts=(r=>' ');

    foreach $req_opt (sort keys %req_opts) {
        if (!(exists($opts{$req_opt}))) {
            print "Error: $script_name: missing required \"-$req_opt\" switch.\n";
            Usage();
            exit 0;
        }
    }
    
    # Gather required switches
    $rootpath=$opts{"r"};

    if (exists($opts{"f"})) {
        $fileonly=$opts{"f"};
    }

    # File::Find variables
    *name   = *File::Find::name;
    *dir    = *File::Find::dir;
    *prune  = *File::Find::prune;
}


##############################################################################
# Usage
##############################################################################
sub Usage {
    print <<EOF;

Usage: $scriptName [-h] [-v] -r {rootpath} [-f {fileonly}]

       -h    Displays help
       -v    Displays version
       -r    Root path to calculate md5 of files
       -f    Calculate for a single file only


EOF
}


##############################################################################
# Version
##############################################################################
sub Version {
    print <<EOF;

$scriptName (v$scriptVersion)

by $scriptAuthorName ($scriptAuthorEmail, $scriptAuthorExt)

EOF
}


##############################################################################
# GetFileMD5
##############################################################################
sub GetFileMD5 {
    my ($file)=@_;
    my $rc="";

    if (-f $file) {
        open(FILE, $file) || die "Can't open '$file': $!";
        binmode(FILE);
        $rc=Digest::MD5->new->addfile(*FILE)->hexdigest;
        close(FILE) || die "Can't close '$file': $!";
    }
    
    return $rc;
}


##############################################################################
# wanted
##############################################################################
sub wanted {
    if (-f $_) {
        $files{$dir . '/' . $_}='';
    }
}


##############################################################################
# FindFiles
##############################################################################
sub FindFiles {
    $md5="";
    
    File::Find::find({wanted => \&wanted}, $rootpath);

    foreach $file (sort keys %files) {
        $file=~s/^\.\///g;
        $md5=GetFileMD5($file);
        print "$md5 *$file\n";
    }
}


##############################################################################
# FindSingleFile
##############################################################################
sub FindSingleFile {
    $md5="";

    $file="$rootpath/$fileonly";
    $file=~s/^\.\///g;
    $md5=GetFileMD5($file);
    print "$md5 *$file\n";
}


##############################################################################
# Process
##############################################################################
sub Process {
    if ($main::fileonly eq "") {
        FindFiles();
    } else {
        FindSingleFile();
    }
}


##############################################################################
# MAIN MAIN MAIN MAIN MAIN MAIN MAIN MAIN MAIN MAIN MAIN MAIN MAIN MAIN MAIN #
##############################################################################
Init();
Process();

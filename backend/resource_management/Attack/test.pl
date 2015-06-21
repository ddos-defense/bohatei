#!/usr/bin/perl


my $num = 20;
my $ingress = $num * 2;
system("go run attack.go 1 $ingress > Attack1.json");
